package com.TovidY.kunluncontinent.datagen.lootprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

/**
 * 实体掉落表（数据生成）。
 *
 * <h3>ID 命名规则（很容易踩坑）</h3>
 * <p>实体战利品表的 ID 不是随便起的：{@code LivingEntity#dropFromLootTable} 用的是
 * {@code EntityType#getDefaultLootTable()}，而 {@code EntityType.Builder#build("lone_bamboo")}
 * 会把它设成 <b>{@code <命名空间>:entities/<注册名>}</b>。
 * 所以这里必须是 {@code kunluncontinent:entities/lone_bamboo}，
 * 写错名字的表现是「击杀后什么都不掉」，而且不会有任何报错。</p>
 *
 * <h3>为什么单开一个 Provider</h3>
 * <p>战利品表要挂在同一个 {@code LootTableProvider} 下，但每种 {@code LootContextParamSet}
 * 是不同的子 Provider（方块 BLOCK / 宝箱 CHEST / 实体 ENTITY）。
 * 实体表也不该混进 {@link ModChestLootTablesProvider}，否则很容易漏改 paramSet。</p>
 */
public class ModEntityLootTablesProvider implements LootTableSubProvider {

    /** 孤竹掉落表。ID 必须等于 {@code kunluncontinent:entities/lone_bamboo}。 */
    public static final ResourceLocation LONE_BAMBOO =
            new ResourceLocation(KlMain.MOD_ID, "entities/lone_bamboo");

    /** 雪帝掉落表。ID 必须等于 {@code kunluncontinent:entities/ice_empress}。 */
    public static final ResourceLocation ICE_EMPRESS =
            new ResourceLocation(KlMain.MOD_ID, "entities/ice_empress");

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(LONE_BAMBOO, loneBamboo());
        output.accept(ICE_EMPRESS, iceEmpress());
    }

    /**
     * 孤竹：击杀必定掉落 1 根竹子。
     *
     * <p>单池 + 1 次抽取 + 单条目 → 必定命中，不需要任何条件。</p>
     */
    private static LootTable.Builder loneBamboo() {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(Items.BAMBOO)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))));
    }

    /**
     * 雪帝：三个独立池子，互相不抢名额。
     *
     * <ol>
     *   <li><b>唱片《冰封王座》</b> —— 100% 必掉 1 张（BOSS 的招牌掉落）；</li>
     *   <li><b>极寒雪花碎片</b> —— 100% 掉 3~6 个（材料保底）；</li>
     *   <li><b>金魂币</b> —— 50% 概率，每级抢夺 +2%，1~2 枚。</li>
     * </ol>
     */
    private static LootTable.Builder iceEmpress() {
        return LootTable.lootTable()
                // 1. 唱片：必定 1 张
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.THRONE_OF_FROZEN_STONE.get())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))))
                // 2. 雪帝之心：必定 1 个
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.ICE_EMPRESS_HEART.get())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))))
                // 3. 金魂币：50%（+2%/级抢夺），1~2 枚
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.GOLDEN_SOUL_COIN.get())
                                .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.5F, 0.02F))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))));
    }
}
