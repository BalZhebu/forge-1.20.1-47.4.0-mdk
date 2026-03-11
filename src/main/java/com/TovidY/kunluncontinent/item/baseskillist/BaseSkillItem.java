package com.TovidY.kunluncontinent.item.baseskillist;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class BaseSkillItem extends Item {
    public BaseSkillItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    // --- 核心数值接口：子类必须实现 ---
    public abstract float getBaseCost();         // 精神力基础消耗
    public abstract float getDamageMultiplier(); // 技能本身的伤害倍率
    public abstract int getCastTime();           // 吟唱时间
    public abstract int getCooldownTicks();      // 冷却时间

    public String getDescriptionKey() {
        return this.getDescriptionId() + ".description";
    }

    public Component getDynamicDescription(float costMultiplier) {
        float finalCost = getBaseCost() * costMultiplier;
        return Component.translatable(getDescriptionKey(), String.format("%.1f", finalCost));
    }

    public float getPowerMultiplier(long nianxian) {
        if (nianxian >= 100000000) return 100.0f;
        if (nianxian >= 10000000) return 50.0f;
        if (nianxian >= 1000000) return 15.0f;
        if (nianxian >= 100000) return 8.0f;
        if (nianxian >= 10000) return 4.0f;
        if (nianxian >= 1000) return 2.5f;
        if (nianxian >= 100) return 1.5f;
        return 1.0f;
    }

    public float getCostMultiplier(int nianxian) {
        if (nianxian <= 100) return 1.0f;
        float costFactor = (float) (1.0 + Math.log10(nianxian / 10.0) * 0.4);
        return Math.min(3.5f, costFactor);
    }

    // --- 执行逻辑 ---
    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float finalCost = getBaseCost() * costMultiplier;
            cap.setJingshenli(Math.max(0, cap.getJingshenli() - finalCost));
        });
    }

    public abstract void executeEffect(Level level, Player player, float powerMultiplier);

    public void handleRelease(Level level, Player player, int nianxian) {
        float costMultiplier = getCostMultiplier(nianxian);
        float finalCost = getBaseCost() * costMultiplier;
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getJingshenli() >= finalCost && !player.getCooldowns().isOnCooldown(this)) {
                float powerMultiplier = getPowerMultiplier(nianxian);
                executeEffect(level, player, powerMultiplier);
                applyPenalty(player, costMultiplier); // 扣除消耗
                player.getCooldowns().addCooldown(this, getCooldownTicks());
                SynsAPI.synsPlayerAttribute(player); // 确保同步
            }
        });
    }
}