package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
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
        //图片位置
        this.imageHeight = 178;
        this.imageWidth = 322;

        //init方法，没有的话无法渲染
        super.init();

        //将物品栏三个字移除
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
        int textColor = -65436;
        final int SPACING = 12;
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {
            int y = startY;
            guiGraphics.drawString(this.font, "生命值: " + NumberFormatter.formatHealth(player.getHealth(), player.getMaxHealth()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "精神力: " + NumberFormatter.formatRange(attributes.getJingshenli(), attributes.getMaxjingshenli()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "经验: " + NumberFormatter.formatRange(attributes.getJingyan(), attributes.getMaxjingyan()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "攻击力: " + NumberFormatter.formatNumber(attributes.getGongji()), startX, y, textColor, false);
            y += SPACING;
            float finalDefense = ModAttributeAPI.getFangyu(player);
            guiGraphics.drawString(this.font, "防御力: " + NumberFormatter.formatNumber(finalDefense), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "暴击率: " + NumberFormatter.formatPercentage(attributes.getBaojilv()) + "%", startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "暴击伤害: " + NumberFormatter.formatPercentage(attributes.getBaojishanghai()) + "%", startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "吸血: " + NumberFormatter.formatNumber(attributes.getXixue()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "闪避: " + NumberFormatter.formatNumber(attributes.getShanbi()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "命中: " + NumberFormatter.formatNumber(attributes.getMingzhong()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "物穿: " + NumberFormatter.formatNumber(attributes.getWuchuan()), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "抗暴: " + NumberFormatter.formatNumber(attributes.getKangbao()), startX, y, textColor, false);
            y += SPACING;
            float shengmingHuifu = ModAttributeAPI.getShengminghuifu(player);
            guiGraphics.drawString(this.font, "生命恢复: " + NumberFormatter.formatNumber(shengmingHuifu), startX, y, textColor, false);
            y += SPACING;
            guiGraphics.drawString(this.font, "等级: " + (int)attributes.getDengji(), startX, y, textColor, false);

            int xiulianTime = attributes.getXiulianTime();
            String timeText = "可修炼时间: " + (xiulianTime / 60) + "分" + (xiulianTime % 60) + "秒";
            int timeTextWidth = this.font.width(timeText);
            int rightX = this.leftPos + this.imageWidth - timeTextWidth - 10;
            int topY = this.topPos + 8;
            guiGraphics.drawString(this.font, timeText, rightX, topY, 0xFFFF00, true);
        });
    }

    //可用ESC或E关闭窗口
    @Override
    public boolean keyPressed(int key, int i, int j) {
        return super.keyPressed(key, i, j);
    }
}