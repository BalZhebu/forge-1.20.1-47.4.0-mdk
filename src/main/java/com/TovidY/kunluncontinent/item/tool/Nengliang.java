package com.TovidY.kunluncontinent.item.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface Nengliang {
    void setNengliang(Player player, ItemStack itemStack, int value);
    int getNengliang(Player player, ItemStack itemStack);
}
