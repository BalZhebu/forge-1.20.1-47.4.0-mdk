package com.TovidY.kunluncontinent.screen.attribute.point;

import com.TovidY.kunluncontinent.capability.playerattributes.AttributePointSpec;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.CPacketAllocatePoint;
import com.TovidY.kunluncontinent.network.client.CPacketResetPoints;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * 属性加点面板。
 *
 * <p>11 条属性全部由 {@link AttributePointSpec} 驱动 —— 名字、原属性名、每点收益、上限、
 * 颜色都在那一个枚举里，这里只负责画和发包。加属性 = 加一行枚举，不用改本文件。</p>
 *
 * <p>规则：<b>只能加不能减</b>，想减点必须用右下角"重置"（全额退还）。</p>
 */
public class PointScreen extends AbstractContainerScreen<PointMenu> implements AttributeTabs.Host {

    // ==================== 布局常量 ====================
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 236;

    /** 顶部"可用点数"信息块。 */
    private static final int HEADER_Y = 28;
    private static final int HEADER_H = 34;

    /** 11 条属性分两列（6 + 5）。 */
    private static final int ROWS_PER_COL = 6;
    private static final int COL_W = 182;
    private static final int COL_GAP = 8;
    private static final int LIST_X = 12;
    private static final int ROW_H = 22;

    private static final int BAR_H = 2;
    private static final int NAME_DX = 7;
    /** "x/99" 右边缘。 */
    private static final int POINTS_RIGHT = COL_W - 26;
    private static final int BTN_W = 18;
    private static final int BTN_H = 12;
    private static final int BTN_X = COL_W - BTN_W - 6;

    public PointScreen(PointMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = PANEL_W;
        this.imageHeight = PANEL_H;
    }

    private int listTop() {
        return this.topPos + HEADER_Y + HEADER_H + 8;
    }

    private int colX(int col) {
        return this.leftPos + LIST_X + col * (COL_W + COL_GAP);
    }

    private static int rowOf(int index) {
        return index % ROWS_PER_COL;
    }

