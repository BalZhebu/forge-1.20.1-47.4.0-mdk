package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.screen.playernpc.NpcDialogScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenNpcDialogPacket {
    private final int entityId;

    public S2COpenNpcDialogPacket(int entityId) {
        this.entityId = entityId;
    }

    public S2COpenNpcDialogPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static void handle(S2COpenNpcDialogPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                net.minecraft.client.Minecraft.getInstance().setScreen(new NpcDialogScreen(msg.entityId));
            });
        });
        context.setPacketHandled(true);
    }
}