package com.TovidY.kunluncontinent.datagen.lootprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;

/**
 * 宝箱战利品表（数据生成）。
 *
 * <h3>生成到哪</h3>
 * <p>输出 {@code src/generated/resources/data/kunluncontinent/loot_tables/chests/abandoned_danfang.json}，
 * 资源 ID 即 {@link #ABANDONED_DANFANG}。改奖励只改本类，然后跑 {@code runData}，
 * <b>不要再手写 JSON</b>。</p>
 *
 * <h3>谁在用它</h3>
 * <ul>
 *   <li>主路径：结构 {@code abandoned_danfang.nbt} 里两个 chest 的 {@code LootTable} 标签；
 *       原版放置结构时会 {@code blockentity.load(nbt)} 并补写随机 {@code LootTableSeed}，
 *       所以每箱独立随机、首次开箱才掷。</li>
 *   <li>唯一路径：结构 NBT 里的箱子带 {@code LootTable} 标签，原版放置时补随机 {@code LootTableSeed}，
 *       首次开箱才掷。Java 侧<b>不再</b>有任何兜底（老的兜底会给"自带 LootTable 的箱子"提前结算，
 *       见 {@code DanfangFurnaceRandomizer} 的类注释）。</li>
 * </ul>
 *
 * <h3>权重设计</h3>
 * <p>抽取 3~7 次。整体分四档（括号为单次抽取命中该档的概率）：</p>
 * <ul>
 *   <li>常见材料 权重 20~30 —— 合计 124（50.2%）</li>
 *   <li>中低阶 权重 8~18 —— 合计 76（30.8%）</li>
 *   <li>稀有 权重 5~8 —— 合计 36（14.6%）</li>
 *   <li>极稀有 权重 1~4 —— 合计 11（4.5%）</li>
 * </ul>
 * <p>池内单条权重之和 = 247。<b>增删条目后记得回来改这一段的合计与百分比</b>，
 * 否则注释会与实际掉率走偏。</p>
 *
 * <h3>加物品 / 加方块</h3>
 * <ul>
 *   <li>物品：{@code .add(entry(ModItems.XXX, 权重, 最小, 最大))}</li>
 *   <li>方块：{@code .add(blockEntry(ModBlocks.XXX, 权重, 最小, 最大))} ——
 *       方块物品是 {@code ModBlocks#registerBlock} 里自动注册的，没有再包一层
 *       {@code RegistryObject<Item>}，所以这里用 {@code blockEntry} 重载。</li>
 * </ul>
 */
public class ModChestLootTablesProvider implements LootTableSubProvider {

    /** 废弃丹房奖励箱。ID 必须与结构 NBT 里 chest 的 {@code LootTable} 标签一致。 */
    public static final ResourceLocation ABANDONED_DANFANG =
            new ResourceLocation(KlMain.MOD_ID, "chests/abandoned_danfang");

