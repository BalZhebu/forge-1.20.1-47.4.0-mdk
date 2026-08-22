package com.TovidY.kunluncontinent.screen.playernpc;

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

    private final int entityId;
    private PlayerNpcEntity npcEntity;

    private final int panelWidth  = 360;
    private final int panelHeight = 250;
    private int panelLeft;
    private int panelTop;

    private final List<String> dialogOptions = new ArrayList<>();
    private String rawDialogText = "";
    private long openScreenTime = 0L;
    private int lastPlayedCharCount = 0;
    private int selectedActionId = -1;

    private final Random decorRandom = new Random(42L);
    private final List<DecorDot> decorDots = new ArrayList<>();
    private final List<DialogButton> buttons = new ArrayList<>();
    private int hoveredButtonIndex = -1;

    // ── 内部类 ──────────────────────────────────────

    private static class DecorDot {
        final float x, y, size, alpha, phase;
        DecorDot(float x, float y, float size, float alpha, float phase) {
            this.x = x; this.y = y; this.size = size;
            this.alpha = alpha; this.phase = phase;
        }
    }

    private static class DialogButton {
        final Component label;
        final int actionId;
        int x, y, w, h;
        DialogButton(Component label, int actionId) {
            this.label = label; this.actionId = actionId;
        }
    }

    // ── 构造 ────────────────────────────────────────

    public NpcDialogScreen(int entityId) {
        super(Component.literal("NPC 对话"));
        this.entityId = entityId;
        dialogOptions.add("看看你这里有什么好东西");
        dialogOptions.add("来一场切磋吧！");
        dialogOptions.add("没事了");
        initDecorDots();
    }

    private void initDecorDots() {
        decorRandom.setSeed(42L);
        for (int i = 0; i < 20; i++) {
            decorDots.add(new DecorDot(
                    decorRandom.nextFloat(),
                    decorRandom.nextFloat(),
                    1.0f + decorRandom.nextFloat() * 1.5f,
                    0.12f + decorRandom.nextFloat() * 0.20f,
                    decorRandom.nextFloat() * (float) Math.PI * 2f
            ));
        }
    }

    // ── 初始化 ──────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.level != null) {
            Entity entity = this.minecraft.level.getEntity(this.entityId);
            if (entity instanceof PlayerNpcEntity playerNpc) {
                this.npcEntity = playerNpc;
            }
        }
        this.rawDialogText    = NpcDialogTextLibrary.getRandomGeneralDialog();
        this.openScreenTime   = Util.getMillis();
        this.panelLeft        = (this.width - this.panelWidth)  / 2;
        this.panelTop         = (this.height - this.panelHeight) / 2;

        buttons.clear();
        int btnX = this.panelLeft + 20;
        int btnY = this.panelTop + 120;
        for (int i = 0; i < dialogOptions.size(); i++) {
            final int idx = i;
            DialogButton db = new DialogButton(Component.literal(dialogOptions.get(i)), idx);
            db.w = 320;
            db.h = 24;
            db.x = btnX;
            db.y = btnY + i * 26;
            buttons.add(db);

            // 仅用于接收点击事件，视觉由 drawAnimatedContent 手绘
            addRenderableWidget(Button.builder(Component.empty(), btn -> {
                selectedActionId = idx;
                onClose();
            }).bounds(db.x, db.y, db.w, db.h).build());
        }
    }

    // ── 绘制入口 ────────────────────────────────────

    @Override
    protected void drawAnimatedContent(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                       float partialTick, int alphaBits) {
        int elapsedMs = (int) (Util.getMillis() - openScreenTime);
        hoveredButtonIndex = -1;

        drawPanelBackground(guiGraphics, alphaBits);
        drawDecorDots(guiGraphics, elapsedMs, alphaBits);
        drawTopBar(guiGraphics, elapsedMs, alphaBits);
        renderTypewriterText(guiGraphics, rawDialogText,
                panelLeft + 20, panelTop + 40, 14, alphaBits, elapsedMs);
        drawTypewriterProgress(guiGraphics, elapsedMs, alphaBits);
        drawDivider(guiGraphics, elapsedMs, alphaBits);
        drawButtons(guiGraphics, mouseX, mouseY, elapsedMs, alphaBits);
    }

    // ── 背景 ────────────────────────────────────────

    private void drawPanelBackground(GuiGraphics guiGraphics, int alphaBits) {
        int l = panelLeft, t = panelTop;
        int r = l + panelWidth,  b = t + panelHeight;

        // 渐变背景
        guiGraphics.fillGradient(l, t, r, b,
                withAlpha(0x0C101A, alphaBits),
                withAlpha(0x141C2A, alphaBits));

        // 呼吸脉冲外发光边框
        float pulse = 0.55f + 0.45f * Mth.sin(Util.getMillis() / 900f);
        int glowA   = (int) (pulse * 0x38) & 0xFF;
        int borderCol = withAlpha(0x4A7FB5, alphaBits);
        int glowCol   = withAlpha(0x2A5080, alphaBits | glowA);

        // 外发光层
        guiGraphics.fill(l - 2, t - 2, r + 2, b + 2, glowCol);
        drawBorderRect(guiGraphics, l, t, r, b, borderCol);
        // 顶部高光条
        guiGraphics.fill(l + 4, t, r - 4, t + 2, withAlpha(0x5A9FD8, alphaBits | 0x28));
    }

    private void drawBorderRect(GuiGraphics g, int l, int t, int r, int b, int color) {
        g.fill(l,      t,      r,      t + 1, color);
        g.fill(l,      b - 1,  r,      b,      color);
        g.fill(l,      t,      l + 1,  b,      color);
        g.fill(r - 1,  t,      r,      b,      color);
    }

    // ── 装饰粒子 ────────────────────────────────────

    private void drawDecorDots(GuiGraphics g, int elapsedMs, int alphaBits) {
        int l = panelLeft, t = panelTop;
        for (DecorDot d : decorDots) {
            float flicker = 0.55f + 0.45f * Mth.sin((elapsedMs + (int) (d.phase * 220f)) / 750f);
            int px = l + (int) (d.x * panelWidth);
            int py = t + (int) (d.y * panelHeight);
            int a  = (int) (d.alpha * flicker * 0xFF) & 0xFF;
            g.fill(px, py, px + (int) d.size, py + (int) d.size,
                    withAlpha(0x5090C0, alphaBits | a));
        }
    }

    // ── 顶部栏：角括号 + 名字 ───────────────────────

    private void drawTopBar(GuiGraphics g, int elapsedMs, int alphaBits) {
        int l = panelLeft, t = panelTop;

        drawCornerBracket(g, l, t, false, alphaBits);
        drawCornerBracket(g, l + panelWidth, t, true, alphaBits);

        String npcName = (npcEntity != null) ? npcEntity.getName().getString() : "未知NPC";

        // 左侧色块图标
        int iconX = l + 14, iconY = t + 12;
        g.fill(iconX, iconY, iconX + 4, iconY + 14, withAlpha(0x5A9FD8, alphaBits));
        g.fill(iconX + 4, iconY + 3, iconX + 6, iconY + 11, withAlpha(0x8AC0F0, alphaBits));

        g.drawString(font, Component.literal("§l§e▶ " + npcName),
                iconX + 12, iconY + 2, withAlpha(0xE8D080, alphaBits), false);

        // 两侧小装饰线
        int lineY = t + 34;
        g.fill(l + 14, lineY, l + 22, lineY + 1, withAlpha(0x3A6090, alphaBits));
        g.fill(l + panelWidth - 22, lineY, l + panelWidth - 14, lineY + 1, withAlpha(0x3A6090, alphaBits));
    }

    private void drawCornerBracket(GuiGraphics g, int x, int y, boolean flip, int alphaBits) {
        int c = withAlpha(0x5A9FD8, alphaBits);
        if (!flip) {
            // ┌
            g.fill(x, y,     x + 11, y + 2, c);
            g.fill(x, y,     x + 2,  y + 11, c);
        } else {
            // ┐
            g.fill(x - 11, y, x, y + 2, c);
            g.fill(x - 2,  y, x, y + 11, c);
        }
    }

    // ── 打字机文本 ──────────────────────────────────

    private void renderTypewriterText(GuiGraphics g, String fullText,
                                      int startX, int startY, int lineHeight,
                                      int alphaBits, int elapsedMs) {
        if (fullText == null || fullText.isEmpty()) return;

        int visible = Mth.clamp(elapsedMs / 65, 0, fullText.length());

        // 打字音效
        if (visible > lastPlayedCharCount) {
            char ch = fullText.charAt(visible - 1);
            if (ch != ' ' && ch != '\n' && this.minecraft != null) {
                this.minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.4f, 0.3f));
            }
            lastPlayedCharCount = visible;
        }

        // 逐行绘制
        String[] lines = fullText.split("\n", -1);
        int charsLeft = visible;
        for (int i = 0; i < lines.length && charsLeft > 0; i++) {
            int take = Math.min(lines[i].length(), charsLeft);
            g.drawString(font, lines[i].substring(0, take),
                    startX, startY + i * lineHeight,
                    withAlpha(0xC8D8E8, alphaBits), false);
            charsLeft -= take;
        }

        // 光标闪烁
        if (visible < fullText.length()) {
            int lineOfCursor = 0;
            int cursorCharInLine = 0;
            for (int i = 0; i < visible && i < fullText.length(); i++) {
                if (fullText.charAt(i) == '\n') {
                    lineOfCursor++;
                    cursorCharInLine = 0;
                } else {
                    cursorCharInLine++;
                }
            }
            int cursorX = startX + font.width(lines[Math.min(lineOfCursor, lines.length - 1)].substring(0, cursorCharInLine));
            int cursorY = startY + lineOfCursor * lineHeight;
            if ((elapsedMs / 260) % 2 == 0) {
                g.fill(cursorX, cursorY + 1, cursorX + 2, cursorY + lineHeight - 1,
                        withAlpha(0x80B0E0, alphaBits));
            }
        }
    }

    // ── 进度条 ──────────────────────────────────────

    private void drawTypewriterProgress(GuiGraphics g, int elapsedMs, int alphaBits) {
        int barL = panelLeft + 20;
        int barT = panelTop + 104;
        int barW = panelWidth - 56;
        int barH = 3;

        g.fill(barL, barT, barL + barW, barT + barH, withAlpha(0x1A2535, alphaBits));

        int filled = Mth.clamp(elapsedMs / 65, 0, rawDialogText.length());
        float ratio = (float) filled / Math.max(rawDialogText.length(), 1);
        int fillW = (int) (barW * ratio);
        if (fillW > 0) {
            g.fill(barL, barT, barL + fillW, barT + barH, withAlpha(0x4A90D0, alphaBits));
        }
    }

    // ── 分割线 ──────────────────────────────────────

    private void drawDivider(GuiGraphics g, int elapsedMs, int alphaBits) {
        int dy = panelTop + 94;
        int alphaFade = Math.min(0xFF, elapsedMs / 4);
        int col = withAlpha(0x2A4060, alphaBits | alphaFade);
        g.fill(panelLeft + 20, dy, panelLeft + panelWidth - 20, dy + 1, col);
        // 中心菱形
        int cx = panelLeft + panelWidth / 2;
        g.fill(cx - 3, dy - 1, cx + 3, dy + 2, withAlpha(0x3A6090, alphaBits | alphaFade));
    }

    // ── 按钮 ────────────────────────────────────────

    private void drawButtons(GuiGraphics g, int mouseX, int mouseY,
                             int elapsedMs, int alphaBits) {
        for (int i = 0; i < buttons.size(); i++) {
            DialogButton btn = buttons.get(i);
            boolean isHovered = mouseX >= btn.x && mouseX < btn.x + btn.w
                    && mouseY >= btn.y && mouseY < btn.y + btn.h;
            if (isHovered) hoveredButtonIndex = i;
            drawDialogButton(g, btn, isHovered, alphaBits, elapsedMs);
        }
    }

    private void drawDialogButton(GuiGraphics g, DialogButton btn,
                                  boolean isHovered, int alphaBits, int elapsedMs) {
        int l = btn.x, t = btn.y, r = l + btn.w, b = t + btn.h;

        // 悬浮光晕（外层柔光）
        if (isHovered) {
            int glowA = (int) (0x30 + 0x14 * Mth.sin(elapsedMs / 180f));
            g.fill(l - 3, t - 3, r + 3, b + 3, withAlpha(0x2A5080, alphaBits | glowA));
        }

        // 背景
        int bgColor = isHovered
                ? withAlpha(0x1E2E42, alphaBits)
                : withAlpha(0x121A28, alphaBits);
        int borderCol = isHovered
                ? withAlpha(0x5A9FD8, alphaBits)
                : withAlpha(0x2A3A50, alphaBits);

        g.fill(l, t, r, b, bgColor);
        drawBorderRect(g, l, t, r, b, borderCol);

        // 顶部高光条
        g.fill(l + 2, t, r - 2, t + 1,
                withAlpha(isHovered ? 0x3A5A7A : 0x1A2535, alphaBits));

        // 左侧指示点
        int dotX = l + 8, dotY = t + btn.h / 2;
        g.fill(dotX, dotY - 2, dotX + 4, dotY + 2,
                withAlpha(isHovered ? 0x5A9FD8 : 0x2A4060, alphaBits));

        // 文字
        String text = btn.label.getString();
        int textColor = isHovered ? 0xFFFF90 : 0xA8B8C8;
        int tx = l + (btn.w - font.width(text)) / 2;
        int ty = t + (btn.h - 8) / 2;
        g.drawString(font, text, tx, ty, withAlpha(textColor, alphaBits), false);
    }

    // ── 工具 ────────────────────────────────────────

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
