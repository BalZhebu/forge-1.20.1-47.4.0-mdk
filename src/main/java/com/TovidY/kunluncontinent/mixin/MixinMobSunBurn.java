package com.TovidY.kunluncontinent.mixin;

import com.TovidY.kunluncontinent.event.server.SunBurnAccess;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 记录「这只 Mob 正在被阳光灼烧」，供 {@code SunBurnDamageHandler} 把它换算成百分比伤害。
 *
 * <h3>为什么是注入 {@code isSunBurnTick} 而不是自己写判定</h3>
 * <p>原版的判定条件是「白天 + 视野光照 + 露天 + 没在水/细雪里」，<b>但不含‘是不是亡灵’</b>——
 * 亡灵与否由各子类的 {@code aiStep} 决定要不要调用它。所以：</p>
 * <ul>
 *   <li>自己复刻判定 → 会把不燃烧的亡灵（凋灵这类 {@code aiStep} 根本不调用的）也算进去；</li>
 *   <li>注入这里 → 「会不会烧」完全交给原版，子类调用了才记，天然准确。</li>
 * </ul>
 *
 * <h3>为什么要宽限期（grace）</h3>
 * <p>{@code isSunBurnTick} 里有个随机因子：
 * {@code random.nextFloat() * 30 < (light - 0.4) * 2}，光照拉满时也只有 <b>约 4%</b> 的 tick 返回 true。
 * 如果直接拿返回值当「现在在阳光下」，每秒一次的扣血会经常正好落在 false 上而漏掉。
 * 所以一旦返回 true 就记 60 tick（3 秒）宽限，期间都算在燃烧 —— 这个时长比原版「点燃 8 秒」还短，
 * 玩家离开阳光后最多多掉 3% 血。</p>
 */
@Mixin(Mob.class)
public abstract class MixinMobSunBurn implements SunBurnAccess {

    /** 宽限期剩余 tick。每 tick 由 {@code kunlun$tickSunBurn()} 衰减。 */
    @Unique
    private int kunlun$sunBurnGrace;

    @Inject(method = "isSunBurnTick", at = @At("RETURN"))
    private void kunlun$markSunBurn(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            this.kunlun$sunBurnGrace = 60;
        }
    }

    @Override
    public boolean kunlun$isSunBurning() {
        return this.kunlun$sunBurnGrace > 0;
    }

    @Override
    public void kunlun$tickSunBurn() {
        if (this.kunlun$sunBurnGrace > 0) {
            this.kunlun$sunBurnGrace--;
        }
    }
}
