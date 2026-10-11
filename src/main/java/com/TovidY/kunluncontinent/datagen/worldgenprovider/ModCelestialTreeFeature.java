package com.TovidY.kunluncontinent.datagen.worldgenprovider;

import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

/**
 * <b>神界大树</b>地物 —— <b>完全照抄原版樱花树（cherry）的形状逻辑</b>，只把方块换成神界木。
 *
 * <p><b>为什么用 CherryTrunkPlacer + CherryFoliagePlacer</b>：
 * 你要的形态是「枝干细、顶部枝干横向蔓延、旁边被树叶包裹」，这正是樱花的树形 ——
 * 主干细高（7格）→ 顶部分叉出 3 条<b>横向</b>枝干向四周蔓延 → 树叶在枝端成团包裹。
 * 这两个 placer 在 1.20.1 里都是 <b>public</b>，可以直接用（樱花树本身就靠它们）。</p>
 *
 * <p>原版樱花参数（{@code TreeFeatures#cherry()}）与本项目完全一致：</p>
 * <pre>
 *   CherryTrunkPlacer(7, 1, 0,权重[1,2,3], UniformInt(2,4), UniformInt(-4,-3), UniformInt(-1,0))
 *   CherryFoliagePlacer(ConstantInt(4), ConstantInt(0), ConstantInt(5),
 *                       0.25F, 0.5F, 0.16666667F, 0.33333334F)
 *   TwoLayersFeatureSize(1, 0, 2)
 * </pre>
 *
 * <p>⚠️ 想调形态只改下面这几处数字，<b>不要换 placer 类型</b>：</p>
 * <ul>
 *   <li>{@code 7}（第1个参数）= 主干基高，越大越瘦高</li>
 *   <li>{@code UniformInt.of(2, 4)} = <b>横向枝干伸出长度</b>，调大→ 树冠更宽更蔓延</li>
 *   <li>{@code ConstantInt.of(4)} / {@code ConstantInt.of(5)} = 树叶团半径 / 厚度</li>
 * </ul>
 */
public final class ModCelestialTreeFeature {

    private ModCelestialTreeFeature() {
    }

