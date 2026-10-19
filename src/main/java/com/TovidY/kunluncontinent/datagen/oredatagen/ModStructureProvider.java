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
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ModStructureProvider {

    public static final ResourceKey<Structure> MY_BUILDING = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(KlMain.MOD_ID, "my_building"));

    public static final ResourceKey<StructureSet> MY_BUILDING_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            new ResourceLocation(KlMain.MOD_ID, "my_building_set"));

    public static final ResourceKey<Structure> UNDERWATER_RUINS = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(KlMain.MOD_ID, "underwater_ruins"));

    public static final ResourceKey<StructureSet> UNDERWATER_RUINS_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            new ResourceLocation(KlMain.MOD_ID, "underwater_ruins_set"));

    /** 废弃丹房：主世界地表稀有结构，内含炼丹炉与 2 个奖励箱。 */
    public static final ResourceKey<Structure> ABANDONED_DANFANG = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang"));

    /**
     * 废弃丹房（地下版）：同一个模板，埋在 y=0~50 的地层里。
     *
     * <p>和地表版共用一个 {@code structure_set}，靠权重分配出现比例。
     * 之所以要做成两个 structure 而不是一个：{@code JigsawStructure} 的
     * {@code project_start_to_heightmap} 是"贴地表"和"按 start_height 自由放"的<b>二选一</b>，
     * 单个结构表达不了"地表也生成、地下也生成"。</p>
     */
    public static final ResourceKey<Structure> ABANDONED_DANFANG_UNDERGROUND = ResourceKey.create(Registries.STRUCTURE,
            new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang_underground"));

    public static final ResourceKey<StructureSet> ABANDONED_DANFANG_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang_set"));

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

        // 废弃丹房（地表）：主世界地表结构
        //
        // ⚠️ start_height 用 absolute(1) 而不是 0 —— 这是修「整栋建筑陷进地里一格」的关键。
        //    JigsawPlacement#addPieces 的落位公式是：
        //        k = start_height.y + 高度图高度        // WORLD_SURFACE_WG 给的是「地表上方第一格」
        //        y = k - (模板 minY + groundLevelDelta)  // StructurePoolElement 的 groundLevelDelta 恒为 1
        //    absolute(0) 时 y = 高度 - 1，正好是<b>草地那一层</b>，模板 y=0 的地板会把草方块顶掉
        //    → 玩家看到的整栋房子比地面低一格。+1 后 y = 高度，地板落在地表上方的空气层，与地面齐平。
        //    （不能改成 terrain_adaptation=NONE 绕过：那只影响地形过渡，不动这个 -1。）
        context.register(ABANDONED_DANFANG, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                ),
                pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang_pool"))),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(1)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
        ));

        // 废弃丹房（地下）：不投影高度图，起始 y 在 0~50 之间均匀采样。
        // 用 NONE 而不是 BEARD_THIN：地下不需要"土坡过渡"，BEARD 会在结构底部撑起一大片人造地形。
        // 模板里的 air 会正常覆写石头，所以挖出来的是一座封闭的废弃房间（挖矿时才会撞见）。
        context.register(ABANDONED_DANFANG_UNDERGROUND, new JigsawStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        Map.of(),
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.NONE
                ),
                pools.getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang_pool"))),
                Optional.empty(),
                1,
                UniformHeight.of(VerticalAnchor.absolute(1), VerticalAnchor.absolute(50)),
                false,
                Optional.empty(),
                80
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

        // 废弃丹房模板池
        context.register(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang_pool")),
                new StructureTemplatePool(
                        emptyPool,
                        List.of(
                                Pair.of(StructurePoolElement.single(KlMain.MOD_ID + ":abandoned_danfang"), 1)
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

        // 废弃丹房：地表 3 : 地下 1（同一格点按权重二选一）。
        //
        // 密度对照原版村庄（spacing 34 / separation 8）：
        //   本结构 40 / 10 → 每个格点平均 40×40 = 1600 区块出现一次，约为村庄的 72%
        //   （原来是 200 / 60 = 40000 区块一次，只有村庄的 2.9%，过于稀有）。
        // spacing 是「格点间距」，separation 是「两个结构最小间隔」，两者都必须 < spacing。
        context.register(ABANDONED_DANFANG_SET, new StructureSet(
                List.of(
                        StructureSet.entry(structures.getOrThrow(ABANDONED_DANFANG), 3),
                        StructureSet.entry(structures.getOrThrow(ABANDONED_DANFANG_UNDERGROUND), 1)
                ),
                new RandomSpreadStructurePlacement(
                        40,
                        10,
                        RandomSpreadType.LINEAR,
                        20261009
                )
        ));
    }
}