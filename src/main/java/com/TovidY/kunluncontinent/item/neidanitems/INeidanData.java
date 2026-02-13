package com.TovidY.kunluncontinent.item.neidanitems;

import net.minecraft.world.item.ItemStack;

public interface INeidanData {
    int getTier();

    // 从物品堆的 NBT 中获取品质
    NeidanQuality getQuality(ItemStack stack);

    NeidanQuality getQuality();
}
