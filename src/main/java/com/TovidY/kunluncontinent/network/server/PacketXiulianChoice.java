package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.capability.playerattributes.XiulianLogic;
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

    /** 安全上限，防止极端数值下 while 失控。 */
    private static final int MAX_UPGRADE_LOOP = 200;

    private static void processSkipXiulian(ServerPlayer player, PlayerAttributeCapability cap) {
        int remainingSeconds = cap.getXiulianTime();
        if (remainingSeconds <= 0) return;

        int startLevel = cap.getDengji();

        // ⚠️ 精神力上限必须走 ModAttributeAPI（**含加点加成**）。
        //    旧代码用的是 cap.getMaxjingshenli() —— 那是裸字段，不含「凝神」加点。
        //    玩家加过点时裸上限 < 面板上限，于是「跳过修炼」把精神力写成裸上限，
        //    表现为**精神力反而被扣**（例：面板上限 6000、裸上限 3000，
        //    当前 4800 点满 → 跳过后变成 3000，凭空掉 30%）。
        float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);
        if (maxJingshenli <= 0.0f) maxJingshenli = cap.getMaxjingshenli();

        // ⭐ 经验公式与蒲团挂机**完全共用** XiulianLogic。
        //    旧代码在这里另写了一份 `lvl<=30?5 : lvl<=89?7 : 10`（89 还是笔误，
        //    原分界是 70），导致「跳过」比「站着挂」多给 27%~70% 经验。
        //    现在挂 1 分钟与跳过 1 分钟的收益必然相同。
        float totalExpGained = XiulianLogic.expFor(startLevel, remainingSeconds, cap.getMaxjingyan());

        // 修炼时间直接扣光
        cap.setXiulianTime(0);
        // 精神力充满（用面板口径，见上面的注释）
        cap.setJingshenli(maxJingshenli);

        // 逐级升级：把剩余经验按当前门槛消耗，升级后继续拿溢出部分冲下一级
        for (int loop = 0; loop < MAX_UPGRADE_LOOP && totalExpGained > 0.0f; loop++) {
            float maxExp = cap.getMaxjingyan();
            if (maxExp <= 0.0f) break;

            float needed = maxExp - cap.getJingyan();
            if (needed > 0.0f && totalExpGained >= needed) {
                totalExpGained -= needed;
                cap.setJingyan(maxExp);

                int oldLevel = cap.getDengji();
                PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);
                if (cap.getDengji() == oldLevel) {
                    // 没升上去（满级 / 缺魂环 / 突破失败）→ 余量留在经验条里并收手
                    cap.setJingyan(Math.min(cap.getMaxjingyan(), cap.getJingyan() + totalExpGained));
                    break;
                }
                // 升级成功 → 门槛会变大，循环继续把余量用于下一级
            } else {
                // 剩下的经验不够再升一级，直接写进经验条
                cap.setJingyan(Math.min(maxExp, cap.getJingyan() + totalExpGained));
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