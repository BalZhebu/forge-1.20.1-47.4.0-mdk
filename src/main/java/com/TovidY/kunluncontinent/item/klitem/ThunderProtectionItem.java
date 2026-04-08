package com.TovidY.kunluncontinent.item.klitem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ThunderProtectionItem extends Item {

    public ThunderProtectionItem(Properties properties, int level) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int maxDurability = stack.getMaxDamage();
        int currentDurability = maxDurability - stack.getDamageValue();
        ChatFormatting durabilityColor = ChatFormatting.GREEN;
        if (currentDurability < maxDurability * 0.5) durabilityColor = ChatFormatting.YELLOW;
        if (currentDurability < maxDurability * 0.2) durabilityColor = ChatFormatting.RED;

        tooltip.add(Component.literal("能量余量：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(currentDurability + " / " + maxDurability).withStyle(durabilityColor)));
        int totalSeconds = currentDurability * 5;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeStr = minutes > 0 ? minutes + " 分 " + seconds + " 秒" : seconds + " 秒";
        tooltip.add(Component.literal("预计护身时长：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(timeStr).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.empty());
        if (currentDurability <= 0) {
            tooltip.add(Component.literal("!!! 法宝已损坏，失去避雷效果 !!!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        } else if (currentDurability < 10) {
            tooltip.add(Component.literal("!!! 能量即将耗尽，请尽快补充 !!!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}