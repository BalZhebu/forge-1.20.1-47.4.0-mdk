package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 禁锢的「无法攻击」那一半。
 *
 * <h3>为什么用 {@link LivingAttackEvent}</h3>
 * <p>它是<b>伤害真正结算之前</b>的唯一通用闸门：近战 {@code doHurtTarget}、
 * 弹射物命中、多数技能伤害都会经过 {@code LivingEntity#hurt} → 本事件。
 * 在这里取消，既能挡住直接伤害，也连带挡掉附带的击退/点燃。</p>
 *
 * <p>⚠️ 判定的是<b>攻击者</b>（{@code source.getEntity()}）而不是被攻击者 ——
 * 禁锢是「不许出手」，不是「不挨打」。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JinguEventHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity living && living.hasEffect(ModEffects.JINGU.get())) {
            event.setCanceled(true);
        }
    }
}
