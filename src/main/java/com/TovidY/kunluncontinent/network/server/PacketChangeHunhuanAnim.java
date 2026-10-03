package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换魂环"开启方式"动画样式（客户端 → 服务端，仅用于持久化）。
 *
 * <p>样式本身只影响渲染，但存在 capability 里，所以要像
 * {@link PacketChangeDisplayMode} 一样落一次服务端存档，并回同步给客户端。</p>
 */
public class PacketChangeHunhuanAnim {
    private final int style;

    public PacketChangeHunhuanAnim(int style) {
        this.style = style;
    }

    public static void encode(PacketChangeHunhuanAnim msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.style);
    }

    public static PacketChangeHunhuanAnim decode(FriendlyByteBuf buffer) {
        return new PacketChangeHunhuanAnim(buffer.readInt());
    }

    public static void handle(PacketChangeHunhuanAnim msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setHunhuanOpenAnim(msg.style);
                    // 同步回客户端，确保界面文字与存档一致
                    SynsAPI.synsPlayerAttribute(player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
