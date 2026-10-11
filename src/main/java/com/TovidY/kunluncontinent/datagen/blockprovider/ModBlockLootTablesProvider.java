package com.TovidY.kunluncontinent.datagen.blockprovider;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.item.Items;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.klblock.KLCropBlock;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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
    // ==================== 神界草掉落概率（对齐原版） ====================

    /** 种子掉落总概率 —— 原版 {@code createGrassDrops} 用的是 {@code 0.125F}。 */
    private static final float SEED_DROP_CHANCE = 0.125F;

    /** 草药本体（返气草/归元草）掉落概率，各 5%。 */
    private static final float HERB_DROP_CHANCE = 0.05F;

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

        //矿物块
        dropSelf(ModBlocks.GRAY_BLOCK.get());
        dropSelf(ModBlocks.CLOUD_PATTERNED_BRONZE_BLOCK.get());
        dropSelf(ModBlocks.RED_FIRE_BLOCK.get());
        dropSelf(ModBlocks.SUNKEN_SILVER_BLOCK.get());
        dropSelf(ModBlocks.COLD_HEARTED_STEEL_BLOCK.get());
        dropSelf(ModBlocks.RINSEI_BLOCK.get());

        //宝石矿
        dropSelf(ModBlocks.RUBY_ORE.get());
        dropSelf(ModBlocks.AMETHYST_ORE.get());
        dropSelf(ModBlocks.SAPPHIRE_ORE.get());
        dropSelf(ModBlocks.STARLIGHT_STONE_ORE.get());

        //石碑
        dropSelf(ModBlocks.STONE_STAMP.get());

        //蒲团
        dropSelf(ModBlocks.PUTUAN_BLOCK.get());

        //魂土
        dropSelf(ModBlocks.SOUL_SOIL.get());

        //神界木
        dropSelf(ModBlocks.DIVINE_REALM_LOG.get());
        dropSelf(ModBlocks.DIVINE_REALM_PLANKS.get());
        dropSelf(ModBlocks.DIVINE_REALM_SAPLING.get());

        //丹渣
        dropSelf(ModBlocks.DROSS_BLOCK.get());

        //聚灵祭坛
        dropSelf(ModBlocks.SPIRIT_GATHERING_ALTAR.get());
        dropSelf(ModBlocks.SPIRIT_GATHERING_STONE.get());
        dropSelf(ModBlocks.SPIRIT_GATHERING_STONE_0.get());
        dropSelf(ModBlocks.SPIRIT_GATHERING_STONE_1.get());
        dropSelf(ModBlocks.SPIRIT_GATHERING_STONE_2.get());

        //炼丹炉
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
        dropSelf(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get());

        //树苗
        this.add(ModBlocks.DIVINE_REALM_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.DIVINE_REALM_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES)
        );

        this.add(ModBlocks.DIVINE_REALM_GRASS.get(), this::createDivineRealmGrassDrops);

        // 红宝石矿石（使用矿石破坏战利品列表）
        this.add(ModBlocks.RUBY_ORE.get(), block -> createCopperOreLikeDrops(block, ModItems.RUBY.get()));
        this.add(ModBlocks.AMETHYST_ORE.get(), block -> createCopperOreLikeDrops(block, ModItems.AMETHYST.get()));
        this.add(ModBlocks.SAPPHIRE_ORE.get(), block -> createCopperOreLikeDrops(block, ModItems.SAPPHIRE.get()));
        this.add(ModBlocks.STARLIGHT_STONE_ORE.get(), block -> createCopperOreLikeDropsTwo(block, ModItems.STARLIGHT_STONE.get()));

        // 草药类
        // 参数：方块对象, 成熟掉落物, 种子, 最大等级
        this.add(ModBlocks.RED_SPIDER_LILY_BLOCK.get(),
                block -> createFortuneCropDrops(block, ModItems.RED_SPIDER_LILY_ITEM.get(), ModItems.RED_SPIDER_SEEDS.get(), 3));

        this.add(ModBlocks.GUYUANCAO_BLOCK.get(), block -> createKLCropDrops(block, ModItems.GUYUANCAO_ITEM.get(), ModItems.GUYUANCAO_SEEDS.get()));
        this.add(ModBlocks.FANQICAO_BLOCK.get(), block -> createKLCropDrops(block, ModItems.FANQICAO_ITEM.get(), ModItems.FANQICAO_SEEDS.get()));
    }

    private LootTable.Builder createDivineRealmGrassDrops(Block grass) {
        LootItemCondition.Builder hasNoShearsOrSilkTouch = HAS_SHEARS.or(HAS_SILK_TOUCH).invert();

        LootPool.Builder silkPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(HAS_SHEARS.or(HAS_SILK_TOUCH))
                .add(LootItem.lootTableItem(grass));

        LootPool.Builder seedPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(hasNoShearsOrSilkTouch)
                .when(LootItemRandomChanceCondition.randomChance(SEED_DROP_CHANCE))
                // --- 修仙种子 ---
                .add(LootItem.lootTableItem(ModItems.RED_SPIDER_SEEDS.get()).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.FANQICAO_SEEDS.get()).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.GUYUANCAO_SEEDS.get()).setWeight(1))
                // --- 原版植物种子 ---
                .add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(1))
                .add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(1))
                .add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(1))
                .add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(1))
                .add(LootItem.lootTableItem(Items.TORCHFLOWER_SEEDS).setWeight(1))
                .add(LootItem.lootTableItem(Items.PITCHER_POD).setWeight(1));

        LootPool.Builder herbPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(hasNoShearsOrSilkTouch) // 解决私有变量报错
                .when(LootItemRandomChanceCondition.randomChance(HERB_DROP_CHANCE))
                .add(LootItem.lootTableItem(ModItems.FANQICAO_ITEM.get())
                        .setWeight(1)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                .add(LootItem.lootTableItem(ModItems.GUYUANCAO_ITEM.get())
                        .setWeight(1)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))));

        return LootTable.lootTable()
                .withPool(silkPool)
                .withPool(seedPool)
                .withPool(herbPool);
    }

    protected LootTable.Builder createKLCropDrops(Block block, Item product, Item seed) {
        KLCropBlock crop = (KLCropBlock) block;
        return createFortuneCropDrops(block, product, seed, crop.getMaxAge());
    }

    protected LootTable.Builder createFortuneCropDrops(Block block, Item product, Item seed, int maxAge) {
        IntegerProperty ageProp = (block instanceof KLCropBlock crop) ? crop.getAgeProperty() : BlockStateProperties.AGE_3;
        LootItemCondition.Builder isMaxAge = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(ageProp, maxAge));
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(seed)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(isMaxAge)
                        .add(LootItem.lootTableItem(product)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
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
