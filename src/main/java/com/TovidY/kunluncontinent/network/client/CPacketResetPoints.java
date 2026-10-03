package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.AttributePoints;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端：属性点面板上的"重置属性点"按钮。
 *
 * <p>与右键 {@code ModItems.RESET_SCROLL} 共用
 * {@link AttributePoints#resetWithScroll} 这一个入口 —— 两条路都会消耗 1 个重置卷轴。</p>
 */
public class CPacketResetPoints {

    public CPacketResetPoints() {
    }

    public static void encode(CPacketResetPoints msg, FriendlyByteBuf buf) {
    }

    public static CPacketResetPoints decode(FriendlyByteBuf buf) {
        return new CPacketResetPoints();
    }

    public static void handle(CPacketResetPoints msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            sender.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (AttributePoints.resetWithScroll(sender, cap)) {
                    SynsAPI.synsPlayerAttribute(sender);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}