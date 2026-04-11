package com.TovidY.kunluncontinent.godclass.interfac;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class GodInfo {
    public final String id;
    public final String name;
    private boolean isCumulative = false;

    private final Map<Integer, List<String>> rewardDescriptions = new HashMap<>();

    private final Map<Integer, List<GodTask>> examPools = new HashMap<>();

    private final Map<Integer, List<GodReward>> rewardPools = new HashMap<>();

    public GodInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public GodInfo setCumulative(boolean val) {
        this.isCumulative = val;
        return this;
    }

    public void executeRewards(int stage, Player player) {
        if (this.isCumulative) {
            if (stage < 9) {
                player.sendSystemMessage(Component.literal("§e奖励已积攒，完成第九考后统一发放！"));
            } else {
                for (int i = 1; i <= 9; i++) {
                    grantStageRewards(i, player);
                }
            }
        } else {
            grantStageRewards(stage, player);
        }
    }

    private void grantStageRewards(int stage, Player player) {
        List<GodReward> rewards = rewardPools.get(stage);
        if (rewards != null) {
            rewards.forEach(r -> r.grant(player));
        }
    }

    private String formatRewardDesc(String desc) {
        if (desc == null || desc.length() <= 16) return desc;
        StringBuilder sb = new StringBuilder();
        String currentColor = "§f";
        int count = 0;
        for (int i = 0; i < desc.length(); i++) {
            char c = desc.charAt(i);
            if (c == '§' && i + 1 < desc.length()) {
                currentColor = "§" + desc.charAt(i + 1);
                sb.append(currentColor);
                i++;
                continue;
            }
            sb.append(c);
            count++;
            if (count >= 16 && i < desc.length() - 1) {
                sb.append("\n").append(currentColor); // 换行并补颜色
                count = 0;
            }
        }
        return sb.toString();
    }

    public GodInfo addTask(int stage, GodTaskType type, String target, int count, String desc) {
        examPools.computeIfAbsent(stage, k -> new ArrayList<>())
                .add(new GodTask(type, target, count, desc));
        return this;
    }

    public GodInfo addItemReward(int stage, Item item, int count, String desc) {
        String formatted = formatRewardDesc(desc);
        rewardPools.computeIfAbsent(stage, k -> new ArrayList<>()).add(player -> {
            player.getInventory().add(new ItemStack(item, count));
        });
        rewardDescriptions.computeIfAbsent(stage, k -> new ArrayList<>()).add(formatted);
        return this;
    }

    public String getRewardTooltip(int stage) {
        List<String> descs = rewardDescriptions.get(stage);
        if (descs == null || descs.isEmpty()) return "无";
        return String.join(", ", descs);
    }

    public GodInfo addAttrReward(int stage, String attrKey, float value, String desc) {
        String formatted = formatRewardDesc(desc);
        rewardPools.computeIfAbsent(stage, k -> new ArrayList<>()).add(player -> {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (attrKey.equals("gongji")) cap.setGongji(cap.getGongji() + value);
                if (attrKey.equals("maxshengming")) cap.setMaxshengming(cap.getMaxshengming() + value);
                if (attrKey.equals("maxjingshenli")) cap.setMaxjingshenli(cap.getMaxjingshenli() + value);
                if (attrKey.equals("dengji")) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        int currentLevel = cap.getDengji();
                        int rewardLevels = (int) value;
                        for (int i = 0; i < rewardLevels; i++) {
                            int nextLevel = currentLevel + 1;
                            if (currentLevel >= 99) {
                                applyLevelStatsOnly(serverPlayer, cap, 99);
                            } else {
                                PlayerUpgradeSystem.processSuccessfulUpgrade(serverPlayer, cap, nextLevel);
                                currentLevel = nextLevel;
                            }
                        }
                    }
                }
                SynsAPI.synsPlayerAttribute(player);
            });
        });
        rewardDescriptions.computeIfAbsent(stage, k -> new ArrayList<>()).add(formatted);
        return this;
    }

    private void applyLevelStatsOnly(ServerPlayer player, PlayerAttributeCapability capability, int level) {
        capability.setMaxshengming(capability.getMaxshengming() + (level * 1.4f) * 0.7f);
        capability.setFangyu(capability.getFangyu() + (level * 0.3f) * 0.7f);
        capability.setGongji(capability.getGongji() + (level * 0.5f) * 0.65f);
        capability.setMaxjingshenli(capability.getMaxjingshenli() + (level * 2f) * 1.2f);
        capability.setMaxjingyan(capability.getMaxjingyan() + (level * 1.4f) * 1.3f);
        capability.setShengming(capability.getShengming() + 1f);
        capability.setWuchuan(capability.getWuchuan() + 1f);
        capability.setShanbi(capability.getShanbi() + 1f);
        capability.setKangbao(capability.getMingzhong() + 1f);
        player.sendSystemMessage(Component.literal("§d§l【神赐】 §f由于你已达99级巅峰，无法升级百级，但神赐属性已强化！"));
    }

    public GodInfo addCommandReward(int stage, String command) {
        rewardPools.computeIfAbsent(stage, k -> new ArrayList<>()).add(player -> {
            if (player.getServer() != null) {
                player.getServer().getCommands().performPrefixedCommand(
                        player.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                        command.replace("%player%", player.getScoreboardName())
                );
            }
        });
        return this;
    }

    public GodTask getRandomTask(int stage, RandomSource random) {
        List<GodTask> pool = examPools.get(stage);
        if (pool == null || pool.isEmpty()) return null;
        return pool.get(random.nextInt(pool.size()));
    }
}