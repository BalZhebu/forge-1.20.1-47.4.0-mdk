package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.six;

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

public class SkillBahuang6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 500f; }
    @Override public float getDamageMultiplier() { return 3.3f; }
    @Override public int getCastTime() { return 5; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            player.displayClientMessage(Component.literal("§4§l第六魂技：破军·影杀！"), true);
            player.setDeltaMovement(look.x * 5.0, 0.2, look.z * 5.0);
            player.hurtMarked = true;

            // ---- 特效：三十格影杀突进 —— 缠绕暗影的螺旋轨迹 + 每隔五格一道爆裂环 ----
            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.6;
            if (fx != null) {
                Vec3 origin = player.position().add(0, 1.0, 0);
                Vec3 end = origin.add(look.scale(30.0));

                fx.budget(2600);
                // 突进主轴：暗影 + 灵魂双线
                fx.line(ParticleTypes.SOUL, origin, end, 0.6, 0.12, 0.0, Vec3.ZERO);
                fx.line(ParticleTypes.SQUID_INK, origin, end, 1.1, 0.35, 0.0, Vec3.ZERO);
                // 三股螺旋缠绕（“破军”的绞杀感）
                fx.helixAround(ParticleTypes.SOUL_FIRE_FLAME, origin, look, 0.9, 30.0, 14.0, 3, rot, 110);
                // 每隔五格一道冲击环（对应判定节点）
                for (int i = 0; i < 30; i += 5) {
                    Vec3 c = origin.add(look.scale(i));
                    fx.ringAround(ParticleTypes.SWEEP_ATTACK, c, look, 1.6, 22, rot + i * 0.3, 0.05);
                    fx.ringAround(ParticleTypes.CRIT, c, look, 1.1, 18, -rot - i * 0.3, 0.05);
                }
                // 收招：终点空间撕裂
                fx.sphereBlades(ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME, end, 1.6, 16, 1.6, rot);
                fx.burst(ParticleTypes.DRAGON_BREATH, end, 40, 1.2, true);
            }

            for (int i = 0; i < 30; i++) {
                double dist = i;
                double px = player.getX() + look.x * dist;
                double py = player.getY() + 1.0 + look.y * dist;
                double pz = player.getZ() + look.z * dist;
                if (i % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0, 0, 0, 0);
                    level.playSound(null, px, py, pz, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                AABB hitBox = new AABB(px - 2.5, py - 2.0, pz - 2.5, px + 2.5, py + 2.0, pz + 2.5);
                level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())
                        .forEach(target -> {
                            target.hurt(player.damageSources().playerAttack(player), finalDamage);
                            Vec3 push = target.position().subtract(player.position()).normalize().scale(0.8);
                            target.push(push.x, 0.3, push.z);
                        });
            }
            Vec3 endPos = player.position().add(look.scale(30));
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, endPos.x, endPos.y + 1, endPos.z, 50, 1.0, 1.0, 1.0, 0.1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.5f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }
}
