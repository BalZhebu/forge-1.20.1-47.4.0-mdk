package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.tower.floor.ClientGodEffectManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketPlayGodRitualEffect {
    private final int totalTicks;
    private final String godName;

    public PacketPlayGodRitualEffect(int totalTicks, String godName) {
        this.totalTicks = totalTicks;
        this.godName = godName;
    }

    public static void encode(PacketPlayGodRitualEffect msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.totalTicks);
        buf.writeUtf(msg.godName);
    }

    public static PacketPlayGodRitualEffect decode(FriendlyByteBuf buf) {
        return new PacketPlayGodRitualEffect(buf.readInt(), buf.readUtf());
    }

    public static void handle(PacketPlayGodRitualEffect msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientGodEffectManager.startRitual(msg.totalTicks, msg.godName);
        });
        ctx.get().setPacketHandled(true);
    }
}
