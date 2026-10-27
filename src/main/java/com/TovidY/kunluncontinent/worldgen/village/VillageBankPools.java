package com.TovidY.kunluncontinent.worldgen.village;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.mixin.SinglePoolElementAccessor;
import com.TovidY.kunluncontinent.mixin.StructureTemplatePoolAccessor;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * 把「银行」挂进所有<b>村庄房屋池</b>，让它像普通民房一样随机生成在村庄里。
 *
 * <h3>为什么运行时注入，而不是 datagen 覆盖原版 JSON</h3>
 * <p>数据包覆盖是<b>整文件替换</b>：要覆盖 {@code minecraft:village/plains/houses}
 * 就得把原版那 37 条房屋全部抄一遍（5 种村庄 × 正/僵尸 = 10 个池子），
 * 而且只要别的模组也改过村庄，就会互相覆盖。这里改成服务器启动时往池子的
 * 「已展开元素表」里追加若干份 bank —— 原版内容一条不动。</p>
 *
 * <h3>权重怎么算</h3>
 * <p>{@code StructureTemplatePool#getRandomTemplate} 就是
 * {@code templates.get(random.nextInt(templates.size()))}，那张表在构造时已按 weight 展开。
 * 所以<b>追加 N 份 = N 点权重</b>。原版 houses 池按 weight 展开后总权重 68~87，
 * 这里追 {@link #WEIGHT_COPIES} 份，单次抽取命中率约 42%（一个村庄 4 个挂点 ≈ 89% 出一次）。</p>
 *
 * <h3>「一个村庄最多 1 个」</h3>
 * <p>池子本身没有「每实例唯一」的机制，靠 {@link com.TovidY.kunluncontinent.mixin.MixinStructurePiecesBuilder}
 * 在一次结构生成（= 一个村庄）里拦掉第二枚 bank。</p>
 *
 * <h3>⚠️ 前提：bank.nbt 必须有 jigsaw 连接块</h3>
 * <p>村庄是 jigsaw 结构，房屋靠 {@code minecraft:jigsaw} 方块和街道的
 * {@code target=building_entrance} 对接。没有这个方块的模板<b>永远接不上</b>。
 * 参数见 {@code .workbuddy/memory/2026-10-26.md}。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VillageBankPools {

    /** 银行结构模板 id —— 对应 {@code data/kunluncontinent/structures/bank.nbt}。 */
    public static final ResourceLocation BANK_TEMPLATE = new ResourceLocation(KlMain.MOD_ID, "bank");

    /**
     * 是否把银行塞进村庄房屋池。
     *
     * <p>结构是 <b>10×6×8</b>（面宽 10 / 进深 8 / 面积 80，比原版最大的 11×17 小得多），
     * 落在「短边 ≤ 11、长边 ≤ 15、面积 ≤ 190」的安全区间，可以像普通民房一样进村。</p>
     *
     * <p>历史：16×9×18 时无论权重多高都被碰撞检测丢弃（18 格宽 &gt; 16 格路网），
     * 当时临时关掉过。现在已缩到 10×8，重新打开。</p>
     */
    public static final boolean INJECT_INTO_VILLAGES = true;

    /**
     * 追加份数 = 权重。
     *
     * <p>原版 houses 池按 weight 展开后总权重 68~87。给 64 份 →
     * 单次抽取命中率约 42%，一个村庄 4 个挂点算下来 ≈ 89% 会出现。</p>
     *
     * <p>⚠️ 权重不能无脑拉满：{@code MixinStructurePiecesBuilder} 只保留第一枚 bank，
     * 多抽到的会被 cancel —— 那些位置本来能放普通民房，权重越高村庄越"缺房子"。
     * 64 份平均每村会挤掉 ~0.7 栋民房，属于可接受范围；嫌房子少就降到 32。</p>
     */
    public static final int WEIGHT_COPIES = 64;

    private VillageBankPools() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!INJECT_INTO_VILLAGES) {
            // 见 INJECT_INTO_VILLAGES 的说明：改走独立结构了，这里不再往村庄池子里塞
            return;
        }
        RegistryAccess access = event.getServer().registryAccess();
        Registry<StructureTemplatePool> pools = access.registryOrThrow(Registries.TEMPLATE_POOL);

        StructurePoolElement bank = createBankElement(access);
        if (bank == null) {
            KlMain.LOGGER.error("[昆仑大陆] 银行池元素创建失败，村庄里不会生成银行");
            return;
        }

        int injected = 0;
        int skipped = 0;
        for (Map.Entry<ResourceKey<StructureTemplatePool>, StructureTemplatePool> entry : pools.entrySet()) {
            if (!isVillageHousesPool(entry.getKey().location())) {
                continue;
            }
            if (inject(entry.getValue(), bank)) {
                injected++;
            } else {
                skipped++;
            }
        }

        if (injected > 0) {
            KlMain.LOGGER.info("[昆仑大陆] 银行结构已注入 {} 个村庄房屋池（每池权重 {}）", injected, WEIGHT_COPIES);
        }
        if (skipped > 0) {
            KlMain.LOGGER.debug("[昆仑大陆] {} 个村庄房屋池已含银行，跳过重复注入", skipped);
        }
    }

    /**
     * 用官方 codec 造一个 {@code minecraft:single_pool_element}。
     *
     * <p><b>为什么不直接 {@code new SinglePoolElement(...)}</b>：那个构造器是 {@code protected}，
     * 包外拿不到。走 {@code StructurePoolElement.CODEC} 解析一份等价的 JSON/NBT 最稳，
     * 以后原版改字段也不会编译不过。</p>
     *
     * <p>{@code RegistryOps} 是必须的：{@code processors} 字段是
     * {@code Holder<StructureProcessorList>}，没有注册表上下文就解析不出 {@code minecraft:empty}。</p>
     */
    private static StructurePoolElement createBankElement(RegistryAccess access) {
        CompoundTag tag = new CompoundTag();
        tag.putString("element_type", "minecraft:single_pool_element");
        tag.putString("location", BANK_TEMPLATE.toString());
        tag.putString("processors", "minecraft:empty");
        tag.putString("projection", "rigid");
        return StructurePoolElement.CODEC
                .parse(RegistryOps.create(NbtOps.INSTANCE, access), tag)
                .resultOrPartial(err -> KlMain.LOGGER.error("[昆仑大陆] bank 池元素解析失败: {}", err))
                .orElse(null);
    }

    /**
     * 只认 {@code minecraft:village/<type>/houses}。
     *
     * <p>排除 {@code /zombie/} 变体：僵尸村庄共用同一套房屋模板，只是多一层
     * {@code zombie_*} 处理器；往它里面也塞一份会让僵尸村出现两套坐标不同的 bank。
     * 想连僵尸村一起加，把这个排除条件去掉即可。</p>
     */
    private static boolean isVillageHousesPool(ResourceLocation id) {
        if (!"minecraft".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.startsWith("village/")
                && path.endsWith("/houses")
                && !path.contains("/zombie/");
    }

    /** @return 本次是否真的注入了（已含 bank 则返回 false，防止重进世界时权重翻倍） */
    private static boolean inject(StructureTemplatePool pool, StructurePoolElement bank) {
        ObjectArrayList<StructurePoolElement> templates =
                ((StructureTemplatePoolAccessor) pool).kl$getTemplates();
        if (templates.stream().anyMatch(VillageBankPools::isBankElement)) {
            return false;
        }
        for (int i = 0; i < WEIGHT_COPIES; i++) {
            templates.add(bank);
        }
        return true;
    }

    /** 这个池元素是不是我们的银行模板。 */
    public static boolean isBankElement(StructurePoolElement element) {
        if (!(element instanceof SinglePoolElement single)) {
            return false;
        }
        Either<ResourceLocation, StructureTemplate> template =
                ((SinglePoolElementAccessor) single).kl$getTemplate();
        return template != null && template.left().filter(BANK_TEMPLATE::equals).isPresent();
    }

    /** 这个结构片段是不是我们的银行（Mixin 里用来限制「每村庄最多一个」）。 */
    public static boolean isBankPiece(StructurePiece piece) {
        return piece instanceof PoolElementStructurePiece pop && isBankElement(pop.getElement());
    }
}
