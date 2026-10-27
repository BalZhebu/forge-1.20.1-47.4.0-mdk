package com.TovidY.kunluncontinent.datagen.blockprovider;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.item.Items;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.klblock.KLCropBlock;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.spiritstone.SpiritStoneQuality;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
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

        //极寒石：精准采集掉自身，否则掉极寒圆石（和原版石头/圆石一个道理）
        this.add(ModBlocks.EXTREME_COLD_STONE.get(), block -> createSilkTouchDispatchTable(block,
                this.applyExplosionDecay(block,
                        LootItem.lootTableItem(ModBlocks.EXTREME_COLD_COBBLESTONE.get()))));

        //极寒圆石：本身掉自己
        dropSelf(ModBlocks.EXTREME_COLD_COBBLESTONE.get());

        // ==================== 蕴灵矿 ====================
        // 每阶矿掉对应阶的蕴灵石，品质随机；时运会同时提高「数量」和「高品质占比」。
        this.add(ModBlocks.ONE_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.FIRST_SPIRIT_STONE.get()));
        this.add(ModBlocks.TWO_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.TWO_SPIRIT_STONE.get()));
        this.add(ModBlocks.THREE_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.THREE_SPIRIT_STONE.get()));
        this.add(ModBlocks.FOUR_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.FOUR_SPIRIT_STONE.get()));
        this.add(ModBlocks.FIVE_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.FIVE_SPIRIT_STONE.get()));
        this.add(ModBlocks.SIX_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.SIX_SPIRIT_STONE.get()));
        this.add(ModBlocks.SEVEN_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.SEVEN_SPIRIT_STONE.get()));
        this.add(ModBlocks.EIGHT_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.EIGHT_SPIRIT_STONE.get()));
        this.add(ModBlocks.NINE_SPIRIT_STONE_ORE.get(),
                block -> createSpiritOreDrops(block, ModItems.NINE_SPIRIT_STONE.get()));

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

    // ==================== 蕴灵矿掉落 ====================

    /**
     * 品质权重表（千分比，顺序 = {@link SpiritStoneQuality} 的 ordinal：
     * 废品 / 下品 / 中品 / 上品 / 极品 / 绝品 / 仙品）。
     *
     * <p>时运每一级都"略微"往高品质挪一点，而不是直接给满 —— 避免运气好时一镐子全是仙品。</p>
     */
    private static final int[] SPIRIT_WEIGHTS_BASE = {250, 300, 250, 130, 50, 18, 2};
    private static final int[] SPIRIT_WEIGHTS_FORTUNE_1 = {180, 280, 260, 160, 75, 35, 10};
    private static final int[] SPIRIT_WEIGHTS_FORTUNE_2 = {120, 250, 270, 190, 100, 50, 20};
    private static final int[] SPIRIT_WEIGHTS_FORTUNE_3 = {80, 220, 260, 210, 130, 70, 30};

    /**
     * 蕴灵矿的战利品表。
     *
     * <p>两个互斥的池子：</p>
     * <ul>
     *   <li><b>无时运</b>：固定 1 个，品质按 {@link #SPIRIT_WEIGHTS_BASE} 抽；</li>
     *   <li><b>有时运</b>：抽 1~3 次（每次独立抽品质 = 数量 1~3 且各颗品质不同），
     *       品质表按当前时运等级选，等级越高高品质权重越大。</li>
     * </ul>
     *
     * <p>⚠️ 品质不是单独掉一组物品，而是用 {@code set_nbt} 写进同一个蕴灵石的
     * {@code SpiritStoneQuality} 字段 —— 物品本身只有一个 id。</p>
     */
    private LootTable.Builder createSpiritOreDrops(Block ore, Item stone) {
        return LootTable.lootTable()
                // 无时运：固定 1 个
                .withPool(spiritPool(stone, SPIRIT_WEIGHTS_BASE,
                        ConstantValue.exactly(1.0F),
                        spiritFortuneAtLeast(1).invert()))
                // 时运 I：1~3 个
                .withPool(spiritPool(stone, SPIRIT_WEIGHTS_FORTUNE_1,
                        UniformGenerator.between(1.0F, 3.0F),
                        spiritFortuneAtLeast(1).and(spiritFortuneAtLeast(2).invert())))
                // 时运 II：1~3 个
                .withPool(spiritPool(stone, SPIRIT_WEIGHTS_FORTUNE_2,
                        UniformGenerator.between(1.0F, 3.0F),
                        spiritFortuneAtLeast(2).and(spiritFortuneAtLeast(3).invert())))
                // 时运 III+：1~3 个
                .withPool(spiritPool(stone, SPIRIT_WEIGHTS_FORTUNE_3,
                        UniformGenerator.between(1.0F, 3.0F),
                        spiritFortuneAtLeast(3)));
    }

    /**
     * 一个「按品质加权、抽 {@code rolls} 次」的池子。
     *
     * <p>⚠️ 品质条目必须**直接挂在 pool 上**（pool 内部按 weight 随机选一个），
     * 不能用 {@code AlternativesEntry} 包起来 —— alternatives 是"按条件分支、第一个命中即停"，
     * 里面放一堆无条件条目会被数据校验判成 `Unreachable entry!` 并直接让 runData 失败。</p>
     */
    private static LootPool.Builder spiritPool(Item stone, int[] weights,
                                               NumberProvider rolls,
                                               LootItemCondition.Builder condition) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(rolls).when(condition);
        SpiritStoneQuality[] qualities = SpiritStoneQuality.values();
        for (int i = 0; i < qualities.length; i++) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(SpiritStoneQuality.NBT_KEY, qualities[i].ordinal());
            pool.add(LootItem.lootTableItem(stone)
                    .setWeight(weights[i])
                    .apply(SetNbtFunction.setTag(tag)));
        }
        return pool;
    }

    /** 工具上的时运是否 ≥ 指定等级。 */
    private static LootItemCondition.Builder spiritFortuneAtLeast(int level) {
        return MatchTool.toolMatches(ItemPredicate.Builder.item()
                .hasEnchantment(new EnchantmentPredicate(
                        Enchantments.BLOCK_FORTUNE,
                        MinMaxBounds.Ints.atLeast(level))));
    }

    private LootTable.Builder createDivineRealmGrassDrops(Block grass) {        LootItemCondition.Builder hasNoShearsOrSilkTouch = HAS_SHEARS.or(HAS_SILK_TOUCH).invert();

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
