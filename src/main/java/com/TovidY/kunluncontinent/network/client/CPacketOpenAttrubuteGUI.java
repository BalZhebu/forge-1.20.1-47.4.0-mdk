package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.screen.attribute.AttributeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

//该类内容是打开玩家属性的GUI
//用于发送数据包
public class CPacketOpenAttrubuteGUI {
    public static void encode(CPacketOpenAttrubuteGUI msg, FriendlyByteBuf buf) {
    }

    public static CPacketOpenAttrubuteGUI decode(FriendlyByteBuf buf) {
        return new CPacketOpenAttrubuteGUI();
    }

    public static void handle(CPacketOpenAttrubuteGUI msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                NetworkHooks.openScreen(sender, new SimpleMenuProvider(
                        (containerId, playerInventory, player) -> new AttributeMenu(containerId, playerInventory),
                        Component.empty()
                ));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
