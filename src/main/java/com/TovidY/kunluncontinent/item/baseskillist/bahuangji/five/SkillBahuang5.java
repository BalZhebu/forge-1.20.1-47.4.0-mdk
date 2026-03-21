package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.five;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.Blocks;

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
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, e.getX(), e.getY() + 3.0, e.getZ(), 5, 0.2, 0.5, 0.2, 0.05);
            }
            List<LivingEntity> damageTargets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0), e -> e != player && e.isAlive());
            for (LivingEntity target : damageTargets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.push(0, 0.5, 0);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                        target.getX(), target.getY(), target.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
            }
            for (int i = 0; i < 360; i += 15) {
                double rad = Math.toRadians(i);
                for (double dist = 1.0; dist <= 10.0; dist += 2.0) {
                    double px = player.getX() + Math.cos(rad) * dist;
                    double pz = player.getZ() + Math.sin(rad) * dist;
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, px, player.getY() + 0.1, pz, 1, 0, 0.1, 0, 0.02);
                    if (dist > 5.0 && i % 30 == 0) {
                        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, px, player.getY() + 0.1, pz, 1, 0, 0, 0, 0);
                    }
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2f, 0.6f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 0.5f);
        }
    }
}