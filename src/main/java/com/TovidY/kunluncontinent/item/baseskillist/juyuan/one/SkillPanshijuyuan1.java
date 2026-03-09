package com.TovidY.kunluncontinent.item.baseskillist.juyuan.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.ChatFormatting;
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
    public int getCooldownTicks() { return 300; }

    @Override
    public float getDamageMultiplier() { return 1.8f; }

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

            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget && target != player) {
                    livingTarget.hurt(level.damageSources().playerAttack(player), finalDamage);
                    int slowLevel = (int) (1 + (powerMultiplier / 10));
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, Math.min(slowLevel, 5)));
                    double pushPower = 0.5 + (powerMultiplier * 0.05);
                    livingTarget.push(0, Math.min(pushPower, 3.0), 0);
                    livingTarget.hurtMarked = true;
                }
            }

            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY(), player.getZ(),
                    (int) powerMultiplier, 0.5, 0.5, 0.5, 0.0);

            int particleDensity = (int) (20 * Math.sqrt(powerMultiplier));
            for (int i = 0; i < particleDensity; i++) {
                double angle = i * Math.PI * 2 / particleDensity;
                double dx = Math.cos(angle) * range;
                double dz = Math.sin(angle) * range;
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        player.getX() + dx, player.getY(), player.getZ() + dz,
                        2, 0.1, 0.1, 0.1, 0.02);
            }

            float volume = Math.min(1.0f + powerMultiplier * 0.1f, 5.0f);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, volume, 0.5f);
            level.playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, volume * 1.5f, 0.5f);
        }
    }
}