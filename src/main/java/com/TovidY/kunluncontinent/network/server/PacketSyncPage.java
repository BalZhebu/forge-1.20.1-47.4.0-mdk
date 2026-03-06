package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.screen.attribute.AttributeMenu;
import com.TovidY.kunluncontinent.screen.attribute.hungu.HunguMenu;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class PacketSyncPage {
    private final int pageIndex;

    public PacketSyncPage(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public static void encode(PacketSyncPage msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.pageIndex);
    }

    public static PacketSyncPage decode(FriendlyByteBuf buffer) {
        return new PacketSyncPage(buffer.readInt());
    }

    public static void handle(PacketSyncPage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (msg.pageIndex == 0) {
                    NetworkHooks.openScreen(player, new AttributeMenu.Provider());
                } else if (msg.pageIndex == 1) {
                    NetworkHooks.openScreen(player, new HunguMenu.Provider());
                }else if (msg.pageIndex == 2) {
                    NetworkHooks.openScreen(player, new HunhuanMenu.Provider());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
