package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Map;

public class HunguScreen extends AbstractContainerScreen<HunguMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/screens/hungu.png");

    public HunguScreen(HunguMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 322;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.literal("属性"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0));
        }).bounds(this.leftPos + 5, this.topPos - 20, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("魂骨"), b -> {
        }).bounds(this.leftPos + 47, this.topPos - 20, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("魂环"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2));
        }).bounds(this.leftPos + 89, this.topPos - 20, 40, 20).build());

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        String hintLine1 = "§a放入魂骨后需切换或刷新界面";
        String hintLine2 = "§a左侧才会出现具体数值";
        int line1Width = this.font.width(hintLine1);
        int line2Width = this.font.width(hintLine2);

        // 计算 X：面板起始位置 + 面板宽度 - 文字宽度 - 5像素边距
        int hintX1 = this.leftPos + this.imageWidth - line1Width - 5;
        int hintX2 = this.leftPos + this.imageWidth - line2Width - 5;

        // 计算 Y：按钮上方位置
        int hintY = this.topPos + 5;

        int modelX = this.leftPos + 160;
        int modelY = this.topPos + 85;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, modelX, modelY, 30, (float) modelX - mouseX, (float) modelY - mouseY, this.minecraft.player);

        guiGraphics.drawString(this.font, hintLine1, hintX1, hintY, 0xFFFFFF, true);
        guiGraphics.drawString(this.font, hintLine2, hintX2, hintY + 10, 0xFFFFFF, true);

        // --------------------------

        int textX = this.leftPos + 7;
        int textY = this.topPos + 5;
        guiGraphics.drawString(this.font, "§6[魂骨属性总和]", textX, textY, 0xFFFFFF, true);
        this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            Map<String, Float> stats = cap.getBoneOnlyStats();
            final int[] currentLine = {1};
            final int LINE_HEIGHT = 12;
                renderStat(guiGraphics, "生命加成: +", stats.getOrDefault("maxshengming", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "攻击加成: +", stats.getOrDefault("gongji", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "防御加成: +", stats.getOrDefault("fangyu", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "暴击率加成: +", stats.getOrDefault("baojilv", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "暴击伤害加成: +", stats.getOrDefault("baojishanghai", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "吸血加成: +", stats.getOrDefault("xixue", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "闪避加成: +", stats.getOrDefault("shanbi", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "命中加成: +", stats.getOrDefault("mingzhong", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "抗暴加成: +", stats.getOrDefault("kangbao", 0f), textX, textY, currentLine);
                renderStat(guiGraphics, "生命恢复: +", stats.getOrDefault("shengminghuifu", 0f), textX, textY, currentLine);
            });
    }

    private void renderStat(GuiGraphics guiGraphics, String label, float value, int x, int baseY, int[] lineCounter) {
        if (value > 0.001f) {
            int y = baseY + (lineCounter[0] * 12);
            String text = label + String.format("%.1f", value);
            guiGraphics.drawString(this.font, text, x, y, 0x00FF00, false);
            lineCounter[0]++;
        }
    }
}
