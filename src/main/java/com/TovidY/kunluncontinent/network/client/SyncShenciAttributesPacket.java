package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 武魂属性同步包。
 *
 * <p>⚠️ 客户端代码在 {@link ClientPacketHandlers}，本类不直接引用 {@code Minecraft}——
 * 否则专用服务器注册包头时会加载客户端类并崩服。</p>
 */
public class SyncShenciAttributesPacket {
    private final PlayerAttributeCapability attributes;

    public SyncShenciAttributesPacket(PlayerAttributeCapability attributes) {
        this.attributes = attributes;
    }

    public static void encode(SyncShenciAttributesPacket msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.attributes.serializeNBT());
    }

    public static SyncShenciAttributesPacket decode(FriendlyByteBuf buffer) {
        PlayerAttributeCapability attributes = new PlayerAttributeCapability();
        attributes.deserializeNBT(buffer.readNbt());
        return new SyncShenciAttributesPacket(attributes);
    }

    public static void handle(SyncShenciAttributesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.syncShenciAttributes(msg.attributes)));
        ctx.get().setPacketHandled(true);
    }
}