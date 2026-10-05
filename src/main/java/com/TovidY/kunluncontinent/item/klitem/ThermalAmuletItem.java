package com.TovidY.kunluncontinent.item.klitem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ThermalAmuletItem extends Item {
    private final boolean isInfinite;

    public ThermalAmuletItem(Properties properties, boolean isInfinite) {
        super(properties);
        this.isInfinite = isInfinite;
    }

    public boolean isInfinite() {
        return isInfinite;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isInfinite) {
            tooltip.add(Component.literal("御寒能量：").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("无限").withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.literal("提示：在雪地中寻找遮蔽处以延长使用时间").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            int remainDurability = stack.getMaxDamage() - stack.getDamageValue();
            int baseMinutes = (remainDurability * 5) / 60;
            int snowMinutes = (remainDurability * 3) / 60;
            tooltip.add(Component.literal("预计可用：").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(baseMinutes + " 分钟").withStyle(ChatFormatting.GREEN)));
            tooltip.add(Component.literal("若在暴雪中暴露：").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(snowMinutes + " 分钟").withStyle(ChatFormatting.RED)));
            tooltip.add(Component.literal("提示：在雪地中寻找遮蔽处以延长使用时间").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, level, tooltip, flag);


        tooltip.add(Component.translatable("tooltip.kunluncontinent.thermal_amulet").withStyle(ChatFormatting.DARK_GRAY));
    }
}