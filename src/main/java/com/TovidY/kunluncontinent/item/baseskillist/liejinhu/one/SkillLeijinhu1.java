package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkillLeijinhu1 extends BaseSkillItem {

    public SkillLeijinhu1() {
        super();
    }

    @Override
    public int getCastTime() {
        return 20; // 1秒吟唱 (20 ticks = 1s)
    }

    @Override
    public int getCooldownTicks() {
        return 160; // 8秒冷却
    }

    @Override
    public float getDamageMultiplier() {
        return 1.3f;//倍率
    }

    @Override
    public float getBaseCost() {
        return 100;//精神力消耗
    }

    @Override
    public String getDescriptionKey() {
        return "skill.leijinhu.one.description";
    }

    @Override
    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            float finalCost = 100f * costMultiplier;
            cap.setJingshenli(Math.max(0, currentJs - finalCost));
        });
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            double range = 4.0 + Math.log10(powerMultiplier) * 2.0;
            List<Entity> targets = level.getEntities(player, player.getBoundingBox().inflate(range));
            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget && target != player) {
                    float damage = 10.0f * getDamageMultiplier() * powerMultiplier;
                    livingTarget.hurt(level.damageSources().playerAttack(player), damage);
                    livingTarget.knockback(0.5, player.getX() - target.getX(), player.getZ() - target.getZ());
                }
            }
            for (int i = 0; i < 5; i++) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        player.getX() + (level.random.nextDouble() - 0.5) * 4,
                        player.getY() + 1.0,
                        player.getZ() + (level.random.nextDouble() - 0.5) * 4,
                        1, 0.1, 0.1, 0.1, 0.0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }
}
