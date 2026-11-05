package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.network.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncNpcWuhunPacket {
    private final int entityId;
    private final List<Integer> nianxianList;

    public SyncNpcWuhunPacket(int entityId, List<Integer> nianxianList) {
        this.entityId = entityId;
        this.nianxianList = nianxianList;
    }

    public static void encode(SyncNpcWuhunPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.nianxianList.size());
        for (int nianxian : msg.nianxianList) {
            buf.writeInt(nianxian);
        }
    }

    public static SyncNpcWuhunPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int size = buf.readInt();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(buf.readInt());
        }
        return new SyncNpcWuhunPacket(entityId, list);
    }

    public static void handle(SyncNpcWuhunPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // 客户端逻辑统一在 ClientPacketHandlers（本类不能出现 Minecraft）
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.npcWuhun(msg.entityId, msg.nianxianList)));
        ctx.get().setPacketHandled(true);
    }
}