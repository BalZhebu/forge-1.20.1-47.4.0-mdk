package com.TovidY.kunluncontinent.item.baseskillist.juyuan.nine;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillPanshijuyuan9 extends BaseSkillItem {
    @Override public float getBaseCost() { return 700f; }
    @Override public float getDamageMultiplier() { return 4.5f; }
    @Override public int getCastTime() {return 20;}
    @Override public int getCooldownTicks() { return 1400; }
    @Override public String getDescriptionKey() { return "skill.panshijuyuan.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§6§l第九魂技：不周倾！"), true);
            player.setDeltaMovement(0, 2.5, 0);
            player.hurtMarked = true;
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("BuZhouQing_Active", true);
            nbt.putFloat("BuZhouQing_Damage", finalDamage);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }
}
