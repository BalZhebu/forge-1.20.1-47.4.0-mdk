package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.eight;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillLeijinhu8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 750f; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() { return "skill.leijinhu.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("Liejinhu8_Active", true);
            nbt.putInt("Liejinhu8_Remaining", 10);
            nbt.putInt("Liejinhu8_Timer", 0);
            nbt.putFloat("Liejinhu8_Damage", finalDamage);

            player.displayClientMessage(Component.literal("§e§l第八魂技：裂天！"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 1.2f);
        }
    }
}