package com.TovidY.kunluncontinent.command;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class HunguAdminStatus {
    // 弱引用 Map：键为 Player，值为是否开启百分百掉落
    private static final Map<Player, Boolean> ALWAYS_DROP_MAP = new WeakHashMap<>();

    public static void setStatus(Player player, boolean enabled) {
        ALWAYS_DROP_MAP.put(player, enabled);
    }

    public static boolean isAlwaysDrop(Player player) {
        return ALWAYS_DROP_MAP.getOrDefault(player, false);
    }
}