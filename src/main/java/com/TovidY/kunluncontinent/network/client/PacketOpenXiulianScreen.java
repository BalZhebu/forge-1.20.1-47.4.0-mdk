package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.screen.attribute.XiulianSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenXiulianScreen {
    private final int xiulianTime;

    public PacketOpenXiulianScreen(int xiulianTime) {
        this.xiulianTime = xiulianTime;
    }

    public PacketOpenXiulianScreen(FriendlyByteBuf buf) {
        this.xiulianTime = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.xiulianTime);
    }

    public static void handle(PacketOpenXiulianScreen msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.openScreen(msg.xiulianTime));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientHandler {
        private static void openScreen(int xiulianTime) {
            Minecraft.getInstance().setScreen(new XiulianSelectionScreen(xiulianTime));
        }
    }
}