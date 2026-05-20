package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateUIOffset {
    private final int amount;
    private final boolean isReset;

    // 常规调整构造器
    public PacketUpdateUIOffset(int amount) {
        this.amount = amount;
        this.isReset = false;
    }

    // 重置或特殊指定构造器
    public PacketUpdateUIOffset(int amount, boolean isReset) {
        this.amount = amount;
        this.isReset = isReset;
    }

    // 解码器（从网络流读取）
    public PacketUpdateUIOffset(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.isReset = buf.readBoolean();
    }

    // 编码器（写入网络流）
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeBoolean(this.isReset);
    }

    // 核心处理逻辑
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    if (this.isReset) {
                        cap.setUiOffsetY(0);
                    } else {
                        int newOffset = cap.getUiOffsetY() + this.amount;
                        cap.setUiOffsetY(newOffset);
                    }
                       SynsAPI.synsPlayerAttribute(player);
                });
            }
        });
        return true;
    }
}