package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateUIOffset {
    // 操作类型定义
    public static final int TYPE_OFFSET = 0; // 修改平移
    public static final int TYPE_SCALE  = 1; // 修改缩放
    public static final int TYPE_RESET  = 2; // 重置全部

    private final int type;
    private final int amount;    // 平移增量
    private final float scale;   // 目标缩放比例

    // 1. 修改平移用的构造器
    public PacketUpdateUIOffset(int amount) {
        this.type = TYPE_OFFSET;
        this.amount = amount;
        this.scale = 1.0f;
    }

    // 2. 修改缩放/重置用的构造器
    public PacketUpdateUIOffset(int amount, boolean isReset) {
        if (isReset) {
            this.type = TYPE_RESET;
            this.amount = 0;
            this.scale = 1.0f;
        } else {
            this.type = TYPE_OFFSET;
            this.amount = amount;
            this.scale = 1.0f;
        }
    }

    // 3. 专门修改缩放比例用的构造器
    public PacketUpdateUIOffset(float scale) {
        this.type = TYPE_SCALE;
        this.amount = 0;
        this.scale = scale;
    }

    // 解码器（从网络流读取）
    public PacketUpdateUIOffset(FriendlyByteBuf buf) {
        this.type = buf.readInt();
        this.amount = buf.readInt();
        this.scale = buf.readFloat();
    }

    // 编码器（写入网络流）
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.type);
        buf.writeInt(this.amount);
        buf.writeFloat(this.scale);
    }

    // 核心处理逻辑
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    switch (this.type) {
                        case TYPE_OFFSET -> {
                            int newOffset = cap.getUiOffsetY() + this.amount;
                            cap.setUiOffsetY(newOffset);
                        }
                        case TYPE_SCALE -> {
                            cap.setUiScale(this.scale);
                        }
                        case TYPE_RESET -> {
                            cap.setUiOffsetY(0);
                            cap.setUiScale(1.0f);
                        }
                    }
                    // 调用你的数据同步接口
                    SynsAPI.synsPlayerAttribute(player);
                });
            }
        });
        return true;
    }
}