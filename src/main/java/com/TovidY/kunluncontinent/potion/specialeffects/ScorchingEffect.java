package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

//燃烧debuff
public class ScorchingEffect extends MobEffect {
    public ScorchingEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFAA00);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        //每秒造成伤害效果
        float damage = entity.getMaxHealth() * 0.02f;

        entity.hurt(entity.damageSources().magic(), damage);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {

        return duration % 20 == 0;
    }
}