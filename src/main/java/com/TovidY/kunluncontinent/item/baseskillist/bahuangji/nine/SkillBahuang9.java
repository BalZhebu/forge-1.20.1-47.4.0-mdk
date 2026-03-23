package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.nine;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

// Skill 类只负责开启任务
public class SkillBahuang9 extends BaseSkillItem {
    @Override public float getBaseCost() { return 850f; }
    @Override
    public float getDamageMultiplier() {
        return 0;
    }
    @Override
    public int getCastTime() {
        return 0;
    }

    @Override public int getCooldownTicks() { return 1400; } // 65秒
    @Override public String getDescriptionKey() { return "skill.bahuangji.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("Bahuang9_Active", true);
            nbt.putInt("Bahuang9_Remaining", 10);
            nbt.putInt("Bahuang9_Timer", 0);
            nbt.putFloat("Bahuang9_Damage", finalDamage * 1.5f);
            player.displayClientMessage(Component.literal("§6§l第九魂技：八荒寂灭！"), true);
        }
    }
}
