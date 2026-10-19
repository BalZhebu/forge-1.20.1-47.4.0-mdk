package com.TovidY.kunluncontinent.worldgen.placement;

import com.TovidY.kunluncontinent.worldgen.ModPlacementModifiers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * 只在<b>干燥地面</b>上保留放置位置：该位置及其正下方都不能是流体。
 *
 * <h3>为什么需要它</h3>
 * <p>{@code heightmap(OCEAN_FLOOR)} 的判定是「第一个非空气方块的上方」，而<b>水也算非空气</b>，
 * 所以水面上的位置会被当成合法位置返回。下游的 {@code TreeFeature} 只做
 * {@code validTreePos} 检查，而 {@code minecraft:water} 在
 * {@code #minecraft:replaceable_by_trees} 里 —— 于是树会把水替换掉，直接长在水里。</p>
 *
 * <p>原版的 {@code trees_grove} 用 {@code surface_water_depth_filter} 来挡这种情况，
 * 但那个修饰符依赖 {@code WORLD_SURFACE} 高度图，而这张图在区块生成阶段根本不维护
 * （{@code ProtoChunk} 只有 {@code *_WG} 版本），{@code getHeight} 会返回世界最低点，
 * 判定恒通过 —— 等于没有过滤。所以这里自己做一份，直接读流体会更可靠。</p>
 *
 * <p>雪层不会命中：雪是方块不是流体，{@code getFluidState} 为空，所以雪地上的树照常生成。</p>
 */
public class DryGroundFilter extends PlacementModifier {

    public static final DryGroundFilter INSTANCE = new DryGroundFilter();

    /**
     * ⚠️ 必须是 {@link Codec} 而不是 {@code MapCodec}：
     * {@code PlacementModifierType#codec()} 的签名就是 {@code Codec<P>}。
     */
    public static final Codec<DryGroundFilter> CODEC = Codec.unit(INSTANCE);

    private DryGroundFilter() {
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        // 位置本身在水里
        if (!context.getLevel().getFluidState(pos).isEmpty()) {
            return Stream.empty();
        }
        // 脚下是水（河/湖/海面）—— 树会长在水上，直接剔除
        if (!context.getLevel().getFluidState(pos.below()).isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.DRY_GROUND.get();
    }
}
