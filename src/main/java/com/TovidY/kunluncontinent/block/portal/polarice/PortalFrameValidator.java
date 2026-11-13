package com.TovidY.kunluncontinent.block.portal.polarice;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 传送门「框架是否还完整」的校验（门内腔固定 2 宽 × 3 高）。
 *
 * <p>用途与地狱门的 {@code PortalShape#isComplete()} 一致：框架被拆掉一块之后，
 * 门方块要能自己判断「我已经不成立了」，然后原地消散 —— 这就是
 * {@code PolarIcePortalBlock#updateShape} / {@code ThunderRealmPortalBlock#updateShape}
 * 里的判定。</p>
 *
 * <h3>为什么不用原版的 PortalShape</h3>
 * <p>{@code PortalShape} 的框架判定写死了 {@code Blocks.OBSIDIAN}（{@code FRAME} 静态谓词），
 * 且最小宽度 2 / 高度 3 + 会去找「最大的那块门」。我们这两座门：
 * <b>框架是自己的方块、内腔固定 2×3、形状不会变</b>，用二十来次
 * {@code getBlockState} 直接校验反而更准，也不会被原版的黑曜石判定带偏。</p>
 *
 * <h3>为什么从「当前格」反推原点</h3>
 * <p>邻居变化时拿到的只有「被更新的那一格」，不知道它在门里的第几行第几列。
 * 所以先往下走到最下一行、再往左走到最左一列，得到内腔左下角，
 * 再从那里按 2×3 整体校验（和原版 {@code calculateBottomLeft} 同一思路）。
 * 两个方向都限了步数，避免异常数据把循环拖长。</p>
 */
public final class PortalFrameValidator {

    /** 门内腔宽（格）。与 {@code ModPortalEvents#checkFrame} 保持一致。 */
    public static final int WIDTH = 2;
    /** 门内腔高（格）。 */
    public static final int HEIGHT = 3;

    private PortalFrameValidator() {
    }

    /** 门平面里「向右」的方向：X 轴的门朝 EAST 排开，Z 轴的门朝 SOUTH 排开。 */
    public static Direction rightOf(Direction.Axis axis) {
        return axis == Direction.Axis.Z ? Direction.SOUTH : Direction.EAST;
    }

    /**
     * 从任意一块门方块出发，判断它所在的这座门是否还完整。
     *
     * @param pos    任意一块门方块的位置
     * @param axis   这扇门的朝向轴（取自方块状态的 {@code AXIS}）
     * @param portal 门方块本身
     * @param frame  框架方块
     * @return {@code false} = 框架已经残缺，这扇门应当消散
     */
    public static boolean isComplete(BlockGetter level, BlockPos pos, Direction.Axis axis,
                                     Block portal, Block frame) {
        Direction right = rightOf(axis);
        Direction left = right.getOpposite();

        // ① 往下走到最下一行
        BlockPos origin = pos;
        for (int i = 0; i < HEIGHT - 1; i++) {
            BlockPos below = origin.below();
            if (!isPortal(level, below, portal, axis)) {
                break;
            }
            origin = below;
        }
        // ② 往左走到最左一列
        for (int i = 0; i < WIDTH - 1; i++) {
            BlockPos next = origin.relative(left);
            if (!isPortal(level, next, portal, axis)) {
                break;
            }
            origin = next;
        }
        return isFrameIntact(level, origin, right, frame) && isInteriorIntact(level, origin, right, axis, portal);
    }

    /**
     * 只校验框架（内腔不管）。建门时也能用。
     *
     * @param origin 内腔左下角
     * @param right  门平面里「向右」的方向
     */
    public static boolean isFrameIntact(BlockGetter level, BlockPos origin, Direction right, Block frame) {
        if (frame == null) {
            return false;
        }
        Direction left = right.getOpposite();
        for (int d = 0; d < WIDTH; d++) {
            // 上下两条横梁
            if (!level.getBlockState(origin.relative(right, d).below()).is(frame)) {
                return false;
            }
            if (!level.getBlockState(origin.relative(right, d).above(HEIGHT)).is(frame)) {
                return false;
            }
        }
        for (int h = 0; h < HEIGHT; h++) {
            // 左右两根立柱
            if (!level.getBlockState(origin.relative(left).above(h)).is(frame)) {
                return false;
            }
            if (!level.getBlockState(origin.relative(right, WIDTH).above(h)).is(frame)) {
                return false;
            }
        }
        return true;
    }

    /** 内腔 2×3 必须全是「同一扇门的同一轴」的门方块。 */
    public static boolean isInteriorIntact(BlockGetter level, BlockPos origin, Direction right,
                                           Direction.Axis axis, Block portal) {
        for (int d = 0; d < WIDTH; d++) {
            for (int h = 0; h < HEIGHT; h++) {
                if (!isPortal(level, origin.relative(right, d).above(h), portal, axis)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 该格是否是指定门方块、且朝向轴一致。 */
    public static boolean isPortal(BlockGetter level, BlockPos pos, Block portal, Direction.Axis axis) {
        BlockState state = level.getBlockState(pos);
        return state.is(portal) && state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == axis;
    }
}
