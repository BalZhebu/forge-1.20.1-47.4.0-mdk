package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Random;

import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI.addWuHun;
import net.minecraft.ChatFormatting;

//天赋系统
public class PlayerUpgradeSystem {

    private static final Random random = new Random();

    public static void checkAndProcessUpgrade(ServerPlayer player, PlayerAttributeCapability capability) {
        if (capability.getJingyan() >= capability.getMaxjingyan()) {
            performUpgrade(player, capability);
        }
    }

    static void performUpgrade(ServerPlayer player, PlayerAttributeCapability capability) {
        int currentLevel = capability.getDengji();
        if (currentLevel == 0) {
            processAwakening(player, capability);
            return;
        }
        int nextLevel = currentLevel + 1;
        if (attemptUpgrade(capability)) {
            processSuccessfulUpgrade(capability, nextLevel);
            player.sendSystemMessage(Component.literal("§a突破成功！晋升至 " + nextLevel + " 级！"));
            syncAttributesToClient(player, capability);
        } else {
            processUpgradeFailure(player, capability);
        }
    }

    private static void processAwakening(ServerPlayer player, PlayerAttributeCapability capability) {
        int talentLevel;
        float bonusHP = 0, bonusAtk = 0, bonusDef = 0;
        String talentName;
        ChatFormatting color;

        String forced = capability.getForcedTalent();
        int r;

        if (forced != null && !forced.isEmpty()) {
            switch (forced) {
                case "tiancai" -> r = 5;
                case "zhuoyue" -> r = 25;
                case "youxiu"  -> r = 50;
                case "feiwu"   -> r = 95;
                default -> r = random.nextInt(100);
            }

            capability.setForcedTalent("");
        } else {
            r = random.nextInt(100);
        }

        if (r < 10) {
            talentLevel = 9;
            talentName = "天才资质";
            color = ChatFormatting.GOLD;
            bonusHP = 100.0f;
            bonusAtk = 20.0f;
            bonusDef = 10.0f;
            sendGlobalAnnouncement(player);
        } else if (r < 40) {
            talentLevel = random.nextInt(2) + 7;
            talentName = "卓越资质";
            color = ChatFormatting.DARK_AQUA;
            bonusHP = 40.0f;
            bonusAtk = 8.0f;
            bonusDef = 4.0f;
        } else if (r < 90) {
            talentLevel = random.nextInt(3) + 4;
            talentName = "优秀资质";
            color = ChatFormatting.GREEN;
        } else {
            talentLevel = random.nextInt(4);
            talentName = "废物资质";
            color = ChatFormatting.GRAY;
        }
        int finalLevel = 1 + talentLevel;
        applyGrowthAndBonus(capability, finalLevel, bonusHP, bonusAtk, bonusDef);
        addWuHun(player);
        capability.setXiantianTalent(talentLevel);
        player.sendSystemMessage(Component.literal("§e【觉醒仪式】§f你的魂力已觉醒！"));
        player.sendSystemMessage(Component.literal("检测到资质：").append(Component.literal(talentName).withStyle(color))
                .append(" §f(先天等级: +" + talentLevel + ")"));
        syncAttributesToClient(player, capability);
    }
    private static void sendGlobalAnnouncement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            Component message = Component.literal("§l§f【全服通知】§b恭喜玩家 §e" + player.getName().getString() + " §b觉醒了")
                    .append(Component.literal("天才资质").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append("§b，真乃旷世奇才！让我们为他欢呼，期待他登顶巅峰！");

            server.getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private static void applyGrowthAndBonus(PlayerAttributeCapability capability, int targetLevel, float bHP, float bAtk, float bDef) {
        capability.setDengji(targetLevel);
        capability.setJingyan(0.0f);
        for (int i = 1; i <= targetLevel; i++) {
            capability.setMaxshengming(capability.getMaxshengming() + (i * 0.6f));
            capability.setFangyu(capability.getFangyu() + (i * 0.2f));
            capability.setGongji(capability.getGongji() + (i * 0.4f));
            capability.setJingshenli(capability.getJingshenli() + (i * 0.5f));
            capability.setMaxjingshenli(capability.getMaxjingshenli() + (i * 0.5f));
            capability.setMaxjingyan(capability.getMaxjingyan() + (i * 5.0f));
        }
        capability.setMaxshengming(capability.getMaxshengming() + bHP);
        capability.setGongji(capability.getGongji() + bAtk);
        capability.setFangyu(capability.getFangyu() + bDef);
        capability.setShengming(capability.getMaxshengming());
    }

    private static boolean attemptUpgrade(PlayerAttributeCapability capability) {
        int level = capability.getDengji();
        float baseRate = capability.getTupochenggonglv();
        float levelPenalty = level * 0.5f;
        float finalRate = Math.max(5.0f, baseRate - levelPenalty);
        return (random.nextFloat() * 100.0f) <= finalRate;
    }

    private static void processUpgradeFailure(ServerPlayer player, PlayerAttributeCapability capability) {
        capability.setTupochenggonglv(capability.getTupochenggonglv() + 1.0f);
        float penalty = capability.getMaxjingyan() * 0.8f;
        capability.setJingyan(Math.max(0, capability.getJingyan() - penalty));
        player.sendSystemMessage(Component.literal("§c突破失败！保底率提升至: " + capability.getTupochenggonglv() + "%"));
        syncAttributesToClient(player, capability);
    }

    private static void processSuccessfulUpgrade(PlayerAttributeCapability capability, int newLevel) {
        capability.setDengji(newLevel);
        capability.setJingyan(0.0f);

        capability.setMaxshengming(capability.getMaxshengming() + (newLevel * 1.4f) * 0.7f);
        capability.setFangyu(capability.getFangyu() + (newLevel * 0.3f) * 0.7f);
        capability.setGongji(capability.getGongji() + (newLevel * 0.5f) * 0.65f);
        capability.setMaxjingshenli(capability.getMaxjingshenli() + (newLevel * 2f) * 0.7f);
        capability.setMaxjingyan(capability.getMaxjingyan() + (newLevel * 1.4f) * 1.3f);
        capability.setShengming(capability.getShengming() + 1f);
        capability.setWuchuan(capability.getWuchuan() + 1f);
    }

    private static void syncAttributesToClient(ServerPlayer player, PlayerAttributeCapability capability) {
        SPacketSyncPlayerAttribute packet = new SPacketSyncPlayerAttribute(
                capability.getShengming(), capability.getMaxshengming(), capability.getJingshenli(), capability.getMaxjingshenli(),
                capability.getMingzhong(), capability.getFangyu(), capability.getGongji(), capability.getBaojilv(), capability.getBaojishanghai(),
                capability.getXixue(), capability.getShanbi(), capability.getKangbao(), capability.getJingyan(), capability.getDengji(), capability.getMaxjingyan()
        );
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void triggerUpgradeCheck(ServerPlayer player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            checkAndProcessUpgrade(player, capability);
        });
    }
}