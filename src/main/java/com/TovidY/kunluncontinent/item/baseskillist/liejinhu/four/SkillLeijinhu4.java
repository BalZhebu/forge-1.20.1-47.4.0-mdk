package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.four;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillLeijinhu4 extends BaseSkillItem {

    @Override public float getBaseCost() { return 210f; }
    @Override public float getDamageMultiplier() { return 2.3f; }
    @Override public int getCastTime() { return 60; }
    @Override public int getCooldownTicks() { return 640; } // 32s

    @Override
    public String getDescriptionKey() {
        return "skill.leijinhu.four.description";
    }


    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos center = player.blockPosition().relative(player.getDirection(), 2);

            // 范围伤害
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(2.0), e -> e != player);
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().mobAttack(player), finalDamage);
            }

            // ---- 特效：地脉炸裂 —— 五格碎金多边形 + 崩塌岩屑锥 + 双重冲击环 ----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 c = new Vec3(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
                double rot = serverLevel.getGameTime() * 0.35;

                fx.budget(2000);
                // 碎裂地纹：不规则多边形 + 星芒碎金
                fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, c.add(0, 0.1, 0), ParticleFx.Axis.Y, 5.0, 7, rot, 0.25);
                fx.star(ParticleTypes.LAVA, c.add(0, 0.15, 0), ParticleFx.Axis.Y, 4.4, 1.6, 6, -rot, 0.08);
                fx.dashedRing(ParticleTypes.CRIT, c.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.0, 10, 0.5, rot * 1.5, 0.12);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, c.add(0, 0.05, 0), 5.5, 2.0, 40);
                // 崩塌岩屑锥（向上喷发）
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        c, new Vec3(0, 1, 0), 4.0, 2.2, 16, rot, 6);
                fx.cone(ParticleTypes.GLOW, c, new Vec3(0, 1, 0), 3.4, 1.6, 12, -rot, 5);
                // 金色爆闪
                fx.burst(ParticleTypes.CRIT, c.add(0, 0.5, 0), 34, 1.2, true);
                fx.dot(ParticleTypes.FLASH, c.add(0, 0.6, 0));
                fx.bloom(ParticleTypes.WAX_ON, c.add(0, 0.8, 0), 20, 0.9, 0.2);
            }

            // 破坏方块逻辑 (3x3x3)
            BlockPos.betweenClosedStream(center.offset(-2, 0, -2), center.offset(2, 1, 2)).forEach(pos -> {
                BlockState state = level.getBlockState(pos);
                // 判定硬度低于 1.5 的方块（泥土、沙子、树叶、木头等）
                if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0 && state.getDestroySpeed(level, pos) <= 1.5f) {
                    level.destroyBlock(pos, true);
                }
            });

            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.getX(), center.getY(), center.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);
            player.displayClientMessage(Component.literal("§e§l第四魂技：碎金！"), true);
        }
    }
}
