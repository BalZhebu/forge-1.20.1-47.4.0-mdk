package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KluxTabButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class HunguScreen extends AbstractContainerScreen<HunguMenu> {
    private static final ResourceLocation TEXTURE =new ResourceLocation(KlMain.MOD_ID,"textures/screens/hungu.png");

    public HunguScreen(HunguMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 322;
        this.imageHeight = 178;
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
        this.addRenderableWidget(new KluxTabButton(currentX, startY - 5, selectedSize, selectedSize,
                new ItemStack(ModItems.SOUL_BONE_BUTTON.get()), Component.literal("魂骨面板"), true, b -> {
        }));
        currentX += (selectedSize + spacing);
        this.addRenderableWidget(new KluxTabButton(currentX, startY - 2, normalSize, normalSize,
                new ItemStack(ModItems.HUNHUAN_BUTTON.get()), Component.literal("魂环配置"), false, b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2));
        }));
        currentX += (normalSize + spacing);
        if (GodClientData.godName != null && !GodClientData.godName.equals("无")) {
            this.addRenderableWidget(new KluxTabButton(currentX, startY - 2, normalSize, normalSize,
                    new ItemStack(ModItems.SHENKAO_BUTTON.get()), Component.literal("神考面板"), false, b -> {
                NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(3));
            }));
        }

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
