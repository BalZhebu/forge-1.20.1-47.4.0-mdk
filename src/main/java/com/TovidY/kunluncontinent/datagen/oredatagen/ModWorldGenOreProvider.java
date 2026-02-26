package com.TovidY.kunluncontinent.datagen.oredatagen;

import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ModWorldGenOreProvider {

    /**
     * 第一步：配置特征 (ConfiguredFeature)
     * 定义：矿石替换什么方块、矿簇的大小（一坨有多少个）
     */
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        // 参数说明：(上下文, 矿物方块, 矿簇大小)
        // 铁矿参考：原版 size 为 9
        registerOre(context, ModBlocks.GRAY_IRON_ORE.get(), 7);
        // 红玉参考
        registerOre(context, ModBlocks.RUBY_ORE.get(), 5);
        //紫水晶
        registerOre(context, ModBlocks.AMETHYST_ORE.get(), 5);
        //蓝晶
        registerOre(context, ModBlocks.SAPPHIRE_ORE.get(), 10);
        //星辰石
        registerOre(context, ModBlocks.STARLIGHT_STONE_ORE.get(), 5);
        // 钻石参考：原版 size 为 4 到 8
        registerOre(context, ModBlocks.CLOUD_PATTERNED_BRONZE_ORE.get(), 9);
        //赤火矿
        registerOre(context, ModBlocks.RED_FIRE_ORE.get(), 3);
        //银矿
        registerOre(context, ModBlocks.SUNKEN_SILVER_ORE.get(), 5);
        //寒心钢
        registerOre(context, ModBlocks.COLD_HEARTED_STEEL_ORE.get(), 3);
    }

    /**
     * 第二步：放置特征 (PlacedFeature)
     * 定义：矿物的稀有度、高度范围、分布方式
     */

    public static void placement(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        // 参数含义解析：
        // count: 每区块尝试生成的次数。数值越大，矿越密。
        // min/max: 生成的高度区间。
        // 【灰铁矿】 模拟原版铁矿分布
        // 原版铁矿参考：count 为 10-20，高度在 -64 到 72 之间
        registerPlacement(context, configuredFeatures, ModBlocks.GRAY_IRON_ORE, -16, 40, 5);
        registerPlacement(context, configuredFeatures, ModBlocks.RUBY_ORE, -64, -5, 3);
        // 【云纹青铜矿】 模拟稀有矿物 (如钻石/黄金)
        // 原版钻石参考：count 为 4-8，高度在 -64 到 16 之间
        registerPlacement(context, configuredFeatures, ModBlocks.CLOUD_PATTERNED_BRONZE_ORE, -64, -0, 3);
        //赤火矿
        registerPlacement(context, configuredFeatures, ModBlocks.RED_FIRE_ORE, 1, 100, 3);
        //银矿
        registerPlacement(context, configuredFeatures, ModBlocks.SUNKEN_SILVER_ORE, 0, 128, 5);
        //寒心钢
        registerPlacement(context, configuredFeatures, ModBlocks.COLD_HEARTED_STEEL_ORE, -64, 0, 3);
        //紫水晶
        registerPlacement(context, configuredFeatures, ModBlocks.AMETHYST_ORE, 0, 100, 3);
        //蓝晶
        registerPlacement(context, configuredFeatures, ModBlocks.SAPPHIRE_ORE, 0, 110, 5);
        //星辰石
        registerPlacement(context, configuredFeatures, ModBlocks.STARLIGHT_STONE_ORE, -64, 0, 5);
    }

    // 在 registerOre 中增加对不同维度的支持
    private static void registerOre(BootstapContext<ConfiguredFeature<?, ?>> context, Block block, int size) {
        List<OreConfiguration.TargetBlockState> targets = List.of(
                // 主世界：石头和深层岩
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), block.defaultBlockState()),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), block.defaultBlockState()),
                // 下界：地狱岩 (如果以后有下界矿物，这行就起作用了)
                OreConfiguration.target(new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES), block.defaultBlockState()),
                // 末地：末地石 (更改自定义方块写入BlockMatchTest方法)
                OreConfiguration.target(new BlockMatchTest(Blocks.END_STONE), block.defaultBlockState())
        );
        context.register(createConfigKey(block), new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(targets, size)));
    }

    private static void registerPlacement(BootstapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> getter,
                                          net.minecraftforge.registries.RegistryObject<Block> block, int min, int max, int count) {

        var configHolder = getter.getOrThrow(createConfigKey(block.get()));

        context.register(createPlaceKey(block.get()), new PlacedFeature(configHolder, List.of(
                CountPlacement.of(count),          // 每区块尝试生成的次数
                InSquarePlacement.spread(),        // 水平方向随机分布
                HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max)),
                BiomeFilter.biome()                // 只有符合条件的生物群系才会生成
        )));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> createConfigKey(Block block) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ForgeRegistries.BLOCKS.getKey(block));
    }

    public static ResourceKey<PlacedFeature> createPlaceKey(Block block) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ForgeRegistries.BLOCKS.getKey(block));
    }
}