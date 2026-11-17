package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 吟唱状态同步。
 *
 * <p>发给<b>施法者自己和所有能看到他的人</b>（{@code TRACKING_ENTITY_AND_SELF}）：
 * 客户端要靠这个知道「谁在吟唱」，才能给那个玩家摆出结印姿势。</p>
 *
 * <p>{@code totalTicks = 0} 表示吟唱收尾（唱完 / 打断 / 死亡 / 转生 / 换武魂都走
 * {@code stopCasting}）。客户端只是收起进度条和姿势，不需要区分两者。</p>
 */
public class S2CCastingSyncPacket {

    private final int entityId;
    private final int totalTicks;

    public S2CCastingSyncPacket(int entityId, int totalTicks) {
        this.entityId = entityId;
        this.totalTicks = totalTicks;
    }

    /** 开始吟唱：广播给周围 + 自己。 */
    public static void begin(ServerPlayer player, int totalTicks) {
        if (player == null || NetworkHandler.INSTANCE == null) {
            return;
        }
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new S2CCastingSyncPacket(player.getId(), totalTicks));
    }

    /**
     * 立刻收起吟唱状态（{@code totalTicks = 0}）。
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
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                new S2CCastingSyncPacket(serverPlayer.getId(), 0));
    }

    public static void encode(S2CCastingSyncPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeInt(msg.totalTicks);
    }

    public static S2CCastingSyncPacket decode(FriendlyByteBuf buffer) {
        return new S2CCastingSyncPacket(buffer.readInt(), buffer.readInt());
    }

    public static void handle(S2CCastingSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // 客户端逻辑统一在 ClientPacketHandlers（本类不能出现 Minecraft/GuiGraphics）
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.castingSync(msg.entityId, msg.totalTicks)));
        ctx.get().setPacketHandled(true);
    }
}
