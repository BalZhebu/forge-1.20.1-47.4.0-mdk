package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S：客户端请求一次神考数据全量同步。
 *
 * <p>⭐ <b>解决"神考面板消失，必须重启游戏才恢复"</b>。</p>
 *
 * <p>成因：客户端的 {@code GodClientData} 是<b>静态字段</b>，一旦某次发包时机不对
 * （登出/换维度/服务端发包早于客户端 ready），它就会停留在错误的旧值上，
 * 而客户端<b>不会主动要</b> —— 于是 UI 一直错，只有重登（登录必定全量同步）才恢复。</p>
 *
 * <p>本包让客户端在<b>打开属性面板时</b>主动拉一次，把状态对齐到服务端。
 * 幂等、零副作用，比排查"到底哪次发包丢了"要省事得多。</p>
 */
public class C2SRequestGodSync {

    public C2SRequestGodSync() {
    }

    public static void encode(C2SRequestGodSync msg, FriendlyByteBuf buf) {
    }

    public static C2SRequestGodSync decode(FriendlyByteBuf buf) {
        return new C2SRequestGodSync();
    }

    public static void handle(C2SRequestGodSync msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        ctx.get().enqueueWork(() -> player.getCapability(
                PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap ->
                NetworkHandler.sendToClient(new PacketSyncGodData(cap), player)));

        ctx.get().setPacketHandled(true);
    }
}
