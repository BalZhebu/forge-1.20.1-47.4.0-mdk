package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensions {
    // 维度实例的 ResourceKey
    public static final ResourceKey<Level> POLAR_ICE_REALM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "polar_ice_realm"));

    // 维度类型的 ResourceKey (定义物理规则)
    public static final ResourceKey<DimensionType> POLAR_ICE_REALM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "polar_ice_realm_type"));

    // 群系的 ResourceKey
    public static final ResourceKey<Biome> POLAR_ICE_BIOME = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "polar_ice_realm"));
}