package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.network.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 打开 NPC 对话界面。
 *
 * <p>⚠️ 客户端代码全部在 {@link ClientPacketHandlers} 里，本类<b>不允许</b>直接引用
 * {@code Minecraft} / {@code Screen} —— 否则专用服务器在校验包类时就会加载客户端类并崩服
 * （{@code Attempted to load class .../Screen for invalid dist DEDICATED_SERVER}）。</p>
 */
public class S2COpenNpcDialogPacket {
    private final int entityId;

    public S2COpenNpcDialogPacket(int entityId) {
        this.entityId = entityId;
    }

    public S2COpenNpcDialogPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static void handle(S2COpenNpcDialogPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.openNpcDialog(msg.entityId)));
        context.setPacketHandled(true);
    }
}