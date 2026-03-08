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
    public int getCastTime() {
        return 60; // 3秒吟唱 (20 ticks * 3)
    }

    @Override
    public int getCooldownTicks() {
        return 300; // 15秒冷却，强力控制技能建议冷却稍长
    }

    @Override
    public float getDamageMultiplier() {
        return 3.5f; // 重击效果，给予极高的伤害倍率
    }

    @Override
    public Component getSkillDescription() {
        return Component.translatable("skill.panshijuyuan.one.description").withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void applyPenalty(Player player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            cap.setJingshenli(Math.max(0, currentJs - 95f));
        });
    }

    @Override
    public void executeEffect(Level level, Player player) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            double range = 5.0;

            List<Entity> targets = level.getEntities(player, player.getBoundingBox().inflate(range));

            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget && target != player) {
                    float damage = 15.0f * getDamageMultiplier();
                    livingTarget.hurt(level.damageSources().playerAttack(player), damage);

                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));

                    livingTarget.push(0, 0.5, 0);
                }
            }

            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY(), player.getZ(),
                    1, 0, 0, 0, 0);

            for (int i = 0; i < 20; i++) {
                double angle = i * Math.PI * 2 / 20;
                double dx = Math.cos(angle) * 3.0;
                double dz = Math.sin(angle) * 3.0;
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        player.getX() + dx, player.getY(), player.getZ() + dz,
                        2, 0.1, 0.1, 0.1, 0.02);
            }

            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}