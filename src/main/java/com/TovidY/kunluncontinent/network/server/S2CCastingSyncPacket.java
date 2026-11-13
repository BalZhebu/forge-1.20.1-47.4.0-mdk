package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.event.client.CastBarRenderer;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class S2CCastingSyncPacket {
    private final int totalTicks;

    public S2CCastingSyncPacket(int totalTicks) {
        this.totalTicks = totalTicks;
    }

    /**
     * 立刻收起客户端的吟唱条（{@code totalTicks = 0}）。
     *
     * <p><b>为什么必须有这个</b>：吟唱条只是「收到包时按 totalTicks 自己倒数」，
     * 客户端并不知道服务端的吟唱已经被打断 / 取消。不补这个包的话，
     * 受击打断后进度条还会继续走到 100%，玩家以为技能照放（实际没有）。</p>
     *
     * <p>调用点统一在 {@code PlayerAttributeCapability#stopCasting} ——
     * 那里是吟唱收尾的唯一出口（唱完 / 打断 / 死亡 / 转生 / 换武魂都走它）。</p>
     */
    public static void clear(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || NetworkHandler.INSTANCE == null) {
            return;
        }
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                new S2CCastingSyncPacket(0));
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
