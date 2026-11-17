package com.TovidY.kunluncontinent.screen.playernpc;

import com.TovidY.kunluncontinent.capability.CapabilityRegistryHandler;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SNpcDialogActionPacket;
import com.TovidY.kunluncontinent.screen.AnimatedScreen;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * NPC 对话界面 —— galgame 风格。
 *
 * 布局：屏幕底部一条半透明长条（宽度随屏幕自适应），左上角探出名牌，
 * 选项条悬浮在对话框上方右侧、自下而上排列，逐条延迟淡入。
 *
 * 交互：打在对话框矩形内点击 —— 文字没打完就一次全显，打完则随机换下一句；
 * 点击框外不响应，选项条始终优先。
 */
public class NpcDialogScreen extends AnimatedScreen {

    // ── 视觉参数 ──
    private static final float FONT_SCALE   = 1.15f;
    private static final int   ROW_H        = 15;   // 正文行距（逻辑像素）
    private static final int   NAME_H       = 20;
    private static final int   NAME_LIFT    = 13;   // 名牌探出对话框顶部的高度
    private static final int   OPT_H        = 20;
    private static final int   OPT_GAP      = 5;
    private static final int   CHARS_PER_SEC = 28;  // 打字机速度

    // ── 配色 ──
    private static final int CLR_BOX_TOP    = 0x0D1319;
    private static final int CLR_BOX_BOT    = 0x05080C;
    private static final int CLR_ACCENT     = 0x9FC6E8; // 冷蓝高光线
    private static final int CLR_TEXT       = 0xE6EDF4;
    private static final int CLR_TEXT_HOVER = 0xFFE9A8;
    private static final int CLR_OPT_IDLE   = 0xD2DCE6;
    private static final int CLR_OPT_ACCENT = 0xC9A227;

    private final int entityId;
    private PlayerNpcEntity npcEntity;

    private final List<String> dialogOptions = List.of(
            "看看你这里有什么好东西",
            "来一场切磋吧！",
            "我这里有好东西要不要看看？",
            "没事了"
    );

    private String rawDialogText = "";
    private List<String> lines = List.of();
    private int totalChars = 0;

    private long openScreenTime = 0L;
    private long typingStartTime = 0L;
    private int lastSoundBucket = -1;
    private boolean skipTyping = false;
    private int selectedActionId = -1;

    // 每帧刷新的动画透明度（0~1）
    private float animAlpha = 1f;

    // ── 运行时布局 ──
    private int barX, barY, barW, barH;
    private int optX, optW, optTop;

    public NpcDialogScreen(int entityId) {
        super(Component.literal("NPC 对话"));
        this.entityId = entityId;
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
        this.openScreenTime  = Util.getMillis();
        this.selectedActionId = -1;
        startTyping(NpcDialogTextLibrary.getRandomGeneralDialog());
    }

    /** 换一句台词并重新开始打字（点击对话框空白处触发）。 */
    private void startTyping(String text) {
        this.rawDialogText   = text;
        this.typingStartTime = Util.getMillis();
        this.skipTyping      = false;
        this.lastSoundBucket = -1;
        computeLayout();
    }

    /**
     * 点击对话框时换下一句：随机抽，但避免和当前这句重复。
     */
    private void advanceDialog() {
        String next = NpcDialogTextLibrary.getRandomGeneralDialog();
        for (int i = 0; i < 4 && next.equals(this.rawDialogText); i++) {
            next = NpcDialogTextLibrary.getRandomGeneralDialog();
        }
        startTyping(next);
    }

    /** 当前已经显示了多少个字（打字机进度）。 */
    private int visibleCharsNow() {
        if (skipTyping) return totalChars;
        long elapsed = Util.getMillis() - typingStartTime;
        return (int) Math.min(totalChars, elapsed * CHARS_PER_SEC / 1000L);
    }

    // ──────────────────────────────────────────────────────────
    //  布局
    // ──────────────────────────────────────────────────────────

