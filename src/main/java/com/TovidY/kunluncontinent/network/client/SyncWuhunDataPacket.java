package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

//武魂数据同步包
public class SyncWuhunDataPacket {
    private final UUID playerUUID;
    private final List<Integer> wuhunNianxianList;
    private final boolean isPlayingAnimation;
    private final long animationStartTime;

    public SyncWuhunDataPacket(UUID playerUUID, List<Integer> wuhunNianxianList,
                               boolean isPlayingAnimation, long animationStartTime) {
        this.playerUUID = playerUUID;
        this.wuhunNianxianList = wuhunNianxianList != null ? wuhunNianxianList : Collections.emptyList();
        this.isPlayingAnimation = isPlayingAnimation;
        this.animationStartTime = animationStartTime;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public List<Integer> getWuhunNianxianList() {
        return wuhunNianxianList;
    }

    public boolean isPlayingAnimation() {
        return isPlayingAnimation;
    }

    public long getAnimationStartTime() {
        return animationStartTime;
    }

    public static void encode(SyncWuhunDataPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.playerUUID);
        List<Integer> list = pkt.wuhunNianxianList != null ? pkt.wuhunNianxianList : Collections.emptyList();
        buf.writeInt(list.size());
        for (int nianxian : list) {
            buf.writeInt(nianxian);
        }
        buf.writeBoolean(pkt.isPlayingAnimation);
        buf.writeLong(pkt.animationStartTime);
    }

    public static SyncWuhunDataPacket decode(FriendlyByteBuf buf) {
        UUID playerUUID = buf.readUUID();
        int size = buf.readInt();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readInt());
        }
        boolean isPlayingAnimation = buf.readBoolean();
        long animationStartTime = buf.readLong();
        return new SyncWuhunDataPacket(playerUUID, list, isPlayingAnimation, animationStartTime);
    }

    public static void handle(SyncWuhunDataPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Player player = mc.level.getPlayerByUUID(pkt.playerUUID);
            if (player != null) {
                PWRenderPlayerEvent.updatePlayerWuhunData(
                        player,
                        pkt.wuhunNianxianList,
                        pkt.isPlayingAnimation,
                        pkt.animationStartTime
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}