package com.TovidY.kunluncontinent.godclass.buff;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 神位被动的<b>事件调度器</b>。
 *
 * <p>把 {@link GodBuffs#ALL_PASSIVES} 里的被动挂到Forge 事件上。
 * 新增神位被动只要在 {@code GodBuffs.ALL_PASSIVES} 加一行，这里不用动。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class GodBuffEventHandler {

    /**
     * 伤害事件：天使神加伤、罗刹神吸血走这里。
     *
     * <p>⚠️ <b>必须是 LOWEST</b>（排在所有监听器之后）。原因：
     * {@code CombatEventHandler#onLivingHurt} 是 {@code HIGHEST}，它会用
     * {@code (攻击 + amount) × 防御系数} <b>整体重算</b>并 {@code setAmount(...)}。
     * 如果这里用默认优先级（NORMAL），buff 的加伤会先跑、然后被那边直接覆盖 ——
     * 表现就是「天使神的 +1000% 对亡灵从来没生效过」。
     * 放在 LOWEST 才能乘在<b>最终伤害</b>上，且各 buff 之间仍然是纯乘算、与顺序无关。</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        for (GodBuff buff : GodBuffs.ALL_PASSIVES) {
            float newAmount = buff.onLivingHurt(event);
            if (!Float.isNaN(newAmount)) {
                event.setAmount(newAmount);
            }
        }
    }

    /** 换攻击目标：海神解除敌意走这里。 */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        for (GodBuff buff : GodBuffs.ALL_PASSIVES) {
            buff.onTargeting(event);
        }
    }

    /** 维度 tick：修罗神挂"远离"Goal 走这里。 */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        for (GodBuff buff : GodBuffs.ALL_PASSIVES) {
            buff.onMobTick(event);
        }
    }
}
