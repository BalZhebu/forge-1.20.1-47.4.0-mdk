package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.datagen.oredatagen.ModWorldGenOreProvider;
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

    public static final ResourceKey<BiomeModifier> ADD_LONE_BAMBOO = ResourceKey.create(
            ForgeRegistries.Keys.BIOME_MODIFIERS,
            new ResourceLocation(KlMain.MOD_ID, "add_lone_bamboo")
    );

    /** ⭐ 神界草丛：只加到神界群系（装饰层，紧贴地表生成）。 */
    public static final ResourceKey<BiomeModifier> ADD_DIVINE_REALM_GRASS =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(KlMain.MOD_ID, "add_divine_realm_grass"));

    // ── 蕴灵矿：按维度分组，一组带多个 placed feature ──────────────
    /** 1/2/3 阶：主世界。 */
    public static final ResourceKey<BiomeModifier> ADD_SPIRIT_ORE_OVERWORLD =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(KlMain.MOD_ID, "add_spirit_ore_overworld"));
    /** 4 阶：下界。 */
    public static final ResourceKey<BiomeModifier> ADD_SPIRIT_ORE_NETHER =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(KlMain.MOD_ID, "add_spirit_ore_nether"));
    /** 5 阶：末地。 */
    public static final ResourceKey<BiomeModifier> ADD_SPIRIT_ORE_END =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(KlMain.MOD_ID, "add_spirit_ore_end"));
    /** 6 阶：极寒冰域。 */
    public static final ResourceKey<BiomeModifier> ADD_SPIRIT_ORE_POLAR_ICE =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(KlMain.MOD_ID, "add_spirit_ore_polar_ice"));
    /** 7 阶：万雷天域。 */
    public static final ResourceKey<BiomeModifier> ADD_SPIRIT_ORE_THUNDER =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(KlMain.MOD_ID, "add_spirit_ore_thunder"));

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

        // ⭐ 神界草丛：与树同一个装饰层（VEGETAL_DECORATION），
        //   它自己的 placed feature 里已有 SurfaceWaterDepthFilter(0)+Heightmap，
        //   所以只会贴地长、不会浮空、不会长在水里。
        context.register(ADD_DIVINE_REALM_GRASS, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.CELESTIAL_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(
                        ModCelestialTreeFeature.DIVINE_REALM_GRASS_PATCH_PLACED)),
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

        // ── 孤竹 ───────────────────────────────────────
        // CREATURE 类别本身就不分昼夜，所以「白天晚上都出」不需要额外配置。
        // 权重 6：比牛羊（8~12）略低一点，当景观桩子用，不至于满地都是。
        context.register(ADD_LONE_BAMBOO, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                List.of(new MobSpawnSettings.SpawnerData(EntityInit.LONE_BAMBOO.get(), 3, 1, 2))
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

        // ══════════════════ 蕴灵矿 ══════════════════
        // 密度/高度全在 ModWorldGenOreProvider.placement() 里，这里只负责「挂到哪些群系」。
        var step = net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES;

        // 主世界：1 / 2 / 3 阶
        context.register(ADD_SPIRIT_ORE_OVERWORLD, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(
                        placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.ONE_SPIRIT_STONE_ORE.get())),
                        placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.TWO_SPIRIT_STONE_ORE.get())),
                        placedFeatures.getOrThrow(ModWorldGenOreProvider.createPlaceKey(ModBlocks.THREE_SPIRIT_STONE_ORE.get()))),
                step
        ));

        // 下界：4 阶
        context.register(ADD_SPIRIT_ORE_NETHER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(
                        ModWorldGenOreProvider.createPlaceKey(ModBlocks.FOUR_SPIRIT_STONE_ORE.get()))),
                step
        ));

        // 末地：5 阶
        context.register(ADD_SPIRIT_ORE_END, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(
                        ModWorldGenOreProvider.createPlaceKey(ModBlocks.FIVE_SPIRIT_STONE_ORE.get()))),
                step
        ));

        // 极寒冰域：6 阶（自定义群系，不是 IS_OVERWORLD）
        context.register(ADD_SPIRIT_ORE_POLAR_ICE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.POLAR_ICE_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(
                        ModWorldGenOreProvider.createPlaceKey(ModBlocks.SIX_SPIRIT_STONE_ORE.get()))),
                step
        ));

        // 万雷天域：7 阶
        context.register(ADD_SPIRIT_ORE_THUNDER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModDimensions.THUNDER_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(
                        ModWorldGenOreProvider.createPlaceKey(ModBlocks.SEVEN_SPIRIT_STONE_ORE.get()))),
                step
        ));
    }
}