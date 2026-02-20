package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

public class ModWorldGenProvider {
    // 1. 群系具体配置
    public static void bootstrapBiome(BootstapContext<Biome> context) {
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(0xC0D8FF).skyColor(0x90A0FF).waterColor(0x3F76E4).waterFogColor(0x050533)
                .build();
        BiomeGenerationSettings.Builder genSettings = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        BiomeDefaultFeatures.addDefaultOres(genSettings);
        BiomeDefaultFeatures.addSurfaceFreezing(genSettings);
        context.register(ModDimensions.POLAR_ICE_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(true).temperature(-0.8F).downfall(0.8F)
                .specialEffects(effects)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(genSettings.build()).build());
    }

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(ModDimensions.POLAR_ICE_REALM_TYPE, new DimensionType(
                OptionalLong.empty(), true, false, false, true, 1.0, true, false,
                -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.1f, new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        ));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);
        LevelStem stem = new LevelStem(types.getOrThrow(ModDimensions.POLAR_ICE_REALM_TYPE),
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(biomes.getOrThrow(ModDimensions.POLAR_ICE_BIOME)),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD) // 使用主世界噪声地形
                ));

        context.register(ResourceKey.create(Registries.LEVEL_STEM, ModDimensions.POLAR_ICE_REALM_LEVEL_KEY.location()), stem);
    }
}
