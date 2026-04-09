package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.advancement.ModTriggers;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.server.SPacketSyncPlayerAttribute;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI.addWuHun;
import net.minecraft.ChatFormatting;
import org.antlr.v4.codegen.model.Sync;
import org.jetbrains.annotations.NotNull;

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

        if (!isTupoDengji(player, capability)) {
            return;
        }

        int nextLevel = currentLevel + 1;

        if (attemptUpgrade(capability)) {
            processSuccessfulUpgrade(player,capability, nextLevel);
            player.sendSystemMessage(Component.literal("§a突破成功！晋升至 " + nextLevel + " 级！"));
            SynsAPI.synsPlayerAttribute(player);
        } else {
            processUpgradeFailure(player, capability);
        }

        SynsAPI.synsPlayerAttribute(player);
    }

    static boolean isTupoDengji(ServerPlayer player, @NotNull PlayerAttributeCapability cap) {
        int level = cap.getDengji();
        if (level >= 199) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("已经满级")));
            return false;
        }
        if (level == 99) {
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.literal("已经满级，请封神后再突破").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            ));
            return false;
        }
        if (level >= 100 && level < 199) {
            int rings = getMaxRings(cap);
            int required = level / 10;
            if (rings < required) {
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("不能突破")));
                return false;
            }
            return true;
        }
        int rings = getMaxRings(cap);
        if (level >= rings * 10 + 10) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("需要吸收魂环").withStyle(ChatFormatting.RED)));
            return false;
        }
        return true;
    }

    private static void sendDeityAnnouncement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            Component message = Component.literal("§l§f【全服通告】§6风云变幻，天地共鸣！§b玩家 §e" + player.getName().getString() + " §b通过自身不懈努力，修为已臻§6§l 99级 §b极限之境！")
                    .append("\n§d§l>>> §f万众瞩目之下，我们期待他能成功夺取神位，破茧成神，成就永恒传奇！");

            server.getPlayerList().broadcastSystemMessage(message, false);

            player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    /**
     * 辅助方法：获取当前已吸收的魂环数量
     * 请根据你的 Capability 实际字段修改此处的获取逻辑
     */
    private static int getMaxRings(PlayerAttributeCapability cap) {
        int max = 0;
        Map<String, List<MobAttributeCapability>> map = cap.getMonsterCapabilityLists();
        if (map == null || map.isEmpty()) return 0;
        for (List<MobAttributeCapability> list : map.values()) {
            if (list != null && list.size() > max) max = list.size();
        }
        return max;
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
        ModTriggers.LEVEL_TRIGGER.trigger(player, finalLevel);
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
        float realRate = getFinalSuccessRate(capability);
        float penalty = capability.getMaxjingyan() * 0.8f;
        capability.setJingyan(Math.max(0, capability.getJingyan() - penalty));
        player.sendSystemMessage(Component.literal("§c突破失败！心境磨砺，突破成功率：+1%"));
        SynsAPI.synsPlayerAttribute(player);
    }

    public static float getFinalSuccessRate(PlayerAttributeCapability cap) {
        int level = cap.getDengji();
        float extraRate = cap.getTupochenggonglv();
        float baseRate = 100.0f - (level * 0.5f);
        float finalRate = baseRate + extraRate;
        return Math.max(5.0f, Math.min(95.0f, finalRate));
    }

    public static void processSuccessfulUpgrade(ServerPlayer player, PlayerAttributeCapability capability, int newLevel) {
        capability.setDengji(newLevel);
        capability.setJingyan(0.0f);

        capability.setMaxshengming(capability.getMaxshengming() + (newLevel * 1.4f) * 0.7f);
        capability.setFangyu(capability.getFangyu() + (newLevel * 0.3f) * 0.7f);
        capability.setGongji(capability.getGongji() + (newLevel * 0.5f) * 0.65f);
        capability.setMaxjingshenli(capability.getMaxjingshenli() + (newLevel * 2f) * 1.2f);
        capability.setMaxjingyan(capability.getMaxjingyan() + (newLevel * 1.4f) * 1.3f);
        capability.setShengming(capability.getShengming() + 1f);
        capability.setWuchuan(capability.getWuchuan() + 1f);
        capability.setShanbi(capability.getShanbi() + 1f);
        capability.setKangbao(capability.getMingzhong() + 1f);

        if (newLevel == 99) {
            sendDeityAnnouncement(player);
        }

        ModTriggers.LEVEL_TRIGGER.trigger(player, newLevel);
    }

    private static void syncAttributesToClient(ServerPlayer player, PlayerAttributeCapability capability) {
        SPacketSyncPlayerAttribute packet = new SPacketSyncPlayerAttribute(
                capability.getShengming(), capability.getMaxshengming(), capability.getJingshenli(), capability.getMaxjingshenli(),
                capability.getMingzhong(), capability.getFangyu(), capability.getGongji(), capability.getBaojilv(), capability.getBaojishanghai(),
                capability.getXixue(), capability.getShanbi(), capability.getKangbao(), capability.getJingyan(), capability.getDengji(), capability.getMaxjingyan(),
                (int)capability.getWuchuan(),capability.getShengmingHuifu(), capability.getBoneOnlyStats()
        );
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void triggerUpgradeCheck(ServerPlayer player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            checkAndProcessUpgrade(player, capability);
        });
    }
}