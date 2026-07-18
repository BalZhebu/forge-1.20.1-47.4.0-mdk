package com.TovidY.kunluncontinent.screen.spiritgatheringaltar;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;


public class SpiritGatheringaltarScreen extends AbstractContainerScreen<SpiritGatheringaltarMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(KlMain.MOD_ID, "textures/screens/julingtai_gui.png");

    public SpiritGatheringaltarScreen(SpiritGatheringaltarMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 180;
        this.imageHeight = 162;

        // 隐藏原版多余文字
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // ✨【新加：槽位未解锁的红叉遮罩渲染】
        int score = this.menu.getStoneScore();
        int[] slotX = {27, 82, 135}; // 对应 Menu 里注册的 3 个槽位的 X 坐标
        int[] slotY = {15, 15, 15};   // 对应 Menu 里注册的 3 个槽位的 Y 坐标
        int[] limits = {5, 140, 300}; // 解锁要求阈值

        for (int i = 0; i < 3; i++) {
            if (score <= limits[i]) {
                // 1. 在对应的 16x16 槽位上覆盖一层半透明的暗红色（0x55 表现为 33% 左右不刺眼的透明度）
                guiGraphics.fill(this.leftPos + slotX[i], this.topPos + slotY[i],
                        this.leftPos + slotX[i] + 16, this.topPos + slotY[i] + 16, 0x55FF0000);

                // 2. 在格子正中间画一个小巧的白红色“×”，极度省地方而且一眼就能看出被锁了
                guiGraphics.drawString(this.font, "×", this.leftPos + slotX[i] + 6, this.topPos + slotY[i] + 4, 0xAAFF3333, false);
            }
        }

        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int score = this.menu.getStoneScore();
        int ticks = this.menu.getIntervalTicks();
        int amount = this.menu.getRecoverAmount();

        double seconds = ticks / 20.0;

        int labelColor = 0x404040;      // 标签灰
        int startX = 28;
        int startY = 42;
        int spacingY = 11;

        // 绘制聚灵强度
        guiGraphics.drawString(this.font, "大阵聚灵强度:", startX, startY, labelColor, false);
        guiGraphics.drawString(this.font, score > 0 ? String.valueOf(score) : "未起阵", startX + 76, startY, score > 0 ? 0xAA00AA : 0xBB0000, false);
        // 绘制运转周期
        guiGraphics.drawString(this.font, "运转充能周期:", startX, startY + spacingY, labelColor, false);
        guiGraphics.drawString(this.font, score > 0 ? String.format("%.1f 秒", seconds) : "——", startX + 76, startY + spacingY, score > 0 ? 0x00AA00 : 0xBB0000, false);
        // 绘制单次吸纳
        guiGraphics.drawString(this.font, "单次灵气吸纳:", startX, startY + (spacingY * 2), labelColor, false);
        guiGraphics.drawString(this.font, score > 0 ? ("+" + amount + " 能量") : "阵法未激活", startX + 76, startY + (spacingY * 2), score > 0 ? 0x00AAAA : 0xBB0000, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 先调用原版的物品 Tooltip 渲染（这样已解锁放了瓶子时能看瓶子属性）
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int score = this.menu.getStoneScore();
        int[] slotX = {27, 82, 135};
        int[] slotY = {15, 15, 15};
        int[] limits = {5, 140, 300};

        // ✨【新加：悬停在锁定的槽位上时弹出 Tooltip 详情窗】
        for (int i = 0; i < 3; i++) {
            // 判定鼠标指针是否在当前槽位的 16x16 坐标区域内
            if (mouseX >= this.leftPos + slotX[i] && mouseX < this.leftPos + slotX[i] + 16 &&
                    mouseY >= this.topPos + slotY[i] && mouseY < this.topPos + slotY[i] + 16) {

                // 如果当前大阵的强度不足以解开这个槽位，就给它加一段警告文本
                if (score <= limits[i]) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.literal("🔒 聚灵阵插槽已锁定").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    tooltip.add(Component.literal("当前聚灵强度: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(score)).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    tooltip.add(Component.literal("解锁需要强度: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("> " + limits[i]).withStyle(ChatFormatting.GOLD)));

                    // 用原版的浮窗方法渲染文本组件，能自动根据屏幕边缘调整位置，不占任何死空间
                    guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                }
            }
        }
    }
}