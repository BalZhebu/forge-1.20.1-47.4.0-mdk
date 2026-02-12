package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

//眩晕debuff
public class DizzinessEffect extends MobEffect {

    public DizzinessEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8C00);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "57F1BADC-F545-4D89-B218-751C2FF8053D",
                -0.8D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED,
                "FA233E1C-4180-4865-B01B-BCCE9785ACA3",
                -0.8D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob && mob.getTarget() != null) {
            mob.setTarget(null);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}