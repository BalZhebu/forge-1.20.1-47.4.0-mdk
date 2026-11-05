package com.TovidY.kunluncontinent.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 打开修炼选择界面。
 *
 * <p>⚠️ 客户端代码统一放在 {@link ClientPacketHandlers}，本类不直接引用 {@code Minecraft} / {@code Screen}。</p>
 */
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
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.openXiulianScreen(msg.xiulianTime)));
        ctx.get().setPacketHandled(true);
    }
}