package com.TovidY.kunluncontinent.screen.attribute.shenkao;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SCheckTaskPacket;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KluxTabButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ShenkaoScreen extends AbstractContainerScreen<ShenkaoMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(KlMain.MOD_ID, "textures/screens/shenkaogui.png");

    public ShenkaoScreen(ShenkaoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 322;
        this.imageHeight = 178;

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        this.imageWidth = 322;
        this.imageHeight = 178;
        super.init();

        int normalSize = 28;
        int selectedSize = 33;
        int spacing = 6;
        int startX = this.leftPos + 8;
        int startY = this.topPos - 28;

        this.addRenderableWidget(new KluxTabButton(startX, startY - 2, normalSize, normalSize,
                new ItemStack(ModItems.ATTRIBUTE_BUTTON.get()), Component.literal("属性面板"), false, b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0));
        }));

        int currentX = startX + normalSize + spacing;

        this.addRenderableWidget(new KluxTabButton(currentX, startY - 2, normalSize, normalSize,
                new ItemStack(ModItems.SOUL_BONE_BUTTON.get()), Component.literal("魂骨面板"), false, b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(1));
        }));

        currentX += (normalSize + spacing);

        this.addRenderableWidget(new KluxTabButton(currentX, startY - 2, normalSize, normalSize,
                new ItemStack(ModItems.HUNHUAN_BUTTON.get()), Component.literal("魂环配置"), false, b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2));
        }));

        currentX += (normalSize + spacing);

        this.addRenderableWidget(new KluxTabButton(currentX, startY - 5, selectedSize, selectedSize,
                new ItemStack(ModItems.SHENKAO_BUTTON.get()), Component.literal("神考面板"), true, b -> {
        }));

        this.addRenderableWidget(Button.builder(Component.literal("进度检查"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new C2SCheckTaskPacket());
        }).bounds(this.leftPos + 210, this.topPos + 10, 76, 20).build());

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        String titleStr = "当前神位: §c" + (GodClientData.godName.isEmpty() ? "无" : GodClientData.godName);
        String stageStr = "考核阶段: 第 " + GodClientData.currentStage + " 考";
        guiGraphics.drawString(this.font, titleStr, this.leftPos + 10, this.topPos + 10, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, stageStr, this.leftPos + 10, this.topPos + 25, 0x00FF00, false);

        String rawContent = GodClientData.currentDesc.replace("\n", "");
        String desc = "§1考核目标: §6" + rawContent;
        guiGraphics.drawString(this.font, desc, this.leftPos + 10, this.topPos + 40, 0xFFFFFF, false);

        String progress = "当前进度: " + GodClientData.taskProgress + " / " + GodClientData.requiredCount;
        guiGraphics.drawString(this.font, progress, this.leftPos + 10, this.topPos + 55, 0xAAAAAA, false);
        if (!GodClientData.isGod) {
            guiGraphics.drawString(this.font, "§6考核奖励:", this.leftPos + 10, this.topPos + 78, 0xFFFFFF, false);
            String rText = GodClientData.rewardDesc;
            if (rText != null && !rText.isEmpty()) {
                String[] rewardLines = rText.split("\n");
                for (int i = 0; i < rewardLines.length; i++) {
                    guiGraphics.drawString(this.font, rewardLines[i], this.leftPos + 10, this.topPos + 92 + (i * 10), 0xFFFFFF, false);
                }
            }
        }
        if (GodClientData.isGod) {
            guiGraphics.drawString(this.font, "§6★ 已成就神位 ★", this.leftPos + 40, this.topPos + 85, 0xFFD700, true);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}