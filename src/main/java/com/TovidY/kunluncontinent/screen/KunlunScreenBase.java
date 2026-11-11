package com.TovidY.kunluncontinent.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 昆仑<b>普通面板</b>（非容器）的自适应基类：面板装不下就整体等比缩小。
 *
 * <h3>与 {@link KunlunContainerScreen} 的区别</h3>
 * <p>容器面板有 {@code leftPos/topPos} 和槽位坐标系，缩放时要把左上角重新居中；
 * 普通面板（技能轮盘、选择框…）的内容都是围绕<b>屏幕中心</b>用固定偏移量画出来的，
 * 所以这里直接<b>以屏幕中心为锚点</b>缩放，布局代码一行都不用改。</p>
 *
 * <h3>子类约定</h3>
 * <ul>
 *   <li>构造时给出 {@code designWidth/designHeight}：这套布局能容纳的最小逻辑分辨率
 *       （也就是作者在自己屏幕上调出来的那个"刚好装下"的尺寸）；</li>
 *   <li>绘制逻辑写在 {@link #renderContent}，<b>不要</b>覆写 {@code render}；</li>
 *   <li>整屏暗化由 {@link #renderBackdrop} 在缩放外统一画（子类想换颜色就覆写它，
 *       别再自己在 {@code renderContent} 里 fill 全屏）。</li>
 * </ul>
 *
 * <p>鼠标坐标会按「中心 + (鼠标 - 中心) / 缩放」换算后再交给 {@code super}，
 * 所以子类里的 {@code isMouseInSector}/{@code mouseClicked} 等命中判断不用改。</p>
 */
public abstract class KunlunScreenBase extends Screen {

    public static final int SAFE_MARGIN = 6;

    private final int designWidth;
    private final int designHeight;
    private float uiScale = 1.0F;

    protected KunlunScreenBase(Component title, int designWidth, int designHeight) {
        super(title);
        this.designWidth = Math.max(1, designWidth);
        this.designHeight = Math.max(1, designHeight);
    }

    @Override
    protected void init() {
        super.init();
        this.uiScale = computeUiScale();
    }

    protected float computeUiScale() {
        float fitW = (this.width - SAFE_MARGIN * 2.0F) / (float) this.designWidth;
        float fitH = (this.height - SAFE_MARGIN * 2.0F) / (float) this.designHeight;
        return Math.max(0.40F, Math.min(1.0F, Math.min(fitW, fitH)));
    }

    public float getUiScale() {
        return this.uiScale;
    }

    // ==================================================================================
    //  渲染
    // ==================================================================================

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackdrop(guiGraphics);

        if (this.uiScale >= 0.999F) {
            renderContent(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        float cx = this.width / 2.0F;
        float cy = this.height / 2.0F;
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0.0F);
        pose.scale(this.uiScale, this.uiScale, 1.0F);
        pose.translate(-cx, -cy, 0.0F);
        try {
            renderContent(guiGraphics,
                    (int) panelX(mouseX),
                    (int) panelY(mouseY),
                    partialTick);
        } finally {
            pose.popPose();
        }
    }

    /** 整屏底衬（缩放外画一次，保证铺满）。 */
    protected void renderBackdrop(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);
    }

    /** 真正的绘制入口（<b>子类覆写这个</b>）。 */
    protected abstract void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    // ==================================================================================
    //  鼠标：屏幕坐标 → 内容坐标（以屏幕中心为锚点反算）
    // ==================================================================================

    public double panelX(double screenX) {
        return this.width / 2.0 + (screenX - this.width / 2.0) / this.uiScale;
    }

    public double panelY(double screenY) {
        return this.height / 2.0 + (screenY - this.height / 2.0) / this.uiScale;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(panelX(mouseX), panelY(mouseY), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(panelX(mouseX), panelY(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(panelX(mouseX), panelY(mouseY), button,
                dragX / this.uiScale, dragY / this.uiScale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(panelX(mouseX), panelY(mouseY), delta);
    }
}
