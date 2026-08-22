package com.TovidY.kunluncontinent.command.tovid;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class HunguAdminStatus {
    private static final Map<Player, Boolean> ALWAYS_DROP_MAP = new WeakHashMap<>();
    public static void setStatus(Player player, boolean enabled) {
        ALWAYS_DROP_MAP.put(player, enabled);
    }
    public static boolean isAlwaysDrop(Player player) {
        return ALWAYS_DROP_MAP.getOrDefault(player, false);
    }
}