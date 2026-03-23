package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

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

public class SkillPohun7 extends BaseSkillItem {
    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.7.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.addEffect(new MobEffectInstance(ModEffects.POHUNQIANG.get(), 800, 0));
            player.displayClientMessage(Component.literal("§c§l第七魂技：破魂枪真身！"), true);
            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + i * 0.2, player.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.5f);
        }
    }
}