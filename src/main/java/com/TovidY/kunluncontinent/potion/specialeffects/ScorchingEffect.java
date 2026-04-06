package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

//燃烧debuff
public class ScorchingEffect extends MobEffect {
    public ScorchingEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFAA00);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            float damage = entity.getMaxHealth() * 0.02f;
            LivingEntity lastAttacker = entity.getKillCredit();
            if (lastAttacker == null) {
                lastAttacker = entity.getLastAttacker();
            }
            DamageSource source;
            if (lastAttacker instanceof Player player) {
                source = entity.damageSources().playerAttack(player);
            } else if (lastAttacker != null) {
                source = entity.damageSources().mobAttack(lastAttacker);
            } else {
                source = entity.damageSources().magic();
            }
            entity.hurt(source, damage);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}