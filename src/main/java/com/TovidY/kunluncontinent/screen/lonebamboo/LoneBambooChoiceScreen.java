package com.TovidY.kunluncontinent.screen.lonebamboo;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SLoneBambooChoicePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 「孤竹之缘」二选一对话框（纯客户端）。
 *
 * <p>由 {@code S2COpenLoneBambooChoicePacket} 打开，选择用
 * {@link C2SLoneBambooChoicePacket} 回传。<b>服务端会重新校验资格</b>，
 * 这个界面只是表现层。</p>
 */
public class LoneBambooChoiceScreen extends Screen {

    /**
     * 超时兜底：与服务端 {@code LoneBambooGodPath.CHOICE_TIMEOUT_TICKS} 保持一致。
     *
     * <p>客户端这一份只是"到点自动关界面"，<b>真正的判定在服务端</b> ——
     * 玩家把客户端改掉也躲不过，服务端照样按正常路处理。</p>
     */
    private static final int TIMEOUT_TICKS = 120 * 20;

    private int openTicks = 0;

    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int WARN_COLOR = 0xFF6B6B;
    private static final int HINT_COLOR = 0xA0A0A0;

    private final List<Component> warningLines = List.of(
            Component.literal("§c· 你将§l无法获得其它任何神位§r§c；"),
            Component.literal("§c· 你将§l只能吸收孤竹魂环§r§c，其它魂兽的魂环一律吸不了；"),
            Component.literal("§c· 作为交换，你将成为世间唯一的§2孤竹神§r§c。")
    );

    public LoneBambooChoiceScreen() {
        super(Component.literal("孤竹之缘"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonY = this.height / 2 + 42;
        int buttonWidth = 140;
        int buttonHeight = 20;

        // 左：正常魂师之路（无副作用）
        this.addRenderableWidget(Button.builder(Component.literal("§a正常魂师之路"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new C2SLoneBambooChoicePacket(C2SLoneBambooChoicePacket.CHOICE_NORMAL));
            this.onClose();
        }).bounds(centerX - buttonWidth - 10, buttonY, buttonWidth, buttonHeight).build());

        // 右：孤竹神位之路（不可逆）
        this.addRenderableWidget(Button.builder(Component.literal("§2孤竹神位之路"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new C2SLoneBambooChoicePacket(C2SLoneBambooChoicePacket.CHOICE_BAMBOO));
            this.onClose();
        }).bounds(centerX + 10, buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        // ⚠️ 布局铁律：文字全部排在按钮【上方】，按钮固定在 centerY+42 ~ +62。
        //    以前把警示语写在 centerY+38 起，正好压在按钮上，糊成一团。
        int y = this.height / 2 - 92;

        graphics.drawCenteredString(this.font, "§d§l孤 竹 之 缘", centerX, y, TEXT_COLOR);
        y += 24;
        graphics.drawCenteredString(this.font,
                "§7你的第一枚魂环，取自一株孤竹。", centerX, y, TEXT_COLOR);
        y += 14;
        graphics.drawCenteredString(this.font,
                "§7冥冥之中，有个声音问你——要走哪条路？", centerX, y, HINT_COLOR);

        y += 24;
        graphics.drawCenteredString(this.font, "§e选择「孤竹神位之路」意味着：", centerX, y, TEXT_COLOR);
        y += 16;
        for (Component line : warningLines) {
            graphics.drawCenteredString(this.font, line, centerX, y, WARN_COLOR);
            y += 13;
        }

        // 倒计时：到点会自动按"正常魂师之路"处理（放在按钮下方，不挤占按钮区）
        int remainSeconds = Math.max(0, (TIMEOUT_TICKS - openTicks) / 20);
        graphics.drawCenteredString(this.font,
                "§8" + remainSeconds + " 秒内未作抉择，将默认走上正常魂师之路",
                centerX, this.height / 2 + 72, HINT_COLOR);
    }

    @Override
    public void tick() {
        super.tick();
        if (++openTicks >= TIMEOUT_TICKS) {
            // 客户端只是关界面 + 提示；是否生效由服务端说了算（它会发同样的提示）
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("§7【孤竹之缘】迟迟未作抉择，缘分已散。"), false);
            }
            this.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 禁止按 ESC 关掉。
     *
     * <p>这是个<b>一次性</b>的岔路口：关掉界面等于"没选"，而彩蛋只弹一次，
     * 玩家会直接失去这条路。所以强制二选一（两个按钮里必须点一个）。</p>
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