    private void computeLayout() {
        int margin = Mth.clamp(width / 48, 6, 22);
        barX = margin;
        barW = Math.max(180, width - margin * 2);

        // 先按最大文字宽度断行，再据此算对话框高度
        int textWidth = barW - 44;
        List<String> wrapped = wrapText(font, rawDialogText, textWidth);
        if (wrapped.size() > 4) {  // 极端长句截断到 4 行
            wrapped = new ArrayList<>(wrapped.subList(0, 4));
        }
        lines = wrapped;
        totalChars = 0;
        for (String l : lines) totalChars += l.length();

        int contentH = Math.max(1, lines.size()) * ROW_H;
        int maxH = Math.max(72, height / 3);
        barH = Mth.clamp(12 + contentH + 16, 72, maxH);
        barY = height - margin - barH;

        optW = Mth.clamp(Math.round(barW * 0.34f), 130, 240);
        optW = Math.min(optW, barW - 44);
        optX = barX + barW - optW - 22;

        int totalOptH = dialogOptions.size() * OPT_H + (dialogOptions.size() - 1) * OPT_GAP;
        optTop = Math.max(6, barY - 10 - totalOptH);
    }

    private static List<String> wrapText(Font font, String raw, int maxWidth) {
        List<String> out = new ArrayList<>();
        for (String paragraph : raw.split("\n", -1)) {
            StringBuilder sb = new StringBuilder();
            int w = 0;
            for (int i = 0; i < paragraph.length(); i++) {
                char c = paragraph.charAt(i);
                int cw = Math.round(font.width(String.valueOf(c)) * FONT_SCALE);
                if (w + cw > maxWidth && sb.length() > 0) {
                    out.add(sb.toString());
                    sb.setLength(0);
                    w = 0;
                }
                sb.append(c);
                w += cw;
            }
            out.add(sb.toString());
        }
        return out;
    }

    /** 动画从对话框自身中心展开，而不是屏幕中心 */
    @Override protected float getContentCenterX() { return barX + barW / 2f; }
    @Override protected float getContentCenterY() { return barY + barH / 2f; }

    // ──────────────────────────────────────────────────────────
    //  绘制
    // ──────────────────────────────────────────────────────────

    @Override
    protected void drawAnimatedContent(GuiGraphics g, int mouseX, int mouseY,
                                       float partialTick, int alphaBits) {
        animAlpha = ((alphaBits >> 24) & 0xFF) / 255f;
        if (animAlpha <= 0.004f) return;

        long now = Util.getMillis();
        long elapsed = now - openScreenTime;
        int visibleChars = visibleCharsNow();
        boolean typing = visibleChars < totalChars;

        drawDialogBox(g);
        drawNameTag(g);
        drawText(g, visibleChars, now);
        drawOptions(g, mouseX, mouseY, elapsed);
        if (!typing && animAlpha > 0.9f) {
            drawContinueHint(g, elapsed);
        }
    }

    private void drawDialogBox(GuiGraphics g) {
        g.fillGradient(barX, barY, barX + barW, barY + barH,
                col(CLR_BOX_TOP, 0.55f), col(CLR_BOX_BOT, 0.72f));

        // 顶部高光线 + 左右极细描边，代替原来的角括号
        g.fill(barX, barY, barX + barW, barY + 1, col(CLR_ACCENT, 0.26f));
        g.fill(barX, barY, barX + 1, barY + barH, col(CLR_ACCENT, 0.10f));
        g.fill(barX + barW - 1, barY, barX + barW, barY + barH, col(CLR_ACCENT, 0.10f));
        g.fill(barX, barY + barH - 1, barX + barW, barY + barH, col(CLR_ACCENT, 0.07f));
    }

