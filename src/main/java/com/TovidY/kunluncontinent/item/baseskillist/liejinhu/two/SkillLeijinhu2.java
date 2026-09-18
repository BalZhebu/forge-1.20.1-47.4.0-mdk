package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.two;

import com.TovidY.kunluncontinent.effect.ParticleFx;
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
import net.minecraft.world.phys.Vec3;

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
        return 100;
    }

    @Override
    public float getDamageMultiplier() {
        return 0.4f; // 虎啸主要提供控制，伤害较低
    }

    @Override
    public float getBaseCost() {
        return 95f; // 精神力消耗略高
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

            // ---- 特效：虎啸冲击波 —— 五重贴地声浪环 + 前方扇形气浪 + 咆哮冲击面 ----
            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.4;
            if (fx != null) {
                Vec3 base = player.position();
                Vec3 look = player.getLookAngle();
                Vec3 up = new Vec3(0, 1, 0);

                fx.budget(2000);
                // 五重由内向外扩散的声浪环（打散环 → 空气被撕开的断续感）
                for (int r = 1; r <= 5; r++) {
                    fx.dashedRing(ParticleTypes.CLOUD, base.add(0, 0.3 + r * 0.1, 0), ParticleFx.Axis.Y,
                            r * 1.0, 14 + r * 2, 0.62, rot + r * 0.45, 0.06);
                }
                // 前方扇形气浪（自定义高度为 0.35，贴地推进）
                fx.wave(ParticleTypes.CLOUD, base.add(0, 0.35, 0), look, ParticleFx.ortho(look),
                        maxRange * 0.6, maxRange * 0.42, 0.55, rot, 40, 0.0);
                // 咆哮声锥：从口部向外张开的锥面
                fx.cone(ParticleTypes.GLOW, player.getEyePosition(), look, 8.0, 3.2, 14, rot, 6);
                // 冲击面：一道贴着视线方向的横向撕口
                fx.line(ParticleTypes.CLOUD, player.getEyePosition().add(ParticleFx.ortho(look).scale(-4)),
                        player.getEyePosition().add(ParticleFx.ortho(look).scale(4)), 0.4, 0.2, 0.0, Vec3.ZERO);
                fx.burst(ParticleTypes.GLOW, player.getEyePosition(), 30, 1.1, true);
                fx.dot(ParticleTypes.FLASH, player.getEyePosition().add(look.scale(1.0)));
                fx.bloom(ParticleTypes.WAX_ON, player.getEyePosition().add(look.scale(1.2)), 18, 0.7, 0.2);
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
                if (fx != null) {
                    // 被声浪击中者头顶炸开的金色气团
                    Vec3 tp = target.position().add(0, target.getBbHeight() + 0.4, 0);
                    fx.sphere(ParticleTypes.GLOW, tp, 0.9, 16, 1.0, 0.04);
                    fx.dashedRing(ParticleTypes.ELECTRIC_SPARK, tp, ParticleFx.Axis.Y, 0.8, 6, 0.5, -rot, 0.05);
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 0.5f);

            player.displayClientMessage(Component.literal("§e§l第二魂技：虎啸！"), true);
        }
    }
}