    /** 每箱抽取次数。 */
    private static final float ROLLS_MIN = 3.0F;
    private static final float ROLLS_MAX = 7.0F;

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(ABANDONED_DANFANG, abandonedDanfang());
    }

    private static LootTable.Builder abandonedDanfang() {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(UniformGenerator.between(ROLLS_MIN, ROLLS_MAX))
                // ---------- 常见材料 ----------
                .add(entry(ModItems.COPPER_SOUL_COIN, 30, 4.0F, 16.0F))
                .add(entry(ModItems.SILVER_SOUL_COIN, 25, 2.0F, 10.0F))
                .add(entry(ModItems.DROSS, 25, 1.0F, 8.0F))
                .add(entry(ModItems.CHUYUAN_DAN, 24, 2.0F, 8.0F))
                .add(entry(ModItems.BAICAOLING_DAN, 20, 2.0F, 6.0F))
                .add(entry(ModItems.EXTREME_COLD_SNOWFLAKE_FRAGMENT, 15, 1.0F, 2.0F))
                .add(entry(ModItems.GUYUANCAO_SEEDS, 15, 1.0F, 2.0F))
                .add(entry(ModItems.FANQICAO_SEEDS, 15, 1.0F, 2.0F))
                .add(entry(ModItems.FIRST_DECOMPOSITION_GOSSIP, 15, 1.0F, 1.0F))
                .add(entry(ModItems.IRON_ENGRAVING_KNIFE, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_AXE, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_HOE, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_PICKAXE, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_SHOVEL, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_SWORD, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_HELMET, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_CHESTPLATE, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_BOOTS, 15, 1.0F, 1.0F))
                .add(entry(ModItems.GRAY_IRON_LEGGINGS, 15, 1.0F, 1.0F))
                // ---------- 中低阶 ----------
                .add(entry(ModItems.NEIDAN1, 18, 1.0F, 5.0F))
                .add(entry(ModItems.LOW_HUNHUAN_STORAGE_CORE, 12, 1.0F, 3.0F))
                .add(entry(ModItems.QIANHUABAO_DAN, 12, 1.0F, 4.0F))
                .add(entry(ModItems.NEIDAN2, 10, 1.0F, 3.0F))
                .add(entry(ModItems.STRONG_POTION, 9, 1.0F, 1.0F))
                .add(entry(ModItems.TWO_DECOMPOSITION_GOSSIP, 8, 1.0F, 1.0F))
                .add(entry(ModItems.FANQI_DAN, 10, 1.0F, 5.0F))
                .add(entry(ModItems.GUYUAN_DAN, 10, 1.0F, 5.0F))
                .add(entry(ModItems.RUBY, 13, 1.0F, 2.0F))
                .add(entry(ModItems.AMETHYST, 8, 1.0F, 1.0F))
                .add(entry(ModItems.RED_FIRE_INGOT, 8, 1.0F, 5.0F))

                // 方块示例：blockEntry 会自动取到该方块的 BlockItem，照抄这一行换成别的方块即可
                .add(blockEntry(ModBlocks.DROSS_BLOCK, 12, 1.0F, 2.0F))

                // ---------- 稀有 ----------
                .add(entry(ModItems.NEIDAN3, 8, 1.0F, 2.0F))
                .add(entry(ModItems.WANPOXUAN_DAN, 8, 1.0F, 3.0F))
                .add(entry(ModItems.SHIFANGJIE_DAN, 5, 1.0F, 2.0F))
                .add(entry(ModItems.HUNHUAN_STORAGE_FOUR, 5, 1.0F, 2.0F))
                .add(entry(ModItems.DIAMOND_ENGRAVING_KNIFE, 6, 1.0F, 1.0F))
                .add(entry(ModItems.SUNKEN_SILVER_INGOT, 5, 1.0F, 3.0F))
                .add(entry(ModItems.RED_FIRE_HELMET, 5, 1.0F, 1.0F))
                .add(entry(ModItems.RED_FIRE_CHESTPLATE, 5, 1.0F, 1.0F))
                .add(entry(ModItems.RED_FIRE_LEGGINGS, 5, 1.0F, 1.0F))
                .add(entry(ModItems.RED_FIRE_BOOTS, 5, 1.0F, 1.0F))
                .add(entry(ModItems.COLD_HEARTED_STEEL_HOE, 2, 1.0F, 1.0F))
                // ---------- 极稀有 ----------
                .add(entry(ModItems.NEIDAN4, 4, 1.0F, 2.0F))
                .add(entry(ModItems.NEIDAN5, 3, 1.0F, 1.0F))
                .add(entry(ModItems.GOLDEN_SOUL_COIN, 1, 1.0F, 2.0F))
                .add(entry(ModItems.SUNKEN_SILVER_HELMET, 1, 1.0F, 1.0F))
                .add(entry(ModItems.SUNKEN_SILVER_CHESTPLATE, 1, 1.0F, 1.0F))
                .add(entry(ModItems.SUNKEN_SILVER_LEGGINGS, 1, 1.0F, 1.0F))
                .add(entry(ModItems.SUNKEN_SILVER_BOOTS, 1, 1.0F, 1.0F))
                .add(entry(ModItems.NEIDAN6, 2, 1.0F, 1.0F)));

    }

    /** 一条「物品 + 权重 + 数量区间」的池内条目。 */
    private static LootPoolSingletonContainer.Builder<?> entry(RegistryObject<Item> item,
                                                              int weight, float countMin, float countMax) {
        return entry(item.get(), weight, countMin, countMax);
    }

    /**
     * 一条「方块 + 权重 + 数量区间」的池内条目。
     *
     * <p>方块物品是在 {@code ModBlocks#registerBlock} 里自动注册的
     * （{@code ModItems.ITEMS.register(name, () -> new BlockItem(...))}），
     * 没有可以引用的 {@code RegistryObject<Item>}，所以这里直接取 {@link Block#asItem()}。</p>
     *
     * <p>datagen 阶段方块与物品的 Register 事件都已经跑完，
     * {@code BlockItem} 构造时已把自身写进 {@code Item.BY_BLOCK}，所以 {@code asItem()} 拿得到真物品。
     * 反之<b>没有</b>物品的方块（例如直接用 {@code BLOCKS.register} 注册、没走 {@code registerBlock}
     * 的作物方块）会拿到 {@link Items#AIR}，这种情况直接抛错，避免生成一张隐性空表的 JSON。</p>
     */
    private static LootPoolSingletonContainer.Builder<?> blockEntry(RegistryObject<Block> block,
                                                                    int weight, float countMin, float countMax) {
        Item item = block.get().asItem();
        if (item == Items.AIR) {
            throw new IllegalStateException("方块 " + block.getId()
                    + " 没有对应的 BlockItem（请确认它走的是 ModBlocks#registerBlock），无法写进战利品表");
        }
        return entry(item, weight, countMin, countMax);
    }

    /** 物品条目本体：数量恒为 1 时不写 set_count，生成的 JSON 更干净。 */
    private static LootPoolSingletonContainer.Builder<?> entry(Item item, int weight,
                                                               float countMin, float countMax) {
        LootPoolSingletonContainer.Builder<?> builder = LootItem.lootTableItem(item).setWeight(weight);
        if (countMin == 1.0F && countMax == 1.0F) {
            return builder;
        }
        return builder.apply(SetItemCountFunction.setCount(UniformGenerator.between(countMin, countMax)));
    }
}
