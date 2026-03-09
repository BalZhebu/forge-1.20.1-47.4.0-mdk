package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.Init.KeyMappingInit;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SkillWheelScreen extends Screen {
    private final PlayerAttributeCapability cap;
    private int hoveredSlot = -1;

    public SkillWheelScreen(PlayerAttributeCapability cap) {
        super(Component.literal("Skill Wheel"));
        this.cap = cap;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null) {
            this.minecraft.mouseHandler.releaseMouse();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        if (this.minecraft != null && this.minecraft.mouseHandler.isMouseGrabbed()) {
            this.minecraft.mouseHandler.releaseMouse();
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (cap == null) return;
        String currentWuhun = cap.getWuhunName();
        if (currentWuhun == null) return;

        BaseSkillItem[] skills = cap.getWuhunSkillsMap().get(currentWuhun);
        this.hoveredSlot = -1;

        // 绘制魂技轮盘
        for (int i = 0; i < 9; i++) {
            float startAngle = i * 40.0f + 2.0f;
            float endAngle = (i + 1) * 40.0f - 2.0f;

            boolean isHovered = isMouseInSector(mouseX, mouseY, centerX, centerY, i);
            if (isHovered) this.hoveredSlot = i;

            int color = isHovered ? 0xAAFFFFFF : 0x66333333;

            drawSector(graphics, centerX, centerY, 60, 100, startAngle, endAngle, color);

            double iconAngle = Math.toRadians(i * 40 - 70);
            int iconX = (int) (centerX + Math.cos(iconAngle) * 80) - 8;
            int iconY = (int) (centerY + Math.sin(iconAngle) * 80) - 8;

            if (skills != null && i < skills.length && skills[i] != null) {
                graphics.renderItem(new ItemStack(skills[i]), iconX, iconY);
            }
        }
        if (hoveredSlot != -1 && skills != null && skills[hoveredSlot] != null) {
            BaseSkillItem s = skills[hoveredSlot];
            graphics.drawCenteredString(this.font, s.getName(ItemStack.EMPTY), centerX, centerY - 5, 0xFFAA00);
            int panelWidth = 145; // 稍微加宽以容纳更长的描述和年限
            int panelX = centerX + 120;
            int panelY = centerY - 60;
            float costMultiplier = 1.0f;
            List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(currentWuhun);
            if (rings != null && hoveredSlot < rings.size()) {
                int nx = (int) rings.get(hoveredSlot).getNianxian();
                costMultiplier = s.getCostMultiplier(nx);
            }
            graphics.fill(panelX - 5, panelY - 5, panelX + panelWidth + 5, panelY + 120, 0xAA000000);
            graphics.renderOutline(panelX - 5, panelY - 5, panelWidth + 10, 125, 0xFFD4AF37);
            int currentY = panelY;
            graphics.drawString(this.font, s.getName(ItemStack.EMPTY), panelX, currentY, 0xFFAA00);
            currentY += 15;
            String typeTag = s.getCastTime() <= 0 ? "§a[瞬发]" : "§e[吟唱: " + (s.getCastTime()/20.0f) + "s]";
            graphics.drawString(this.font, typeTag, panelX, currentY, 0xFFFFFF);
            currentY += 15;
            Component dynamicDesc = s.getDynamicDescription(costMultiplier);
            var lines = this.font.split(dynamicDesc, panelWidth);
            for (var line : lines) {
                graphics.drawString(this.font, line, panelX, currentY, 0xAAAAAA);
                currentY += 10;
            }
            if (rings != null && hoveredSlot < rings.size()) {
                long nianxian = rings.get(hoveredSlot).getNianxian();
                int nxColor = 0xFFFFFF;
                currentY += 7;
                graphics.drawString(this.font, "§7魂技年限: " + String.format("%,d", nianxian) + "年", panelX, currentY, nxColor);
            }
            currentY += 15;
            graphics.drawString(this.font, "§b魂技冷却: " + (s.getCooldownTicks()/20) + "秒", panelX, currentY, 0x55FFFF);

        } else {
            graphics.drawCenteredString(this.font, "§7选择魂技", centerX, centerY - 5, 0xFFFFFF);
        }
    }

    @Override
    public void tick() {
        super.tick();
        int rKeyCode = KeyMappingInit.SKILL_WHEEL.getKey().getValue();
        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        if (GLFW.glfwGetKey(windowHandle, rKeyCode) == GLFW.GLFW_RELEASE) {
            this.onClose();
        }
    }

    @Override
    public void onClose() {
        if (hoveredSlot != -1) {
            NetworkHandler.INSTANCE.sendToServer(new CPacketSelectSkill(hoveredSlot));
        }
        super.onClose();
    }

    private boolean isMouseInSector(int mx, int my, int cx, int cy, int slotIndex) {
        double dist = Math.sqrt(Math.pow(mx - cx, 2) + Math.pow(my - cy, 2));
        if (dist < 30 || dist > 110) return false;
        double angle = Math.toDegrees(Math.atan2(my - cy, mx - cx)) + 90;
        if (angle < 0) angle += 360;
        return (int)(angle / 40) == slotIndex;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    /**
     * 绘制一个圆环扇区
     * @param graphics 渲染上下文
     * @param cx 中心X
     * @param cy 中心Y
     * @param innerR 内半径
     * @param outerR 外半径
     * @param startAngle 起始角度
     * @param endAngle 结束角度
     * @param color 颜色 (ARGB)
     */

    private void drawSector(GuiGraphics graphics, float cx, float cy, float innerR, float outerR, float startAngle, float endAngle, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (float i = startAngle; i <= endAngle; i += 2) {
            float rad = (float) Math.toRadians(i - 90);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            bufferbuilder.vertex(cx + cos * outerR, cy + sin * outerR, 0).color(red, green, blue, alpha).endVertex();
            bufferbuilder.vertex(cx + cos * innerR, cy + sin * innerR, 0).color(red, green, blue, alpha).endVertex();
        }

        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

}