package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * 禁锢 —— 圣光锁链 / 圣裁之域附带的控制效果。
 *
 * <h3>「移动速度为 0」怎么实现</h3>
 * <p>用一条 {@code MOVEMENT_SPEED} 的 {@link AttributeModifier.Operation#MULTIPLY_TOTAL}
 * 修饰符，数值 {@code -1.0}：所有加速/减速乘完之后再 ×(1 - 1) = <b>0</b>。
 * 比「把 baseValue 设成 0」稳 —— 那个会被其它修饰符重新抬起来，
 * 而且会破坏实体原本的移速基数。</p>
 *
 * <p>{@link #applyEffectTick} 再把水平速度直接清零：只靠属性挡不住击退、
 * 爆炸推力、以及部分 AI 直接 {@code move()} 的位移。</p>
 *
 * <h3>「无法攻击」在哪拦</h3>
 * <p>不在这里 —— 见 {@code event/server/JinguEventHandler}（拦 {@code LivingAttackEvent}，
 * 攻击者带本效果就取消）。之所以拆开：效果类只管状态，攻击拦截必须走事件。</p>
 */
public class JinguEffect extends MobEffect {

    /** 修饰符 UUID（1.20.1 的 MobEffect 这里是 String 形式），固定值。 */
    private static final String SPEED_MODIFIER_ID = "7a1f4c62-9d0b-4a55-8b2e-6c3d9f0a1b7e";

    public JinguEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFD966);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID,
                -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /** 每 tick 都要跑（水平位移清零）。 */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 只靠属性不够：目标若还挂着别的移速增益（MULTIPLY_TOTAL 是「累加后一起乘」，
        // -1 会被其它正修饰符抵消一部分），所以这里再把「移动意图」也压掉。
        entity.setSprinting(false);
        entity.setShiftKeyDown(false);
        entity.setSpeed(0.0F);
        if (entity instanceof Mob mob) {
            // 停掉寻路，否则它会贴着目标原地蹭
            mob.getNavigation().stop();
        }

        Vec3 motion = entity.getDeltaMovement();
        if (Math.abs(motion.x) > 1.0E-4 || Math.abs(motion.z) > 1.0E-4) {
            // y 保留：掉下去还是要能落地的，锁链不负责悬空
            entity.setDeltaMovement(0.0D, motion.y, 0.0D);
            entity.hurtMarked = true;
        }
    }
}
