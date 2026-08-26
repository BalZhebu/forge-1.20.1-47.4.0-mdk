package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// PacketXiulianChoice.java

public class PacketXiulianChoice {
    private final int action; // 0: 挂机打坐, 1: 一键跳过

    public PacketXiulianChoice(int action) {
        this.action = action;
    }

    public static void encode(PacketXiulianChoice msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.action);
    }

    public static PacketXiulianChoice decode(FriendlyByteBuf buffer) {
        return new PacketXiulianChoice(buffer.readInt());
    }

    public static void handle(PacketXiulianChoice msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (msg.action == 0) {
                    int remainMin = (int) Math.ceil(cap.getXiulianTime() / 60.0f);
                    player.sendSystemMessage(Component.literal("§e你选择静心打坐，预计剩余时间：" + remainMin + " 分钟"));
                } else if (msg.action == 1) {
                    processSkipXiulian(player, cap);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private static void processSkipXiulian(ServerPlayer player, PlayerAttributeCapability cap) {
        int remainingSeconds = cap.getXiulianTime();
        if (remainingSeconds <= 0) return;
        int startLevel = cap.getDengji();
        float totalExpGained = 0;
        for (int i = 0; i < remainingSeconds; i++) {
            int lvl = cap.getDengji();
            float minutesToLevel = lvl <= 30 ? 5f : lvl <= 89 ? 7f : 10f;
            totalExpGained += cap.getMaxjingyan() / (minutesToLevel * 60f);
        }
        cap.setXiulianTime(0);
        cap.setJingshenli(cap.getMaxjingshenli());
        int maxLoopCount = 1000;
        int safetyIndex = 0;

        while (totalExpGained > 0 && safetyIndex < maxLoopCount) {
            safetyIndex++;

            float currentExp = cap.getJingyan();
            float maxExp = cap.getMaxjingyan();
            float needed = maxExp - currentExp;

            if (maxExp <= 0) break;

            if (totalExpGained >= needed && needed > 0) {
                totalExpGained -= needed;
                cap.setJingyan(maxExp);

                int oldLevel = cap.getDengji();
                PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);
                if (cap.getDengji() == oldLevel) {
                    float finalExp = Math.min(cap.getMaxjingyan(), cap.getJingyan() + totalExpGained);
                    cap.setJingyan(finalExp);
                    break;
                }
            } else {
                cap.setJingyan(Math.min(maxExp, currentExp + totalExpGained));
                totalExpGained = 0;
                break;
            }
        }
        cap.setUsingAll(true);
        SynsAPI.synsPlayerAttribute(player);
        player.stopRiding();

        int endLevel = cap.getDengji();
        player.sendSystemMessage(Component.literal("§a你消耗了所有修炼时间，修仙等级从 §e"
                + startLevel + "§a 级提升至 §e" + endLevel + "§a 级！精神力已完全充盈！"));
    }
}
