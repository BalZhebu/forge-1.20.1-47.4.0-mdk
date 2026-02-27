package com.TovidY.kunluncontinent.datagen.blockprovider;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
//用于检查方块有没有写战利品列表，若排除方块则在方块后加入.noLootTable
public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.CULTIVATION_PLATFORM.get());

        dropSelf(ModBlocks.GRAY_IRON_ORE.get());
        dropSelf(ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get());
        dropSelf(ModBlocks.RED_FIRE_ORE.get());
        dropSelf(ModBlocks.SUNKEN_SILVER_ORE.get());
        dropSelf(ModBlocks.COLD_HEARTED_STEEL_ORE.get());

        //宝石矿
        dropSelf(ModBlocks.RUBY_ORE.get());
        dropSelf(ModBlocks.AMETHYST_ORE.get());
        dropSelf(ModBlocks.SAPPHIRE_ORE.get());
        dropSelf(ModBlocks.STARLIGHT_STONE_ORE.get());

        dropSelf(ModBlocks.PUTUAN_BLOCK.get());

        dropSelf(ModBlocks.DROSS_BLOCK.get());

        dropSelf(ModBlocks.LIANDANLU1.get());
        dropSelf(ModBlocks.LIANDANLU2.get());
        dropSelf(ModBlocks.LIANDANLU3.get());
        dropSelf(ModBlocks.LIANDANLU4.get());
        dropSelf(ModBlocks.LIANDANLU5.get());
        dropSelf(ModBlocks.LIANDANLU6.get());
        dropSelf(ModBlocks.LIANDANLU7.get());
        dropSelf(ModBlocks.LIANDANLU8.get());
        dropSelf(ModBlocks.LIANDANLU9.get());

        //传送门框架
        dropSelf(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get());

        // 红宝石矿石（使用矿石破坏战利品列表）
        this.add(ModBlocks.RUBY_ORE.get(), block -> createCopperOreLikeDrops(block, ModItems.RUBY.get()));
        this.add(ModBlocks.AMETHYST_ORE.get(), block -> createCopperOreLikeDrops(block, ModItems.AMETHYST.get()));
        this.add(ModBlocks.SAPPHIRE_ORE.get(), block -> createCopperOreLikeDrops(block, ModItems.SAPPHIRE.get()));
        this.add(ModBlocks.STARLIGHT_STONE_ORE.get(), block -> createCopperOreLikeDropsTwo(block, ModItems.STARLIGHT_STONE.get()));

        // 草药类
        // 参数：方块对象, 成熟掉落物, 种子, 最大等级
        this.add(ModBlocks.RED_SPIDER_LILY_BLOCK.get(),
                block -> createFortuneCropDrops(block, ModItems.RED_SPIDER_LILY_ITEM.get(), ModItems.RED_SPIDER_SEEDS.get(), 3));

        // 如果你有其他草药，直接复制这一行即可
        // this.add(ModBlocks.OTHER_HERB.get(), block -> createFortuneCropDrops(block, ModItems.OTHER_HERB_ITEM.get(), ModItems.OTHER_SEEDS.get(), 3));

    }

    protected LootTable.Builder createFortuneCropDrops(Block block, Item product, Item seed, int maxAge) {
        LootItemCondition.Builder isMaxAge = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(BlockStateProperties.AGE_3, maxAge));
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(seed)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                        ))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(isMaxAge)
                        .add(LootItem.lootTableItem(product)
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    //该方法是矿石类，挖矿石会掉落更多矿物，将原本的block方块替换成该方法即可
    //用于直接爆出锭
    protected LootTable.Builder createCopperOreLikeDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock, LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    protected LootTable.Builder createCopperOreLikeDropsTwo(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock, LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
