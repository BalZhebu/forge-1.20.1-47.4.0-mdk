package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.AttributePointSpec;
import com.TovidY.kunluncontinent.capability.playerattributes.AttributePoints;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端：给某条属性加点。
 *
 * <p>payload：{@code (specIndex:int, amount:int)}。
 * 服务端做全部校验（点数够不够、有没有到上限），客户端只负责发意图。</p>
 */
public class CPacketAllocatePoint {

    private final int specIndex;
    private final int amount;

    public CPacketAllocatePoint(int specIndex, int amount) {
        this.specIndex = specIndex;
        this.amount = amount;
    }

    public static void encode(CPacketAllocatePoint msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.specIndex);
        buf.writeInt(msg.amount);
    }

    public static CPacketAllocatePoint decode(FriendlyByteBuf buf) {
        return new CPacketAllocatePoint(buf.readInt(), buf.readInt());
    }

    public static void handle(CPacketAllocatePoint msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            AttributePointSpec spec = AttributePointSpec.byIndex(msg.specIndex);
            if (spec == null) return;

            sender.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (AttributePoints.allocate(sender, cap, spec, msg.amount) > 0) {
                    SynsAPI.synsPlayerAttribute(sender);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}