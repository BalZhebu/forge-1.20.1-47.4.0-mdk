package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.treen;

import com.TovidY.kunluncontinent.effect.ParticleFx;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillBahuang3 extends BaseSkillItem {

    @Override
    public float getBaseCost() { return 165f; }

    @Override
    public float getDamageMultiplier() { return 2.85f; }

    @Override
    public int getCastTime() { return 60; }

    @Override
    public int getCooldownTicks() { return 360; } // 18s

    @Override
    public String getDescriptionKey() {
        return "skill.bahuangji.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * 1.6D, 0.9D, look.z * 1.6D);
        player.hurtMarked = true;

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            double pX = player.getX();
            double pY = player.getY();
            double pZ = player.getZ();
            player.displayClientMessage(Component.literal("§6§l第三魂技：八荒·撼世断岳！"), true);
            level.playSound(null, pX, pY, pZ, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.2f, 0.6f);
            level.playSound(null, pX, pY, pZ, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.8f);

            // ---- 起跳阶段：脚下炸开的升腾气流 ----
            ParticleFx rise = ParticleFx.of(level, player);
            if (rise != null) {
                rise.budget(500);
                rise.burst(ParticleTypes.LAVA, player.position(), 24, 0.7, true);
                rise.shockRing(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.position().add(0, 0.1, 0), 2.5, 1.5, 30);
            }

            serverLevel.getServer().tell(new net.minecraft.server.TickTask(serverLevel.getServer().getTickCount() + 7, () -> {
                if (!player.isAlive()) return;

                Vec3 landPos = player.position();
                double lX = landPos.x;
                double lY = landPos.y;
                double lZ = landPos.z;

                double radius = 10.0D;
                AABB impactArea = new AABB(lX - radius, lY - 3, lZ - radius, lX + radius, lY + 5, lZ + radius);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, impactArea,
                        e -> e != player && e.isAlive() && e.distanceToSqr(player) <= radius * radius);

                for (LivingEntity target : targets) {
                    target.hurt(player.damageSources().playerAttack(player), finalDamage);

                    double distX = target.getX() - lX;
                    double distZ = target.getZ() - lZ;
                    double distMag = Math.max(0.1, Math.sqrt(distX * distX + distZ * distZ));

                    target.setDeltaMovement(distX / distMag * 1.2D, 0.6D, distZ / distMag * 1.2D);
                    target.hurtMarked = true;

                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                }

                // ---- 落地特效：三重冲击环 + 不规则裂地多边形 + 尘柱 ----
                ParticleFx fx = ParticleFx.of(level, player);
                if (fx != null) {
                    Vec3 land = new Vec3(lX, lY, lZ);
                    double rot = level.getGameTime() * 0.2;

                    fx.budget(2600);
                    // 三道由内向外扩散的冲击环（半径 3 / 6 / 9，形成“断岳”层次）
                    for (int ring = 1; ring <= 3; ring++) {
                        double rr = ring * 3.0D;
                        fx.dashedRing(ParticleTypes.LARGE_SMOKE, land.add(0, 0.1, 0), ParticleFx.Axis.Y,
                                rr, 12 + ring * 4, 0.72, rot + ring * 0.4, 0.14);
                        fx.circle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                                land.add(0, 0.15, 0), ParticleFx.Axis.Y, rr, 30, -rot - ring * 0.3, 0.2);
                    }
                    // 裂地多边形（不规则的破坏轮廓）
                    fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, land.add(0, 0.2, 0), ParticleFx.Axis.Y, 9.5, 9, rot * 0.7, 0.25);
                    fx.polygon(ParticleTypes.SWEEP_ATTACK, land.add(0, 0.35, 0), ParticleFx.Axis.Y, 6.8, 7, -rot, 0.2);
                    // 中心碎裂尘柱（自定义高度 6 格）
                    fx.pillars(ParticleTypes.CAMPFIRE_COSY_SMOKE, land, 1.2, 6, 6.0, rot);
                    fx.column(ParticleTypes.LAVA, ParticleTypes.SOUL_FIRE_FLAME, land, 0.7, 6.0, 3, rot, ParticleFx.TAU, 0.4);
                    // 雷弧点缀
                    for (int i = 0; i < 6; i++) {
                        double a = rot + ParticleFx.TAU * i / 6;
                        Vec3 from = land.add(Math.cos(a) * 2.5, 0.4, Math.sin(a) * 2.5);
                        fx.lightning(ParticleTypes.ELECTRIC_SPARK, from, land.add(Math.cos(a) * 9.5, 0.1, Math.sin(a) * 9.5), 12, 0.7, a);
                    }
                    fx.burst(ParticleTypes.FLAME, land.add(0, 0.5, 0), 40, 1.4, true);
                }

                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, lX, lY + 0.5, lZ, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, lX, lY + 1.0, lZ, 3, 0.2, 0.2, 0.2, 0.1);

                serverLevel.playSound(null, lX, lY, lZ, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.5f); // 深沉低音爆
                serverLevel.playSound(null, lX, lY, lZ, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.7f);
                serverLevel.playSound(null, lX, lY, lZ, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5f, 0.8f); // 附带雷霆怒吼
                serverLevel.playSound(null, lX, lY, lZ, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0f, 0.5f); // 铁砧轰地金属重音
            }));
        }
    }
}
