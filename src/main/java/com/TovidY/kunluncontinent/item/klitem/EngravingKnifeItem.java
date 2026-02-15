package com.TovidY.kunluncontinent.item.klitem;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EngravingKnifeItem extends Item {
    public EngravingKnifeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        if (copy.hurt(1, RandomSource.create(), null)) {
            return ItemStack.EMPTY;
        }
        return copy;
    }
}