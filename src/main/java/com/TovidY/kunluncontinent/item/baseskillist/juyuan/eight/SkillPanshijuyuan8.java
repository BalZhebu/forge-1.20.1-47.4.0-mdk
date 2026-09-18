package com.TovidY.kunluncontinent.item.baseskillist.juyuan.eight;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SkillPanshijuyuan8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 666f; }
    @Override public float getDamageMultiplier() { return 4.3f; } // 极高爆发
    @Override public int getCastTime() { return 30; } // 蓄力久一点更霸气
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            Vec3 targetPoint = player.position().add(look.scale(3));

            player.displayClientMessage(Component.literal("§6§l第八魂技：憾岳！"), true);

            // ---- 特效：憾岳 —— 前方七格巨坑陷落 + 垂直拍击柱 + 双重冲击环 ----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 ground = new Vec3(targetPoint.x, player.getY(), targetPoint.z);
                double rot = serverLevel.getGameTime() * 0.3;

                fx.budget(2400);
                // 陷落的巨坑轮廓：不规则九边形 + 内层破碎芒阵
                fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, ground.add(0, 0.12, 0), ParticleFx.Axis.Y, 5.0, 9, rot, 0.3);
                fx.star(ParticleTypes.SOUL_FIRE_FLAME, ground.add(0, 0.2, 0), ParticleFx.Axis.Y, 4.4, 1.8, 7, -rot, 0.15);
                fx.dashedRing(ParticleTypes.WHITE_ASH, ground.add(0, 0.28, 0), ParticleFx.Axis.Y, 6.2, 14, 0.5, rot * 1.5, 0.18);
                // 双重冲击环
                fx.shockRing(ParticleTypes.LARGE_SMOKE, ground.add(0, 0.05, 0), 3.5, 2.5, 40);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, ground.add(0, 0.1, 0), 6.5, 2.0, 44);
                // 竖向拍击柱：从天而降的岩柱（自定义高度 6 格）
                Vec3 apex = ground.add(0, 6.0, 0);
                fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.SOUL_FIRE_FLAME, ground, 1.2, 6.0, 4, rot, ParticleFx.TAU, 0.55);
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        apex, new Vec3(0, -1, 0), 6.0, 2.4, 16, rot, 6);
                fx.helixAround(ParticleTypes.SOUL_FIRE_FLAME, apex, new Vec3(0, -1, 0), 1.5, 6.0, 3.0, 2, rot, 40);
                // 崩解岩屑
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        ground, new Vec3(0, 1, 0), 3.0, 4.0, 16, -rot, 6);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, ground.add(0, 0.5, 0), 44, 1.8, true);
                fx.bloom(ParticleTypes.WHITE_ASH, ground.add(0, 1.0, 0), 36, 2.6, 0.2);
            }

            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, targetPoint.x, targetPoint.y, targetPoint.z, 5, 0.5, 0.5, 0.5, 0);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, targetPoint.x, targetPoint.y, targetPoint.z, 3, 0.1, 0.1, 0.1, 0);
            AABB hitArea = new AABB(targetPoint.x - 3.5, targetPoint.y - 2, targetPoint.z - 3.5, targetPoint.x + 3.5, targetPoint.y + 4, targetPoint.z + 3.5);
            level.getEntitiesOfClass(LivingEntity.class, hitArea, e -> e != player && e.isAlive())
                    .forEach(target -> {
                        target.hurt(player.damageSources().playerAttack(player), finalDamage);
                        target.setDeltaMovement(0, -2.0, 0); // 强行拍进地里
                    });

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 0.2f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}
