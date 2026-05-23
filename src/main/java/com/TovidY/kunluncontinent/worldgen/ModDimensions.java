package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

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
}