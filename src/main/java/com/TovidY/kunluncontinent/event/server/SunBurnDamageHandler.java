package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 阳光灼烧改成「按最大生命值百分比掉血」。
 *
 * <h3>为什么要改</h3>
 * <p>原版亡灵站在太阳下每秒只掉 1 点血，而昆仑大陆的怪血量动辄几十万上千万 ——
 * 太阳等于没有，白天刷出来的一堆亡灵既不死也不消失，白白吃 tick。</p>
 * <p>现在：<b>被阳光灼烧时每秒扣 1% 最大生命值</b>，一条命大约 100 秒清空（无视防御），
 * 不管血量堆多高，太阳都还是那个能清场的东西。</p>
 *
 * <h3>为什么用 {@code magic} 而不是 {@code onFire}</h3>
 * <ul>
 *   <li>{@code onFire} 走护甲减免，高防御的怪 1% 会被削成 0.01%，太阳又废了；</li>
 *   <li>{@code magic} 带 {@code bypasses_armor} 标签，是不吃防御的（这也是唯一没有副作用的真伤类型）；</li>
 *   <li>类型不是火焰 → <b>完全不碰</b>「火焰伤害 / 火焰免疫 / 抗火」那套逻辑，玩家自己点火的伤害一点没变。</li>
 * </ul>
 *
 * <p>⚠️ 抗性提升（{@code RESISTANCE}）仍然会减免，因为 {@code magic} 不在 {@code bypasses_resistance} 里。
 * 想彻底无视只能自建一个 DamageType，代价是要走 datagen 注册，暂不做。</p>
 *
 * <h3>判定来源</h3>
 * <p>「是否在阳光下燃烧」完全交给原版 —— 见 {@code MixinMobSunBurn}，它只在
 * {@code Mob#isSunBurnTick()} 返回 true 时记一笔。这样凋灵这类「亡灵但不怕太阳」的生物不会被误伤。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SunBurnDamageHandler {

    /** 每秒扣掉的比例（1%）。 */
    private static final float PERCENT_PER_SECOND = 0.01f;

    /** 结算间隔：20 tick = 1 秒。 */
    private static final int INTERVAL_TICKS = 20;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (mob.level().isClientSide() || !mob.isAlive()) {
            return;
        }
        if (!(mob instanceof SunBurnAccess access)) {
            return;
        }

        // 每 tick 衰减宽限期（很轻，只是个减法）
        access.kunlun$tickSunBurn();

        if (mob.tickCount % INTERVAL_TICKS != 0) {
            return;
        }
        if (!access.kunlun$isSunBurning()) {
            return;
        }

        float damage = mob.getMaxHealth() * PERCENT_PER_SECOND;
        if (damage <= 0f) {
            return;
        }

        // 清掉受击无敌帧，否则同一次 1% 可能被上一次伤害的 10 tick 无敌挡掉
        mob.invulnerableTime = 0;
        mob.hurt(mob.damageSources().magic(), damage);
    }
}
