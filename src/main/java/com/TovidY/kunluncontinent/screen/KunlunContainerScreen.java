package com.TovidY.kunluncontinent.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * 昆仑所有<b>容器面板</b>的公共基类：负责「面板装不下就整体等比缩小」。
 *
 * <h3>解决的问题</h3>
 * <p>面板的 {@code imageWidth/imageHeight} 是按作者电脑的界面尺寸（GUI Scale 3）
 * 定的固定值（例如 330×190、330×240）。玩家把界面尺寸调大之后，<b>逻辑分辨率会变小</b>
 * （1920×1080 在 Scale 4 下只有 480×270，Scale 5 只有 384×216），
 * 大面板就会顶出屏幕边缘、按钮和槽位点不到。</p>
 *
 * <h3>做法</h3>
 * <ul>
 *   <li>{@code init()} 里按「屏幕逻辑尺寸 / 面板尺寸」算一个 {@link #uiScale}，
 *       <b>只在放不下时缩小</b>（放得下就保持 1，绝不放大小面板以免像素发虚）；</li>
 *   <li>{@code render} 里整体 {@code pose().scale()} 一圈，鼠标坐标在同一个比例下反算；</li>
 *   <li>子类要画自己的东西，覆写 {@link #renderPanel} 而不是 {@code render}
 *       （覆写 render 会绕过缩放，面板又歪了）。</li>
 * </ul>
 *
 * <h3>⚠️ 子类里的 super 调用</h3>
 * <p>在 {@link #renderPanel} 里要画"原版那层"（背景 + 槽位 + 按钮）时，
 * 调 <b>{@code super.renderPanel(...)}</b>，<b>不要</b>调 {@code super.render(...)}
 * ——后者会再次进入缩放包装，缩放叠两次。</p>
 */
public abstract class KunlunContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    /** 面板四周至少留出的逻辑像素，避免贴着屏幕边。 */
    public static final int SAFE_MARGIN = 6;

    /** 当前生效的缩放（1 = 原始大小）。 */
    private float uiScale = 1.0F;

    /** 缩放渲染期间屏蔽内层 renderBackground（外面已经整屏画过一次）。 */
    private boolean suppressBackground = false;

    protected KunlunContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.uiScale = computeUiScale();
        // leftPos/topPos 是「缩放后坐标系」里的位置，所以用 width / scale 来居中
        this.leftPos = Math.max(0, Math.round((this.width / this.uiScale - this.imageWidth) / 2.0F));
        this.topPos = Math.max(0, Math.round((this.height / this.uiScale - this.imageHeight) / 2.0F));
    }

    /** 屏幕放不下就缩小；放得下保持 1。 */
    protected float computeUiScale() {
        float fitW = (this.width - SAFE_MARGIN * 2.0F) / (float) this.imageWidth;
        float fitH = (this.height - SAFE_MARGIN * 2.0F) / (float) this.imageHeight;
        return Math.max(0.45F, Math.min(1.0F, Math.min(fitW, fitH)));
    }

    /** 当前缩放比例（子类要按屏幕坐标画东西时用得上）。 */
    public float getUiScale() {
        return this.uiScale;
    }

    /** 屏幕像素 → 面板坐标。 */
    public double toPanelX(double screenX) {
        return screenX / this.uiScale;
    }

    public double toPanelY(double screenY) {
        return screenY / this.uiScale;
    }

    // ==================================================================================
    //  渲染
    // ==================================================================================

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.uiScale >= 0.999F) {
            renderPanel(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }
        // 模糊 + 暗化必须<b>在缩放之外</b>整屏画一次：
        // 否则 pose 缩放后它只盖住左上角 70%，会露出一圈亮边。
        this.renderBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(this.uiScale, this.uiScale, 1.0F);
        this.suppressBackground = true;
        try {
            renderPanel(guiGraphics,
                    (int) (mouseX / this.uiScale),
                    (int) (mouseY / this.uiScale),
                    partialTick);
        } finally {
            this.suppressBackground = false;
            guiGraphics.pose().popPose();
        }
    }

    /**
     * 真正的绘制入口（<b>子类覆写这个</b>）。
     *
     * <p>默认只画原版那层；需要额外绘制的面板覆写它，并在里面用
     * {@code super.renderPanel(...)} 代替 {@code super.render(...)}。</p>
     */
    protected void renderPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /** 缩放时屏蔽内层重复的整屏暗化（见 {@link #render}）。 */
    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        if (this.suppressBackground) {
            return;
        }
        super.renderBackground(guiGraphics);
    }

    // ==================================================================================
    //  鼠标：屏幕坐标 → 面板坐标
    // ==================================================================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX / this.uiScale, mouseY / this.uiScale, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX / this.uiScale, mouseY / this.uiScale, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX / this.uiScale, mouseY / this.uiScale, button,
                dragX / this.uiScale, dragY / this.uiScale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX / this.uiScale, mouseY / this.uiScale, delta);
    }
}
