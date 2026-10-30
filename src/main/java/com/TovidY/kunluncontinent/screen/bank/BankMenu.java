package com.TovidY.kunluncontinent.screen.bank;

import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.bank.BankExchange;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * <b>银行方块</b>右键打开的面板容器（330×240，储蓄 + 换钱两页）。
 *
 * <p>⚠️ 它和属性面板里的「银行」页签是<b>两套完全独立的东西</b>：
 * 本类只管「站在银行方块前能做的事」，页签那套是 {@link BankTabMenu}（只能取钱、永不绑定方块）。
 * 以前两者共用一个容器类、靠 {@code pos == null} 分支区分，结果任何一处改动都会同时影响两边，
 * 所以拆开了 —— <b>别再往这里加"页签专用"的分支</b>。</p>
 *
 * <h3>为什么是容器（而不是直接开一个 Screen）</h3>
 * <ul>
 *   <li>{@link #stillValid} 免费拿到「离方块不超过 8 格」的校验，走远了自动关界面；</li>
 *   <li>{@link #clickMenuButton} 自带一条<b>合法的客户端 → 服务端</b>通道，
 *       不用为一个面板单独写 C2S 网络包；</li>
 *   <li>菜单开/关、切维度的生命周期由原版管。</li>
 * </ul>
 *
 * <h3>为什么要有玩家背包槽位（关键）</h3>
 * <p>存取魂币是<b>直接操作玩家背包</b>的。原版只同步「当前打开的那个容器」的槽位：
 * 如果银行菜单里没有背包槽，服务端改完背包，客户端那份 ItemStack 还是旧的 ——
 * 玩家会看到"存进去的魂币还在手里"（幽灵物品），下次用就和服务端对不上。</p>
 *
 * <h3>金额怎么同步给客户端</h3>
 * <p><b>不走 {@code ContainerData}</b>：原版 {@code ClientboundContainerSetDataPacket}
 * 把值写成 short（±32767），2000 万的余额会被截断。
 * 余额写在 capability 的 NBT 里，{@code SPacketPlayerAttribute} 每次同步都会一起带过去。</p>
 *
 * <h3>为什么「数量档」要存在服务端</h3>
 * <p>按钮只带得回一个 int（{@code clickMenuButton(id)}），带不回金额。
 * 所以点「×100」时服务端也记下下标（{@link #savePresetIndex} / {@link #tradePresetIndex}），
 * 之后点「取出 / 卖出」用服务端这份值算 —— 客户端那一份只负责高亮选中项。</p>
 */
public class BankMenu extends AbstractContainerMenu {

    /**
     * 储蓄页数量档：0 表示「全部」。
     *
     * <p>只到 ×1000 就够：魂币一格 64 个、背包 36 格，整包也就 2304 枚，再做 ×1万 的档位永远取不满。</p>
     */
    public static final int[] SAVE_PRESETS = {1, 10, 100, 1_000, 0};
    public static final String[] SAVE_PRESET_LABELS = {"×1", "×10", "×100", "×1000", "全部"};

    /**
     * 换钱页数量档（0 = 全部）。
     *
     * <p>没有 ×100/×1000：每个方向<b>每天只有 100 次</b>（见 {@code BankExchange.DAILY_LIMIT}），
     * 大额靠「全部」，小额靠 1/10/30/50。</p>
     */
    public static final int[] TRADE_PRESETS = {1, 10, 30, 50, 0};
    public static final String[] TRADE_PRESET_LABELS = {"×1", "×10", "×30", "×50", "全部"};

    /** 按钮 id 分区（{@code ServerboundContainerButtonClickPacket} 的 buttonId 是 byte，别超过 127）。 */
    public static final int BTN_PRESET_BASE = 0;    // 0 ~ 4   储蓄页数量档
    public static final int BTN_TPRESET_BASE = 5;   // 5 ~ 9   换钱页数量档
    public static final int BTN_DEPOSIT_BASE = 10;  // 10 ~ 12
    public static final int BTN_WITHDRAW_BASE = 20; // 20 ~ 22
    public static final int BTN_TRADE_BASE = 30;    // 30 ~ 33（换钱四方向）

    // ---- 背包槽位：面板 240 高，背包靠右下，左侧让给信息区 ----
    public static final int INV_X = 150;
    public static final int INV_Y = 158;
    public static final int HOTBAR_Y = 216;

    /** 打开这块方块对应的银行时用，负责「离方块太远自动关」的判定。 */
    private final ContainerLevelAccess access;
    /** 方块位置（仅用于日志/调试，业务上用不到）。 */
    private final BlockPos pos;

    /** 储蓄页与换钱页各存一份数量档 —— 两页的档位表不一样，不能共用一个下标。 */
    private int savePresetIndex = 0;
    private int tradePresetIndex = 0;

    public BankMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
    }

    public BankMenu(int id, Inventory inv, BlockPos pos) {
        super(ModMenuTypes.BANK_MENU.get(), id);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);

        // 玩家背包 3×9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, INV_X + col * 18, HOTBAR_Y));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getSavePresetIndex() {
        return savePresetIndex;
    }

    public void setSavePresetIndex(int index) {
        this.savePresetIndex = Math.floorMod(index, SAVE_PRESETS.length);
    }

    public int getTradePresetIndex() {
        return tradePresetIndex;
    }

    public void setTradePresetIndex(int index) {
        this.tradePresetIndex = Math.floorMod(index, TRADE_PRESETS.length);
    }

    /** 储蓄页当前档位对应的数量；{@code <= 0} = 全部。 */
    public int savePresetAmount() {
        return SAVE_PRESETS[savePresetIndex];
    }

    /** 换钱页当前档位对应的数量；{@code <= 0} = 全部。 */
    public int tradePresetAmount() {
        return TRADE_PRESETS[tradePresetIndex];
    }

    /**
     * 客户端点按钮 → 原版发 {@code ServerboundContainerButtonClickPacket} → 落到这里（服务端线程）。
     *
     * <p>只做三件互斥的事：切数量档 / 存入 / 取出 / 换钱。做完立刻把 capability 同步给客户端，
     * 面板上的余额数字就会自己刷新，不需要额外的回包。</p>
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer sp)) {
            return false;
        }

        if (id >= BTN_PRESET_BASE && id < BTN_PRESET_BASE + SAVE_PRESETS.length) {
            setSavePresetIndex(id - BTN_PRESET_BASE);
            return true;
        }

        if (id >= BTN_TPRESET_BASE && id < BTN_TPRESET_BASE + TRADE_PRESETS.length) {
            setTradePresetIndex(id - BTN_TPRESET_BASE);
            return true;
        }

        if (id >= BTN_DEPOSIT_BASE && id < BTN_DEPOSIT_BASE + BankAccount.Coin.values().length) {
            BankAccount.Coin coin = BankAccount.Coin.byIndex(id - BTN_DEPOSIT_BASE);
            BankAccount.Result result = BankAccount.deposit(sp, coin, savePresetAmount());
            feedback(sp, result);
            SynsAPI.synsPlayerAttribute(sp);
            return true;
        }

        if (id >= BTN_WITHDRAW_BASE && id < BTN_WITHDRAW_BASE + BankAccount.Coin.values().length) {
            BankAccount.Coin coin = BankAccount.Coin.byIndex(id - BTN_WITHDRAW_BASE);
            BankAccount.Result result = BankAccount.withdraw(sp, coin, savePresetAmount());
            feedback(sp, result);
            SynsAPI.synsPlayerAttribute(sp);
            return true;
        }

        // 换钱：只动银行余额，不碰背包。汇率在服务端按当天重算（缓存字段只是为了给客户端显示）
        if (id >= BTN_TRADE_BASE && id < BTN_TRADE_BASE + BankExchange.Trade.values().length) {
            BankExchange.Trade trade = BankExchange.Trade.byIndex(id - BTN_TRADE_BASE);
            BankAccount.refreshRates(sp);
            BankExchange.ensureTradeDay(sp);
            BankAccount.Result result = BankExchange.trade(sp, trade, tradePresetAmount(),
                    BankAccount.rateSilverPerGold(sp), BankAccount.rateCopperPerSilver(sp));
            feedback(sp, result);
            SynsAPI.synsPlayerAttribute(sp);
            return true;
        }

        return false;
    }

    /** 操作结果：成功绿、失败黄。走聊天栏（面板左下不会被面板盖住）。 */
    private static void feedback(ServerPlayer player, BankAccount.Result result) {
        player.sendSystemMessage(Component.literal(
                (result.success() ? "§a[昆仑银行] " : "§e[昆仑银行] ") + result.message()));
    }

    /**
     * 面板里的 36 个槽位全是<b>玩家自己的背包</b>，shift 点击没有「搬到机器里」这回事，
     * 原版那套「往容器里塞」的逻辑在这里没有意义，直接返回空。
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.BANK_BLOCK.get());
    }
}
