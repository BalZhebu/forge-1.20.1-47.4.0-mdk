package com.TovidY.kunluncontinent.bank;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * <b>交易自动取币</b>：玩家在商人界面点左侧商品、而背包里的魂币不够时，
 * 自动从「昆仑银行」取出差额塞进背包 —— 原版随后就会把它搬进支付槽。
 *
 * <h3>为什么插在 {@code MerchantMenu#tryMoveItems} 的 HEAD</h3>
 * <p>原版的成交链路是：</p>
 * <pre>
 *   点左侧商品 → ServerboundSelectTradePacket
 *     → ServerGamePacketListenerImpl#handleSelectTrade
 *        → MerchantMenu#setSelectionHint   （只更新 tradeContainer 里记的下标）
 *        → MerchantMenu#tryMoveItems(i)    ← 真正把物品搬到支付槽的地方
 *             · 先把支付槽里已有的东西退回背包
 *             · 两个支付槽都空了，才按 offer.getCostA()/getCostB() 从背包搬到槽里
 * </pre>
 * <p>所以只要在 {@code tryMoveItems} 进入时把背包「补够」，原版逻辑会自然而然
 * 把币搬进交易栏 —— 不需要自己去碰 {@code MerchantContainer}，也不会和原版的音量/XP 结算打架。</p>
 *
 * <h3>什么时候不动手</h3>
 * <ul>
 *   <li>开关关着（{@link BankAccount#CONFIG_AUTO_TRADE} 位，默认开）；</li>
 *   <li>这一条已经卖光（{@code isOutOfStock()}）；</li>
 *   <li>价格用的不是魂币（原版村民用绿宝石 → {@link BankAccount.Coin#byItem} 返回 null）；</li>
 *   <li>背包本来就不缺；或银行里没这么多。</li>
 * </ul>
 * <p><b>补不够就补不够</b>：只补到「银行给得出的部分」，剩下的照原版来（成交失败），
 * 不会把玩家银行掏空之后还给他一个假成功。</p>
 */
public final class BankAutoTrade {

    private BankAutoTrade() {
    }

    /** 交易界面点左侧商品时调用（服务端）。 */
    public static void fillForOffer(MerchantMenu menu, int offerIndex) {
        MerchantOffers offers = menu.getOffers();
        if (offers == null || offerIndex < 0 || offerIndex >= offers.size()) {
            return;
        }
        Player player = playerOf(menu);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PlayerAttributeCapability cap = BankAccount.capability(serverPlayer);
        if (cap == null || !cap.isConfigOpen(BankAccount.CONFIG_AUTO_TRADE)) {
            return;
        }

        MerchantOffer offer = offers.get(offerIndex);
        if (offer.isOutOfStock()) {
            return;
        }

        topUp(serverPlayer, offer.getCostA(), menu.getSlot(1).getItem());
        topUp(serverPlayer, offer.getCostB(), menu.getSlot(0).getItem());
    }

    /**
     * 让背包里该魂币的数量 ≥ {@code cost.getCount()}。
     *
     * @param paySlot 另一个支付槽里的堆叠 —— 原版会先把它退回背包再重新取，
     *                所以这部分也算「已经有」
     */
    private static void topUp(ServerPlayer player, ItemStack cost, ItemStack paySlot) {
        if (cost.isEmpty()) {
            return;
        }
        BankAccount.Coin coin = BankAccount.Coin.byItem(cost.getItem());
        if (coin == null) {
            return;
        }
        int need = cost.getCount();
        if (need <= 0) {
            return;
        }
        // 价格超过一格上限（64）时，原版自己也成交不了：
        //   moveFromInventoryToPaymentSlot 把支付槽卡在 getMaxStackSize()，
        //   而 MerchantOffer#satisfiedBy 要求槽里 count >= 价格 → 永远不成立。
        // 这种情况下不搬钱，免得币被取出银行却什么也买不到。
        if (need > cost.getMaxStackSize()) {
            return;
        }

        int have = countTradeable(player, coin);
        if (!paySlot.isEmpty() && paySlot.is(coin.item())) {
            have += paySlot.getCount();
        }
        int missing = need - have;
        if (missing <= 0) {
            return;
        }
        if (BankAccount.getStored(player, coin) <= 0) {
            return; // 银行也没钱，省一次搬运
        }

        BankAccount.Result result = BankAccount.withdraw(player, coin, missing);
        if (result.success() && result.amount() > 0) {
            // 动作栏提示：钱从银行出来了，得让玩家看见
            player.displayClientMessage(Component.literal(
                    "§b[昆仑银行] §f自动取出 §e" + BankAccount.fmt(result.amount())
                            + " §f枚" + coin.displayName()), true);
        }
    }

    /**
     * 背包 36 格（主背包 + 快捷栏）里的魂币数量。
     *
     * <p>刻意不数盔甲 / 副手：原版 {@code moveFromInventoryToPaymentSlot} 只扫
     * {@code slots[3..38]}（= 背包 0~35），副手那格它看不见。要是我们把它算进「已有」，
     * 就会出现「以为够了 → 没从银行取 → 原版又搬不动」的空转。</p>
     */
    private static int countTradeable(Player player, BankAccount.Coin coin) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(coin.item())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** 槽位 3 起是玩家背包（0/1 支付槽、2 结果槽）。 */
    private static Player playerOf(MerchantMenu menu) {
        if (menu.slots.size() <= 3) {
            return null;
        }
        return menu.slots.get(3).container instanceof Inventory inv ? inv.player : null;
    }
}
