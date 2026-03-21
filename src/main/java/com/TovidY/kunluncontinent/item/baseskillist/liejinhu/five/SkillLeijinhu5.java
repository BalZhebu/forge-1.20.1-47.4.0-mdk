package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.five;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class SkillLeijinhu5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 290f; }
    @Override public float getDamageMultiplier() { return 2.6f; }
    @Override public int getCastTime() { return 20; }
    @Override public int getCooldownTicks() { return 700; }

    @Override
    public String getDescriptionKey() { return "skill.liejinhu.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0),
                            e -> e != player && e.isAlive() && player.distanceTo(e) <= 5.0)
                    .stream().min(Comparator.comparingDouble(player::distanceTo)).orElse(null);
            if (target != null) {
                player.displayClientMessage(Component.literal("§e§l第五魂技：扑杀！"), true);
                if (level.random.nextFloat() < 0.5f) {
                    target.setHealth(0f);
                    player.displayClientMessage(Component.literal("§c§l[触发斩杀]"), true);
                    ((ServerLevel)level).sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                } else {
                    target.hurt(level.damageSources().playerAttack(player), finalDamage);
                }
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 3, 0.1, 0.1, 0.1, 0);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
        }
    }
}
