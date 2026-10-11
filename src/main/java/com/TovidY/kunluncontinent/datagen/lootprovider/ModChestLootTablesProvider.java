package com.TovidY.kunluncontinent.datagen.lootprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
 *   <li>兜底路径：{@code DanfangFurnaceRandomizer#fillLegacyChest} 给老存档已有的空箱子补一次。</li>
 * </ul>
 *
 * <h3>权重设计</h3>
 * <p>抽取 3~7 次，池内单条权重之和 = 235。整体分四档（括号为单次抽取命中该档的概率）：</p>
 * <ul>
 *   <li>常见材料 权重 20~30 —— 合计 124（52.8%）</li>
 *   <li>中低阶 权重 10~18 —— 合计 64（27.2%）</li>
 *   <li>稀有 权重 5~8 —— 合计 36（15.3%）</li>
 *   <li>极稀有 权重 2~4 —— 合计 11（4.7%）</li>
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
                // ---------- 中低阶 ----------
                .add(entry(ModItems.NEIDAN1, 18, 1.0F, 5.0F))
                .add(entry(ModItems.LOW_HUNHUAN_STORAGE_CORE, 12, 1.0F, 3.0F))
                .add(entry(ModItems.GOLDEN_SOUL_COIN, 12, 1.0F, 5.0F))
                .add(entry(ModItems.QIANHUABAO_DAN, 12, 1.0F, 4.0F))
                .add(entry(ModItems.NEIDAN2, 10, 1.0F, 3.0F))
                // ---------- 稀有 ----------
                .add(entry(ModItems.DEMON_WHALE_MEDAL, 8, 1.0F, 2.0F))
                .add(entry(ModItems.NEIDAN3, 8, 1.0F, 2.0F))
                .add(entry(ModItems.WANPOXUAN_DAN, 8, 1.0F, 3.0F))
                .add(entry(ModItems.MIDDLE_HUNHUAN_STORAGE_CORE, 7, 1.0F, 2.0F))
                .add(entry(ModItems.SHIFANGJIE_DAN, 5, 1.0F, 2.0F))
                // ---------- 极稀有 ----------
                .add(entry(ModItems.NEIDAN4, 4, 1.0F, 2.0F))
                .add(entry(ModItems.NEIDAN5, 3, 1.0F, 1.0F))
                .add(entry(ModItems.TOP_HUNHUAN_STORAGE_CORE, 2, 1.0F, 1.0F))
                .add(entry(ModItems.NEIDAN6, 2, 1.0F, 1.0F)));
    }

    /** 一条「物品 + 权重 + 数量区间」的池内条目。 */
    private static LootPoolSingletonContainer.Builder<?> entry(RegistryObject<Item> item,
                                                              int weight, float countMin, float countMax) {
        return LootItem.lootTableItem(item.get())
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(countMin, countMax)));
    }
}
