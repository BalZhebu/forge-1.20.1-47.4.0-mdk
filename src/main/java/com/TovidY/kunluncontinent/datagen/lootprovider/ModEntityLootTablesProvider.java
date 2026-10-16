package com.TovidY.kunluncontinent.datagen.lootprovider;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(LONE_BAMBOO, loneBamboo());
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
}
