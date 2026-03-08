package com.TovidY.kunluncontinent.item.baseskillist;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public abstract class BaseSkillItem extends Item {
    public BaseSkillItem() {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    public Component getSkillDescription() {
        return Component.translatable("无技能").withStyle(ChatFormatting.GRAY);
    }

    // 预留：吟唱时间 (Ticks)
    public abstract int getCastTime();

    // 预留：冷却时间 (Ticks)
    public abstract int getCooldownTicks();

    public abstract float getDamageMultiplier();

    // 预留：魂力/惩罚消耗
    public abstract void applyPenalty(Player player);

    // 核心：具体技能效果
    public abstract void executeEffect(Level level, Player player);

    // V键触发逻辑
    public void handleRelease(Level level, Player player) {
        if (!player.getCooldowns().isOnCooldown(this)) {
            // 这里可以扩展吟唱逻辑
            executeEffect(level, player);
            applyPenalty(player);
            player.getCooldowns().addCooldown(this, getCooldownTicks());
        }
    }
}