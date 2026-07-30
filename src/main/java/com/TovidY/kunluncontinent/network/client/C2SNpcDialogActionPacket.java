package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SNpcDialogActionPacket {
    private final int entityId;
    private final int actionId; // 0: 交易, 1: 切磋, 2: 离开

    public C2SNpcDialogActionPacket(int entityId, int actionId) {
        this.entityId = entityId;
        this.actionId = actionId;
    }

    public C2SNpcDialogActionPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.actionId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.actionId);
    }

    public static void handle(C2SNpcDialogActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(msg.entityId);
                if (entity instanceof PlayerNpcEntity npc) {
                    switch (msg.actionId) {
                        case 0 -> // 交易
                                npc.openTradeMenu(player);
                        case 1 -> // 切磋
                                npc.startSparring(player);
                        case 2 -> {
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}