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
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
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

    // ==================== 放置规则注册 ====================

    public static void placement(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

        // ⚠️ 修饰器顺序有讲究：
        //    InSquare（先随机取一个起点）→ Rarity（决定这个格子要不要长）→ Count（长几棵）
        //    → 只有地面（贴地）→ Heightmap（按地表高度）→ Biome（限定群系）
        List<PlacementModifier> modifiers = List.of(
                InSquarePlacement.spread(),
                // ⭐ 密度：每 RARITY 格最多 1 棵。**调小 → 更密**。
                //    神界是稀疏浮岛，建议 4~8。想成丛就调到 3~4。
                RarityFilter.onAverageOnceEvery(RARITY),
                CountPlacement.of(2),
                // ⭐ 只在「地表水深度为 0」处生成 —— 1.20.1 没有 PlaceOnSurfacePlacement，
                //    这就是官方替代：水面/空气层以下不种树，等价于只长在地面��
                SurfaceWaterDepthFilter.forMaxDepth(0),
                // 用高度图让树根严格贴住地表
                HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                // 群系限定（神界群系会通过 biome modifier 加上这条 placed feature）
                BiomeFilter.biome()
        );

        context.register(DIVINE_REALM_TREE_PLACED,
                new PlacedFeature(features.getOrThrow(DIVINE_REALM_TREE), modifiers));
    }
}
