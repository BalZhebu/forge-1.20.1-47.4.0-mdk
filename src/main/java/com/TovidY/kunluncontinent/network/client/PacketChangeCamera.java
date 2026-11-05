package com.TovidY.kunluncontinent.network.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换视角。
 *
 * <p>⚠️ 客户端代码全部在 {@link ClientPacketHandlers} 里，本类不直接引用
 * {@code Minecraft} / {@code CameraType}，否则专用服务器会加载客户端类并崩服。</p>
 */
public class PacketChangeCamera {
    private final int mode; // 0: 第一人称, 1: 第三人称背面, 2: 第三人称正面

    public PacketChangeCamera(int mode) { this.mode = mode; }

    public PacketChangeCamera(FriendlyByteBuf buf) { this.mode = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeInt(this.mode); }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.changeCamera(mode)));
        return true;
    }
}