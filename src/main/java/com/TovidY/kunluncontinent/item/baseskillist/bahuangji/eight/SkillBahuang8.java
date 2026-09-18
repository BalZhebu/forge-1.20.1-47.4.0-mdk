package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.eight;

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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillBahuang8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 800f; }
    @Override public float getDamageMultiplier() { return 3.8f; }
    @Override public int getCastTime() { return 20; }
    @Override public int getCooldownTicks() { return 1600; } // 55秒

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§4§l第八魂技：降雷！"), true);

            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.45;
            if (fx != null) {
                fx.budget(2400);
            }

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50.0),
                    e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);

                if (fx != null) {
                    Vec3 tp = target.position();
                    Vec3 head = tp.add(0, 20.0, 0);
                    // 头顶雷云环（打散环 → 乌云裂隙感）
                    fx.dashedRing(ParticleTypes.CLOUD, target.position().add(0, 20.0, 0), ParticleFx.Axis.Y, 2.4, 9, 0.55, rot, 0.25);
                    fx.dashedRing(ParticleTypes.GLOW, target.position().add(0, 19.4, 0), ParticleFx.Axis.Y, 1.6, 7, 0.5, -rot, 0.12);
                    // 主雷柱：锯齿状闪电
                    fx.lightning(ParticleTypes.ELECTRIC_SPARK, head, tp, 22, 1.6, rot + target.getId());
                    // 分叉雷
                    for (int k = 0; k < 3; k++) {
                        double a = rot + ParticleFx.TAU * k / 3;
                        Vec3 from = head.add(Math.cos(a) * 1.2, 0, Math.sin(a) * 1.2);
                        fx.lightning(ParticleTypes.ELECTRIC_SPARK, from, tp.add(0, 0.5, 0), 16, 1.2, a * 2.3);
                    }
                    // 落点：电环 + 光晕
                    fx.shockRing(ParticleTypes.GLOW, tp.add(0, 0.1, 0), 2.2, 2.0, 30);
                    fx.ringStack(ParticleTypes.ELECTRIC_SPARK, tp.add(0, 0.1, 0), ParticleFx.Axis.Y, 1.6, 3.0, 4, 20, rot, 1.6, 0.7);
                    fx.burst(ParticleTypes.ELECTRIC_SPARK, tp.add(0, 1.0, 0), 26, 0.9, true);
                    fx.dot(ParticleTypes.GLOW_SQUID_INK, tp.add(0, 1.0, 0));
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 3.0f, 0.8f);
        }
    }
}
