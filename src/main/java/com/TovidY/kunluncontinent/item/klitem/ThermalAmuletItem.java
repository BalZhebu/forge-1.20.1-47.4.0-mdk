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
        } else {
            int remainDurability = stack.getMaxDamage() - stack.getDamageValue();
            int seconds = remainDurability * 5;
            int minutes = seconds / 60;
            int restSeconds = seconds % 60;
            String text = (restSeconds == 0) ? (minutes + " 分钟") : (minutes + " 分 " + restSeconds + " 秒");
            tooltip.add(Component.literal("预计可用：").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(text).withStyle(ChatFormatting.GREEN)));
            tooltip.add(Component.literal("提示：在雪地中寻找遮蔽处以延长使用时间")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, level, tooltip, flag);


        tooltip.add(Component.translatable("tooltip.kunluncontinent.thermal_amulet").withStyle(ChatFormatting.DARK_GRAY));
    }
}