    private void drawNameTag(GuiGraphics g) {
        String npcName = (npcEntity != null) ? npcEntity.getName().getString() : "未知";
        int npcLevel = parseNpcLevel();

        String prefix = CapabilityRegistryHandler.getNpcTitleColorPrefix(npcLevel);
        String title  = CapabilityRegistryHandler.getNpcTitleByLevel(npcLevel);
        if (prefix == null) prefix = "";
        if (title == null)  title  = "";
        int accent = accentFromPrefix(prefix);

        Component label = Component.literal(prefix + "[" + title + "] §7" + npcName);
        int textW = Math.round(font.width(stripFormatting(label.getString())) * FONT_SCALE);
        int w = Math.min(barW - 24, textW + 26);

        int x = barX + 18;
        int y = barY - NAME_LIFT;

        g.fill(x, y, x + w, y + NAME_H, col(0x05080C, 0.84f));
        g.fill(x, y, x + 3, y + NAME_H, (alphaByte(0.92f) << 24) | (accent & 0xFFFFFF));
        g.fill(x + 3, y, x + w, y + 1, col(CLR_ACCENT, 0.16f));

        g.pose().pushPose();
        g.pose().scale(FONT_SCALE, FONT_SCALE, 1.0f);
        g.drawString(font, label,
                (int) ((x + 11) / FONT_SCALE),
                (int) ((y + (NAME_H - 8) / 2f + 1) / FONT_SCALE),
                (alphaByte(0.96f) << 24) | 0xFFFFFF, false);
        g.pose().popPose();
    }

    private void drawText(GuiGraphics g, int visibleChars, long now) {
        int tx = barX + 22;
        int ty = barY + 13;

        int remaining = visibleChars;
        int cursorX = tx;
        int cursorY = ty;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (remaining <= 0) break;
            String shown = (remaining >= line.length()) ? line : line.substring(0, remaining);
            remaining -= line.length();

            g.pose().pushPose();
            g.pose().scale(FONT_SCALE, FONT_SCALE, 1.0f);
            g.drawString(font, shown,
                    (int) (tx / FONT_SCALE),
                    (int) ((ty + i * ROW_H) / FONT_SCALE),
                    col(CLR_TEXT, 0.95f), false);
            g.pose().popPose();

            cursorX = tx + Math.round(font.width(shown) * FONT_SCALE);
            cursorY = ty + i * ROW_H;
        }

        // 打字光标
        if (visibleChars < totalChars && (now / 260) % 2 == 0) {
            g.fill(cursorX + 1, cursorY, cursorX + 2, cursorY + 9, col(0xBFD8F0, 0.85f));
        }

