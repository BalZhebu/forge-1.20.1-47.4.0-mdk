package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.six;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillLeijinhu6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 450f; }
    @Override public float getDamageMultiplier() { return 0f; } // Buff类技能
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.leijinhu.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            boolean isDay = level.isDay();
            if (isDay) {
                player.addEffect(new MobEffectInstance(ModEffects.SUN_POWER.get(), 400, 1)); // 20秒
                player.displayClientMessage(Component.literal("§e§l第六魂技：啸月（大日之威）！"), true);
                serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 1, 0.5, 0.1);
            } else {
                player.addEffect(new MobEffectInstance(ModEffects.POWER_OF_THE_MOON.get(), 500, 1));
                player.displayClientMessage(Component.literal("§b§l第六魂技：啸月（邀月之华）！"), true);
                serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 1, 0.5, 0.05);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.5f, 1.0f);
        }
    }
}