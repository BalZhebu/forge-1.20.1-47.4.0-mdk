package com.TovidY.kunluncontinent.screen.playernpc.shoumai;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SNpcSellPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * NPC 售卖界面。
 * 布局：左侧出售区（0~13），右侧合计面板+输出槽，底部按钮。
 */

@OnlyIn(Dist.CLIENT)
public class SellScreen extends AbstractContainerScreen<SellMenu> {

    // 【修改点 1】：压缩整体面板高度从 166 降到 146
    private static final int PANEL_W = 220;
    private static final int PANEL_H = 146;

    // 左侧合计面板参数（高度微调）
    private static final int SUMMARY_X = -85;
    private static final int SUMMARY_Y = 12;
    private static final int SUMMARY_W = 80;
    private static final int SUMMARY_H = 80;

    // 按钮位置与尺寸
    private static final int BTN_X = 148;
    private static final int BTN_Y = 20;
    private static final int BTN_W = 56;
    private static final int BTN_H = 24;

    private Button button_sell;

    public SellScreen(SellMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth  = PANEL_W;
        this.imageHeight = PANEL_H;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY     = 10000;
        button_sell = Button.builder(Component.empty(), b -> {
                    NetworkHandler.sendToServer(new C2SNpcSellPacket(this.menu.isSellConfirmed()));
                }).bounds(this.leftPos + BTN_X, this.topPos + BTN_Y, BTN_W, BTN_H)
                .build(builder -> new Button(builder) {
                    @Override
                    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
                    }
                });

        this.addRenderableWidget(button_sell);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int px = this.leftPos;
        int py = this.topPos;

        // 1. 绘制主面板背景
        g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xEE0B131D);
        drawBoxBorder(g, px, py, PANEL_W, PANEL_H, 0xFF1E3545, 0xFF3D6B8C);

        // 2. 标题分割线
        g.fill(px + 10, py + 10, px + PANEL_W - 10, py + 11, 0xFF2A4A60);

        // 3. 槽位间分割线（在输入槽和背包槽之间绘制一条暗纹分隔线）
        g.fill(px + 12, py + 54, px + PANEL_W - 12, py + 55, 0xFF16232F);

        // 4. 绘制全部 Slot 背景
        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            drawSlotBg(g, px + slot.x, py + slot.y);
        }

        // 5. 绘制左侧合计面板与手绘按钮
        drawSummaryPanel(g, mouseX, mouseY);
        drawCustomButton(g, mouseX, mouseY);

        updateButton();
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, Component.literal("物品出售"), 14, 2, 0xFF80C0E0, false);
    }

    private void drawSlotBg(GuiGraphics g, int x, int y) {
        int sx = x - 1;
        int sy = y - 1;
        g.fill(sx, sy, sx + 18, sy + 18, 0xFF060B10);
        g.fill(sx, sy, sx + 18, sy + 1, 0xFF16232F);
        g.fill(sx, sy, sx + 1, sy + 18, 0xFF16232F);
        g.fill(sx + 17, sy, sx + 18, sy + 18, 0xFF2C4357);
        g.fill(sx, sy + 17, sx + 18, sy + 18, 0xFF2C4357);
    }

    private void drawCustomButton(GuiGraphics g, int mouseX, int mouseY) {
        if (button_sell == null) return;

        int bx = button_sell.getX();
        int by = button_sell.getY();
        boolean hovered = mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
        boolean confirmed = this.menu.isSellConfirmed();
        boolean active = button_sell.active;

        int bgColor, borderColor, textColor, shadowColor;

        if (!active) {
            bgColor     = 0xFF121A21;
            borderColor = 0xFF22303C;
            textColor   = 0xFF4A5A68;
            shadowColor = 0xFF0A1015;
        } else if (confirmed) {
            bgColor     = hovered ? 0xFF9E2B00 : 0xFF6B1B00;
            borderColor = hovered ? 0xFFFF6A00 : 0xFFC24100;
            textColor   = 0xFFFFD700;
            shadowColor = 0xFF3D0F00;
        } else {
            bgColor     = hovered ? 0xFF1D5A82 : 0xFF103A56;
            borderColor = hovered ? 0xFF4EB8F0 : 0xFF2B78A4;
            textColor   = 0xFFE0F4FF;
            shadowColor = 0xFF081F30;
        }
        g.fill(bx + 1, by + 1, bx + BTN_W + 1, by + BTN_H + 1, shadowColor);
        g.fill(bx, by, bx + BTN_W, by + BTN_H, bgColor);
        drawBoxBorder(g, bx, by, BTN_W, BTN_H, borderColor, borderColor);
        if (active && hovered) {
            g.fill(bx + 1, by + 1, bx + BTN_W - 1, by + 2, 0x40FFFFFF);
            g.fill(bx + 1, by + 1, bx + 2, by + BTN_H - 1, 0x40FFFFFF);
        }
        String text = confirmed ? "确认出售" : "出售";
        int tw = this.font.width(text);
        int tx = bx + (BTN_W - tw) / 2;
        int ty = by + (BTN_H - 8) / 2;
        if (active) {
            g.drawString(this.font, text, tx + 1, ty + 1, 0xFF000000, false);
        }
        g.drawString(this.font, text, tx, ty, textColor, false);
    }

    private void drawSummaryPanel(GuiGraphics g, int mouseX, int mouseY) {
        int px = this.leftPos;
        int py = this.topPos;
        int sx = px + SUMMARY_X;
        int sy = py + SUMMARY_Y;
        boolean hovered = mouseX >= sx && mouseX < sx + SUMMARY_W
                && mouseY >= sy && mouseY < sy + SUMMARY_H;

        int bgColor = hovered ? 0xEE142433 : 0xEE0B141F;
        g.fill(sx, sy, sx + SUMMARY_W, sy + SUMMARY_H, bgColor);

        int borderColor = hovered ? 0xFF4A88B5 : 0xFF1E374D;
        drawBoxBorder(g, sx, sy, SUMMARY_W, SUMMARY_H, borderColor, borderColor);

        String title = "合  计";
        int tw = this.font.width(title);
        g.drawString(this.font, Component.literal(title),
                sx + (SUMMARY_W - tw) / 2, sy + 6,
                0xFF80C0E0, false);

        int sepY = sy + 18;
        g.fill(sx + 4, sepY, sx + SUMMARY_W - 4, sepY + 1, 0xFF1E374D);

        SellMenu menu = this.menu;
        String[] lines = {
                "铜魂币: " + menu.getOutputCopper(),
                "银魂币: " + menu.getOutputSilver(),
                "金魂币: " + menu.getOutputGold()
        };

        int ly = sepY + 10;
        for (String line : lines) {
            int lw = this.font.width(line);
            g.drawString(this.font, Component.literal(line),
                    sx + (SUMMARY_W - lw) / 2, ly,
                    0xFFA0C0D0, false);
            ly += 15;
        }
    }

    private void updateButton() {
        if (button_sell == null) return;
        button_sell.active = hasItemsInSlots(0, 14);
    }

    private boolean hasItemsInSlots(int from, int to) {
        for (int i = from; i < to && i < this.menu.slots.size(); i++) {
            if (!this.menu.slots.get(i).getItem().isEmpty()) return true;
        }
        return false;
    }

    private void drawBoxBorder(GuiGraphics g, int x, int y, int w, int h, int colorInner, int colorOuter) {
        g.fill(x, y, x + w, y + 1, colorOuter);
        g.fill(x, y + h - 1, x + w, y + h, colorOuter);
        g.fill(x, y, x + 1, y + h, colorOuter);
        g.fill(x + w - 1, y, x + w, y + h, colorOuter);
    }
}