package com.TovidY.kunluncontinent.block.blockentity;

import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

/**
 * 把汇灵台上的供品渲染成悬浮自转的物品。
 *
 * <h3>高度</h3>
 * <p>台面顶部在 {@code y = 9/16 = 0.5625}，物品中心取 {@code 0.82}（离台面约 0.26 格），
 * 再叠一个 0.06 的上下浮动，看起来像悬在台上而不是插在台里。</p>
 *
 * <h3>注意</h3>
 * <p>{@code ItemRenderer#getModel} 需要 {@code Level}，方块实体在区块刚加载时
 * {@code getLevel()} 可能还是 null，直接返回避免 NPE。</p>
 */
public class HunlingAltarRenderer implements BlockEntityRenderer<HunlingAltarBlockEntity> {

    /** 物品中心离方块底面的高度（0.82 ≈ 13/16）。 */
    private static final double FLOAT_BASE_Y = 0.82D;

    /** 上下浮动幅度（格）。 */
    private static final double FLOAT_AMPLITUDE = 0.06D;

    /** 常态自转速度（度/ tick）。 */
    private static final float SPIN_DEGREES_PER_TICK = 1.6f;

    /** 最后这么多 tick（3 秒）开始加速旋转。 */
    private static final double FINAL_SPIN_WINDOW = 60.0D;

    /**
     * 加速到常速的 (1 + BOOST) 倍 —— 18 就是最后一刻转 19 倍快。
     * 曲线是 {@code speed(p) = S * (1 + BOOST * p²)}，平方项让前期慢慢起、最后一刻猛冲。
     */
    private static final double FINAL_SPIN_BOOST = 18.0D;

    private final ItemRenderer itemRenderer;

    public HunlingAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(HunlingAltarBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getStored();
        if (stack.isEmpty()) {
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        double time = level.getGameTime() + partialTick;

        poseStack.pushPose();
        poseStack.translate(0.5D, FLOAT_BASE_Y + Math.sin(time * 0.08f) * FLOAT_AMPLITUDE, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) spinAngle(blockEntity, time)));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        BakedModel model = this.itemRenderer.getModel(stack, level, null, 0);
        this.itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource,
                packedLight, packedOverlay, model);

        poseStack.popPose();
    }

    /**
     * 当前的累积旋转角度。
     *
     * <h3>为什么要写成积分而不是 {@code time * speed}</h3>
     * <p>如果直接 {@code time * speed(t)}，因为 {@code time} 是个很大的数（世界时间），
     * {@code speed} 一变角度就会跳好几千度 —— 物品会突然闪一下换个朝向。
     * 所以这里算的是转速的<b>积分</b>：
     * {@code A = S·t₀ + S·W·(p + K·p³/3)}，其中 {@code p} 是窗口内的进度。
     * 转速从 S 连续涨到 S·(1+K)，角度本身也是连续的。</p>
     *
     * <p>没有仪式（{@code ritualEndTick == 0}）时就是最朴素的匀速旋转。</p>
     */
    private static double spinAngle(HunlingAltarBlockEntity blockEntity, double time) {
        long endTick = blockEntity.getRitualEndTick();
        if (endTick <= 0L) {
            return time * SPIN_DEGREES_PER_TICK;
        }

        double windowStart = endTick - FINAL_SPIN_WINDOW;
        if (time <= windowStart) {
            return time * SPIN_DEGREES_PER_TICK;
        }

        double p = Math.min(1.0D, (time - windowStart) / FINAL_SPIN_WINDOW);
        return windowStart * SPIN_DEGREES_PER_TICK
                + SPIN_DEGREES_PER_TICK * FINAL_SPIN_WINDOW
                        * (p + FINAL_SPIN_BOOST * p * p * p / 3.0D);
    }
}
