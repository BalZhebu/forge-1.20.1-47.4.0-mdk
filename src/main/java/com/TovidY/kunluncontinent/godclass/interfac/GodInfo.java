package com.TovidY.kunluncontinent.godclass.interfac;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.chat.Component;
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
        // 如果是累积模式（比如修罗神），前8考不发，第9考全发
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

    public GodInfo addTask(int stage, GodTaskType type, String target, int count, String desc) {
        examPools.computeIfAbsent(stage, k -> new ArrayList<>()).add(new GodTask(type, target, count, desc));
        return this;
    }

    public GodInfo addItemReward(int stage, Item item, int count, String desc) {
        rewardPools.computeIfAbsent(stage, k -> new ArrayList<>()).add(player -> {
            player.getInventory().add(new ItemStack(item, count));
        });
        rewardDescriptions.computeIfAbsent(stage, k -> new ArrayList<>()).add(desc);
        return this;
    }

    public String getRewardTooltip(int stage) {
        List<String> descs = rewardDescriptions.get(stage);
        if (descs == null || descs.isEmpty()) return "无";
        return String.join(", ", descs);
    }

    public GodInfo addAttrReward(int stage, String attrKey, float value, String desc) {
        rewardPools.computeIfAbsent(stage, k -> new ArrayList<>()).add(player -> {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (attrKey.equals("gongji")) cap.setGongji(cap.getGongji() + value);
                if (attrKey.equals("maxshengming")) cap.setMaxshengming(cap.getMaxshengming() + value);
                if (attrKey.equals("dengji")) cap.setDengji((int) (cap.getDengji() + value));
                SynsAPI.synsPlayerAttribute(player);
            });
        });
        rewardDescriptions.computeIfAbsent(stage, k -> new ArrayList<>()).add(desc);
        return this;
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