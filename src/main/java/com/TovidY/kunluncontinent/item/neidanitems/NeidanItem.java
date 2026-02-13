package com.TovidY.kunluncontinent.item.neidanitems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NeidanItem extends Item implements INeidanData {
    private final int tier;

    public NeidanItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    // 从物品堆的 NBT 中获取品质
    @Override
    public NeidanQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            return NeidanQuality.valueOf(stack.getTag().getString("Quality"));
        }
        return NeidanQuality.FAN; // 默认凡品
    }

    // 根据品质改变名称颜色
    @Override
    public Component getName(ItemStack stack) {
        NeidanQuality q = getQuality(stack);
        ChatFormatting color = switch (q) {
            case FAN -> ChatFormatting.GRAY;
            case LIANG -> ChatFormatting.GREEN;
            case SHANG -> ChatFormatting.BLUE;
            case ZHEN -> ChatFormatting.DARK_PURPLE;
            case JUE -> ChatFormatting.GOLD;
            case XIAN -> ChatFormatting.RED;
        };
        return Component.literal("[" + q.name + "] ").withStyle(color)
                .append(super.getName(stack));
    }

    // 预留接口：以后给炼丹炉调用
    public NeidanQuality getQuality() { return null; } // 接口兼容占位
}
