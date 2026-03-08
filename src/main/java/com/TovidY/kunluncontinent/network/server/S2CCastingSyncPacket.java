package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.event.client.CastBarRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CCastingSyncPacket {
    private final int totalTicks;

    public S2CCastingSyncPacket(int totalTicks) {
        this.totalTicks = totalTicks;
    }

    public static void encode(S2CCastingSyncPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.totalTicks);
    }

    public static S2CCastingSyncPacket decode(FriendlyByteBuf buffer) {
        return new S2CCastingSyncPacket(buffer.readInt());
    }

    public static void handle(S2CCastingSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CastBarRenderer.remainingTicks = msg.totalTicks;
            CastBarRenderer.maxTicks = msg.totalTicks;
        });
        ctx.get().setPacketHandled(true);
    }
}
