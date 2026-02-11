package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ScorchingEffect extends MobEffect {
    public ScorchingEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFAA00); // 建议用橙红色区分灼烧
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 1. 获取最大生命值的 1%
        float damage = entity.getMaxHealth() * 0.02f;

        // 2. 造成真实伤害 (bypassArmor 跳过防御)
        // 1.20.1 推荐使用 damageSources().magic() 或自定义 DamageType
        entity.hurt(entity.damageSources().magic(), damage);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每 20 tick (1秒) 触发一次 applyEffectTick
        return duration % 20 == 0;
    }
}