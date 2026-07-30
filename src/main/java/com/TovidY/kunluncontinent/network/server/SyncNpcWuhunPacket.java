package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
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
        ctx.get().enqueueWork(() -> {
            // 在客户端收到发包后，把数据更新到客户端缓存里
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
                if (entity instanceof PlayerNpcEntity npc) {
                    // 更新 NPC 客户端缓存的数据
                    PWRenderPlayerEvent.updateNpcWuhunCache(npc.getUUID(), msg.nianxianList);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}