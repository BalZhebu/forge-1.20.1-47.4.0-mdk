package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.SyncShenciAttributesPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketSelectSkill {
    private final int slot;

    public CPacketSelectSkill(int slot) {
        this.slot = slot;
    }

    public static void encode(CPacketSelectSkill msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slot);
    }

    public static CPacketSelectSkill decode(FriendlyByteBuf buffer) {
        return new CPacketSelectSkill(buffer.readInt());
    }

    public static void handle(CPacketSelectSkill msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setSelectedSkillSlot(msg.slot);
                    // 同步给客户端，确保快捷栏或轮盘高亮更新
                    NetworkHandler.sendToClient(new SyncShenciAttributesPacket(cap), player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
