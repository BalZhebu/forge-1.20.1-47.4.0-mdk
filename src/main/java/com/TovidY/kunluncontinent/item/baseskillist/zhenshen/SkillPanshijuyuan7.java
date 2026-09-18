package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class SkillPanshijuyuan7 extends BaseSkillItem {
    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.7.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.addEffect(new MobEffectInstance(ModEffects.PANSHIJUYUAN.get(), 800, 0));
            player.displayClientMessage(Component.literal("§6§l第七魂技：磐石巨猿真身！"), true);

            // ---- 特效：岩灵真身 —— 巨岩阵 + 八方落地岩柱 + 环绕升腾尘环 ----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.24;

                fx.budget(2200);
                // 巨岩阵：双层多边形 + 芒阵 + 断环
                fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 5.0, 12, rot, 0.15);
                fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.6, 8, -rot, 0.05);
                fx.star(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        base.add(0, 0.25, 0), ParticleFx.Axis.Y, 4.6, 2.0, 8, rot * 1.3, 0.05);
                fx.dashedRing(ParticleTypes.WHITE_ASH, base.add(0, 0.3, 0), ParticleFx.Axis.Y, 5.8, 14, 0.5, -rot * 1.5, 0.1);
                // 八方岩柱
                fx.pillars(ParticleTypes.CAMPFIRE_COSY_SMOKE, base, 5.0, 8, 3.5, rot);
                // 环绕升腾的尘环（自定义高度 5 格）
                fx.ringStack(ParticleTypes.LARGE_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 3.0, 5.0, 4, 26, rot, 0.45, 0.7);
                fx.spiral(ParticleTypes.WHITE_ASH, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.6, 0.8, 6.0, 2.4, -rot, 120, 0.1);
                fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.SOUL_FIRE_FLAME, base, 0.7, 6.0, 4, rot, ParticleFx.TAU * 1.2, 0.4);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.8, 0), 36, 1.3, true);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_DEATH, SoundSource.PLAYERS, 1.5f, 0.5f);
        }
    }
}
