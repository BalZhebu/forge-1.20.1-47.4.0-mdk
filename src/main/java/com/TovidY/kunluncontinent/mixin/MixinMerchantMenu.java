package com.TovidY.kunluncontinent.mixin;

import com.TovidY.kunluncontinent.bank.BankAutoTrade;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>商人交易：点左侧商品时先从银行补币。</b>
 *
 * <h3>为什么是 {@code tryMoveItems} 的 HEAD</h3>
 * <p>服务端收到 {@code ServerboundSelectTradePacket} 后走
 * {@code handleSelectTrade → setSelectionHint → tryMoveItems}，
 * 其中只有 {@code tryMoveItems} 会把物品从背包搬进支付槽。
 * 在它进入前把背包补够，原版的搬运逻辑就会把币放进交易栏 —— 不用自己去碰
 * {@code MerchantContainer}，也不会和原版的音效 / XP 结算冲突。</p>
 *
 * <p>这个方法只在服务端被 {@code handleSelectTrade} 调用，
 * 客户端那份 {@code MerchantMenu} 不会跑进来（真跑了也只是判断开关后什么都不做）。</p>
 */
@Mixin(MerchantMenu.class)
public class MixinMerchantMenu {

    @Inject(method = "tryMoveItems", at = @At("HEAD"))
    private void kl$fillCoinsFromBank(int selectedOffer, CallbackInfo ci) {
        BankAutoTrade.fillForOffer((MerchantMenu) (Object) this, selectedOffer);
    }
}
