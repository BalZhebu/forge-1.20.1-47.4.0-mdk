package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.treen;

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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillLeijinhu3 extends BaseSkillItem {
    @Override
    public float getBaseCost() { return 140f; }
    @Override
    public float getDamageMultiplier() { return 1.82f; } // 裂石巨额伤害
    @Override
    public int getCastTime() { return 10; }
    @Override
    public int getCooldownTicks() { return 300; } // 15s

    @Override
    public String getDescriptionKey() {
        return "skill.liejinhu.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 view = player.getViewVector(1.0f);

            // ---- 特效：碎金一击 —— 扇形金色爪芒 + 前方崩裂的岩屑锥 ----
            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.55;
            if (fx != null) {
                Vec3 origin = player.getEyePosition().add(0, -0.3, 0);
                Vec3 up = new Vec3(0, 1, 0);

                fx.budget(1500);
                // 前方约 53° 扇形里的三道金色爪芒
                fx.fanBlades(ParticleTypes.CRIT, origin, view, up, 3, 3.0, 46, 3);
                fx.fanBlades(ParticleTypes.GLOW, origin, view, up, 3, 2.7, 46, 1);
                // 岩屑锥：沿视线的碎石喷射
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        origin, view, 3.6, 1.4, 16, rot, 5);
                fx.cone(ParticleTypes.ELECTRIC_SPARK, origin, view, 3.6, 1.2, 12, -rot, 5);
                // 收招爆点
                Vec3 focus = origin.add(view.scale(1.6));
                fx.burst(ParticleTypes.CRIT, focus, 24, 0.8, true);
                fx.dot(ParticleTypes.FLASH, focus);
            }

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.0), target -> {
                if (target == player) return false;
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                return toTarget.dot(view) > 0.6; // 扇形判定
            });
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().mobAttack(player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
                if (fx != null) {
                    // 被打裂的目标身上崩出石屑与金色裂痕
                    Vec3 hp = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    Vec3 to = hp.subtract(player.position()).normalize();
                    fx.crossSlash(ParticleTypes.CRIT, hp, to, new Vec3(0, 1, 0), 1.1, 160, 2);
                    fx.burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), hp, 16, 0.4, true);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.5f);
            player.displayClientMessage(Component.literal("§e§l第三魂技：裂石！"), true);
        }
    }
}
