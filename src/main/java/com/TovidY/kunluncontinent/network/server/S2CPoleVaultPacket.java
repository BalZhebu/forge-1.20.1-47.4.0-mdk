package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.special.lonebamboo.client.PoleVaultClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 客户端：某个玩家开始撑杆跳了。
 *
 * <p>撑杆跳本身是纯服务端物理（改速度），但「双手举杆 + 一根竹竿」是纯客户端表现，
 * 客户端无从得知这次起跳是谁放的。所以这里发一个轻量通知，
 * 客户端据此记下「谁、从哪个点、持续多久」（见 {@link PoleVaultClientState}）。</p>
 *
 * <p>用 {@code TRACKING_ENTITY_AND_SELF} 广播，所以<b>别的玩家也看得到</b>。</p>
 */
public class S2CPoleVaultPacket {

    private final int entityId;
    private final int durationTicks;

    public S2CPoleVaultPacket(int entityId, int durationTicks) {
        this.entityId = entityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(S2CPoleVaultPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeInt(msg.durationTicks);
    }

    public static S2CPoleVaultPacket decode(FriendlyByteBuf buffer) {
        return new S2CPoleVaultPacket(buffer.readInt(), buffer.readInt());
    }

    public static void handle(S2CPoleVaultPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> PoleVaultClientState.start(msg.entityId, msg.durationTicks)));
        ctx.get().setPacketHandled(true);
    }
}
