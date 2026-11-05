package com.TovidY.kunluncontinent.screen.bank;

import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabBar;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * 属性面板里的「银行」页签界面（<b>330×190，和属性/魂环面板一样大</b>）。
 *
 * <h3>和方块版（{@link BankScreen}）的关系</h3>
 * <ul>
 *   <li><b>两套完全独立</b>：本界面配 {@link BankTabMenu}，方块版配 {@code BankMenu}；
 *       容器、按钮 id、槽位坐标都各有一份，互不继承 —— 改一边不会牵连另一边。</li>
 *   <li><b>只能取钱</b>：没有存入口、没有换钱页 —— 存钱和换汇必须走到真正的银行方块前。
 *       {@link BankTabMenu#clickMenuButton} 本身就不认识「存入 / 换钱」的按钮 id。</li>
 *   <li>版式紧凑：上面三行「金/银/铜 储量」，中间数量档，下面居中放背包，左下角一块提示框。</li>
 * </ul>
 *
 * <pre>
 *   昆仑银行
 *   ─────────────────────────────────────────────
 *   [金] 金魂币      背包 128           [取出]
 *        库存 1,234 / 20,000,000
 *   [银] …
 *   [铜] …
 *   每次操作 [×1][×10][×100][×1000][全部]
 *   ┌ 提示 ─────┐  [ 9×3 背包 ]  （右侧留白）
 *   │ 存币去村庄 │  [ 快捷栏   ]
 *   │ 存入无利息 │
 *   └───────────┘
 * </pre>
 */
public class BankStorageScreen extends AbstractContainerScreen<BankTabMenu> implements AttributeTabBar.Host {

    // ---- 版式（相对 leftPos / topPos）----
    private static final int ROW_Y0 = 20;
    private static final int ROW_H = 21;
    private static final int ROW_STEP = 22;
    private static final int BTN_X = 252;
    private static final int BTN_W = 60;
    private static final int BTN_H = 16;
    private static final int PRESET_Y = 88;
    private static final int PRESET_X0 = 56;
    private static final int PRESET_STEP = 44;

    // ---- 左下提示框（背包右侧那块空位暂时留白）----
    private static final int LEFT_BOX_X = 10;
    private static final int LEFT_BOX_W = 70;
    private static final int SIDE_BOX_Y = 106;
    private static final int SIDE_BOX_H = 78;

    /** 左侧提示文案（会自动换行，别写太长不然框里放不下）。 */
    private static final String HINT_TEXT =
            "存魂币请前往村庄的银行方块处存入。存入不会产生利息。";

    public BankStorageScreen(BankTabMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 330;
        this.imageHeight = 190;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    // ==================================================================================
    //  控件
    // ==================================================================================

    @Override
    protected void init() {
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        super.init();

        // 顶部页签（银行是页签链的一环，必须能切走）
        AttributeTabBar.buildTabs(this, AttributeTabs.PAGE_BANK);

        // 三个「取出」按钮
        for (BankAccount.Coin coin : BankAccount.Coin.values()) {
            int rowY = this.topPos + ROW_Y0 + coin.ordinal() * ROW_STEP;
            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + BTN_X, rowY + 3, BTN_W, BTN_H,
                    Component.literal("取出"),
                    b -> send(BankTabMenu.BTN_WITHDRAW_BASE + coin.ordinal()),
                    () -> List.of(
                            Component.literal("§7从银行取出" + coin.displayName()),
                            Component.literal("§7本次数量: §e" + presetText()),
                            Component.literal("§8背包放不下的会留在银行"))));
        }

        // 数量档（和方块版储蓄页一致：×1 / ×10 / ×100 / ×1000 / 全部）
        for (int i = 0; i < BankTabMenu.PRESET_LABELS.length; i++) {
            final int index = i;
            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + PRESET_X0 + i * PRESET_STEP, this.topPos + PRESET_Y, 40, 15,
                    Component.literal(BankTabMenu.PRESET_LABELS[i]),
                    b -> {
                        this.menu.setPresetIndex(index);
                        send(BankTabMenu.BTN_PRESET_BASE + index);
                    },
                    () -> List.of(Component.literal("§7每次取出的数量"))));
        }
    }

    private void send(int id) {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(
                    new ServerboundContainerButtonClickPacket(this.menu.containerId, id));
        }
    }

    /** 吞掉 TAB：这个面板全是鼠标按钮，键盘焦点只会让选中框乱跳 / tooltip 粘鼠标。 */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String presetText() {
        int amount = BankTabMenu.PRESETS[this.menu.getPresetIndex()];
        return amount <= 0 ? "全部" : BankAccount.fmt(amount);
    }

    // ==================================================================================
    //  渲染
    // ==================================================================================

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);

        for (var child : this.children()) {
            if (child instanceof KunlunGuiHelper.HandDrawnButton btn) {
                btn.renderTooltip(gui, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        gui.drawString(this.font, "§6昆仑银行", this.leftPos + 12, this.topPos + 7, 0xFFFFFF, true);
        gui.fill(this.leftPos + 10, this.topPos + 17, this.leftPos + this.imageWidth - 10, this.topPos + 18, 0xFF8A6D3B);

        Player player = this.minecraft == null ? null : this.minecraft.player;
        if (player == null) return;

        // 三行储量
        for (BankAccount.Coin coin : BankAccount.Coin.values()) {
            int rowY = this.topPos + ROW_Y0 + coin.ordinal() * ROW_STEP;

            gui.fill(this.leftPos + 10, rowY, this.leftPos + 320, rowY + ROW_H, 0x33000000);
            gui.fill(this.leftPos + 10, rowY, this.leftPos + 13, rowY + ROW_H, 0xFF000000 | coin.color());

            gui.renderItem(new ItemStack(coin.item()), this.leftPos + 16, rowY + 2);
            gui.drawString(this.font, coin.displayName(), this.leftPos + 36, rowY + 2, coin.color(), true);

            String bag = "背包 " + BankAccount.fmt(BankAccount.countInInventory(player, coin));
            gui.drawString(this.font, "§8" + bag,
                    this.leftPos + 244 - this.font.width(bag), rowY + 2, 0xFFFFFF, true);

            int stored = BankAccount.getStored(player, coin);
            gui.drawString(this.font,
                    "§7库存 §e" + BankAccount.fmt(stored) + " §8/ " + BankAccount.fmt(BankAccount.MAX_STORED),
                    this.leftPos + 36, rowY + 12, 0xFFFFFF, true);
        }

        // 数量档 + 选中框
        gui.drawString(this.font, "§7每次操作", this.leftPos + 12, this.topPos + PRESET_Y + 4, 0xFFFFFF, true);
        gui.renderOutline(this.leftPos + PRESET_X0 - 1 + this.menu.getPresetIndex() * PRESET_STEP,
                this.topPos + PRESET_Y - 1, 42, 17, 0xFFE6B800);

        renderHintBox(gui);
        renderInventorySlots(gui);
    }

    /**
     * 左侧提示框：告诉玩家「这里只能取，存钱要去村庄里的银行；而且没有利息」。
     *
     * <p>文本按像素宽度自动换行 —— 框只有 70px 宽，写死换行的话以后改文案就得重新数格子。</p>
     */
    private void renderHintBox(GuiGraphics gui) {
        drawBox(gui, LEFT_BOX_X, SIDE_BOX_Y, LEFT_BOX_W, SIDE_BOX_H);
        drawWrapped(gui, HINT_TEXT,
                this.leftPos + LEFT_BOX_X + 5, this.topPos + SIDE_BOX_Y + 7,
                LEFT_BOX_W - 10, 10, 0x9AA4B2);
    }

    /** 侧栏框的统一底纹。 */
    private void drawBox(GuiGraphics gui, int relX, int relY, int w, int h) {
        int x = this.leftPos + relX;
        int y = this.topPos + relY;
        gui.fill(x, y, x + w, y + h, 0x33000000);
        gui.renderOutline(x, y, w, h, 0xFF3A4354);
    }

    /** 按像素宽度逐字换行（中英混排都能用），返回占用高度。 */
    private int drawWrapped(GuiGraphics gui, String text, int x, int y,
                            int maxWidth, int lineHeight, int color) {
        StringBuilder line = new StringBuilder();
        int cursorY = y;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                gui.drawString(this.font, line.toString(), x, cursorY, color, false);
                line.setLength(0);
                cursorY += lineHeight;
                continue;
            }
            String candidate = line.toString() + c;
            if (!line.isEmpty() && this.font.width(candidate) > maxWidth) {
                gui.drawString(this.font, line.toString(), x, cursorY, color, false);
                line.setLength(0);
                cursorY += lineHeight;
            }
            line.append(c);
        }
        if (!line.isEmpty()) {
            gui.drawString(this.font, line.toString(), x, cursorY, color, false);
            cursorY += lineHeight;
        }
        return cursorY - y;
    }

    /** 背包衬底：框画在 (x-1, y-1)，图标才在格子里居中（原版槽位口径）。 */
    private void renderInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                KunlunGuiHelper.renderSlotBackground(gui,
                        this.leftPos + BankTabMenu.INV_X + col * 18 - 1,
                        this.topPos + BankTabMenu.INV_Y + row * 18 - 1, false);
            }
        }
        for (int col = 0; col < 9; col++) {
            KunlunGuiHelper.renderSlotBackground(gui,
                    this.leftPos + BankTabMenu.INV_X + col * 18 - 1,
                    this.topPos + BankTabMenu.HOTBAR_Y - 1, false);
        }
    }

    // ==================== AttributeTabBar.Host ====================

    @Override
    public int leftPos() {
        return this.leftPos;
    }

    @Override
    public int topPos() {
        return this.topPos;
    }

    @Override
    public int imageWidth() {
        return this.imageWidth;
    }

    @Override
    public Font font() {
        return this.font;
    }

    @Override
    public void addWidget(AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }
}
