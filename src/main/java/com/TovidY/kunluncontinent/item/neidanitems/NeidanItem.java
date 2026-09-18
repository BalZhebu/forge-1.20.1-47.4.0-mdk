package com.TovidY.kunluncontinent.item.neidanitems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NeidanItem extends Item implements INeidanData {
    private final int tier;

    public NeidanItem(Properties properties, int tier) {
        super(tier >= 7 ? properties.fireResistant() : properties);
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public NeidanQuality getQuality() {
        return getBaseQualityByTier();
    }

    public NeidanQuality getBaseQualityByTier() {
        return switch (this.tier) {
            case 9 -> NeidanQuality.JUE;
            case 8 -> NeidanQuality.ZHEN;
            case 7 -> NeidanQuality.SHANG;
            default -> NeidanQuality.FAN;
        };
    }

    @Override
    public NeidanQuality getQuality(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Quality")) {
            try {
                return NeidanQuality.valueOf(stack.getTag().getString("Quality"));
            } catch (IllegalArgumentException e) {
                return getBaseQualityByTier();
            }
        }
        return getBaseQualityByTier();
    }

    /**
     * 写入品质到 NBT（掉落/炼制内丹时调用此方法）
     */
    public static void setQuality(ItemStack stack, NeidanQuality quality) {
        if (quality != null) {
            stack.getOrCreateTag().putString("Quality", quality.name());
        }
    }

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
        // 渲染名字：[品质] X阶内丹
        return Component.literal("[" + q.name + "] ").withStyle(color)
                .append(super.getName(stack));
    }
}