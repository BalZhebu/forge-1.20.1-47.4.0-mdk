package com.TovidY.kunluncontinent.network.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeCamera {
    private final int mode; // 0: 第一人称, 1: 第三人称背面, 2: 第三人称正面

    public PacketChangeCamera(int mode) { this.mode = mode; }

    public PacketChangeCamera(FriendlyByteBuf buf) { this.mode = buf.readInt(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeInt(this.mode); }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) {
                    CameraType type = CameraType.values()[this.mode % CameraType.values().length];
                    mc.options.setCameraType(type);
                }
            });
        });
        return true;
    }
}