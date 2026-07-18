package com.TovidY.kunluncontinent.screen.spiritgatheringaltar;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class AltarFilters {
    public static boolean isValidItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        for (RegistryObject<Item> regObject : ModItems.JUHUNPING) {
            if (regObject.isPresent() && regObject.get() == item) {
                return true;
            }
        }
        return false;
    }
}
