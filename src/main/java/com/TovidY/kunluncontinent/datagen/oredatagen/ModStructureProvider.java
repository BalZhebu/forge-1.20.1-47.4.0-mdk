package com.TovidY.kunluncontinent.datagen.oredatagen;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.List;
import java.util.Map;


public class ModStructureProvider {

    public static final ResourceKey<Structure> MY_BUILDING = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(KlMain.MOD_ID, "my_building"));

    public static final ResourceKey<StructureSet> MY_BUILDING_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            new ResourceLocation(KlMain.MOD_ID, "my_building_set"));

    public static final ResourceKey<Structure> UNDERWATER_RUINS = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(KlMain.MOD_ID, "underwater_ruins"));

    public static final ResourceKey<StructureSet> UNDERWATER_RUINS_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            new ResourceLocation(KlMain.MOD_ID, "underwater_ruins_set"));

    public static void bootstrapStructure(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(MY_BUILDING, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "my_building_pool"))),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        ));

        context.register(UNDERWATER_RUINS, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.IS_DEEP_OCEAN),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "underwater_ruins_pool"))),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                false,
                Heightmap.Types.OCEAN_FLOOR_WG
        ));
    }

    public static void bootstrapPools(BootstapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        Holder<StructureTemplatePool> emptyPool = pools.getOrThrow(Pools.EMPTY);

        context.register(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "my_building_pool")),
                new StructureTemplatePool(
                        emptyPool,
                        List.of(
                                Pair.of(StructurePoolElement.single(KlMain.MOD_ID + ":kl_building_nbt"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );

        context.register(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "underwater_ruins_pool")),
                new StructureTemplatePool(
                        emptyPool,
                        List.of(
                                Pair.of(StructurePoolElement.single(KlMain.MOD_ID + ":underwater_ruins"), 1)
                        ),
                        StructureTemplatePool.Projection.RIGID
                )
        );
    }

    public static void bootstrapStructureSet(BootstapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

        context.register(MY_BUILDING_SET, new StructureSet(
                structures.getOrThrow(MY_BUILDING),
                new RandomSpreadStructurePlacement(500, 100, RandomSpreadType.LINEAR, 143576182)
        ));

        context.register(UNDERWATER_RUINS_SET, new StructureSet(
                structures.getOrThrow(UNDERWATER_RUINS),
                new RandomSpreadStructurePlacement(
                        100,
                        20,
                        RandomSpreadType.LINEAR,
                        986514321
                )
        ));
    }
}