package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.two;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillBahuang2 extends BaseSkillItem {
    public SkillBahuang2() {
        super();
    }

    @Override
    public int getCastTime() {
        return 10;
    }

    @Override
    public int getCooldownTicks() {
        return 140;
    }

    @Override
    public float getDamageMultiplier() {
        return 1.3f;
    }

    @Override
    public float getBaseCost() {
        return 100f; // 基础精神力消耗
    }

    @Override
    public String getDescriptionKey() {
        return "skill.bahuangji.two.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            float radius = 5.0f; // 扇形半径
            float angleRange = 120.0f; // 扇形角度（前方120度）
            Vec3 lookVec = player.getLookAngle();
            for (float i = -angleRange / 2; i <= angleRange / 2; i += 5f) {
                Vec3 particleVec = lookVec.yRot((float) Math.toRadians(i)).scale(radius);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        player.getX() + particleVec.x,
                        player.getY() + 1.2,
                        player.getZ() + particleVec.z,
                        1, 0.1, 0.1, 0.1, 0.0);
                if (i % 20 == 0) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            player.getX() + particleVec.x, player.getY() + 1.0, player.getZ() + particleVec.z,
                            3, 0.2, 0.2, 0.2, 0.1);
                }
            }
            AABB scanArea = player.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, scanArea,
                    e -> e != player && e.isAlive() && e.distanceTo(player) <= radius);

            float finalDamage = 12.0f * getDamageMultiplier() * powerMultiplier;

            for (LivingEntity target : targets) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                double dotProduct = lookVec.dot(toTarget);
                double angleDeg = Math.toDegrees(Math.acos(dotProduct));
                if (angleDeg <= angleRange / 2) {
                    target.hurt(player.damageSources().mobAttack(player), finalDamage);
                    target.knockback(0.8, -toTarget.x, -toTarget.z);
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                            target.getX(), target.getY() + 1, target.getZ(),
                            5, 0.1, 0.1, 0.1, 0.05);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);

            player.displayClientMessage(Component.literal("§6§l第二魂技：横扫！"), true);
        }
    }
}
