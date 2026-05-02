package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleConfig {
    private final int index;
    public PacketToggleConfig(int index) { this.index = index; }

    public static void encode(PacketToggleConfig msg, FriendlyByteBuf buf) { buf.writeInt(msg.index); }
    public static PacketToggleConfig decode(FriendlyByteBuf buf) { return new PacketToggleConfig(buf.readInt()); }

    public static void handle(PacketToggleConfig msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.toggleConfig(msg.index);
                    SynsAPI.synsPlayerAttribute(player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
