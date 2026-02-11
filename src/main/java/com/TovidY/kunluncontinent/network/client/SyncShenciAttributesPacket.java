package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncShenciAttributesPacket {
    private final PlayerAttributeCapability attributes;

    public SyncShenciAttributesPacket(PlayerAttributeCapability attributes) {
        this.attributes = attributes;
    }

    public static void encode(SyncShenciAttributesPacket msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.attributes.serializeNBT());
    }

    public static SyncShenciAttributesPacket decode(FriendlyByteBuf buffer) {
        PlayerAttributeCapability attributes = new PlayerAttributeCapability();
        attributes.deserializeNBT(buffer.readNbt());
        return new SyncShenciAttributesPacket(attributes);
    }

    public static void handle(SyncShenciAttributesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> {
                        float oldWugong = cap.getGongji();
                        cap.deserializeNBT(msg.attributes.serializeNBT());
                    });
        });
        ctx.get().setPacketHandled(true);
    }
}