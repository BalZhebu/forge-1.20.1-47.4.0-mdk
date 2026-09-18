package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.five;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class SkillLeijinhu5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 290f; }
    @Override public float getDamageMultiplier() { return 2.6f; }
    @Override public int getCastTime() { return 20; }
    @Override public int getCooldownTicks() { return 700; }

    @Override
    public String getDescriptionKey() { return "skill.leijinhu.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0),
                            e -> e != player && e.isAlive() && player.distanceTo(e) <= 5.0)
                    .stream().min(Comparator.comparingDouble(player::distanceTo)).orElse(null);
            if (target != null) {
                player.displayClientMessage(Component.literal("§e§l第五魂技：扑杀！"), true);

                ParticleFx fx = ParticleFx.of(level, player);
                double rot = serverLevel.getGameTime() * 0.6;
                Vec3 hitPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                if (fx != null) {
                    // 扑击轨迹：从自身指向目标的掠影弧线
                    Vec3 from = player.getEyePosition();
                    Vec3 to = hitPos;
                    fx.budget(1400);
                    fx.line(ParticleTypes.SOUL_FIRE_FLAME, from, to, 0.35, 0.06, 0.0, Vec3.ZERO);
                    fx.burst(ParticleTypes.GLOW, from, 18, 0.6, true);
                }

                if (level.random.nextFloat() < 0.01f) {
                    player.sendSystemMessage(Component.literal("§c§l[触发斩杀]"));
                    DamageSource killSource = player.damageSources().playerAttack(player);
                    target.hurt(killSource, Float.MAX_VALUE);
                    if (target.isAlive()) {
                        target.setHealth(0f);
                        target.die(killSource);
                    }
                    if (fx != null) {
                        // 斩杀：以目标为中心炸开金色爪阵
                        fx.dot(ParticleTypes.FLASH, hitPos);
                        fx.sphereBlades(ParticleTypes.END_ROD, ParticleTypes.CRIT, hitPos, 1.1, 14, 1.0, rot);
                        fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, hitPos, 3.0, 2.0, 32);
                        fx.bloom(ParticleTypes.WAX_ON, hitPos, 24, 0.6, 0.2);
                    }
                }
                else {
                    target.hurt(player.damageSources().playerAttack(player), finalDamage);
                    if (fx != null) {
                        // 常规扑杀：目标身上留下斜向撕裂爪痕
                        Vec3 to = hitPos.subtract(player.position()).normalize();
                        Vec3 side = ParticleFx.ortho(to);
                        fx.crossSlash(ParticleTypes.CRIT, hitPos, to, side, 1.2, 170, 3);
                        fx.burst(ParticleTypes.CRIT, hitPos, 16, 0.4, true);
                    }
                }
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 3, 0.1, 0.1, 0.1, 0);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
        }
    }
}
