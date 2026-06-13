package com.TovidY.kunluncontinent.api;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import net.minecraft.server.level.ServerPlayer;

public final class KunlunApi {
    private KunlunApi() {
    }

    public static int getLevel(ServerPlayer player) {
        return ModAttributeAPI.getDengji(player);
    }

    public static float getHealth(ServerPlayer player) {
        return player.getHealth();
    }

    public static float getMaxHealth(ServerPlayer player) {
        return (float) player.getMaxHealth();
    }

    public static float getAttack(ServerPlayer player) {
        return ModAttributeAPI.getGongji(player);
    }
}
