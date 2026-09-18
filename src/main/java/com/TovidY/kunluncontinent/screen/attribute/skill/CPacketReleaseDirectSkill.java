package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.SyncShenciAttributesPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketReleaseDirectSkill {
    private final int slot;

    public CPacketReleaseDirectSkill(int slot) {
        this.slot = slot;
    }

    public static void encode(CPacketReleaseDirectSkill msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slot);
    }

    public static CPacketReleaseDirectSkill decode(FriendlyByteBuf buffer) {
        return new CPacketReleaseDirectSkill(buffer.readInt());
    }

    public static void handle(CPacketReleaseDirectSkill msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setSelectedSkillSlot(msg.slot);
                    NetworkHandler.INSTANCE.sendTo(
                            new SyncShenciAttributesPacket(cap),
                            player.connection.connection,
                            net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                    );
                    CPacketReleaseSkill.executeRelease(player, cap);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}