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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;

/**
 * 银行面板的<b>页签版</b>（属性面板里那个「银行」页签）。
 *
 * <p>和方块版共用 {@link BankMenu} 的全部逻辑，只做两件事：</p>
 * <ol>
 *   <li>固定传 {@code pos = null} → {@link BankMenu#isWithdrawOnly()} 为真，
 *       <b>只允许取钱</b>（存入 / 换钱的按钮 id 在服务端会被拒掉）；</li>
 *   <li>换一个 {@link MenuType}，让客户端路由到
 *       {@link BankStorageScreen}（330×190、背包居中偏下的紧凑版）而不是方块版的 {@link BankScreen}。</li>
 * </ol>
 *
 * <p>⚠️ {@code getType()} 必须 override：{@code AbstractContainerMenu} 打开时用的是实例的
 * {@code getType()}，不是构造里传的那个（和配置面板复用 {@code AttributeMenu} 是同一手法）。</p>
 */
public class BankTabMenu extends BankMenu {

    public BankTabMenu(int id, Inventory inv) {
        super(id, inv, (BlockPos) null);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.BANK_TAB_MENU.get();
    }

    /** 页签菜单工厂：忽略额外数据（这条路径本来就没有 BlockPos 可读）。 */
    public static BankTabMenu create(int id, Inventory inv, @Nullable FriendlyByteBuf buf) {
        return new BankTabMenu(id, inv);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
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
