package com.TovidY.kunluncontinent.screen.attribute.point;

import com.TovidY.kunluncontinent.capability.playerattributes.AttributePointSpec;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.CPacketAllocatePoint;
import com.TovidY.kunluncontinent.network.client.CPacketResetPoints;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabBar;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * 属性加点面板（<b>330×190，与属性 / 魂环 / 神考 / 银行页签面板同尺寸</b>）。
 *
 * <p>13 条属性全部由 {@link AttributePointSpec} 驱动 —— 名字、原属性名、每点收益、上限、
 * 颜色都在那一个枚举里，这里只负责画和发包。加属性 = 加一行枚举，不用改本文件。</p>
 *
 * <p>规则：<b>只能加不能减</b>，想减点必须用右下角"重置"（全额退还）。</p>
 *
 * <h3>版式（相对 leftPos / topPos）</h3>
 * <pre>
 *   属性点面板
 *   ────────────────────────────────────────────────
 *   ┌ 可用点数 12            当前等级 99 (每级 +1) ┐
 *   │ 已分配 24 / 1287        点「+」加点，重置可退还 │
 *   └────────────────────────────────────────────┘
 *   强健 12/99 [+]        破甲  0/99 [+]
 *   ▬▬▬▬▬▬▬▬▬             ▬▬▬▬▬▬▬▬▬
 *   …（左列 7 条 / 右列 6 条）
 *                                    [重置属性点]
 * </pre>
 *
 * <p>压缩方式：顶部信息块从 34 高的三栏块改成 22 高的两行条（左信息 / 右信息），
 * 行高 18 → 17，列宽 182 → 150、列间距 8 → 10，重置按钮挪到面板内底部右侧。
 * 13 条属性仍然 7 + 6 分两列，不需要滚动。</p>
 */
public class PointScreen extends AbstractContainerScreen<PointMenu> implements AttributeTabBar.Host {

    // ==================== 布局常量 ====================
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 190;

    /** 顶部信息条（两行文字，无按钮）。 */
    private static final int HEADER_Y = 20;
    private static final int HEADER_H = 22;

    /**
     * 每列的属性条数。
     *
     * <p>共 <b>13 条</b>，按 7 + 6 分列。改属性数量时同步改这个值，
     * 通用公式：{@code ROWS_PER_COL = (总数 + 1) / 2}。</p>
     */
    private static final int ROWS_PER_COL = 7;
    /** 列宽：两列 150 + 间距 10 = 310，正好占满 330 减去左右各 10 的边距。 */
    private static final int COL_W = 150;
    private static final int COL_GAP = 10;
    private static final int LIST_X = 10;
    /** 列表起始 y（标题 6 / 分隔线 17 / 信息条 20~42）。 */
    private static final int LIST_Y = 46;
    private static final int ROW_H = 17;

    private static final int BAR_H = 2;
    private static final int NAME_DX = 7;
    /** "x/99" 右边缘（留出 6px 给 + 按钮）。 */
    private static final int POINTS_RIGHT = COL_W - 26;
    private static final int BTN_W = 18;
    private static final int BTN_H = 12;
    private static final int BTN_X = COL_W - BTN_W - 2;

    private static final int RESET_W = 84;
    private static final int RESET_H = 16;

