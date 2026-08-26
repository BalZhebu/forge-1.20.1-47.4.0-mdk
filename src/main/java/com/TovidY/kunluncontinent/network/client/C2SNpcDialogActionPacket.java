package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.screen.playernpc.shoumai.SellMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class C2SNpcDialogActionPacket {
    private final int entityId;
    private final int actionId; // 0: 交易, 1: 切磋, 2: 出售, 3: 离开

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
            if (player == null) return;

            Entity entity = player.level().getEntity(msg.entityId);
            if (!(entity instanceof PlayerNpcEntity npc)) return;

            switch (msg.actionId) {
                case 0 -> npc.openTradeMenu(player);
                case 1 -> npc.startSparring(player);
                case 2 -> openSellMenu(player, npc);
                case 3 -> {} // 离开，无操作
                default -> {}
            }
        });
        context.setPacketHandled(true);
    }

    /** 打开 NPC 售卖界面 */
    private static void openSellMenu(ServerPlayer player, PlayerNpcEntity npc) {
        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);

        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (containerId, inventory, p) -> new SellMenu(containerId, inventory, npc),
                Component.literal("出售物品")
        ));
    }
}
