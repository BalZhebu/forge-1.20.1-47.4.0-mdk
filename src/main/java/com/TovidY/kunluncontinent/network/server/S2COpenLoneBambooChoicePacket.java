package com.TovidY.kunluncontinent.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 客户端：弹出「孤竹之缘」二选一对话框。
 *
 * <p>无负载（就是"该弹框了"这个信号）。真正的选择由客户端
 * {@code C2SLoneBambooChoicePacket} 回传。</p>
 */
public class S2COpenLoneBambooChoicePacket {

    public S2COpenLoneBambooChoicePacket() {
    }

    public static void encode(S2COpenLoneBambooChoicePacket msg, FriendlyByteBuf buf) {
    }

    public static S2COpenLoneBambooChoicePacket decode(FriendlyByteBuf buf) {
        return new S2COpenLoneBambooChoicePacket();
    }

    public static void handle(S2COpenLoneBambooChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.TovidY.kunluncontinent.network.client.ClientPacketHandlers.openLoneBambooChoice()));
        context.setPacketHandled(true);
    }
}
