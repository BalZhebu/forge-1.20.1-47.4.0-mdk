package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.godclass.buff.CelestialTeleport;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S：点击神界切换按钮（在神界内=离开，其他维度=进入）。 */
public class C2SEnterCelestial {

    public C2SEnterCelestial() {
    }

    public static void encode(C2SEnterCelestial msg, FriendlyByteBuf buf) {
    }

    public static C2SEnterCelestial decode(FriendlyByteBuf buf) {
        return new C2SEnterCelestial();
    }

    public static void handle(C2SEnterCelestial msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;
        ctx.get().enqueueWork(() -> CelestialTeleport.toggle(player));
    }
}
