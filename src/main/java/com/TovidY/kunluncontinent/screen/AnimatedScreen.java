package com.TovidY.kunluncontinent.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public abstract class AnimatedScreen extends Screen {

    // --- 动画核心变量 ---
    private static final long ANIM_DURATION_MS = 350L; // 优雅的时长
    private long openTime = 0L;
    private long closeStartTime = 0L;
    private boolean isClosing = false;

    protected AnimatedScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        this.openTime = Util.getMillis();
        this.isClosing = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 绘制暗化背景
        this.renderBackground(guiGraphics);

        // 2. 计算当前动画进度与 Alpha 值
        float progress = calculateProgress();
        if (progress <= 0.001f && isClosing) {
            this.finishClose();
            return;
        }

        // 缩放与透明度算式
        float scale = isClosing ? (0.85f + 0.15f * easeInQuad(progress)) : easeOutBack(progress);
        float alpha = Mth.clamp(progress, 0.0f, 1.0f);
        int alphaBits = ((int) (alpha * 0xFF) & 0xFF) << 24;

        // 3. 计算缩放中心点（子类重写 getContentCenterX/Y 可动态调整，默认是屏幕中心）
        float centerX = getContentCenterX();
        float centerY = getContentCenterY();

        // 4. 矩阵变换
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);
        pose.scale(scale, scale, 1.0f);
        pose.translate(-centerX, -centerY, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 5. 调用子类的绘制方法，传入 alphaBits 便于文字和背景做透明度融合
        drawAnimatedContent(guiGraphics, mouseX, mouseY, partialTick, alphaBits);

        RenderSystem.disableBlend();
        pose.popPose();
    }

    /**
     * 子类实现此方法，专门绘制面板的内容（背景、文字、按钮等）
     * @param alphaBits 包含当前透明度通道的 32 位颜色掩码 (例如: alphaBits | 0xFFFFFF)
     */
    protected abstract void drawAnimatedContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int alphaBits);

    /**
     * 缩放中心点 X 坐标，默认全屏中心。如果子类面板不在正中间，可重写此方法
     */
    protected float getContentCenterX() {
        return this.width / 2.0f;
    }

    /**
     * 缩放中心点 Y 坐标，默认全屏中心。
     */
    protected float getContentCenterY() {
        return this.height / 2.0f;
    }

    // --- 动画逻辑 & 缓动算法 ---

    private float calculateProgress() {
        long now = Util.getMillis();
        if (isClosing) {
            long elapsed = now - closeStartTime;
            return Mth.clamp(1.0f - (float) elapsed / ANIM_DURATION_MS, 0.0f, 1.0f);
        } else {
            long elapsed = now - openTime;
            return Mth.clamp((float) elapsed / ANIM_DURATION_MS, 0.0f, 1.0f);
        }
    }

    private float easeOutBack(float x) {
        float c1 = 1.1f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2);
    }

    private float easeInQuad(float x) {
        return x * x;
    }

    // --- 按键响应与关闭逻辑 ---

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null) {
            int inventoryKey = this.minecraft.options.keyInventory.getKey().getValue();
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == inventoryKey) {
                this.onClose();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (!isClosing) {
            this.isClosing = true;
            this.closeStartTime = Util.getMillis();
            this.onStartClosing();
        }
    }

    /**
     * 关闭动画触发时的回调（供子类存值或处理逻辑）
     */
    protected void onStartClosing() {}

    /**
     * 动画完全播放完毕，真正的 Screen 关闭
     */
    private void finishClose() {
        super.onClose();
        this.onAnimationFinished();
    }

    /**
     * 关闭动画播放完毕后的回调（例如在此处向服务端发包）
     */
    protected void onAnimationFinished() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}