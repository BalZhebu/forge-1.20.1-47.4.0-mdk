package com.TovidY.kunluncontinent.godclass.buff;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
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
     * 伤害事件：天使神加伤走这里。
     *
     * <p>⚠️ <b>优先级说明</b>：项目里 {@code CombatEventHandler} 也在改这个事件，
     * 默认优先级相同 → <b>注册顺序决定谁后执行</b>。这里用
     * {@link SubscribeEvent#priority()} 的默认优先级并<b>读取当前 amount 再乘</b>，
     * 所以无论谁先谁后都不会把对方的结果覆盖掉（乘算是可交换的）。</p>
     */
    @SubscribeEvent
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
