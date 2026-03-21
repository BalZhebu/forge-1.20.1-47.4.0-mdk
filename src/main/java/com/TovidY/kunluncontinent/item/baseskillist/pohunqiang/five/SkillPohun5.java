package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.five;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SkillPohun5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 350f; }
    @Override public float getDamageMultiplier() { return 1.88f; }
    @Override public int getCastTime() { return 60; }
    @Override public int getCooldownTicks() { return 840; }
    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.five.description";
    }
    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§6§l第五魂技：碎星！"), true);
            CompoundTag nbt = player.getPersistentData();
            nbt.putInt("SuiXingTimer", 100);
            nbt.putFloat("SuiXingDamage", finalDamage);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}