package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.treen;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
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

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.0), target -> {
                if (target == player) return false;
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                return toTarget.dot(view) > 0.6; // 扇形判定
            });
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().mobAttack(player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
            }
            for (int i = 0; i < 5; i++) {
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        player.getX() + view.x * 1.5, player.getY() + 1.2, player.getZ() + view.z * 1.5,
                        10, 0.5, 0.5, 0.5, 0.2);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.5f);
            player.displayClientMessage(Component.literal("§e§l第三魂技：裂石！"), true);
        }
    }
}