        // 打字音效：每 3 个字一次，轻到不吵
        int bucket = visibleChars / 3;
        if (bucket != lastSoundBucket && visibleChars > 0 && visibleChars < totalChars
                && this.minecraft != null) {
            this.minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.85f, 0.06f));
            lastSoundBucket = bucket;
        }
    }

    private void drawOptions(GuiGraphics g, int mouseX, int mouseY, long elapsed) {
        for (int i = 0; i < dialogOptions.size(); i++) {
            int y  = optTop + i * (OPT_H + OPT_GAP);
            int y2 = y + OPT_H;
            boolean hovered = mouseX >= optX && mouseX < optX + optW && mouseY >= y && mouseY < y2;

            // 逐条延迟淡入
            float fade = Mth.clamp((elapsed - 140f - i * 60f) / 180f, 0f, 1f);
            float a = Mth.clamp(animAlpha * fade, 0f, 1f);
            if (a <= 0.004f) continue;

            g.fill(optX, y, optX + optW, y2,
                    col(hovered ? 0x16222E : 0x000000, hovered ? 0.72f : 0.50f, a));
            g.fill(optX, y, optX + 2, y2,
                    hovered ? col(CLR_OPT_ACCENT, 0.90f, a) : col(CLR_ACCENT, 0.16f, a));
            g.fill(optX, y, optX + optW, y + 1,
                    col(0xFFFFFF, hovered ? 0.18f : 0.05f, a));

            if (hovered) {
                g.pose().pushPose();
                g.pose().scale(FONT_SCALE, FONT_SCALE, 1.0f);
                g.drawString(font, "▶",
                        (int) ((optX + 7) / FONT_SCALE),
                        (int) ((y + (OPT_H - 8) / 2f + 1) / FONT_SCALE),
                        col(CLR_OPT_ACCENT, 0.95f, a), false);
                g.pose().popPose();
            }

            String label = dialogOptions.get(i);
            g.pose().pushPose();
            g.pose().scale(FONT_SCALE, FONT_SCALE, 1.0f);
            g.drawString(font, label,
                    (int) ((optX + 16) / FONT_SCALE),
                    (int) ((y + (OPT_H - 8) / 2f + 1) / FONT_SCALE),
                    col(hovered ? CLR_TEXT_HOVER : CLR_OPT_IDLE, hovered ? 1.0f : 0.86f, a), false);
            g.pose().popPose();
        }
    }

    /** 打字完成后右下角闪烁的「▼」，提示可以点击继续。 */
    private void drawContinueHint(GuiGraphics g, long elapsed) {
        if ((elapsed / 520) % 2 != 0) return;
        int x = barX + barW - 26;
        int y = barY + barH - 16;
        int c = col(CLR_ACCENT, 0.9f);
        g.fill(x + 1, y, x + 6, y + 1, c);
        g.fill(x + 2, y + 1, x + 5, y + 2, c);
        g.fill(x + 3, y + 2, x + 4, y + 3, c);
    }

    // ──────────────────────────────────────────────────────────
    //  输入
    // ──────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // ① 选项条最优先：那是明确的按钮
        for (int i = 0; i < dialogOptions.size(); i++) {
            int y = optTop + i * (OPT_H + OPT_GAP);
            if (mouseX >= optX && mouseX < optX + optW
                    && mouseY >= y && mouseY < y + OPT_H) {
                selectedActionId = i;
                onClose();
                return true;
            }
        }

        // ② 对话框矩形内（且不在选项条上）：先补全文字，再点击即换下一句
        boolean insideBar = mouseX >= barX && mouseX < barX + barW
                && mouseY >= barY && mouseY < barY + barH;
        if (!insideBar) {
            return true;   // 框外一律不响应
        }

        if (visibleCharsNow() < totalChars) {
            skipTyping = true;
            lastSoundBucket = totalChars / 3;
            return true;
        }

        advanceDialog();
        return true;
    }

    @Override
    protected void onAnimationFinished() {
        if (selectedActionId != -1) {
            NetworkHandler.INSTANCE.sendToServer(
                    new C2SNpcDialogActionPacket(entityId, selectedActionId));
        }
    }

    // ──────────────────────────────────────────────────────────
    //  工具
    // ──────────────────────────────────────────────────────────

    private int parseNpcLevel() {
        int level = 1;
        if (npcEntity != null && npcEntity.hasCustomName() && npcEntity.getCustomName() != null) {
            String fullName = npcEntity.getCustomName().getString();
            try {
                if (fullName.contains("-----")) {
                    String rightPart = fullName.split("-----")[1];
                    String cleanText = rightPart.replaceAll("§[0-9a-fk-or]", "");
                    if (cleanText.contains("级")) {
                        level = Integer.parseInt(cleanText.split("级")[0].trim());
                    }
                }
            } catch (Exception ignored) {
                level = 1;
            }
        }
        return Math.max(1, level);
    }

    private static String stripFormatting(String s) {
        return s.replaceAll("§.", "");
    }

    private static int accentFromPrefix(String prefix) {
        int rgb = CLR_ACCENT;
        if (prefix == null) return rgb;
        for (int i = 0; i + 1 < prefix.length(); i++) {
            if (prefix.charAt(i) == '§') {
                char c = Character.toLowerCase(prefix.charAt(i + 1));
                if ("0123456789abcdef".indexOf(c) >= 0) rgb = legacyRgb(c);
            }
        }
        return rgb;
    }

    private static int legacyRgb(char code) {
        return switch (code) {
            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            default -> 0xFFFFFF;
        };
    }

    private int alphaByte(float a) {
        return (int) (Mth.clamp(a, 0f, 1f) * 255f) & 0xFF;
    }

    /** 颜色（rgb 部分固定，alpha = 面板透明度 × 动画透明度） */
    private int col(int rgb, float alpha) {
        return col(rgb, alpha, animAlpha);
    }

    private int col(int rgb, float alpha, float mul) {
        return (alphaByte(alpha * mul) << 24) | (rgb & 0xFFFFFF);
    }
}
