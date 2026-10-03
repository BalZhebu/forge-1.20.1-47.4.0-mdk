package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.Init.KeyMappingInit;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.FlySpeedTuning;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.KeyMapping;
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

    private int bindingIndex = -1;
    private boolean isLocked = false;

    private static final int LEFT_PANEL_WIDTH = 130;
    private static final int LEFT_PANEL_HEIGHT = 200;
    private static final int ITEM_HEIGHT = 18;

    // ── 飞行速度档位按钮（左侧面板底部空白区）──
    /** 飞行区标题的基线 Y（相对 panelY）。 */
    private static final int FLY_TITLE_DY = 172;
    /** −/+ 按钮的顶边 Y（相对 panelY）与高度。 */
    private static final int FLY_BTN_DY = 182;
    private static final int FLY_BTN_SIZE = 14;
    /** −/+ 按钮宽度。 */
    private static final int FLY_BTN_W = 20;

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

        graphics.fill(0, 0, this.width, this.height, 0x66000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        if (cap == null) return;
        String currentWuhun = cap.getWuhunName();
        if (currentWuhun == null) return;
        BaseSkillItem[] skills = cap.getWuhunSkillsMap().get(currentWuhun);
        if (skills == null) {
            skills = new BaseSkillItem[0];
        }

        drawLeftKeybindPanel(graphics, mouseX, mouseY, centerX, centerY);

        this.hoveredSlot = -1;
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

            if (i < skills.length && skills[i] != null) {
                ItemStack stack = new ItemStack(skills[i]);
                float scale = 2.5f;
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                graphics.pose().pushPose();
                graphics.pose().translate(iconX + 8, iconY + 8, 100);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.pose().translate(-8, -8, 0);
                graphics.renderItem(stack, 0, 0);
                graphics.renderItemDecorations(this.font, stack, 0, 0);
                graphics.pose().popPose();
            }
        }

        // 4. 右侧技能详情面板
        if (hoveredSlot != -1 && skills != null && hoveredSlot < skills.length && skills[hoveredSlot] != null) {
            BaseSkillItem s = skills[hoveredSlot];
            graphics.drawCenteredString(this.font, s.getName(ItemStack.EMPTY), centerX, centerY - 5, 0xFFAA00);
            int panelWidth = 145;
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

    private void drawLeftKeybindPanel(GuiGraphics graphics, int mouseX, int mouseY, int centerX, int centerY) {
        int panelX = centerX - 120 - LEFT_PANEL_WIDTH;

        int panelY = centerY - (LEFT_PANEL_HEIGHT / 2) + 20;

        graphics.fill(panelX - 5, panelY - 22, panelX + LEFT_PANEL_WIDTH + 5, panelY + LEFT_PANEL_HEIGHT, 0xAA000000);
        graphics.renderOutline(panelX - 5, panelY - 22, LEFT_PANEL_WIDTH + 10, LEFT_PANEL_HEIGHT + 22, 0xFFD4AF37);

        graphics.drawString(this.font, "§e快捷释放设置", panelX, panelY - 18, 0xFFFFFF);

        graphics.drawString(this.font, "§7(点击改键后可松开R)", panelX, panelY - 8, 0xAAAAAA);

        String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};

        for (int i = 0; i < 9; i++) {
            int itemY = panelY + 10 + i * ITEM_HEIGHT;
            int btnX = panelX + 70;
            int btnW = 55;
            int btnH = 14;

            boolean isHovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= itemY && mouseY <= itemY + btnH;

            graphics.drawString(this.font, "第" + numbers[i] + "魂技", panelX, itemY + 3, 0xDDDDDD);

            KeyMapping km = KeyMappingInit.SKILL_KEYS[i];
            String keyName = km.isUnbound() ? "§7未指定" : km.getTranslatedKeyMessage().getString();

            int btnColor = (bindingIndex == i) ? 0xFF55FF55 : (isHovered ? 0xAAFFFFFF : 0x66333333);
            if (bindingIndex == i) {
                keyName = "> 按任意键 <";
            }

            graphics.fill(btnX, itemY, btnX + btnW, itemY + btnH, btnColor);
            graphics.renderOutline(btnX, itemY, btnW, btnH, 0xFF888888);
            graphics.drawCenteredString(this.font, keyName, btnX + btnW / 2, itemY + 3, 0xFFFFFF);
        }

        drawFlySpeedControls(graphics, mouseX, mouseY, panelX, panelY);
    }

    /**
     * 左侧按键面板底部的飞行速度调节区。
     *
     * <p>面板高 {@link #LEFT_PANEL_HEIGHT}=200，9 行按键占到 panelY+168，
     * 所以 172~196 这段是空余的，正好放标题 + 一对按钮 + 进度条。</p>
     *
     * <p>数值全部来自 {@link FlySpeedTuning}（唯一权威表）；
     * 点击只发 {@link CPacketChangeFlySpeed}，由服务端改档并回同步。</p>
     */
    private void drawFlySpeedControls(GuiGraphics graphics, int mouseX, int mouseY, int panelX, int panelY) {
        int level = cap.getFlySpeedLevel();
        String levelName = FlySpeedTuning.levelName(level);

        // 标题行：档位名 + 速度/耗神倍率
        graphics.drawString(this.font, "§b飞行 " + levelName, panelX, panelY + FLY_TITLE_DY, 0xFFFFFF);
        graphics.drawString(this.font,
                "§7速§f×" + trim(FlySpeedTuning.speedMultiplier(level))
                        + " §7耗§f×" + trim(FlySpeedTuning.costMultiplier(level)),
                panelX + 52, panelY + FLY_TITLE_DY, 0xAAAAAA);

        int btnY = panelY + FLY_BTN_DY;
        drawFlyButton(graphics, mouseX, mouseY, panelX, btnY, "-",
                level > 0, 0xFF55FF55);
        drawFlyButton(graphics, mouseX, mouseY, panelX + LEFT_PANEL_WIDTH - FLY_BTN_W, btnY, "+",
                level < FlySpeedTuning.MAX_LEVEL, 0xFFFFAA00);

        int barX = panelX + FLY_BTN_W + 4;
        int barW = LEFT_PANEL_WIDTH - (FLY_BTN_W + 4) * 2;
        int barY = btnY + FLY_BTN_SIZE / 2 - 1;
        graphics.fill(barX, barY, barX + barW, barY + 3, 0xFF1A1A1A);
        graphics.renderOutline(barX - 1, barY - 1, barW + 2, 5, 0xFF555555);

        int segCount = FlySpeedTuning.MAX_LEVEL + 1;
        float segW = (float) barW / segCount;
        for (int i = 0; i <= level; i++) {
            int x0 = barX + Math.round(i * segW);
            int x1 = (i == segCount - 1) ? barX + barW
                    : barX + Math.round((i + 1) * segW) - 1;
            graphics.fill(x0, barY, x1, barY + 3, 0xFF55CCFF);
        }
    }

    /** 画一个 ± 小按钮；到顶/到底时置灰。 */
    private void drawFlyButton(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, String label,
                               boolean enabled, int accent) {
        boolean hovered = enabled && mouseX >= x && mouseX <= x + FLY_BTN_W
                && mouseY >= y && mouseY <= y + FLY_BTN_SIZE;
        int bg = !enabled ? 0xFF3A3A3A : (hovered ? (accent & 0x00FFFFFF) | 0x99000000 : 0x66222222);
        graphics.fill(x, y, x + FLY_BTN_W, y + FLY_BTN_SIZE, bg);
        graphics.renderOutline(x, y, FLY_BTN_W, FLY_BTN_SIZE, enabled ? accent : 0xFF666666);
        graphics.drawCenteredString(this.font, label, x + FLY_BTN_W / 2, y + 3,
                enabled ? 0xFFFFFF : 0x888888);
    }

    /** 整数倍率不带小数点。 */
    private static String trim(float value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.valueOf(value);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 120 - LEFT_PANEL_WIDTH;
        int panelY = centerY - (LEFT_PANEL_HEIGHT / 2) + 20; // 匹配下移后的坐标

        // 飞行速度 −/+ 按钮（放在按键行判定之后、super 之前，坐标与 drawFlySpeedControls 一致）
        int flyBtnY = panelY + FLY_BTN_DY;
        int level = cap.getFlySpeedLevel();
        if (flyBtnY <= mouseY && mouseY <= flyBtnY + FLY_BTN_SIZE) {
            int minusX = panelX;
            int plusX = panelX + LEFT_PANEL_WIDTH - FLY_BTN_W;
            if (mouseX >= minusX && mouseX <= minusX + FLY_BTN_W && level > 0) {
                NetworkHandler.sendToServer(new CPacketChangeFlySpeed(-1));
                return true;
            }
            if (mouseX >= plusX && mouseX <= plusX + FLY_BTN_W && level < FlySpeedTuning.MAX_LEVEL) {
                NetworkHandler.sendToServer(new CPacketChangeFlySpeed(+1));
                return true;
            }
        }

        for (int i = 0; i < 9; i++) {
            int itemY = panelY + 10 + i * ITEM_HEIGHT;
            int btnX = panelX + 70;
            int btnW = 55;
            int btnH = 14;

            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= itemY && mouseY <= itemY + btnH) {
                this.bindingIndex = i;
                this.isLocked = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingIndex != -1) {
            int wheelKeyCode = KeyMappingInit.SKILL_WHEEL.getKey().getValue();

            if (keyCode == wheelKeyCode) {
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                KeyMappingInit.SKILL_KEYS[bindingIndex].setKey(InputConstants.UNKNOWN);
            } else {
                InputConstants.Key newKey = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
                KeyMappingInit.SKILL_KEYS[bindingIndex].setKey(newKey);
            }

            KeyMapping.resetMapping();
            bindingIndex = -1;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();

        if (isLocked) {
            return;
        }

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