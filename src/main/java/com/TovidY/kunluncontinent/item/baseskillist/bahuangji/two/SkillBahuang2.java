package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.two;

import com.TovidY.kunluncontinent.effect.ParticleFx;
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
        return 120;
    }

    @Override
    public float getDamageMultiplier() {
        return 1.6f;
    }

    @Override
    public float getBaseCost() {
        return 120f; // 基础精神力消耗
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

            // ---- 特效：前方 120° 的横扫 —— 多层月牙剑气 + 扇面冲击波 + 碎裂地面 ----
            ParticleFx fx = ParticleFx.of(level, player);
            Vec3 origin = player.getEyePosition().add(0, -0.4, 0);
            Vec3 up = new Vec3(0, 1, 0);
            double rot = serverLevel.getGameTime() * 0.5;
            if (fx != null) {
                fx.budget(1800);
                // 由内向外三道逐渐张开的月牙剑气
                for (int i = 0; i < 3; i++) {
                    double r = radius * (0.6 + i * 0.2);
                    fx.slash(ParticleTypes.SWEEP_ATTACK, origin, lookVec, up, r, angleRange, 3, rot + i * 0.35);
                }
                // 扇面边缘的两条亮线（勾勒斩击范围）
                Vec3 left = ParticleFx.rotate(lookVec, up, Math.toRadians(angleRange / 2.0));
                Vec3 right = ParticleFx.rotate(lookVec, up, -Math.toRadians(angleRange / 2.0));
                fx.line(ParticleTypes.END_ROD, origin, origin.add(left.scale(radius)), 0.35, 0.05, 0.0, Vec3.ZERO);
                fx.line(ParticleTypes.END_ROD, origin, origin.add(right.scale(radius)), 0.35, 0.05, 0.0, Vec3.ZERO);
                // 贴地扇形尘浪（自定义高度仅 0.1，贴地扩散）
                fx.wave(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.position(), lookVec, ParticleFx.ortho(lookVec),
                        radius, radius * 0.55, 0.5, rot, 34, 0.1);
                fx.burst(ParticleTypes.CRIT, origin, 28, 0.85, true);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, player.position().add(0, 0.05, 0), radius * 0.9, 1.5, 40);
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
                    if (fx != null) {
                        // 命中者被斩痕劈开
                        Vec3 hitPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                        Vec3 axis = up.cross(toTarget).normalize();
                        fx.slash(ParticleTypes.CRIT, hitPos, toTarget, axis, 1.2, 160, 2, rot);
                        fx.burst(ParticleTypes.LARGE_SMOKE, hitPos, 12, 0.3, true);
                    }
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);

            player.displayClientMessage(Component.literal("§6§l第二魂技：横扫！"), true);
        }
    }
}
