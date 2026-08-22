package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.worldgen.modifier.AddItemModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void start() {
        // 1. 注入到地牢箱子 (Simple Dungeon)
        add("guyuancao_seeds_in_dungeon", new AddItemModifier(
                new LootItemCondition[] {
                        LootTableIdCondition.builder(BuiltInLootTables.SIMPLE_DUNGEON).build(),
                        LootItemRandomChanceCondition.randomChance(0.25f).build()
                },
                ModItems.GUYUANCAO_SEEDS.get()
        ));

        add("guyuancao_seeds_in_mineshaft", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(BuiltInLootTables.ABANDONED_MINESHAFT).build(),
        }, ModItems.GUYUANCAO_SEEDS.get()));
        add("guyuancao_seeds_in_pyramid", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(BuiltInLootTables.DESERT_PYRAMID).build(),
        }, ModItems.GUYUANCAO_SEEDS.get()));
        add("guyuancao_seeds_in_village", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(BuiltInLootTables.VILLAGE_PLAINS_HOUSE).build(),
                LootItemRandomChanceCondition.randomChance(0.1f).build() // 10% 概率
        }, ModItems.GUYUANCAO_SEEDS.get()));

        // 打草 (Grass / Tall Grass) 掉落固元草种子 (例如 5% 概率)
        add("guyuancao_seeds_from_grass", new AddItemModifier(
                new LootItemCondition[] {
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build(),
                        LootItemRandomChanceCondition.randomChance(0.01f).build()
                },
                ModItems.GUYUANCAO_SEEDS.get()
        ));
        // 如果也希望高草 (Tall Grass) 掉落，可以再加一个：
        add("guyuancao_seeds_from_tall_grass", new AddItemModifier(
                new LootItemCondition[] {
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TALL_GRASS).build(),
                        LootItemRandomChanceCondition.randomChance(0.01f).build()
                },
                ModItems.GUYUANCAO_SEEDS.get()
        ));

        add("fanqicao_seeds_in_dungeon", new AddItemModifier(new LootItemCondition[] {LootTableIdCondition.builder(BuiltInLootTables.SIMPLE_DUNGEON).build(), LootItemRandomChanceCondition.randomChance(0.25f).build()}, ModItems.FANQICAO_ITEM.get()));
        add("fanqicao_seeds_in_mineshaft", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(BuiltInLootTables.ABANDONED_MINESHAFT).build(),
        }, ModItems.FANQICAO_ITEM.get()));
        add("fanqicao_seeds_in_pyramid", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(BuiltInLootTables.DESERT_PYRAMID).build(),
        }, ModItems.FANQICAO_ITEM.get()));
        add("fanqicao_seeds_in_village", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(BuiltInLootTables.VILLAGE_PLAINS_HOUSE).build(),
                LootItemRandomChanceCondition.randomChance(0.1f).build() // 10% 概率
        }, ModItems.FANQICAO_ITEM.get()));
    }
}
