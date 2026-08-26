package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.screen.playernpc.shoumai.SellMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端：售卖按钮交互包。
 * action=false → 第一次点击（变为确认状态）
 * action=true  → 第二次点击（执行售卖）
 */

public class C2SNpcSellPacket {
    private final boolean action; // false=确认  true=售卖

    public C2SNpcSellPacket(boolean action) {
        this.action = action;
    }

    public C2SNpcSellPacket(FriendlyByteBuf buf) {
        this.action = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(action);
    }

    public static void handle(C2SNpcSellPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (player.containerMenu instanceof SellMenu menu) {
                menu.toggleSell(player);
            }
        });
        context.setPacketHandled(true);
    }
}
