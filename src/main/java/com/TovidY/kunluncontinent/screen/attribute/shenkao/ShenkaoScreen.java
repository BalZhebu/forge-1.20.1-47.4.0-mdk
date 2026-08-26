package com.TovidY.kunluncontinent.screen.attribute.shenkao;

import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SCheckTaskPacket;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

public class ShenkaoScreen extends AbstractContainerScreen<ShenkaoMenu> {

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

        int normalSize = 24;
        int selectedSize = 28;
        int spacing = 5;
        int startX = this.leftPos + 10;
        int startY = this.topPos - 26;

        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, startX, startY, normalSize, normalSize,
                new ItemStack(ModItems.ATTRIBUTE_BUTTON.get()), Component.literal("属性面板"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0))
        ));

        int currentX = startX + normalSize + spacing;
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY, normalSize, normalSize,
                new ItemStack(ModItems.SOUL_BONE_BUTTON.get()), Component.literal("魂骨面板"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(1))
        ));

        // 魂环配置页签
        currentX += (normalSize + spacing);
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY, normalSize, normalSize,
                new ItemStack(ModItems.HUNHUAN_BUTTON.get()), Component.literal("魂环配置"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2))
        ));

        // 神考面板页签（当前选中：尺寸升为 selectedSize，startY 向上偏移 2px）
        currentX += (normalSize + spacing);
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY - 2, selectedSize, selectedSize,
                new ItemStack(ModItems.SHENKAO_BUTTON.get()), Component.literal("神考面板"), true,
                b -> {}
        ));

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
}