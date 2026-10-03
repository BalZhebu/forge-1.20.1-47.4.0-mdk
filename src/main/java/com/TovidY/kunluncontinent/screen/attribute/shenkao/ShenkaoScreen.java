package com.TovidY.kunluncontinent.screen.attribute.shenkao;

import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SCheckTaskPacket;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Collections;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;

public class ShenkaoScreen extends AbstractContainerScreen<ShenkaoMenu> implements AttributeTabs.Host {

    public ShenkaoScreen(ShenkaoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 330;
        this.imageHeight = 190;

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        this.imageWidth = 330;
        this.imageHeight = 190;
        super.init();

        // 顶部页签：全部由注册表统一生成（新增面板只需在 AttributeTabs 注册一行）
        AttributeTabs.buildTabs(this, AttributeTabs.PAGE_SHENKAO);

        // 2. 检查进度按钮（等比右对齐居中调整）
        this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                this.font, this.leftPos + this.imageWidth - 85, this.topPos + 12, 75, 18,
                Component.literal("进度检查"),
                b -> NetworkHandler.INSTANCE.sendToServer(new C2SCheckTaskPacket()),
                () -> Collections.singletonList(Component.literal("点击检查并提交神考进度"))
        ));

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // 文本区域与面板边界比例对齐
        int textLeft = this.leftPos + 15;

        String titleStr = "当前神位: §c" + (GodClientData.godName.isEmpty() ? "无" : GodClientData.godName);
        String stageStr = "考核阶段: 第 " + GodClientData.currentStage + " 考";
        guiGraphics.drawString(this.font, titleStr, textLeft, this.topPos + 14, 0xFFFFFF, true);
        guiGraphics.drawString(this.font, stageStr, textLeft, this.topPos + 28, 0x55FF55, true);

        // 分割线
        guiGraphics.fill(this.leftPos + 10, this.topPos + 40, this.leftPos + this.imageWidth - 10, this.topPos + 41, 0xFF8A6D3B);

        String rawContent = GodClientData.currentDesc.replace("\n", "");
        String desc = "§e考核目标: §f" + rawContent;
        guiGraphics.drawString(this.font, desc, textLeft, this.topPos + 48, 0xFFFFFF, true);

        String progress = "当前进度: §e" + GodClientData.taskProgress + " §f/ §a" + GodClientData.requiredCount;
        guiGraphics.drawString(this.font, progress, textLeft, this.topPos + 62, 0xAAAAAA, true);

        if (!GodClientData.isGod) {
            guiGraphics.drawString(this.font, "§6考核奖励:", textLeft, this.topPos + 80, 0xFFFFFF, true);
            String rText = GodClientData.rewardDesc;
            if (rText != null && !rText.isEmpty()) {
                String[] rewardLines = rText.split("\n");
                for (int i = 0; i < rewardLines.length; i++) {
                    guiGraphics.drawString(this.font, rewardLines[i], textLeft, this.topPos + 94 + (i * 11), 0xDDDDDD, true);
                }
            }
        } else {
            guiGraphics.drawString(this.font, "§6★ 已成就神位 ★", textLeft + 20, this.topPos + 90, 0xFFD700, true);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics gui, float pPartialTick, int pMouseX, int pMouseY) {
        // 1. 统一背景
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        // 2. 槽位衬底
        for (Slot slot : this.menu.slots) {
            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;
            boolean isHovered = pMouseX >= slotX && pMouseX < slotX + 18 && pMouseY >= slotY && pMouseY < slotY + 18;
            KunlunGuiHelper.renderSlotBackground(gui, slotX - 1, slotY - 1, isHovered);
        }
    }


    // ==================== AttributeTabs.Host 实现（暴露 protected 成员给工具类） ====================

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
}