    private static int colOf(int index) {
        return index / ROWS_PER_COL;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        AttributeTabs.buildTabs(this, AttributeTabs.PAGE_POINT);

        for (AttributePointSpec spec : AttributePointSpec.values()) {
            final int idx = spec.ordinal();
            addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font,
                    colX(colOf(idx)) + BTN_X,
                    listTop() + rowOf(idx) * ROW_H + 1,
                    BTN_W, BTN_H,
                    Component.literal("+"),
                    b -> NetworkHandler.INSTANCE.sendToServer(new CPacketAllocatePoint(idx, 1)),
                    () -> tooltipFor(spec)
            ));
        }

        int resetW = 84;
        addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                this.font,
                this.leftPos + this.imageWidth - resetW - 10,
                this.topPos + this.imageHeight - 24,
                resetW, 16,
                Component.literal("重置属性点"),
                b -> NetworkHandler.INSTANCE.sendToServer(new CPacketResetPoints()),
                () -> List.of(Component.literal("清空全部属性点，当前使用的点数§a全额退还§f。"),
                        Component.literal("需要消耗 §b1 个重置卷轴§f。")
        )));
    }

    /** 某个属性的 tooltip：别名 · 原属性名 / 每点收益 / 已加点数。 */
    private List<Component> tooltipFor(AttributePointSpec spec) {
        int points = cap() == null ? 0 : cap().getAllocatedPoints(spec);
        String mode = spec.getMode() == AttributePointSpec.Mode.FLAT ? "直接加算" : "百分比放大";
        return List.of(
                Component.literal(spec.getColorCode() + spec.getDisplayName()
                        + " §8· §f" + spec.getAttrName()),
                Component.literal("§7每点 §e+" + spec.getPerPointPercent() + "% §8(" + mode + ")")
                        .append(Component.literal("§7，满 " + AttributePointSpec.MAX_POINTS + " 点 §e+"
                                + spec.getMaxTotalPercent() + "%")),
                Component.literal("§7已加 §e" + points + " §7/ " + AttributePointSpec.MAX_POINTS + " 点")
        );
    }

    private PlayerAttributeCapability cap() {
        if (this.minecraft == null || this.minecraft.player == null) return null;
        return this.minecraft.player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .orElse(null);
    }

    @Override
    public int leftPos() {
        return this.leftPos;
    }

    @Override
    public int topPos() {
        return this.topPos;
    }

    @Override
    public int imageWidth() {
        return this.imageWidth;
    }

    @Override
    public net.minecraft.client.gui.Font font() {
        return this.font;
    }

    @Override
    public void addWidget(net.minecraft.client.gui.components.AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }

    // ==================== 渲染 ====================

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);

        for (var child : this.children()) {
            if (child instanceof KunlunGuiHelper.HandDrawnButton btn
                    && mouseX >= btn.getX() && mouseX < btn.getX() + btn.getWidth()
                    && mouseY >= btn.getY() && mouseY < btn.getY() + btn.getHeight()) {
                btn.renderTooltip(gui, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        gui.drawString(this.font, "§6属性点面板", this.leftPos + 12, this.topPos + 10, 0xFFFFFF, true);
        gui.fill(this.leftPos + 10, this.topPos + 22, this.leftPos + this.imageWidth - 10, this.topPos + 23, 0xFF8A6D3B);

        PlayerAttributeCapability cap = cap();
        if (cap == null) return;

        renderHeader(gui, cap, cap.getAttributePoints(), cap.getDengji());
        renderRows(gui, cap, mouseX, mouseY);
    }

    private void renderHeader(GuiGraphics gui, PlayerAttributeCapability cap, int usable, int level) {
        int x = this.leftPos + 10;
        int y = this.topPos + HEADER_Y;
        int w = this.imageWidth - 20;

        gui.fill(x, y, x + w, y + HEADER_H, 0x40000000);
        gui.renderOutline(x, y, w, HEADER_H, 0xFF4A5568);

        gui.drawString(this.font, "§6可用属性点", x + 6, y + 5, 0xFFFFFF, false);
        gui.drawString(this.font, "§e" + usable, x + 6, y + 17, 0xFFFF55, true);

        int rx = x + w - 6;
        String lv = "§7当前等级 §f" + level + " §8(每级 1 点)";
        gui.drawString(this.font, lv, rx - this.font.width(lv), y + 5, 0xFFFFFF, false);

        String used = "§7已分配 §f" + cap.getAllocatedPointsTotal()
                + " §8/ §f" + (AttributePointSpec.MAX_POINTS * AttributePointSpec.values().length);
        gui.drawString(this.font, used, rx - this.font.width(used), y + 18, 0xFFFFFF, false);
    }

    private void renderRows(GuiGraphics gui, PlayerAttributeCapability cap,
                            int mouseX, int mouseY) {
        for (AttributePointSpec spec : AttributePointSpec.values()) {
            int idx = spec.ordinal();
            int x = colX(colOf(idx));
            int y = listTop() + rowOf(idx) * ROW_H;
            int points = cap.getAllocatedPoints(spec);

            if (mouseX >= x && mouseX < x + COL_W && mouseY >= y && mouseY < y + ROW_H - 2) {
                gui.fill(x, y - 1, x + COL_W, y + ROW_H - 3, 0x28FFFFFF);
            }

            gui.fill(x, y + 2, x + 3, y + 11, spec.getColor());

            gui.drawString(this.font, spec.getColorCode() + spec.getDisplayName(),
                    x + NAME_DX, y + 2, 0xFFFFFF, true);

            String pStr = "§e" + points + "§8/" + AttributePointSpec.MAX_POINTS;
            gui.drawString(this.font, pStr,
                    x + POINTS_RIGHT - this.font.width(pStr), y + 2, 0xFFFFFF, false);

            int barX = x + NAME_DX;
            int barW = COL_W - NAME_DX - 8;
            int barY = y + 14;
            gui.fill(barX, barY, barX + barW, barY + BAR_H, 0x30FFFFFF);
            float ratio = points / (float) AttributePointSpec.MAX_POINTS;
            if (ratio > 0f) {
                gui.fill(barX, barY, barX + Math.max(1, (int) (barW * ratio)), barY + BAR_H, spec.getColor());
            }
        }
    }
}