package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeDisplayMode {
    private final int mode;

    public PacketChangeDisplayMode(int mode) {
        this.mode = mode;
    }

    public static void encode(PacketChangeDisplayMode msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.mode);
    }

    public static PacketChangeDisplayMode decode(FriendlyByteBuf buffer) {
        return new PacketChangeDisplayMode(buffer.readInt());
    }

    public static void handle(PacketChangeDisplayMode msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setDamageDisplayMode(msg.mode);
                    // 同步回客户端，确保界面文字更新
                    SynsAPI.synsPlayerAttribute(player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
