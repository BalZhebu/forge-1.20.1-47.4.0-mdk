package com.TovidY.kunluncontinent.screen.playernpc;

import com.TovidY.kunluncontinent.capability.CapabilityRegistryHandler;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SNpcDialogActionPacket;
import com.TovidY.kunluncontinent.screen.AnimatedScreen;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NpcDialogScreen extends AnimatedScreen {

    private static final int DIALOG_BAR_HEIGHT = 180;
    private static final int OPTION_PANEL_W   = 200;
    private static final int OPTION_GAP       = 6;
    private static final int OPTION_BTN_H     = 28;
    private static final int TEXT_ROW_H       = 19;
    private static final int NAME_BAR_H       = 32;
    private static final float FONT_SCALE     = 1.15f;

    private static final float UI_MAX_ALPHA_FACTOR = 0.75f;

    private final int entityId;
    private PlayerNpcEntity npcEntity;

    private final List<String> dialogOptions = List.of(
            "看看你这里有什么好东西",
            "来一场切磋吧！",
            "我这里有好东西要不要看看？",
            "没事了"
    );
    private String rawDialogText = "";
    private long openScreenTime = 0L;
    private int lastPlayedCharCount = 0;
    private int selectedActionId = -1;

    private final Random decorRandom = new Random(42L);
    private final List<DecorDot> decorDots = new ArrayList<>();

    private static class DecorDot {
        final float x, y, size, alpha, phase;
        DecorDot(float x, float y, float size, float alpha, float phase) {
            this.x = x; this.y = y; this.size = size;
            this.alpha = alpha; this.phase = phase;
        }
    }

    public NpcDialogScreen(int entityId) {
        super(Component.literal("NPC 对话"));
        this.entityId = entityId;
        initDecorDots();
    }

    private void initDecorDots() {
        decorRandom.setSeed(42L);
        for (int i = 0; i < 20; i++) {
            decorDots.add(new DecorDot(
                    decorRandom.nextFloat(),
                    decorRandom.nextFloat(),
                    1.0f + decorRandom.nextFloat() * 1.5f,
                    0.10f + decorRandom.nextFloat() * 0.18f,
                    decorRandom.nextFloat() * (float) Math.PI * 2f
            ));
        }
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.level != null) {
            Entity entity = this.minecraft.level.getEntity(this.entityId);
            if (entity instanceof PlayerNpcEntity npc) {
                this.npcEntity = npc;
            }
        }
        this.rawDialogText    = NpcDialogTextLibrary.getRandomGeneralDialog();
        this.openScreenTime   = Util.getMillis();
        this.selectedActionId = -1;

        OptionBtnLayout layout = computeOptionLayout();
        for (int i = 0; i < dialogOptions.size(); i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(
                    Component.empty(),
                    btn -> { selectedActionId = idx; onClose(); }
            ).bounds(layout.btnX, layout.btnY + i * (OPTION_BTN_H + OPTION_GAP),
                    layout.btnW, OPTION_BTN_H).build());
        }
    }

    private OptionBtnLayout computeOptionLayout() {
        int panelLeft = width - OPTION_PANEL_W - 12;
        int panelTop  = height - DIALOG_BAR_HEIGHT - 8;
        int sepY      = panelTop + 28;
        return new OptionBtnLayout(panelLeft + 12, sepY + 8, OPTION_PANEL_W - 24);
    }

    private static final class OptionBtnLayout {
        final int btnX, btnY, btnW;
        OptionBtnLayout(int btnX, int btnY, int btnW) {
            this.btnX = btnX; this.btnY = btnY; this.btnW = btnW;
        }
    }

    @Override
    protected void drawAnimatedContent(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                       float partialTick, int alphaBits) {
        // 提取动画传入的当前 Alpha 值 (0~255)，并按 85% 比例缩放
        int rawAlpha = (alphaBits >> 24) & 0xFF;
        int scaledAlpha = (int) (rawAlpha * UI_MAX_ALPHA_FACTOR);
        int adjustedAlphaBits = (scaledAlpha & 0xFF) << 24;

        int elapsedMs = (int) (Util.getMillis() - openScreenTime);
        drawDecorDots(guiGraphics, elapsedMs, adjustedAlphaBits);
        drawDialogBar(guiGraphics, mouseX, mouseY, elapsedMs, adjustedAlphaBits);
        drawOptionPanel(guiGraphics, mouseX, mouseY, elapsedMs, adjustedAlphaBits);
    }

    private void drawDecorDots(GuiGraphics g, int elapsedMs, int alphaBits) {
        for (DecorDot d : decorDots) {
            float flicker = 0.55f + 0.45f * Mth.sin((elapsedMs + (int) (d.phase * 220f)) / 800f);
            int px = (int) (d.x * width);
            int py = (int) (d.y * height);
            int a  = (int) (d.alpha * flicker * 0xFF) & 0xFF;
            g.fill(px, py, px + (int) d.size, py + (int) d.size,
                    withAlpha(0x5090C0, alphaBits | (a << 24)));
        }
    }

    private void drawDialogBar(GuiGraphics g, int mouseX, int mouseY,
                               int elapsedMs, int alphaBits) {
        int dialogLeft   = 12;
        int dialogRight  = width - OPTION_PANEL_W - 12;
        int dialogTop    = height - DIALOG_BAR_HEIGHT - 8;
        int dialogBottom = height - 8;
        int dialogW      = dialogRight - dialogLeft;

        PlayerAttributeCapability npcCap = (npcEntity != null) ? npcEntity.getSoulCapability() : null;

        int glowA = (int) (0x28 + 0x18 * Mth.sin(elapsedMs / 900f));
        g.fill(dialogLeft - 2, dialogTop - 2, dialogRight + 2, dialogBottom + 2,
                withAlpha(0x1A3050, alphaBits | (glowA << 24)));
        drawGradientRect(g, dialogLeft, dialogTop, dialogRight, dialogBottom,
                withAlpha(0x080C14, alphaBits),
                withAlpha(0x0E1620, alphaBits));
        g.fill(dialogLeft + 2, dialogTop, dialogRight - 2, dialogTop + 2,
                withAlpha(0x3A5A7A, alphaBits | (0x20 << 24)));

        int cx = dialogLeft + dialogW / 2;

        drawCornerBracket(g, dialogLeft, dialogTop, false, alphaBits);
        drawCornerBracket(g, dialogRight, dialogTop, true, alphaBits);

        // ── 名字条 & 称号 ──
        String npcName = (npcEntity != null) ? npcEntity.getName().getString() : "未知";
        int npcLevel = 1;
        if (npcEntity != null && npcEntity.hasCustomName() && npcEntity.getCustomName() != null) {
            String fullName = npcEntity.getCustomName().getString();
            try {
                if (fullName.contains("-----")) {
                    String rightPart = fullName.split("-----")[1];
                    String cleanText = rightPart.replaceAll("§[0-9a-fk-or]", "");
                    if (cleanText.contains("级")) {
                        String levelStr = cleanText.split("级")[0];
                        npcLevel = Integer.parseInt(levelStr.trim());
                    }
                }
            } catch (Exception e) {
                npcLevel = 1;
            }
        }

        if (npcLevel <= 0) npcLevel = 1;

        // 提取称号与颜色前缀
        String colorPrefix = CapabilityRegistryHandler.getNpcTitleColorPrefix(npcLevel);
        String titleName = CapabilityRegistryHandler.getNpcTitleByLevel(npcLevel);
        String formattedTitle = colorPrefix + "[" + titleName + "]";

        int nameX = dialogLeft + 14;
        int nameY = dialogTop + 6;

        g.fill(dialogLeft + 4, nameY, dialogRight - 4, nameY + NAME_BAR_H,
                withAlpha(0x121C28, alphaBits));
        g.fill(nameX, nameY + 6, nameX + 5, nameY + NAME_BAR_H - 6,
                withAlpha(0x5A9FD8, alphaBits));
        g.fill(nameX + 5, nameY + 9, nameX + 7, nameY + NAME_BAR_H - 9,
                withAlpha(0x8AC0F0, alphaBits));
        int nameTextX = nameX + 12;
        g.drawString(font, Component.literal("▶ " + npcName),
                nameTextX, nameY + 8,
                withAlpha(0xE8D080, alphaBits), false);

        g.drawString(font, Component.literal(formattedTitle),
                nameTextX + 3, nameY + 20,
                withAlpha(0xFFFFFF, alphaBits), false);
        int textLeft  = dialogLeft + 14;
        int textRight = dialogRight - 14;
        int textTop   = dialogTop + NAME_BAR_H + 11;
        int textMaxH  = dialogBottom - textTop - 8;
        int maxRows   = textMaxH / TEXT_ROW_H;
        int visible   = Mth.clamp(elapsedMs / 65, 0, rawDialogText.length());

        // 打字音效过滤换行符
        if (visible > lastPlayedCharCount) {
            char ch = rawDialogText.charAt(visible - 1);
            if (ch != ' ' && ch != '\n' && ch != '\r' && this.minecraft != null) {
                this.minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.4f, 0.3f));
            }
            lastPlayedCharCount = visible;
        }

        int row = 0;
        int charIdx = 0;
        int cursorY = -1;

        while (charIdx < visible && row < maxRows) {
            int rowStart = charIdx;
            int col = 0;
            boolean hitNewLine = false;

            while (charIdx < visible) {
                char c = rawDialogText.charAt(charIdx);
                // 检测到换行符，标记并跳出当前行解析，不加进 rowText
                if (c == '\n' || c == '\r') {
                    hitNewLine = true;
                    break;
                }
                int cw = (int)(font.width(String.valueOf(c)) * FONT_SCALE);
                if (col + cw > textRight - textLeft) break;
                col += cw;
                charIdx++;
            }

            String rowText = rawDialogText.substring(rowStart, charIdx);

            if (hitNewLine) {
                charIdx++;
            }

            g.pose().pushPose();
            g.pose().scale(FONT_SCALE, FONT_SCALE, 1.0f);
            g.drawString(font, rowText,
                    (int)(textLeft / FONT_SCALE), (int)((textTop + row * TEXT_ROW_H) / FONT_SCALE),
                    withAlpha(0xC8D8E8, alphaBits), false);
            g.pose().popPose();

            if (charIdx < visible) {
                cursorY = textTop + row * TEXT_ROW_H + 1;
            }
            row++;
        }

        if (visible < rawDialogText.length() && cursorY >= 0) {
            if ((elapsedMs / 260) % 2 == 0) {
                g.fill(textLeft, cursorY, textLeft + 2, cursorY + TEXT_ROW_H - 2,
                        withAlpha(0x80B0E0, alphaBits));
            }
        }

        int sepY = dialogBottom - 12;
        int fade = Math.min(0xFF, elapsedMs / 5);
        g.fill(textLeft, sepY, textRight, sepY + 1,
                withAlpha(0x2A4060, alphaBits | (fade << 24)));
        g.fill(cx - 3, sepY - 1, cx + 3, sepY + 2,
                withAlpha(0x3A6090, alphaBits | (fade << 24)));
    }

    private void drawOptionPanel(GuiGraphics g, int mouseX, int mouseY,
                                 int elapsedMs, int alphaBits) {
        int panelLeft = width - OPTION_PANEL_W - 12;
        int panelRight = width - 12;
        int panelTop   = height - DIALOG_BAR_HEIGHT - 8;
        int panelBottom = height - 8;

        int glowA = (int) (0x18 + 0x10 * Mth.sin(elapsedMs / 1100f + 1f));
        g.fill(panelLeft - 2, panelTop - 2, panelRight + 2, panelBottom + 2,
                withAlpha(0x121E2E, alphaBits | (glowA << 24)));
        drawGradientRect(g, panelLeft, panelTop, panelRight, panelBottom,
                withAlpha(0x080C14, alphaBits),
                withAlpha(0x0C121A, alphaBits));
        g.fill(panelLeft + 2, panelTop, panelRight - 2, panelTop + 2,
                withAlpha(0x2A4060, alphaBits | (0x20 << 24)));

        drawCornerBracket(g, panelLeft, panelTop, false, alphaBits);
        drawCornerBracket(g, panelRight, panelTop, true, alphaBits);

        String title = "— 对 话 —";
        int titleW = font.width(title);
        g.drawString(font, title,
                panelLeft + (OPTION_PANEL_W - titleW) / 2,
                panelTop + 10,
                withAlpha(0x5A9FD8, alphaBits), false);

        int sepY = panelTop + 28;
        g.fill(panelLeft + 16, sepY, panelRight - 16, sepY + 1,
                withAlpha(0x2A4060, alphaBits));

        int btnW = OPTION_PANEL_W - 24;
        int btnStartY = sepY + 8;

        for (int i = 0; i < dialogOptions.size(); i++) {
            String label = dialogOptions.get(i);
            int btnH = OPTION_BTN_H;
            int btnY = btnStartY + i * (btnH + OPTION_GAP);
            int btnX = panelLeft + 12;
            int btnX2 = btnX + btnW;

            boolean hovered = mouseX >= btnX && mouseX < btnX2
                    && mouseY >= btnY && mouseY < btnY + btnH;

            int bgCol = hovered ? withAlpha(0x141E2A, alphaBits) : withAlpha(0x0C1218, alphaBits);
            g.fill(btnX, btnY, btnX2, btnY + btnH, bgCol);

            int borderCol = hovered ? withAlpha(0x4A80B0, alphaBits) : withAlpha(0x1E2E40, alphaBits);
            g.fill(btnX,      btnY,      btnX2,    btnY + 1, borderCol);
            g.fill(btnX,      btnY + btnH - 1, btnX2, btnY + btnH, borderCol);
            g.fill(btnX,      btnY,      btnX + 1, btnY + btnH, borderCol);
            g.fill(btnX2 - 1, btnY,      btnX2,    btnY + btnH, borderCol);

            g.fill(btnX + 2, btnY, btnX2 - 2, btnY + 1,
                    withAlpha(hovered ? 0x2A4060 : 0x121A28, alphaBits));

            int dotX = btnX + 6, dotY = btnY + btnH / 2;
            g.fill(dotX, dotY - 2, dotX + 4, dotY + 2,
                    withAlpha(hovered ? 0x5A9FD8 : 0x2A4060, alphaBits));

            int tw = (int)(font.width(label) * FONT_SCALE);
            g.pose().pushPose();
            g.pose().scale(FONT_SCALE, FONT_SCALE, 1.0f);
            g.drawString(font, label,
                    (int)((btnX + (btnW - tw) / 2) / FONT_SCALE),
                    (int)((btnY + (btnH - 8) / 2) / FONT_SCALE),
                    withAlpha(hovered ? 0xFFFF90 : 0xA8B8C8, alphaBits), false);
            g.pose().popPose();
        }

        g.fill(panelLeft + 16, panelBottom - 6, panelRight - 16, panelBottom - 5,
                withAlpha(0x1E2E40, alphaBits));
    }

    private void drawGradientRect(GuiGraphics g, int x1, int y1, int x2, int y2, int color1, int color2) {
        g.fillGradient(x1, y1, x2, y2, color1, color2);
    }

    private void drawCornerBracket(GuiGraphics g, int x, int y, boolean flip, int alphaBits) {
        int c = withAlpha(0x5A9FD8, alphaBits);
        if (!flip) {
            g.fill(x, y,     x + 10, y + 2, c);
            g.fill(x, y,     x + 2,  y + 10, c);
        } else {
            g.fill(x - 10, y, x, y + 2, c);
            g.fill(x - 2,  y, x, y + 10, c);
        }
    }

    private int withAlpha(int rgb, int alphaBits) {
        return (rgb & 0x00FFFFFF) | (alphaBits & 0xFF000000);
    }

    @Override
    protected void onAnimationFinished() {
        if (selectedActionId != -1) {
            NetworkHandler.INSTANCE.sendToServer(
                    new C2SNpcDialogActionPacket(entityId, selectedActionId));
        }
    }
}