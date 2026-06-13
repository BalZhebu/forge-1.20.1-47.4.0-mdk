package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.tower.floor.ClientTimerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;


public class PacketSyncTowerTimer {
    private final int remainingSeconds;
    private final boolean show;

    public PacketSyncTowerTimer(int remainingSeconds, boolean show) {
        this.remainingSeconds = remainingSeconds;
        this.show = show;
    }

    public static void encode(PacketSyncTowerTimer msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.remainingSeconds);
        buf.writeBoolean(msg.show);
    }

    public static PacketSyncTowerTimer decode(FriendlyByteBuf buf) {
        return new PacketSyncTowerTimer(buf.readInt(), buf.readBoolean());
    }

    public static void handle(PacketSyncTowerTimer msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                ClientTimerManager.setTimer(msg.remainingSeconds, msg.show);
            });
        });
        context.setPacketHandled(true);
    }

}