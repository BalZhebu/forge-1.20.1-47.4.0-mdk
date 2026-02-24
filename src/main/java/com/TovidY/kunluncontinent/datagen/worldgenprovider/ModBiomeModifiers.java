package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ModBiomeModifiers {
    // 基础生成：所有雪地群系（低概率）
    public static final ResourceKey<BiomeModifier> ADD_ICE_CRYSTAL_BASE = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "add_ice_crystal_snowy")
    );

    // 额外生成：仅限冰刺之地（高概率）
    public static final ResourceKey<BiomeModifier> ADD_ICE_CRYSTAL_SPIKES = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "add_ice_crystal_spikes")
    );

    public static final ResourceKey<BiomeModifier> ADD_ICE_CRYSTAL_ICE = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "add_ice_crystal_ice")
    );

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);

        // 1. 基础配置：覆盖所有雪狐出现的群系（权重 10）
        context.register(ADD_ICE_CRYSTAL_BASE, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.SPAWNS_SNOW_FOXES),
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.ICE_CRYSTAL.get(), 20, 1, 2))
        ));

        // 2. 增强配置：仅针对冰刺之地 (Ice Spikes)
        // 我们通过 HolderSet.direct 直接指向特定群系，也可以自己写个 Tag
        context.register(ADD_ICE_CRYSTAL_SPIKES, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.ICE_SPIKES)),
                // 这里给一个很高的权重（比如 80），叠加基础的 10，总权重就是 90，几乎和僵尸一样多
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.ICE_CRYSTAL.get(), 30, 2, 4))
        ));

        //让这个生物生成在极寒冰域
        context.register(ADD_ICE_CRYSTAL_ICE, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.POLAR_ICE_BIOME)),
                List.of(
                        //冰晶
                        new MobSpawnSettings.SpawnerData(EntityInit.ICE_CRYSTAL.get(), 5, 1, 1),
                        //雪魔
                        new MobSpawnSettings.SpawnerData(EntityInit.SNOW_DEMON.get(), 2, 1, 1),
                        //流浪者
                        new MobSpawnSettings.SpawnerData(EntityType.STRAY, 20, 2, 4),
                        //苦力怕
                        new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 10, 1, 1)
                )
        ));
    }
}