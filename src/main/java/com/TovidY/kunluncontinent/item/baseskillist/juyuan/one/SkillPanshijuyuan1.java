package com.TovidY.kunluncontinent.item.baseskillist.juyuan.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillPanshijuyuan1 extends BaseSkillItem {

    public SkillPanshijuyuan1() {
        super();
    }

    @Override
    public int getCastTime() { return 60; }

    @Override
    public float getBaseCost() {
        return 95;
    }

    @Override
    public int getCooldownTicks() { return 200; }

    @Override
    public float getDamageMultiplier() { return 1.45f; }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.one.description";
    }

    @Override
    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            float finalCost = 95f * costMultiplier;
            cap.setJingshenli(Math.max(0, currentJs - finalCost));
        });
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            double range = 5.0 + Math.log10(powerMultiplier) * 2.0;

            float finalDamage = 15.0f * getDamageMultiplier() * powerMultiplier;

            List<Entity> targets = level.getEntities(player, player.getBoundingBox().inflate(range));

            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                fx.budget(2600);
            }

            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget && target != player) {
                    livingTarget.hurt(level.damageSources().playerAttack(player), finalDamage);
                    int slowLevel = (int) (1 + (powerMultiplier / 10));
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, Math.min(slowLevel, 5)));
                    double pushPower = 0.5 + (powerMultiplier * 0.05);
                    livingTarget.push(0, Math.min(pushPower, 3.0), 0);
                    livingTarget.hurtMarked = true;

                    if (fx != null) {
                        Vec3 hp = livingTarget.position().add(0, livingTarget.getBbHeight() * 0.4, 0);
                        fx.sphere(ParticleTypes.SOUL_FIRE_FLAME, hp, 0.9, 14, 1.0, 0.04);
                        fx.burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), hp, 12, 0.4, true);
                    }
                }
            }

            // ---- 特效：震地 —— 地盘裂阵 + 多重冲击环 + 升腾尘柱 ----
            if (fx != null) {
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.25;

                // 地盘裂阵：不规则十二边形 + 内层星芒 + 边缘断环
                fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.12, 0), ParticleFx.Axis.Y, range, 12, rot, 0.22);
                fx.star(ParticleTypes.LARGE_SMOKE, base.add(0, 0.18, 0), ParticleFx.Axis.Y, range * 0.8, range * 0.3, 8, -rot, 0.1);
                fx.dashedRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.25, 0), ParticleFx.Axis.Y, range * 1.05, 14, 0.55, rot * 1.4, 0.12);
                // 由内向外三重冲击环
                for (int r = 1; r <= 3; r++) {
                    fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.06 * r, 0), range * r / 3.0, 2.0, 40);
                }
                // 岩屑喷发 + 尘柱
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        base, new Vec3(0, 1, 0), 3.5, range * 0.55, 16, rot, 6);
                fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.WHITE_ASH, base, 0.8, 5.0, 4, rot, ParticleFx.TAU, 0.4);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.4, 0), 40, 1.2, true);
                fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 0.6, 0), 30, range * 0.5, 0.12);
            }

            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY(), player.getZ(),
                    (int) powerMultiplier, 0.5, 0.5, 0.5, 0.0);

            float volume = Math.min(1.0f + powerMultiplier * 0.1f, 5.0f);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, volume, 0.5f);
            level.playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, volume * 1.5f, 0.5f);
        }
    }
}
