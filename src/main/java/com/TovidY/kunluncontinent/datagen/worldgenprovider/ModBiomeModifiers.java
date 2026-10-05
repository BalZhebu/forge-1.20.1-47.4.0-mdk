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
            new ResourceLocation(KlMain.MOD_ID, "add_ice_crystal_snowy")
    );

    // 额外生成：仅限冰刺之地（高概率）
    public static final ResourceKey<BiomeModifier> ADD_ICE_CRYSTAL_SPIKES = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_ice_crystal_spikes")
    );

    public static final ResourceKey<BiomeModifier> ADD_ICE_CRYSTAL_ICE = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_ice_crystal_ice")
    );

    // ── PlayerNPC 生成配置 ─────────────────────────
    public static final ResourceKey<BiomeModifier> ADD_NPC_OVERWORLD = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_npc_overworld")
    );
    public static final ResourceKey<BiomeModifier> ADD_NPC_NETHER = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_npc_nether")
    );
    public static final ResourceKey<BiomeModifier> ADD_NPC_END = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_npc_end")
    );

    // ── 神界大树：只在神界群系生成 ─────────────────────
    public static final ResourceKey<BiomeModifier> ADD_DIVINE_REALM_TREE = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_divine_realm_tree")
    );

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        // ⭐ 神界大树：只加到神界群系（celestial_realm）
        //    密度由 ModCelestialTreeFeature.placement() 里的 RARITY 控制（每 6 格最多 1 棵）
        context.register(ADD_DIVINE_REALM_TREE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.CELESTIAL_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(
                        ModCelestialTreeFeature.DIVINE_REALM_TREE_PLACED)),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION
        ));

        // 1. 基础配置：覆盖所有雪狐出现的群系（权重 10）
        context.register(ADD_ICE_CRYSTAL_BASE, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.SPAWNS_SNOW_FOXES),
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.ICE_CRYSTAL.get(), 20, 1, 2))
        ));
        // 2. 增强配置：仅针对冰刺之地 (Ice Spikes)
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
                        new MobSpawnSettings.SpawnerData(EntityInit.ICE_CRYSTAL.get(), 15, 1, 1),
                        //雪魔
                        new MobSpawnSettings.SpawnerData(EntityInit.SNOW_DEMON.get(), 10, 1, 1),
                        //流浪者
                        new MobSpawnSettings.SpawnerData(EntityType.STRAY, 20, 2, 4),
                        //苦力怕
                        new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 10, 1, 1)
                )
        ));

        // ── PlayerNPC 自然生成 ───────────────────────
        // 权重 3：主世界普通群系，等级由 getSpawnLevel 控制（1-40级，5%稀有出高50-99级）
        context.register(ADD_NPC_OVERWORLD, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.PLAYER_NPC.get(), 4, 1, 1))
        ));
        // 权重 3：下界，等级 30-70，5%稀有出80-99级
        context.register(ADD_NPC_NETHER, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.PLAYER_NPC.get(), 3, 1, 1))
        ));
        // 权重 3：末地，等级 50-99
        context.register(ADD_NPC_END, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.PLAYER_NPC.get(), 3, 1, 1))
        ));
    }
}