package com.TovidY.kunluncontinent.screen.bank;

import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.bank.BankExchange;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * <b>属性面板里的「银行」页签</b>用的容器（330×190，只取钱）。
 *
 * <p>它和银行方块那套（{@link BankMenu}）是<b>两个独立实现</b>，不继承、不共享按钮 id、
 * 不共享槽位常量 —— 页签这边少一个「存入口 / 换钱页」就少一整块，硬凑在一个类里
 * 只会让两边的改动互相牵连（以前就是靠 {@code pos == null} 分支区分的）。</p>
 *
 * <h3>为什么永不绑定方块</h3>
 * <ul>
 *   <li>页签随时能打开，玩家可能站在地图任何角落 → {@link #stillValid} 直接返回 {@code true}；</li>
 *   <li>没有方块就没有距离校验，所以页签版天生是「随身取钱窗口」，
 *       存入和换汇<b>必须</b>走到村庄里的银行方块前 —— 这是设计约束，不是权限。</li>
 * </ul>
 *
 * <p>⚠️ {@link #getType()} 必须 override：{@code AbstractContainerMenu} 打开时用的是<b>实例的</b>
 * {@code getType()}，不是构造里传的那个，客户端据此路由到 {@link BankStorageScreen}。</p>
 */
public class BankTabMenu extends AbstractContainerMenu {

    /**
     * 取钱数量档：0 = 全部。
     *
     * <p>和方块版储蓄页数值一样，但<b>各存一份</b> —— 页签只取不存，档位含义独立，
     * 以后想给页签单独换档位表时不用回头改方块版。</p>
     */
    public static final int[] PRESETS = {1, 10, 100, 1_000, 0};
    public static final String[] PRESET_LABELS = {"×1", "×10", "×100", "×1000", "全部"};

    /** 页签版按钮 id（每个菜单实例独立解释，只要不和自己冲突即可）。 */
    public static final int BTN_PRESET_BASE = 0;    // 0 ~ 4   数量档
    public static final int BTN_WITHDRAW_BASE = 10; // 10 ~ 12 三个币种取出

    // ---- 背包槽位：面板 190 高，背包居中偏下，左下留给提示框 ----
    public static final int INV_X = 84;
    public static final int INV_Y = 108;
    public static final int HOTBAR_Y = 166;

    private int presetIndex = 0;

    public BankTabMenu(int id, Inventory inv) {
        super(ModMenuTypes.BANK_TAB_MENU.get(), id);

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

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.BANK_TAB_MENU.get();
    }

    /** 页签菜单工厂：忽略额外数据（这条路径本来就没有 BlockPos 可读，别去读 buffer）。 */
    public static BankTabMenu create(int id, Inventory inv, @Nullable FriendlyByteBuf buf) {
        return new BankTabMenu(id, inv);
    }

    public int getPresetIndex() {
        return presetIndex;
    }

    public void setPresetIndex(int index) {
        this.presetIndex = Math.floorMod(index, PRESETS.length);
    }

    /** 当前档位对应的数量；{@code <= 0} = 全部。 */
    public int presetAmount() {
        return PRESETS[presetIndex];
    }

    /**
     * 页签版只认两类按钮：数量档 + 取出。
     *
     * <p>存入 / 换钱的 id 一律落到末尾 {@code return false} —— 即使有人改包发这些 id，
     * 这个容器也不知道怎么处理，天然拒绝了。</p>
     */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer sp)) {
            return false;
        }

        if (id >= BTN_PRESET_BASE && id < BTN_PRESET_BASE + PRESETS.length) {
            setPresetIndex(id - BTN_PRESET_BASE);
            return true;
        }

        if (id >= BTN_WITHDRAW_BASE && id < BTN_WITHDRAW_BASE + BankAccount.Coin.values().length) {
            BankAccount.Coin coin = BankAccount.Coin.byIndex(id - BTN_WITHDRAW_BASE);
            BankAccount.Result result = BankAccount.withdraw(sp, coin, presetAmount());
            sp.sendSystemMessage(Component.literal(
                    (result.success() ? "§a[昆仑银行] " : "§e[昆仑银行] ") + result.message()));
            SynsAPI.synsPlayerAttribute(sp);
            return true;
        }

        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /** 页签随时能开，不做距离校验。 */
    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** 页签入口：先把汇率 / 今日次数刷到最新再开，客户端一打开就是当前值。 */
    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable("container.kunluncontinent.bank");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            if (player instanceof ServerPlayer sp) {
                BankAccount.refreshRates(sp);
                BankExchange.ensureTradeDay(sp);
                SynsAPI.synsPlayerAttribute(sp);
            }
            return new BankTabMenu(id, inv);
        }
    }
}
