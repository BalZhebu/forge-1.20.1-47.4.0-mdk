package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class ModDimensions {
    // 维度实例的 ResourceKey
    public static final ResourceKey<Level> POLAR_ICE_REALM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(KlMain.MOD_ID, "polar_ice_realm"));

    // 维度类型的 ResourceKey (定义物理规则)
    public static final ResourceKey<DimensionType> POLAR_ICE_REALM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(KlMain.MOD_ID, "polar_ice_realm_type"));

    // 群系的 ResourceKey
    public static final ResourceKey<Biome> POLAR_ICE_BIOME = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(KlMain.MOD_ID, "polar_ice_realm"));

    // 万雷天域的维度实例
    public static final ResourceKey<Level> THUNDER_REALM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(KlMain.MOD_ID, "thunder_realm"));

    // 万雷天域的物理规则
    public static final ResourceKey<DimensionType> THUNDER_REALM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(KlMain.MOD_ID, "thunder_realm_type"));

    // 万雷天域唯一的群系
    public static final ResourceKey<Biome> THUNDER_BIOME = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(KlMain.MOD_ID, "thunder_biome"));

    // 关卡 Key (LevelStem)
    public static final ResourceKey<LevelStem> THUNDER_REALM_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(KlMain.MOD_ID, "thunder_realm"));

    // 维度实例的 Key (用于传送玩家: /execute in klmod:tower_realm ...)
    public static final ResourceKey<Level> TOWER_REALM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(KlMain.MOD_ID, "tower_realm"));

    // 维度类型 Key (定义物理规则: 能不能睡觉、高低限制)
    public static final ResourceKey<DimensionType> TOWER_REALM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(KlMain.MOD_ID, "tower_realm_type"));

    // 爬塔群系 Key (使用原版 void 虚空群系也可以，但自己定义一个可以改天空和雾气颜色，更帅)
    public static final ResourceKey<Biome> TOWER_BIOME = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(KlMain.MOD_ID, "tower_biome"));

    // 关卡 Key (LevelStem)
    public static final ResourceKey<LevelStem> TOWER_REALM_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(KlMain.MOD_ID, "tower_realm"));

    // ==================== 神界（天境） ====================
    // 梦幻蓝天 + 大尺寸浮空岛 + 岛下虚空
    public static final ResourceKey<Level> CELESTIAL_REALM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(KlMain.MOD_ID, "celestial_realm"));

    public static final ResourceKey<DimensionType> CELESTIAL_REALM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(KlMain.MOD_ID, "celestial_realm_type"));

    public static final ResourceKey<Biome> CELESTIAL_BIOME = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(KlMain.MOD_ID, "celestial_realm"));

    public static final ResourceKey<LevelStem> CELESTIAL_REALM_STEM = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(KlMain.MOD_ID, "celestial_realm"));

    /**
     * 天境的<b>噪声设置</b>（地形骨架）—— 改岛屿大小/高度/形状全靠这个 Key。
     * 岛屿表面规则（草方块/石头）另见 {@code ModWorldGenProvider#celestialSurfaceRules}。
     */
    public static final ResourceKey<NoiseGeneratorSettings> CELESTIAL_NOISE = ResourceKey.create(
            Registries.NOISE_SETTINGS, new ResourceLocation(KlMain.MOD_ID, "celestial_realm"));
}