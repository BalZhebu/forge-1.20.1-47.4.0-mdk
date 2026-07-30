package com.TovidY.kunluncontinent.screen.playernpc;

import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SNpcDialogActionPacket;
import com.TovidY.kunluncontinent.screen.AnimatedScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NpcDialogScreen extends AnimatedScreen {

    private final int entityId;
    private PlayerNpcEntity npcEntity;

    private final int panelWidth = 320;
    private final int panelHeight = 200;
    private int panelLeft;
    private int panelTop;

    private final List<String> dialogOptions = new ArrayList<>();
    private String rawDialogText = "";
    private long openScreenTime = 0L;
    private int lastPlayedCharCount = 0;
    private int selectedActionId = -1;

    public NpcDialogScreen(int entityId) {
        super(Component.literal("NPC 对话"));
        this.entityId = entityId;

        dialogOptions.add("看看你这里有什么好东西");
        dialogOptions.add("来一场切磋吧！");
        dialogOptions.add("没事了");
    }

    @Override
    protected void init() {
        super.init(); // 必须调用，用于初始化基类动画时间

        if (this.minecraft != null && this.minecraft.level != null) {
            Entity entity = this.minecraft.level.getEntity(this.entityId);
            if (entity instanceof PlayerNpcEntity playerNpc) {
                this.npcEntity = playerNpc;
            }
        }

        this.rawDialogText = NpcDialogTextLibrary.getRandomGeneralDialog();
        this.openScreenTime = Util.getMillis();
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        // 添加按钮
        int startX = this.panelLeft + 15;
        int startY = this.panelTop + 110;
        for (int i = 0; i < dialogOptions.size(); i++) {
            final int actionId = i;
            Button optionButton = Button.builder(Component.literal(dialogOptions.get(i)), btn -> {
                        this.selectedActionId = actionId;
                        this.onClose(); // 直接调用 onClose() 就会自动触发收回动画！
                    })
                    .bounds(startX, startY + (i * 26), 290, 22)
                    .build();

            this.addRenderableWidget(optionButton);
        }
    }

    // --- 核心：这里只关心画什么，不用管怎么做动画 ---
    @Override
    protected void drawAnimatedContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int alphaBits) {
        int bgColor = (alphaBits & 0xD0000000) | 0x10141E;
        int borderColor = alphaBits | 0x3A4F66;

        // 1. 绘制背景与边框
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, bgColor);
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 1, borderColor);
        guiGraphics.fill(panelLeft, panelTop + panelHeight - 1, panelLeft + panelWidth, panelTop + panelHeight, borderColor);
        guiGraphics.fill(panelLeft, panelTop, panelLeft + 1, panelTop + panelHeight, borderColor);
        guiGraphics.fill(panelLeft + panelWidth - 1, panelTop, panelLeft + panelWidth, panelTop + panelHeight, borderColor);

        // 2. NPC 名字与打字机文本
        String npcName = (this.npcEntity != null) ? this.npcEntity.getName().getString() : "未知NPC";
        guiGraphics.drawString(this.font, "§l§e[" + npcName + "]", panelLeft + 15, panelTop + 15, 0xFFFFFF | alphaBits, false);

        renderTypewriterText(guiGraphics, this.rawDialogText, panelLeft + 15, panelTop + 38, 15, alphaBits);

        // 3. 渲染自定义按钮
        renderCustomButtons(guiGraphics, mouseX, mouseY, alphaBits);
    }

    // 关闭动画播放完毕后，发送网络包
    @Override
    protected void onAnimationFinished() {
        if (this.selectedActionId != -1) {
            NetworkHandler.INSTANCE.sendToServer(new C2SNpcDialogActionPacket(this.entityId, this.selectedActionId));
        }
    }

    // 辅助绘制按钮与文本
    private void renderCustomButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, int alphaBits) {
        for (var widget : this.renderables) {
            if (widget instanceof Button btn) {
                boolean isHovered = mouseX >= btn.getX() && mouseX < btn.getX() + btn.getWidth() &&
                        mouseY >= btn.getY() && mouseY < btn.getY() + btn.getHeight();

                int btnBgColor = isHovered ? ((alphaBits & 0xD0000000) | 0x223246) : ((alphaBits & 0xD0000000) | 0x161F2C);
                int btnBorderColor = isHovered ? (alphaBits | 0x5C80A6) : (alphaBits | 0x2C3D50);

                guiGraphics.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), btnBgColor);
                guiGraphics.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + 1, btnBorderColor);
                guiGraphics.fill(btn.getX(), btn.getY() + btn.getHeight() - 1, btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), btnBorderColor);
                guiGraphics.fill(btn.getX(), btn.getY(), btn.getX() + 1, btn.getY() + btn.getHeight(), btnBorderColor);
                guiGraphics.fill(btn.getX() + btn.getWidth() - 1, btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), btnBorderColor);

                int textX = btn.getX() + (btn.getWidth() - this.font.width(btn.getMessage())) / 2;
                int textY = btn.getY() + (btn.getHeight() - 8) / 2;
                guiGraphics.drawString(this.font, btn.getMessage(), textX, textY, (isHovered ? 0xFFFF55 : 0xE0E0E0) | alphaBits, false);
            }
        }
    }

    private void renderTypewriterText(GuiGraphics guiGraphics, String fullText, int startX, int startY, int lineHeight, int alphaBits) {
        if (fullText == null || fullText.isEmpty()) return;

        long elapsedMs = Util.getMillis() - this.openScreenTime;
        int visibleCharCount = Mth.clamp((int) (elapsedMs / 70L), 0, fullText.length());

        if (visibleCharCount > this.lastPlayedCharCount) {
            char currentChar = fullText.charAt(visibleCharCount - 1);
            if (currentChar != ' ' && currentChar != '\n' && this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.6F, 0.25F));
            }
            this.lastPlayedCharCount = visibleCharCount;
        }

        String[] lines = fullText.substring(0, visibleCharCount).split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            guiGraphics.drawString(this.font, lines[i], startX, startY + (i * lineHeight), 0xFAFAFA | alphaBits, false);
        }
    }
}