    /** 地物（ConfiguredFeature）：决定"长什么样"。 */
    public static final ResourceKey<ConfiguredFeature<?, ?>> DIVINE_REALM_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    new ResourceLocation("kunluncontinent", "divine_realm_tree"));

    /** 放置规则（PlacedFeature）：决定"长在哪、多密"。 */
    public static final ResourceKey<PlacedFeature> DIVINE_REALM_TREE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation("kunluncontinent", "divine_realm_tree_placed"));

    // ==================== 调参入口 ====================

    /** 每多少格最多长 1 棵。越小越密。 */
    private static final int RARITY = 2;
    /** 每次尝试额外生成的树苗数量。 */
    private static final int EXTRA_TREES = 5;
    /** 额外树苗的生成概率。 */
    private static final float EXTRA_CHANCE = 0.1f;

    // ==================== 注册 ====================

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        // —— 参照原版樱花，只把 CHERRY_LOG / CHERRY_LEAVES 换成神界木 / 神界树叶——
        FeatureUtils.register(context, DIVINE_REALM_TREE, Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        // 树干：神界原木
                        BlockStateProvider.simple(ModBlocks.DIVINE_REALM_LOG.get()),
                        // 主干：细高 7 格，顶部横向蔓延出枝干
                        new CherryTrunkPlacer(
                                7,                // baseHeight：主干基高
                                1,                // heightA
                                0,                // heightB
                                // 横向枝干的"分叉高度权重"（1/2/3 各占 1 份）
                                new WeightedListInt(
                                        net.minecraft.util.random.SimpleWeightedRandomList.<IntProvider>builder()
                                                .add(ConstantInt.of(1), 1)
                                                .add(ConstantInt.of(2), 1)
                                                .add(ConstantInt.of(3), 1)
                                                .build()),
                                UniformInt.of(2, 4),    // ⭐ 枝干横向伸出长度（想更宽就调大）
                                UniformInt.of(-4, -3), // 枝干纵向偏移
                                UniformInt.of(-1, 0)   // 枝干横向偏移
                        ),
                        // 树叶：神界树叶
                        BlockStateProvider.simple(ModBlocks.DIVINE_REALM_LEAVES.get()),
                        // 树叶 placer：在枝端成团，包裹住蔓延的枝干
                        new CherryFoliagePlacer(
                                ConstantInt.of(4),    // ⭐ 树叶团横向半径（原版 4，越大越茂盛）
                                ConstantInt.of(0),    // 叶片团横向半径偏移
                                ConstantInt.of(5),    // 树叶团纵向半径（原版 5）
                                0.25F,               // 枝叶水平间距
                                0.5F,                // 枝叶垂直间距
                                0.16666667F,         // 叶团缩放
                                0.33333334F          // 顶部叶团缩放
                        ),
                        new TwoLayersFeatureSize(1, 0, 2)
                )
                        .ignoreVines()   // 神界木不生藤蔓
                        .build());
    }

    /**
     * 地物：<b>神界草丛</b>（随机小簇草）。
     *
     * <p>用 {@link Feature#RANDOM_PATCH} 而不是 {@code SIMPLE_PATCH}：
     * 前者会随机偏移 + 需要 {@code tries} 次尝试，铺出来的更自然；
     * 后者是固定 3×3 网格，一眼就能看出规律。</p>
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> DIVINE_REALM_GRASS_PATCH =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    new ResourceLocation("kunluncontinent", "divine_realm_grass_patch"));

    /** 放置规则：每格 chance 概率尝试，成功则生成一簇草。 */
    public static final ResourceKey<PlacedFeature> DIVINE_REALM_GRASS_PATCH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation("kunluncontinent", "divine_realm_grass_patch_placed"));

    /** ⭐ 草丛密度：每 GRASS_RARITY 格尝试 1 次。调小 → 更密。 */
    private static final int GRASS_RARITY = 1;

    /**
     * 内层地物「放一株草」的 <b>ConfiguredFeature</b>。
     *
     * <p>1.20.1 的 {@code RandomPatchConfiguration} <b>只有这一个构造器</b>：
     * {@code (tries, xzSpread, ySpread, Holder<PlacedFeature> feature)}——
     * 它<b>不接受方块</b>，只能嵌套一个 {@link PlacedFeature}。
     * 而"直接放一个方块"的地物是 {@link Feature#SIMPLE_BLOCK}
     *（配 {@code SimpleBlockConfiguration}），两者必须组合使用。</p>
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> DIVINE_REALM_GRASS_TALL =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    new ResourceLocation("kunluncontinent", "divine_realm_grass_tall"));

    public static final ResourceKey<PlacedFeature> DIVINE_REALM_GRASS_TALL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation("kunluncontinent", "divine_realm_grass_tall_placed"));

    private static final int GRASS_PER_CLUMP = 20;

    public static void bootstrapGrass(BootstapContext<ConfiguredFeature<?, ?>> context) {
        // ① 内层：真正"放一株草"的地物
        FeatureUtils.register(context, DIVINE_REALM_GRASS_TALL, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(
                        BlockStateProvider.simple(
                                com.TovidY.kunluncontinent.block.ModBlocks.DIVINE_REALM_GRASS
                                        .get().defaultBlockState())));
        FeatureUtils.register(context, DIVINE_REALM_GRASS_PATCH, Feature.RANDOM_PATCH,
                new RandomPatchConfiguration(
                        GRASS_PER_CLUMP,// tries：每次撒点撒几株
                        2,             // xzSpread：3×3 格内随机
                        0,             // ySpread：0 = 贴地
                        context.lookup(Registries.PLACED_FEATURE)
                                .getOrThrow(DIVINE_REALM_GRASS_TALL_PLACED)));
    }

    public static void placement(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        List<PlacementModifier> modifiers = List.of(
                InSquarePlacement.spread(),
                RarityFilter.onAverageOnceEvery(RARITY),
                CountPlacement.of(2),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                BiomeFilter.biome()
        );

        context.register(DIVINE_REALM_TREE_PLACED,
                new PlacedFeature(features.getOrThrow(DIVINE_REALM_TREE), modifiers));

        context.register(DIVINE_REALM_GRASS_TALL_PLACED,
                new PlacedFeature(features.getOrThrow(DIVINE_REALM_GRASS_TALL),
                        java.util.List.of()));

        context.register(DIVINE_REALM_GRASS_PATCH_PLACED,
                new PlacedFeature(features.getOrThrow(DIVINE_REALM_GRASS_PATCH), List.of(
                        InSquarePlacement.spread(),
                        // ⭐ 密度：每 GRASS_RARITY 格尝试一簇。调小 → 更密。
                        RarityFilter.onAverageOnceEvery(GRASS_RARITY),
                        // ⚠️ 这里【不能】加 CountPlacement：
                        //   草是单株方块（SIMPLE_BLOCK），叠放会对同一格反复放置，
                        //   浪费 tries 且更乱。成簇交给内层 RandomPatch 的 tries。
                        // 只在「地表水深度为 0」处生成 → 不会长在水里/空中
                        SurfaceWaterDepthFilter.forMaxDepth(0),
                        // ⭐ 关键：高度图让草严格贴住岛顶地表。
                        //   神界是浮空岛，没有这行草会长在岛侧壁甚至半空。
                        HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                        BiomeFilter.biome()
                )));
    }
}
