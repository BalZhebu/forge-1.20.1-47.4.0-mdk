package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse;

//属性面板渲染

public class AttributeScreen extends AbstractContainerScreen<AttributeMenu> {
    //资源路径，如果提示警告则在ResourceLocation的后面加入fromNamespaceAndPath
    private static final int ATTRIBUTE_TEXT_OFFSET_X = 14;
    private static final int ATTRIBUTE_TEXT_OFFSET_Y = 14;
    private static final int LINE_SPACING = 15;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/attributemenu.png");

    private float xMouse;
    private float yMouse;

    List<net.minecraft.network.chat.MutableComponent> mutableComponents = new ArrayList<>();

    public AttributeScreen(AttributeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        this.imageWidth = 322;
        this.imageHeight = 178;
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("属性"), b -> {
        }).bounds(this.leftPos + 5, this.topPos - 20, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("魂骨"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(1));
        }).bounds(this.leftPos + 47, this.topPos - 20, 40, 20).build());

        // 魂环按钮 (新增)
        this.addRenderableWidget(Button.builder(Component.literal("魂环"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2));
        }).bounds(this.leftPos + 89, this.topPos - 20, 40, 20).build());

        if (GodClientData.godName != null &&
                !GodClientData.godName.isEmpty() &&
                !GodClientData.godName.equals("无")) {
            this.addRenderableWidget(Button.builder(Component.literal("神考"), b -> {
                NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(3));
            }).bounds(this.leftPos + 131, this.topPos - 20, 40, 20).build());
        }

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pGuiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        renderPlayerAttibute(pGuiGraphics, minecraft.player);
        int entityX = this.leftPos + 180;
        int entityY = this.topPos + 80;
        renderEntityInInventoryFollowsMouse(pGuiGraphics, entityX, entityY, 30, (float) (entityX) - this.xMouse, (float) (entityY) - this.yMouse, this.minecraft.player);

        Component placeholderText = Component.translatable("gui.kunluncontinent.shift_hint");
        int textWidth = this.font.width(placeholderText);
        int textX = entityX - (textWidth / 2) -20;
        int textY = entityY + 6;
        pGuiGraphics.drawString(this.font, placeholderText, textX, textY, -1, true);
    }

    private void renderPlayerAttibute(GuiGraphics guiGraphics, Player player) {
        mutableComponents.clear();
        int startX = this.leftPos + ATTRIBUTE_TEXT_OFFSET_X - 6;
        int startY = this.topPos + ATTRIBUTE_TEXT_OFFSET_Y - 5;
        int textColor = -65436; // 也就是你的自定义颜色
        final int SPACING = 12;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {
            int y = startY;
            guiGraphics.drawString(this.font, "生命值: " + NumberFormatter.formatHealth(player.getHealth(), player.getMaxHealth()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "精神力: " + NumberFormatter.formatRange(attributes.getJingshenli(), attributes.getMaxjingshenli()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "经验: " + NumberFormatter.formatRange(attributes.getJingyan(), attributes.getMaxjingyan()), startX, y, textColor, false);
            y += SPACING;
            float totalGongji = ModAttributeAPI.getGongji(player);
            guiGraphics.drawString(this.font, "攻击力: " + NumberFormatter.formatNumber(totalGongji), startX, y, textColor, false);
            y += SPACING;
            float finalDefense = ModAttributeAPI.getFangyu(player);
            guiGraphics.drawString(this.font, "防御力: " + NumberFormatter.formatNumber(finalDefense), startX, y, textColor, false);
            y += SPACING;
            float totalBaojilv = ModAttributeAPI.getBaojilv(player);
            guiGraphics.drawString(this.font, "暴击率: " + NumberFormatter.formatPercentage(totalBaojilv) + "%", startX, y, textColor, false);
            y += SPACING;
            float totalBaojiShanghai = ModAttributeAPI.getBaojishanghai(player);
            guiGraphics.drawString(this.font, "暴击伤害: " + NumberFormatter.formatPercentage(totalBaojiShanghai) + "%", startX, y, textColor, false);
            y += SPACING;
            float totalXixue = ModAttributeAPI.getXixue(player);
            guiGraphics.drawString(this.font, "吸血: " + NumberFormatter.formatNumber(totalXixue), startX, y, textColor, false);
            y += SPACING;
            float totalShanbi = ModAttributeAPI.getShanbi(player);
            guiGraphics.drawString(this.font, "闪避: " + NumberFormatter.formatNumber(totalShanbi), startX, y, textColor, false);
            y += SPACING;
            float totalMingzhong = ModAttributeAPI.getMingzhong(player);
            guiGraphics.drawString(this.font, "命中: " + NumberFormatter.formatNumber(totalMingzhong), startX, y, textColor, false);
            y += SPACING;
            float totalWuchuan = ModAttributeAPI.getWuchuan(player);
            guiGraphics.drawString(this.font, "物穿: " + NumberFormatter.formatNumber(totalWuchuan), startX, y, textColor, false);
            y += SPACING;
            float totalKangbao = ModAttributeAPI.getKangbao(player);
            guiGraphics.drawString(this.font, "抗暴: " + NumberFormatter.formatNumber(totalKangbao), startX, y, textColor, false);
            y += SPACING;
            float shengmingHuifu = ModAttributeAPI.getShengminghuifu(player);
            guiGraphics.drawString(this.font, "生命恢复: " + NumberFormatter.formatNumber(shengmingHuifu), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "等级: " + attributes.getDengji(), startX, y, textColor, false);
            int xiulianTime = attributes.getXiulianTime();

            String timeText = "可修炼时间: " + (xiulianTime / 60) + "分" + (xiulianTime % 60) + "秒";
            int timeTextWidth = this.font.width(timeText);
            int rightX = this.leftPos + this.imageWidth - timeTextWidth - 10;
            int topY = this.topPos + 8;
            guiGraphics.drawString(this.font, timeText, rightX, topY, 0xFFFF00, true);

            int rightXOffset = 10;
            int topYOffset = 8;

            float baseRate = attributes.getTupochenggonglv();
            int level = attributes.getDengji();
            float levelPenalty = level * 0.5f;
            float finalDisplayRate = Math.max(5.0f, baseRate - levelPenalty);
            String rateText = "当前等级突破概率: " + NumberFormatter.formatPercentage(finalDisplayRate) + "%";
            int rateTextWidth = this.font.width(rateText);
            int rateX = this.leftPos + this.imageWidth - rateTextWidth - rightXOffset;
            int rateColor = finalDisplayRate <= 5.0f ? 0xFF0000 : 0x00FF00;
            guiGraphics.drawString(this.font, rateText, rateX, this.topPos + topYOffset + SPACING, rateColor, true);
        });
    }

    //可用ESC或E关闭窗口
    @Override
    public boolean keyPressed(int key, int i, int j) {
        return super.keyPressed(key, i, j);
    }
}