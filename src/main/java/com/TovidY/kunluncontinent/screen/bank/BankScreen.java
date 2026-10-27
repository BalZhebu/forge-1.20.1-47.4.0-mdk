package com.TovidY.kunluncontinent.screen.bank;

import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.bank.BankExchange;
import com.TovidY.kunluncontinent.bank.BankRates;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
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
 * 昆仑银行面板（<b>储蓄 / 换钱 两页</b>），版式沿用属性面板那套：
 * 黑底金边（{@link KunlunGuiHelper#renderKunlunBackground}）、标题 {@code §6} + 金色分隔线、手绘按钮。
 *
 * <h3>整体分区</h3>
 * <pre>
 *   ┌─────────────── 上部：本页内容 ────────────────┐
 *   │ 储蓄：三种魂币各一行（库存 / 背包 / 存 / 取）    │
 *   │ 换钱：四行牌价（金→银、银→金、银→铜、铜→银）    │
 *   ├──────────── 数量档（×1 … 全部）───────────────┤
 *   ├──── 左下信息区 ────┬────── 背包 9×4 ─────────┤
 *   │ 换钱：三种魂币余额  │                          │
 *   │ 储蓄：本次操作预览  │                          │
 *   └────────────────────┴──────────────────────────┘
 * </pre>
 *
 * <h3>换钱页为什么一行一个方向</h3>
 * <p>原来把「卖 / 买」塞在一行写成 {@code 卖 1:47 | 买 1:90}，符号太省略。
 * 现在每个方向单独一行，直接写成人话：{@code 1 金 = 47 银}、{@code 90 银 = 1 金}，
 * 左边那颗图标就是<b>你要付出去的币</b>。</p>
 *
 * <h3>两处设计说明</h3>
 * <ul>
 *   <li><b>余额从哪来</b>：{@link BankAccount#getStored(Player, BankAccount.Coin)} 读客户端 capability ——
 *       服务端每次存取/换钱后都会 {@code SynsAPI.synsPlayerAttribute}，余额与汇率随
 *       {@code SPacketPlayerAttribute} 一起下来，不需要额外同步包。</li>
 *   <li><b>为什么面板里要显示背包</b>：存取是直接改玩家背包的，
 *       菜单里带 36 个背包槽，原版才会把改动同步回客户端（否则会出"幽灵物品"）。</li>
 * </ul>
 */
public class BankScreen extends AbstractContainerScreen<BankMenu> implements AttributeTabs.Host {

    private static final int PAGE_SAVE = 0;
    private static final int PAGE_EXCHANGE = 1;

    // ---- 储蓄页：三种魂币行 ----
    private static final int ROW_Y0 = 26;
    private static final int ROW_H = 28;
    private static final int ROW_STEP = 30;
    private static final int BTN_SAVE_W = 36;
    private static final int BTN_H = 15;

    // ---- 换钱页：四个方向各一行（比储蓄行矮一点，四行才放得下）----
    private static final int EX_ROW_Y0 = 24;
    private static final int EX_ROW_H = 21;
    private static final int EX_ROW_STEP = 22;
    private static final int EX_BTN_X = 254;
    private static final int EX_BTN_W = 64;
    private static final int EX_BTN_H = 16;

    // ---- 数量档 ----
    private static final int PRESET_Y = 118;
    private static final int PRESET_X0 = 56;
    private static final int PRESET_STEP = 44;

    // ---- 左下信息区（背包往右让出来的位置）----
    private static final int BLOCK_X = 10;
    private static final int BLOCK_Y = 156;
    private static final int BLOCK_W = 134;
    private static final int BLOCK_H = 78;

    private int page = PAGE_SAVE;

    public BankScreen(BankMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 330;
        this.imageHeight = 240;
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

        // 顶部页签：银行是从属性面板那条页签链进来的，必须能切走
        AttributeTabs.buildTabs(this, AttributeTabs.PAGE_BANK);

        // 顶部两页签（始终存在）
        this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                this.font, this.leftPos + 232, this.topPos + 5, 40, 16,
                Component.literal(page == PAGE_SAVE ? "§e储蓄" : "储蓄"),
                b -> switchPage(PAGE_SAVE),
                () -> List.of(Component.literal("§7存取魂币"))));

        this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                this.font, this.leftPos + 276, this.topPos + 5, 40, 16,
                Component.literal(page == PAGE_EXCHANGE ? "§e换钱" : "换钱"),
                b -> switchPage(PAGE_EXCHANGE),
                () -> List.of(Component.literal("§7银行内币种兑换"))));

        if (page == PAGE_SAVE) {
            initSavePage();
        } else {
            initExchangePage();
        }
    }

    /** 切页：{@code rebuildWidgets()} 会重跑 {@link #init()}，按钮按新页重建。 */
    private void switchPage(int newPage) {
        if (this.page == newPage) return;
        this.page = newPage;
        this.rebuildWidgets();
    }

    private void initSavePage() {
        BankAccount.Coin[] coins = BankAccount.Coin.values();

        for (int i = 0; i < coins.length; i++) {
            final BankAccount.Coin coin = coins[i];
            final int rowY = this.topPos + ROW_Y0 + i * ROW_STEP;

            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + 240, rowY + 6, BTN_SAVE_W, BTN_H,
                    Component.literal("存入"),
                    b -> send(BankMenu.BTN_DEPOSIT_BASE + coin.ordinal()),
                    () -> List.of(
                            Component.literal("§7把背包里的" + coin.displayName() + "存进银行"),
                            Component.literal("§7本次数量: §e" + presetText()),
                            Component.literal("§8银行上限 " + BankAccount.fmt(BankAccount.MAX_STORED)))));

            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + 280, rowY + 6, BTN_SAVE_W, BTN_H,
                    Component.literal("取出"),
                    b -> send(BankMenu.BTN_WITHDRAW_BASE + coin.ordinal()),
                    () -> List.of(
                            Component.literal("§7从银行取出" + coin.displayName()),
                            Component.literal("§7本次数量: §e" + presetText()),
                            Component.literal("§8背包放不下的会留在银行"))));
        }

        addPresetButtons();
    }

    private void initExchangePage() {
        Player player = this.minecraft == null ? null : this.minecraft.player;
        int level = player == null ? 0 : ModAttributeAPI.getDengji(player);

        for (BankExchange.Trade trade : BankExchange.Trade.values()) {
            addTradeButton(trade, level);
        }

        addPresetButtons();
    }

    private void addTradeButton(BankExchange.Trade trade, int level) {
        int rowY = this.topPos + EX_ROW_Y0 + trade.ordinal() * EX_ROW_STEP;
        KunlunGuiHelper.HandDrawnButton button = new KunlunGuiHelper.HandDrawnButton(
                this.font, this.leftPos + EX_BTN_X, rowY + 2, EX_BTN_W, EX_BTN_H,
                Component.literal(trade.isUpgrade()
                        ? "买" + shortName(trade.to())
                        : "卖" + shortName(trade.from())),
                b -> send(BankMenu.BTN_TRADE_BASE + trade.ordinal()),
                () -> {
                    if (!trade.unlockedAt(level)) {
                        return List.of(Component.literal("§c需要 §e" + trade.minLevel() + " 级§c 以上"));
                    }
                    Player p = this.minecraft == null ? null : this.minecraft.player;
                    int left = p == null ? 0 : BankExchange.remainingToday(p, trade);
                    return List.of(
                            Component.literal("§7" + trade.label() + "（只动银行余额）"),
                            Component.literal("§7本次数量: §e" + presetText()),
                            Component.literal("§7今日剩余: §e" + left + " §7次 / 每日 "
                                    + BankExchange.DAILY_LIMIT + " 次"),
                            Component.literal("§8汇率每日 05:00 刷新"));
                });
        if (!trade.unlockedAt(level)) {
            button.active = false;
        }
        this.addRenderableWidget(button);
    }

    private void addPresetButtons() {
        boolean save = page == PAGE_SAVE;
        String[] labels = save ? BankMenu.SAVE_PRESET_LABELS : BankMenu.TRADE_PRESET_LABELS;
        int idBase = save ? BankMenu.BTN_PRESET_BASE : BankMenu.BTN_TPRESET_BASE;

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + PRESET_X0 + i * PRESET_STEP, this.topPos + PRESET_Y, 40, 15,
                    Component.literal(labels[i]),
                    b -> {
                        // 本地也记一份，高亮立刻变；服务端那份在 clickMenuButton 里写
                        if (save) {
                            this.menu.setSavePresetIndex(index);
                        } else {
                            this.menu.setTradePresetIndex(index);
                        }
                        send(idBase + index);
                    },
                    () -> List.of(Component.literal(save
                            ? "§7每次存取的数量"
                            : "§7每次兑换的目标数量（每天每方向限 "
                              + BankExchange.DAILY_LIMIT + " 次）"))));
        }
    }

    /** 把按钮点击发给服务端（原版通道，buttonId 是 byte）。 */
    private void send(int id) {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(
                    new ServerboundContainerButtonClickPacket(this.menu.containerId, id));
        }
    }

    /**
     * 吞掉 TAB。
     *
     * <p>原版 {@code Screen#keyPressed} 里 TAB 会 {@code changeFocus}，把焦点在按钮之间轮转 ——
     * 银行面板的按钮全是鼠标操作，键盘焦点只会带来两个副作用：选中框乱跳、tooltip 粘在鼠标上。
     * 这里直接吃掉，按 TAB 什么也不发生。</p>
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String presetText() {
        boolean save = page == PAGE_SAVE;
        int index = save ? this.menu.getSavePresetIndex() : this.menu.getTradePresetIndex();
        int amount = save ? BankMenu.SAVE_PRESETS[index] : BankMenu.TRADE_PRESETS[index];
        return amount <= 0 ? "全部" : BankAccount.fmt(amount);
    }

    /** 币种单字简称：金魂币 → 金。 */
    private static String shortName(BankAccount.Coin coin) {
        return coin.displayName().substring(0, 1);
    }

    // ==================== AttributeTabs.Host 实现（页签需要访问 protected 成员） ====================

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

    // ==================================================================================
    //  渲染
    // ==================================================================================

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);

        // 手绘按钮的 tooltip 要自己画（它们不是原版 Button）
        for (var child : this.children()) {
            if (child instanceof KunlunGuiHelper.HandDrawnButton btn) {
                btn.renderTooltip(gui, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        gui.drawString(this.font, "§6昆仑银行", this.leftPos + 12, this.topPos + 9, 0xFFFFFF, true);
        gui.fill(this.leftPos + 10, this.topPos + 20, this.leftPos + this.imageWidth - 10, this.topPos + 21, 0xFF8A6D3B);

        Player player = this.minecraft == null ? null : this.minecraft.player;
        if (player == null) return;

        if (page == PAGE_SAVE) {
            renderSavePage(gui, player);
        } else {
            renderExchangePage(gui, player);
        }

        renderSideBlock(gui, player);
        renderInventorySlots(gui);
    }

    // ==================================================================================
    //  储蓄页
    // ==================================================================================

    private void renderSavePage(GuiGraphics gui, Player player) {
        BankAccount.Coin[] coins = BankAccount.Coin.values();

        for (int i = 0; i < coins.length; i++) {
            BankAccount.Coin coin = coins[i];
            int rowY = this.topPos + ROW_Y0 + i * ROW_STEP;

            drawRowBackground(gui, rowY, ROW_H, coin.color());
            gui.renderItem(new ItemStack(coin.item()), this.leftPos + 16, rowY + 6);

            // 第一行：币种名 + 背包持有（右对齐到按钮左边）
            gui.drawString(this.font, coin.displayName(), this.leftPos + 36, rowY + 4, coin.color(), true);
            String bag = "背包 " + BankAccount.fmt(BankAccount.countInInventory(player, coin));
            gui.drawString(this.font, "§8" + bag,
                    this.leftPos + 234 - this.font.width(bag), rowY + 4, 0xFFFFFF, true);

            // 第二行：库存 / 上限
            int stored = BankAccount.getStored(player, coin);
            gui.drawString(this.font,
                    "§7库存 §e" + BankAccount.fmt(stored) + " §8/ " + BankAccount.fmt(BankAccount.MAX_STORED),
                    this.leftPos + 36, rowY + 15, 0xFFFFFF, true);
        }

        renderPresetRow(gui, "§7每次操作");

        // 换钱提示放在右侧按钮下方——左侧已经让给信息区了
        gui.drawString(this.font, "§8取出按背包空间结算，装不下的留在银行。",
                this.leftPos + 12, this.topPos + 137, 0xFFFFFF, true);
    }

    // ==================================================================================
    //  换钱页
    // ==================================================================================

    private void renderExchangePage(GuiGraphics gui, Player player) {
        int silverPerGold = BankAccount.rateSilverPerGold(player);
        int copperPerSilver = BankAccount.rateCopperPerSilver(player);
        int level = ModAttributeAPI.getDengji(player);

        for (BankExchange.Trade trade : BankExchange.Trade.values()) {
            int rowY = this.topPos + EX_ROW_Y0 + trade.ordinal() * EX_ROW_STEP;

            drawRowBackground(gui, rowY, EX_ROW_H, trade.from().color());
            gui.renderItem(new ItemStack(trade.from().item()), this.leftPos + 16, rowY + 3);

            // "金 → 银"（绿=卖、红=买）+ 人话牌价 + 今日剩余次数
            String head = (trade.isUpgrade() ? "§c" : "§a") + trade.label();
            String price = " §8· §f" + priceText(trade, silverPerGold, copperPerSilver);
            String need = "";
            if (trade.isUpgrade()) {
                need = " " + (trade.unlockedAt(level) ? "§8" : "§c") + "(需" + trade.minLevel() + "级)";
            }
            int left = BankExchange.remainingToday(player, trade);
            String remain = left <= 0 ? " §c今日已满" : " §8剩§f" + left + "§8次";

            gui.drawString(this.font, head + price + need + remain,
                    this.leftPos + 38, rowY + 7, 0xFFFFFF, true);
        }

        renderPresetRow(gui, "§7每次兑换");

        gui.drawString(this.font, "§8牌价每日 05:00 刷新（按世界时间算，不用挂机）",
                this.leftPos + 12, this.topPos + 137, 0xFFFFFF, true);
    }

    /** 每个方向写成一句人话：{@code 1 金 = 47 银} / {@code 90 银 = 1 金}。 */
    private static String priceText(BankExchange.Trade trade, int silverPerGold, int copperPerSilver) {
        return switch (trade) {
            case GOLD_TO_SILVER -> "1 金 = " + silverPerGold + " 银";
            case SILVER_TO_GOLD -> BankRates.GOLD_BUY_PRICE_SILVER + " 银 = 1 金";
            case SILVER_TO_COPPER -> "1 银 = " + copperPerSilver + " 铜";
            case COPPER_TO_SILVER -> BankRates.SILVER_BUY_PRICE_COPPER + " 铜 = 1 银";
        };
    }

    // ==================================================================================
    //  公用绘制
    // ==================================================================================

    /** 行底 + 左侧币色竖条。 */
    private void drawRowBackground(GuiGraphics gui, int rowY, int height, int color) {
        gui.fill(this.leftPos + 10, rowY, this.leftPos + 320, rowY + height, 0x33000000);
        gui.fill(this.leftPos + 10, rowY, this.leftPos + 13, rowY + height, 0xFF000000 | color);
    }

    /** 数量档那一行（含选中金框）。 */
    private void renderPresetRow(GuiGraphics gui, String label) {
        int index = page == PAGE_SAVE ? this.menu.getSavePresetIndex() : this.menu.getTradePresetIndex();
        gui.drawString(this.font, label, this.leftPos + 12, this.topPos + PRESET_Y + 4, 0xFFFFFF, true);
        gui.renderOutline(this.leftPos + PRESET_X0 - 1 + index * PRESET_STEP,
                this.topPos + PRESET_Y - 1, 42, 17, 0xFFE6B800);
    }

    /**
     * 左下信息区（背包往右挪出来的空位）。
     *
     * <ul>
     *   <li>换钱页：三种魂币的<b>当前银行余额</b>，不用来回切页确认；</li>
     *   <li>储蓄页：本次操作数量 / 背包可容纳 / 上限提醒。</li>
     * </ul>
     */
    private void renderSideBlock(GuiGraphics gui, Player player) {
        int x = this.leftPos + BLOCK_X;
        int y = this.topPos + BLOCK_Y;
        gui.fill(x, y, x + BLOCK_W, y + BLOCK_H, 0x33000000);
        gui.renderOutline(x, y, BLOCK_W, BLOCK_H, 0xFF3A4354);

        if (page == PAGE_EXCHANGE) {
            gui.drawString(this.font, "§8账户余额", x + 6, y + 5, 0xFFFFFF, true);
            int rowY = y + 19;
            for (BankAccount.Coin coin : BankAccount.Coin.values()) {
                gui.renderItem(new ItemStack(coin.item()), x + 4, rowY);
                gui.drawString(this.font, "§7" + coin.displayName(), x + 24, rowY + 4, 0xFFFFFF, true);

                String value = BankAccount.fmt(BankAccount.getStored(player, coin));
                gui.drawString(this.font, "§f" + value,
                        x + BLOCK_W - 6 - this.font.width(value), rowY + 4, 0xFFFFFF, true);
                rowY += 18;
            }
            return;
        }

        gui.drawString(this.font, "§8本次操作", x + 6, y + 5, 0xFFFFFF, true);
        gui.drawString(this.font, "§e" + presetText(), x + 6, y + 16, 0xFFFFFF, true);
        gui.drawString(this.font, "§8背包可容 §f" + BankAccount.fmt(BankAccount.freeSpace(player, BankAccount.Coin.GOLD)),
                x + 6, y + 33, 0xFFFFFF, true);
        gui.drawString(this.font, "§8银行上限 §f2000万", x + 6, y + 46, 0xFFFFFF, true);
        gui.drawString(this.font, "§8取不完留银行", x + 6, y + 59, 0xFFFFFF, true);
    }

    /**
     * 36 个背包槽的衬底。
     *
     * <p>⚠️ 框要画在 <b>(x-1, y-1)</b> 而不是 (x, y)：物品是按 {@code slot.x/y} 画 16×16 的，
     * 18×18 的框如果也从 (x, y) 起，图标就贴在框的左上角、右下空 2px，看起来"格子对不上"。
     * 原版槽位贴图也是这个口径（框比图标四周各大 1px）。</p>
     */
    private void renderInventorySlots(GuiGraphics gui) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                KunlunGuiHelper.renderSlotBackground(gui,
                        this.leftPos + BankMenu.INV_X + col * 18 - 1,
                        this.topPos + BankMenu.INV_Y + row * 18 - 1, false);
            }
        }
        for (int col = 0; col < 9; col++) {
            KunlunGuiHelper.renderSlotBackground(gui,
                    this.leftPos + BankMenu.INV_X + col * 18 - 1,
                    this.topPos + BankMenu.HOTBAR_Y - 1, false);
        }
    }
}
