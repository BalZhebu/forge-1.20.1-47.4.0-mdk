package com.TovidY.kunluncontinent.item.baseskillist.juyuan.six;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillPanshijuyuan6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 450f; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 0; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§6§l第六魂技：裂地！"), true);
            player.setDeltaMovement(0, 1.5, 0);
            player.hurtMarked = true;
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("LieDiActive", true);
            nbt.putFloat("LieDiDamage", finalDamage);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }
}