package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.two;

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
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SkillLeijinhu2 extends BaseSkillItem {
    public SkillLeijinhu2() {
        super();
    }

    @Override
    public int getCastTime() {
        return 10;
    }

    @Override
    public int getCooldownTicks() {
        return 200;
    }

    @Override
    public float getDamageMultiplier() {
        return 1.0f; // 虎啸主要提供控制，伤害较低
    }

    @Override
    public float getBaseCost() {
        return 130f; // 精神力消耗略高
    }

    @Override
    public String getDescriptionKey() {
        return "skill.liejinhu.two.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            double maxRange = 20.0;
            for (int i = 0; i < 3; i++) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                        player.getX(), player.getY() + 1.2, player.getZ(),
                        1, 0, 0, 0, 0);
            }
            for (int r = 1; r <= 5; r++) {
                for (int d = 0; d < 360; d += 20) {
                    double rad = Math.toRadians(d);
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            player.getX() + Math.cos(rad) * r,
                            player.getY() + 0.5,
                            player.getZ() + Math.sin(rad) * r,
                            1, 0, 0.1, 0, 0.02);
                }
            }
            AABB area = player.getBoundingBox().inflate(maxRange);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive() && e.distanceTo(player) <= maxRange);
            for (LivingEntity target : targets) {
                double distance = target.distanceTo(player);
                if (distance <= 5.0) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0)); // 缓慢1级
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0)); // 虚弱
                    target.hurt(player.damageSources().mobAttack(player), 4.0f * powerMultiplier);
                }
                else if (distance <= 10.0) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2)); // 缓慢3级
                }
                else {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0)); // 缓慢1级
                }
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        target.getX(), target.getY() + 2.0, target.getZ(),
                        3, 0.2, 0.2, 0.2, 0);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 0.5f);

            player.displayClientMessage(Component.literal("§e§l第二魂技：虎啸！"), true);
        }
    }
}
