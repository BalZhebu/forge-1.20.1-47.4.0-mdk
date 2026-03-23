package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.eight;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkillBahuang8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 800f; }
    @Override public float getDamageMultiplier() { return 3.8f; }
    @Override public int getCastTime() { return 20; }
    @Override public int getCooldownTicks() { return 1600; } // 55秒

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§4§l第八魂技：降雷！"), true);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50.0),
                    e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                for (double h = 0; h < 20; h += 2) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + h, target.getZ(), 10, 0.1, 0.5, 0.1, 0.2);
                }
                serverLevel.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY() + 1, target.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
                serverLevel.sendParticles(ParticleTypes.GLOW_SQUID_INK, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 3.0f, 0.8f);
        }
    }
}
