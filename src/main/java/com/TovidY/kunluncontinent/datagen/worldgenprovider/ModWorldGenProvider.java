package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import com.TovidY.kunluncontinent.worldgen.placement.DryGroundFilter;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Optional;
import java.util.OptionalLong;

public class ModWorldGenProvider {

    /**
     * 极寒冰域的云杉（<b>自建</b> placed feature）。
     *
     * <p>原先直接引用原版 {@code minecraft:trees_grove}，结果一颗树都不生成。
     * 拆开原版 JSON 后发现它的 placement 比想象中多两道：</p>
     * <pre>
     *   count(10/11) → in_square → surface_water_depth_filter(0) → heightmap(OCEAN_FLOOR) → biome
     * </pre>
     * <p>其中 {@code surface_water_depth_filter} 会用
     * {@code WORLD_SURFACE - OCEAN_FLOOR <= 0} 做判定，而这两张高度图的 Usage 是
     * {@code CLIENT/LIVE_WORLD}，在区块生成阶段并不维护（{@code ProtoChunk} 只有 {@code *_WG} 版本），
     * 判定恒通过 —— 等于没有过滤。这里自己写一份最小链：</p>
     * <pre>
     *   count(3) → in_square → heightmap(OCEAN_FLOOR) → dry_ground
     * </pre>
     * <ul>
     *   <li>{@code count} 从 8 降到 <b>3</b>：原来的密度太密，极寒冰域应该是稀疏针叶林；</li>
     *   <li>{@code dry_ground}（{@link DryGroundFilter}）把「脚下是水」的位置剔除，
     *       修掉"树长在水面上"的问题。雪是方块不是流体，不会被误杀；</li>
     *   <li>去掉了 {@code biome} 与 {@code would_survive} 修饰符 —— 树自己会做
     *       {@code validTreePos} 检查，草方块 / 泥土 / 雪层上都能正常生长。</li>
     * </ul>
     */
    public static final ResourceKey<PlacedFeature> POLAR_SPRUCE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE,
            new ResourceLocation(KlMain.MOD_ID, "polar_spruce"));

    /** 极寒冰域的云杉注册。 */
    public static void bootstrapPlacedFeature(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        PlacementUtils.register(context, POLAR_SPRUCE_PLACED,
                configured.getOrThrow(TreeFeatures.SPRUCE),
                CountPlacement.of(3),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                DryGroundFilter.INSTANCE);
    }

    // 1. 群系具体配置
    public static void bootstrapBiome(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(0xC0D8FF).skyColor(0x90A0FF).waterColor(0x3F76E4).waterFogColor(0x050533)
                .build();
        BiomeGenerationSettings.Builder genSettings = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        BiomeDefaultFeatures.addDefaultOres(genSettings);
        BiomeDefaultFeatures.addSurfaceFreezing(genSettings);
        // 极寒冰域的针叶林：自建 placed feature（原版 spruce，纯云杉）。
        // 不用 addGroveTrees 的原因见 POLAR_SPRUCE_PLACED 的注释（原版 trees_grove 带
        // surface_water_depth_filter + biome，在自定义维度里会整段筛掉）。
        genSettings.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, POLAR_SPRUCE_PLACED);
        context.register(ModDimensions.POLAR_ICE_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(true).temperature(-0.8F).downfall(0.8F)
                .specialEffects(effects)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(genSettings.build()).build());

        BiomeSpecialEffects thunderEffects = new BiomeSpecialEffects.Builder()
                .fogColor(0x232323)  // 深灰色浓雾
                .skyColor(0x101010)  // 极暗的天空
                .waterColor(0x1E3355) // 深蓝色水
                .waterFogColor(0x050510)
                .grassColorOverride(0x3D4D3D) // 枯败的草色
                .build();

        BiomeGenerationSettings.Builder gen = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        BiomeDefaultFeatures.addDefaultOres(gen);
        BiomeDefaultFeatures.addDefaultSprings(gen);
        BiomeDefaultFeatures.addDefaultExtraVegetation(gen); // 少量植被

        context.register(ModDimensions.THUNDER_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.7F)
                .downfall(1.0F)
                .specialEffects(thunderEffects)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(gen.build())
                .build());

        BiomeSpecialEffects towerEffects = new BiomeSpecialEffects.Builder()
                .fogColor(0x0A0515)
                .skyColor(0x05020A)
                .waterColor(0x110522)
                .waterFogColor(0x050111)
                .build();
        BiomeGenerationSettings.Builder towerGen = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        context.register(ModDimensions.TOWER_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.0F)
                .specialEffects(towerEffects)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(towerGen.build())
                .build());

    }

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(ModDimensions.POLAR_ICE_REALM_TYPE, new DimensionType(
                OptionalLong.empty(), true, false, false, true, 1.0, true, false,
                -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.1f, new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        ));

        context.register(ModDimensions.THUNDER_REALM_TYPE, new DimensionType(
                OptionalLong.empty(), true, false,
                false,
                true, 1.0, true, false,
                -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.1f, new DimensionType.MonsterSettings(false, false, ConstantInt.of(7), 0)
        ));

        context.register(ModDimensions.TOWER_REALM_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0,
                true,
                false,
                0,
                256,
                256,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.END_EFFECTS,
                0.1f,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0) // 禁止原版任何自然刷怪
        ));

    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        // 1. 极地冰原
        LevelStem stem = new LevelStem(types.getOrThrow(ModDimensions.POLAR_ICE_REALM_TYPE),
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(biomes.getOrThrow(ModDimensions.POLAR_ICE_BIOME)),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                ));

        // 2. 万雷天域
        context.register(ModDimensions.THUNDER_REALM_STEM, new LevelStem(
                types.getOrThrow(ModDimensions.THUNDER_REALM_TYPE),
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(biomes.getOrThrow(ModDimensions.THUNDER_BIOME)),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED)
                )
        ));

        net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings flatSettings =
                new net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings(
                        java.util.Optional.empty(),
                        biomes.getOrThrow(ModDimensions.TOWER_BIOME),
                        java.util.Collections.emptyList()
                );
        flatSettings.getLayersInfo().clear();
        flatSettings.getLayersInfo().add(new net.minecraft.world.level.levelgen.flat.FlatLayerInfo(1, net.minecraft.world.level.block.Blocks.AIR));

        net.minecraft.world.level.chunk.ChunkGenerator voidGenerator =
                new net.minecraft.world.level.levelgen.FlatLevelSource(flatSettings);

        LevelStem towerStem = new LevelStem(
                types.getOrThrow(ModDimensions.TOWER_REALM_TYPE),
                voidGenerator
        );

        context.register(ResourceKey.create(Registries.LEVEL_STEM, ModDimensions.TOWER_REALM_LEVEL_KEY.location()), towerStem);
        context.register(ResourceKey.create(Registries.LEVEL_STEM, ModDimensions.POLAR_ICE_REALM_LEVEL_KEY.location()), stem);
    }
}