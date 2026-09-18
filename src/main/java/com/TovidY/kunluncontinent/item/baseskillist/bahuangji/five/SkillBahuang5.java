package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.five;

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

public class SkillBahuang5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 350f; }
    @Override public float getDamageMultiplier() { return 3.0f; }
    @Override public int getCastTime() { return 20; }
    @Override public int getCooldownTicks() { return 800; }

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§4§l第五魂技：镇岳！"), true);

            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.3;

            List<LivingEntity> slowTargets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(30.0), e -> e != player && e.isAlive());
            for (LivingEntity e : slowTargets) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                if (fx != null) {
                    // 被镇压者头顶落下一道封镇光柱
                    Vec3 tp = e.position();
                    fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.SOUL_FIRE_FLAME,
                            tp, 0.5, 3.0, 3, rot + e.getId() * 0.3, ParticleFx.TAU * 0.9, 0.4);
                    fx.dashedRing(ParticleTypes.LAVA, tp.add(0, 0.15, 0), ParticleFx.Axis.Y, 1.2, 6, 0.5, -rot, 0.05);
                }
            }

            List<LivingEntity> damageTargets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0), e -> e != player && e.isAlive());
            for (LivingEntity target : damageTargets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.push(0, 0.5, 0);
                if (fx != null) {
                    Vec3 tp = target.position().add(0, target.getBbHeight() * 0.4, 0);
                    // 被“镇”住的碎裂爆发
                    fx.sphere(ParticleTypes.SOUL_FIRE_FLAME, tp, 1.1, 20, 1.0, 0.05);
                    fx.burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                            tp, 18, 0.45, true);
                }
            }

            // ---- 主特效：半径十格的“镇岳”巨型地阵 + 自定义高度光柱环 ----
            if (fx != null) {
                Vec3 base = player.position();
                fx.budget(2800);
                // 十格地阵：双层十六边形（八荒 × 双重）
                fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 10.0, 16, rot, 0.0);
                fx.polygon(ParticleTypes.LAVA, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 7.0, 8, -rot, 0.05);
                fx.magicCircle(ParticleTypes.FLAME, ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 5.0, rot * 1.4);
                // 由内向外铺设的贴地尘浪
                fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), 10.0, 2.5, 52);
                // 自定义高度的光柱环：一圈一圈向上收束成“山岳”轮廓
                fx.ringStack(ParticleTypes.SONIC_BOOM, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 10.0, 8.0, 4, 28, rot, 0.35, 0.6);
                fx.column(ParticleTypes.LAVA, ParticleTypes.SOUL_FIRE_FLAME, base, 0.8, 8.0, 4, rot, ParticleFx.TAU * 1.1, 0.3);
                // 八根地脉光柱
                fx.pillars(ParticleTypes.SOUL_FIRE_FLAME, base, 10.0, 8, 4.0, rot);
                fx.burst(ParticleTypes.FLAME, base.add(0, 0.4, 0), 44, 1.6, true);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.2f, 0.6f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 0.5f);
        }
    }
}
