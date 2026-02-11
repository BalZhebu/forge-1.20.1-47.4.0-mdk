package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class DizzinessEffect extends MobEffect {

    public DizzinessEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8C00); // 橙色
        // 1. 降低移动速度 (适配灾变逻辑：减少 50% 移速)
        // 这里的 UUID 需要唯一，且 Operation 建议用 MULTIPLY_TOTAL 会更稳定
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "57F1BADC-F545-4D89-B218-751C2FF8053D",
                -0.8D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);

        // 2. 降低攻击速度 (让玩家挥不动武器)
        this.addAttributeModifier(Attributes.ATTACK_SPEED,
                "FA233E1C-4180-4865-B01B-BCCE9785ACA3",
                -0.8D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 如果是怪物，强行清除其当前的攻击目标，实现“发呆”效果
        if (entity instanceof Mob mob && mob.getTarget() != null) {
            mob.setTarget(null);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // 每 tick 都执行 applyEffectTick
    }
}