    public PointScreen(PointMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = PANEL_W;
        this.imageHeight = PANEL_H;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    private int listTop() {
        return this.topPos + LIST_Y;
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
        AttributeTabBar.buildTabs(this, AttributeTabs.PAGE_POINT);

        // 每条属性一个「+」
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

        // 重置：面板底部右侧（列表下方留白处）
        addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                this.font,
                this.leftPos + this.imageWidth - RESET_W - 10,
                this.topPos + this.imageHeight - 20,
                RESET_W, RESET_H,
                Component.literal("重置属性点"),
                b -> NetworkHandler.INSTANCE.sendToServer(new CPacketResetPoints()),
                () -> List.of(Component.literal("清空全部属性点，当前使用的点数§a全额退还§f。"),
                        Component.literal("需要消耗 §b1 个重置卷轴§f。")
                )));
    }

    /**
     * 某个属性的 tooltip：别名 · 原属性名 / 每点收益 / 已加点数。
     *
     * <p>「定魂」（抗打断）多一行说明封顶与等级加成 —— 它是唯一有额外规则的属性，
     * 不说清楚玩家会以为 99 点就 100% 免疫。</p>
     */
    private List<Component> tooltipFor(AttributePointSpec spec) {
        int points = cap() == null ? 0 : cap().getAllocatedPoints(spec);
        String mode = spec.getMode() == AttributePointSpec.Mode.FLAT ? "直接加算" : "百分比放大";

        List<Component> lines = new java.util.ArrayList<>(List.of(
                Component.literal(spec.getColorCode() + spec.getDisplayName()
                        + " §8· §f" + spec.getAttrName()),
                Component.literal("§7每点 §e+" + spec.getPerPointPercent() + "% §8(" + mode + ")")
                        .append(Component.literal("§7，满 " + AttributePointSpec.MAX_POINTS + " 点 §e+"
                                + spec.getMaxTotalPercent() + "%")),
                Component.literal("§7已加 §e" + points + " §7/ " + AttributePointSpec.MAX_POINTS + " 点")
        ));

        if (spec == AttributePointSpec.KANGDAPAN) {
            lines.add(Component.literal("§8吟唱开始时按此概率获得护体"));
            lines.add(Component.literal("§8掷中则整段吟唱都不会被打断"));
            lines.add(Component.literal("§7最终 §f点数 + 等级加成§7（等级上限 +15%）"));
            lines.add(Component.literal("§7抗打断上限 §e99%§7，不会完全免疫"));
        }
        return lines;
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

        gui.drawString(this.font, "§6属性点面板", this.leftPos + 12, this.topPos + 6, 0xFFFFFF, true);
        gui.fill(this.leftPos + 10, this.topPos + 17, this.leftPos + this.imageWidth - 10, this.topPos + 18, 0xFF8A6D3B);

        PlayerAttributeCapability cap = cap();
        if (cap == null) return;

        renderHeader(gui, cap, cap.getAttributePoints(), cap.getDengji());
        renderRows(gui, cap, mouseX, mouseY);
    }

    /** 顶部信息条：两行，左「可用点数 / 已分配」，右「当前等级 / 操作提示」。 */
    private void renderHeader(GuiGraphics gui, PlayerAttributeCapability cap, int usable, int level) {
        int x = this.leftPos + 10;
        int y = this.topPos + HEADER_Y;
        int w = this.imageWidth - 20;

        gui.fill(x, y, x + w, y + HEADER_H, 0x40000000);
        gui.renderOutline(x, y, w, HEADER_H, 0xFF4A5568);

        int rx = x + w - 6;

        // 第一行
        gui.drawString(this.font, "§6可用点数 §e" + usable, x + 6, y + 4, 0xFFFFFF, false);
        String lv = "§7当前等级 §f" + level;
        gui.drawString(this.font, lv, rx - this.font.width(lv), y + 4, 0xFFFFFF, false);

        // 第二行
        String used = "§7已分配 §f" + cap.getAllocatedPointsTotal()
                + " §8/ §f" + (AttributePointSpec.MAX_POINTS * AttributePointSpec.values().length);
        gui.drawString(this.font, used, x + 6, y + 13, 0xFFFFFF, false);
        String tip = "§8点「+」加点，重置可全额退还";
        gui.drawString(this.font, tip, rx - this.font.width(tip), y + 13, 0xFFFFFF, false);
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

            gui.fill(x, y + 1, x + 3, y + 10, spec.getColor());

            gui.drawString(this.font, spec.getColorCode() + spec.getDisplayName(),
                    x + NAME_DX, y + 1, 0xFFFFFF, true);

            String pStr = "§e" + points + "§8/" + AttributePointSpec.MAX_POINTS;
            gui.drawString(this.font, pStr,
                    x + POINTS_RIGHT - this.font.width(pStr), y + 1, 0xFFFFFF, false);

            int barX = x + NAME_DX;
            int barW = COL_W - NAME_DX - 8;
            int barY = y + 12;
            gui.fill(barX, barY, barX + barW, barY + BAR_H, 0x30FFFFFF);
            float ratio = points / (float) AttributePointSpec.MAX_POINTS;
            if (ratio > 0f) {
                gui.fill(barX, barY, barX + Math.max(1, (int) (barW * ratio)), barY + BAR_H, spec.getColor());
            }
        }
    }
}
