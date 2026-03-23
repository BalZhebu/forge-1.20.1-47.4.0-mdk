package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.nine;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class SkillPohun9 extends BaseSkillItem {
    @Override public float getBaseCost() { return 750F; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 25; }
    @Override public int getCooldownTicks() { return 1400; } // 70秒

    @Override
    public String getDescriptionKey() { return "skill.pohunqiang.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            Vec3 end = start.add(look.scale(12));
            player.displayClientMessage(Component.literal("§4§l第九魂技：弑神！"), true);
            AABB targetBox = new AABB(start, end).inflate(1.5);
            LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, targetBox,
                            e -> e != player && e.isAlive() && player.distanceTo(e) <= 12.0)
                    .stream().min(Comparator.comparingDouble(player::distanceTo)).orElse(null);
            for (double d = 0; d < 12; d += 0.5) {
                double px = start.x + look.x * d;
                double py = start.y + look.y * d;
                double pz = start.z + look.z * d;
                serverLevel.sendParticles(ParticleTypes.FLASH, px, py, pz, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.SQUID_INK, px, py, pz, 5, 0.1, 0.1, 0.1, 0.02);
            }

            if (target != null) {
                float baseChance = 0.05f; // 基础 5%
                if (target instanceof Player targetPlayer) {
                    int targetLevel = ModAttributeAPI.getDengji(targetPlayer);
                    float levelBonus = targetLevel * 0.0015f;
                    baseChance = Math.min(0.20f, baseChance + levelBonus); // 最高 20%
                }
                DamageSource killSource = player.damageSources().playerAttack(player);
                if (level.random.nextFloat() < baseChance) {
                    player.sendSystemMessage(Component.literal("§c§l[触发弑神斩杀 - 概率: " + (int)(baseChance * 100) + "%]"));
                    target.hurt(killSource, Float.MAX_VALUE);
                    if (target.isAlive()) {
                        target.setHealth(0f);
                        target.die(killSource);
                    }
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1, target.getZ(), 2, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                } else {
                    target.hurt(killSource, finalDamage);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1)); // 虚弱2
                }
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5, 0.5, 0.5, 0.5);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}