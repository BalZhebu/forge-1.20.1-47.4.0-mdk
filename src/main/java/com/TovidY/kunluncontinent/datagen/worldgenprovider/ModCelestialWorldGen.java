package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;
import java.util.OptionalLong;

/**
 * <b>神界（天境）</b>的 datagen —— 梦幻蓝天 + 大尺寸浮空岛 + 岛下虚空。
 *
 * <p>全部用 datagen 生成，产物在 {@code src/generated/resources/data/kunluncontinent/}：
 * 改数值后重跑 {@code runData} 即可，不用手写 json。</p>
 *
 * <h3>调参入口（都在这一个文件里）</h3>
 * <pre>
 *   ISLAND_MIN_Y / ISLAND_HEIGHT  → 岛屿在 Y 轴的高度与厚度
 *   base.continents()的倍率        → 岛屿的疏密（见 celestialNoiseSettings 的注释）
 *   SKY_COLOR / FOG_COLOR         → 天空与雾的颜色
 *   celestialSurfaceRules()       → 岛屿表面用什么方块
 * </pre>
 */
public final class ModCelestialWorldGen {

    private ModCelestialWorldGen() {
    }

    // ==================== 可调参数 ====================

    /**
     * 岛屿基岩高度与厚度。
     *
     * <p>{@code noiseSizeHorizontal=4} 让噪声格子变4 倍 → 岛屿<b>大很多</b>
     * （原版末地浮空岛是 2，岛屿零碎）。</p>
     */
    // ⚠️ 这两个值必须跟着 end() 的设计走（0 / 128）。改高改低都会让岛屿消失 ——
    //    NoiseRouterData.end() 的密度函数是按这个高度区间设计的。
    private static final int ISLAND_MIN_Y = 0;
    private static final int ISLAND_HEIGHT = 128;     // 必须是 16 的倍数
    /**
     * 岛屿横向尺寸系数（1~4）。**越大岛越大**。
     * 2 = 原版末地浮空岛尺寸（很零碎），4 = 大片连续的地块。
     * ⚠️ 只改这个数就能调岛屿大小；不要动 min_y / height。
     */
    private static final int ISLAND_NOISE_SIZE_H = 4;
    /**
     * 岛屿密度阈值：噪声值 &gt; 它的地方才是实心岛屿。
    /**
     * ⭐ BlendedNoise 的 **XZ 缩放**（Aether 原值 0.25）：控制岛屿横向大小。
     * 调大 → 岛更大更少；调小 → 岛更碎更小。
     */
    private static final double ISLAND_XZ_SCALE = 0.25;
    /**
     * ⭐ BlendedNoise 的 **Y 缩放**（Aether 原值 0.25）：控制岛的垂直厚度。
     *
     * <p><b>必须与 XZ 保持同量级</b>：Aether 两者都是 0.25。
     * 之前我把它压到 0.035 反而成了"无限延伸的柱子"—— Y 越平坦，等值面越接近柱体。</p>
     */
    private static final double ISLAND_Y_SCALE = 0.25;
    /** Aether 原版第一道阈值（越大岛越少）。 */
    private static final double ISLAND_THRESHOLD_1 = -0.13D;
    /** Aether 原版第二道阈值（在slide 之后再压一道）。 */
    private static final double ISLAND_THRESHOLD_2 = -0.05D;

    /** 梦幻蓝天空色。 */
    private static final int SKY_COLOR = 0x87CEEB;      // 天蓝
    /** 淡青雾色（远处岛屿朦胧感）。 */
    private static final int FOG_COLOR = 0xBFE3FF;
    private static final int WATER_COLOR = 0x3FA9F5;
    private static final int WATER_FOG_COLOR = 0x7FC4FF;
    /** 天境草色偏青蓝，和普通世界区分开。 */
    private static final int GRASS_COLOR = 0x7FD4A8;

    // ==================== 群系 ====================

