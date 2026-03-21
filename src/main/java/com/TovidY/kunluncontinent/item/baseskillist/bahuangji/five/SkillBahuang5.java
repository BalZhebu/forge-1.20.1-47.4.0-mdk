package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.five;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkillBahuang5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 350f; }
    @Override public float getDamageMultiplier() { return 3.0f; }
    @Override public int getCastTime() { return 20; }
    @Override public int getCooldownTicks() { return 800; }

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§4§l第五魂技：镇岳！"), true);
            List<LivingEntity> slowTargets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30.0), e -> e != player && e.isAlive());
            for (LivingEntity e : slowTargets) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            }
            List<LivingEntity> damageTargets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0), e -> e != player && e.isAlive());
            for (LivingEntity target : damageTargets) {
                target.hurt(level.damageSources().playerAttack(player), finalDamage);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(), target.getZ(), 5, 0.1, 0.1, 0.1, 0.05);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }
}
