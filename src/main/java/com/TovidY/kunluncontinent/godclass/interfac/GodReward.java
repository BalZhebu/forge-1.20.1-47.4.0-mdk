package com.TovidY.kunluncontinent.godclass.interfac;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface GodReward {
    void grant(Player player);
}