    public static void bootstrapCelestialBiome(BootstapContext<Biome> context) {
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(FOG_COLOR)
                .skyColor(SKY_COLOR)
                .waterColor(WATER_COLOR)
                .waterFogColor(WATER_FOG_COLOR)
                .grassColorOverride(GRASS_COLOR)
                .build();

        // ⭐ 不加任何默认特征：不要矿脉、不要草、不要藤蔓 —— 岛下就是虚空
        BiomeGenerationSettings gen = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER)).build();

        context.register(ModDimensions.CELESTIAL_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.8F)
                .downfall(0.0F)
                .specialEffects(effects)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)   // 天境不刷怪
                .generationSettings(gen)
                .build());
    }

    // ==================== 维度类型 ====================

    public static void bootstrapCelestialType(BootstapContext<DimensionType> context) {
        context.register(ModDimensions.CELESTIAL_REALM_TYPE, new DimensionType(
                OptionalLong.empty(),      // 没有固定时间
                true,                      // hasSkylight：要有天光
                false,                     // hasCeiling：没有天花板（上方是天空）
                false,                     // ultraWarm
                true,                      // natural
                1.0,                       // coordinateScale
                true,                      // bedWorks
                false,                     // respawnAnchorWorks
                0,// minY（与 noise_settings 一致）
                256,                       // height
                256,                       // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,   // 用主世界的天空/云，梦幻蓝由 biome 的 skyColor 覆盖
                0.0f,                      // 坐标缩放偏移
                // 不生成任何原版怪物
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        ));
    }

    // ==================== 噪声设置（岛屿骨架） ====================

    public static void bootstrapCelestialNoise(BootstapContext<NoiseGeneratorSettings> context) {
        HolderGetter<NormalNoise.NoiseParameters> noiseParameters = context.lookup(Registries.NOISE);

        HolderGetter<DensityFunction> densityFunctions = context.lookup(Registries.DENSITY_FUNCTION);

        // ① BlendedNoise：宽频段混合，xz/y 缩放相同（原版 0.25/0.25）
        DensityFunction density = BlendedNoise.createUnseeded(
                ISLAND_XZ_SCALE,   // xzScale = 0.25（Aether 原值）
                ISLAND_Y_SCALE,    // yScale  = 0.25（**与 xz 相同** —— 这就是"不像山也不像柱"的根）
                80.0D,             // Aether 原值
                160.0D,            // Aether 原值
                8.0D);             // Aether 原值

        // ② 第一道阈值
        density = DensityFunctions.add(density, DensityFunctions.constant(ISLAND_THRESHOLD_1));

        // ③ 上下双向滑动（Aether#slide 的复刻，全用 public API）
        density = aetherSlide(density);

        // ④ 第二道阈值
        density = DensityFunctions.add(density, DensityFunctions.constant(ISLAND_THRESHOLD_2));

        // ⑤⑥ 缩放 + 下限夹取
        density = DensityFunctions.mul(density, DensityFunctions.constant(1.0D));
        density = DensityFunctions.max(density, DensityFunctions.constant(0.0D));

        // ⑦ ⭐ 收尾：blendedDensity —— Aether 最后一步，不能省
        DensityFunction finalDensity = DensityFunctions.blendDensity(density);


        NoiseRouter router = new NoiseRouter(
                DensityFunctions.zero(),   // barrierNoise
                DensityFunctions.zero(),   // fluidLevelFloodedness
                DensityFunctions.zero(),   // fluidLevelSpread
                DensityFunctions.zero(),   // lavaNoise
                DensityFunctions.zero(),   // temperature
                DensityFunctions.zero(),   // vegetation
                DensityFunctions.zero(),   // continents
                DensityFunctions.zero(),   // erosion
                DensityFunctions.zero(),   // depth
                DensityFunctions.zero(),   // ridges
                finalDensity,              // initialDensityWithoutJaggedness
                finalDensity,              // ⭐ finalDensity = 实际地形密度
                DensityFunctions.zero(),   // veinToggle
                DensityFunctions.zero(),   // veinRidged
                DensityFunctions.zero()    // veinGap
        );

        NoiseSettings noiseSettings = NoiseSettings.create(
                ISLAND_MIN_Y,         // minY：0
                ISLAND_HEIGHT,        // height：128
                ISLAND_NOISE_SIZE_H,  // size_horizontal：**唯一能自由调的**，越大岛越大
                1);                   // size_vertical：1（末地原值，别动）

        context.register(ModDimensions.CELESTIAL_NOISE, new NoiseGeneratorSettings(
                noiseSettings,
                Blocks.STONE.defaultBlockState(),    // ⭐ defaultBlock 必须是石头（stoneDepth 计数器要靠它推进）
                Blocks.AIR.defaultBlockState(),      // defaultFluid：没有水
                router,
                celestialSurfaceRules(),             // 岛顶草方块 + 岛体石头
                List.of(),                           // spawnTarget：天境不刷怪
                -64,                                 // seaLevel
                true,                                // disableMobGeneration
                false,                               // aquifersEnabled：关含水层
                false,                               // oreVeinsEnabled
                true                                 // useLegacyRandomSource
        ));
    }

    /**
     * Aether（天境 mod）{@code slide()} 的复刻 —— <b>上下双向</b>的高度滑动。
     *
     * <p>原版参数：{@code slide(density, 0, 128, 72, 0, -0.2, 8, 40, -0.1)}，拆成两段：</p>
     * <ul>
     *   <li><b>上压</b>：{@code yClampedGradient(0+128-72, 0+128-0, 1, 0)}
     *       = 从 Y=56 到 Y=128 由 1 降到 0，再和原密度 lerp →
     *       <b>岛顶以上逐渐变虚空</b>，所以顶不会被世界天花板削平。</li>
     *   <li><b>下顶</b>：{@code yClampedGradient(0+8, 0+40, 0, 1)}
     *       = 从 Y=8 到 Y=40 由 0 升到 1，同样 lerp →
     *       <b>岛底以下逐渐变虚空</b>，所以下面是干净虚空、不需要额外挖空。</li>
     * </ul>
     *
     * <p>我上一版只做了"上压"且是线性的 → 所有岛顶在同一 Y 附近被切 → 高度趋同 + 侧壁直。
     * 双向 + 中间平滑才是 Aether 的做法。</p>
     */
    private static DensityFunction aetherSlide(DensityFunction density) {
        // 上压：Y 56→128，值 1→0
        DensityFunction topSlide = DensityFunctions.yClampedGradient(56, 128, 1.0D, 0.0D);
        DensityFunction afterTop = DensityFunctions.lerp(topSlide, -0.2D, density);
        // 下顶：Y 8→40，值 0→1
        DensityFunction bottomSlide = DensityFunctions.yClampedGradient(8, 40, 0.0D, 1.0D);
        return DensityFunctions.lerp(bottomSlide, -0.1D, afterTop);
    }

    /**
     * 岛屿表面规则：<b>顶部草方块，下面 6 格石头，再往下是虚空</b>。
     *
     * <p>{@code ON_FLOOR} 是岛屿的"地面"，{@code UNDER_FLOOR} 往下 3 格是石头，
     * 超过深度就是虚空 —— 这样岛屿是有厚度的浮空块，而不是一张纸。</p>
     */
    private static SurfaceRules.RuleSource celestialSurfaceRules() {
        // ⭐ 岛屿结构：最上面 1 格草方块，其余是石头，下面自然是虚空。
        //
        // ⚠️ 三条规则都用 defaultBlock(=石头) 打底之后才成立：
        //    stoneDepthCheck 统计的是"从下往上连续固体格数"，
        //    只有 defaultBlock 是石头，计数器才会推进、条件才会命中。
        //
        // 层级说明（sequence 第一条命中就停）：
        //   1) 岛面（石深=0，也就是岛顶那一格）→ 草方块
        //   2) 石深 1~ISLAND_THICKNESS → 石头
        //   3) 更深 → 石头（兜底；深于阈值的部分被洞穴/虚空规则接管）
        // ⚠️ SurfaceRules.state() 不能省 —— sequence/ifTrue 只收 RuleSource，不收 BlockState。
        return SurfaceRules.sequence(
                // 1) 岛顶：草方块
                SurfaceRules.ifTrue(
                        SurfaceRules.stoneDepthCheck(0, false, CaveSurface.FLOOR),
                        SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                // 2) 岛体：石头
                SurfaceRules.state(Blocks.STONE.defaultBlockState())
        );
    }

    // ==================== 关卡 ====================

    public static void bootstrapCelestialStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        context.register(ModDimensions.CELESTIAL_REALM_STEM, new LevelStem(
                types.getOrThrow(ModDimensions.CELESTIAL_REALM_TYPE),
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(biomes.getOrThrow(ModDimensions.CELESTIAL_BIOME)),
                        noiseSettings.getOrThrow(ModDimensions.CELESTIAL_NOISE)
                )));
    }
}