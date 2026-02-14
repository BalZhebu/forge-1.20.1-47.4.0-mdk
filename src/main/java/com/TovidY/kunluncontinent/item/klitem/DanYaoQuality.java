package com.TovidY.kunluncontinent.item.klitem;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public enum DanYaoQuality {
    PO_SUI("破碎", 0.0f, ChatFormatting.DARK_GRAY),
    LV_SE("药散", 0.3f, ChatFormatting.GREEN),
    LAN_SE("药丹", 0.5f, ChatFormatting.BLUE),
    ZI_SE("灵丹", 1.0f, ChatFormatting.DARK_PURPLE),
    JIN_SE("宝丹", 1.5f, ChatFormatting.GOLD),
    HONG_SE("仙丹", 2.0f, ChatFormatting.RED);

    public final String name;
    public final float multiplier;
    public final ChatFormatting color;

    DanYaoQuality(String name, float multiplier, ChatFormatting color) {
        this.name = name;
        this.multiplier = multiplier;
        this.color = color;
    }

    public static DanYaoQuality getFromStack(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("DanYaoQuality")) {
            int index = stack.getTag().getInt("DanYaoQuality");
            return DanYaoQuality.values()[Math.min(index, DanYaoQuality.values().length - 1)];
        }
        return ZI_SE;
    }
}
