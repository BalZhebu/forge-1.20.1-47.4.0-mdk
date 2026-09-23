package com.TovidY.kunluncontinent.godclass.interfac;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketChangeCamera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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

    private final List<GodReward> finalRewards = new ArrayList<>();
    private final List<String> finalRewardDescriptions = new ArrayList<>();

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
        if (stage < 9) {
            if (this.isCumulative) {
                // 积攒型神考：前八考的奖励先不发，留到第九考后的飞升动画结束再统一结算
                player.sendSystemMessage(Component.literal("§e奖励已积攒，完成第九考后统一发放！"));
            } else {
                grantStageRewards(stage, player);
            }
            return;
        }

        // ⚠️ 第九考的奖励**不在这里发** —— 统一交给飞升动画结束后的 finalizeAscension。
        // （老代码这里也发了一遍，导致第九考奖励发两次：积攒型更是 1~9 考全部双倍。）
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            int currentLevel = cap.getDengji();
            if (currentLevel < 99) {
                serverPlayer.sendSystemMessage(Component.literal("§c§l【传承受阻】 §f你当前的等级为 §e" + currentLevel + " §f级。"));
                serverPlayer.sendSystemMessage(Component.literal("§f必须达到 §699级巅峰 §f方可承载 §b" + this.name + " §f神位！"));
                return;
            }
            if (player.getServer() != null) {
                Component message = Component.literal("§d§l[神界传音] §f玩家 §b§l" + player.getScoreboardName() +
                        " §f正在接受 §e§l" + this.name + " §f最后的传承，神位晋升中...");
                player.getServer().getPlayerList().broadcastSystemMessage(message, false);
            }
            startAscensionAnimation(serverPlayer);
        });
    }

    public void startAscensionAnimation(ServerPlayer player) {
        NetworkHandler.sendToClient(new PacketChangeCamera(1), player);
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 220, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 250, 5, false, false));
        final int totalSeconds = 10;
        new Thread(() -> {
            try {
                for (int i = 0; i < totalSeconds; i++) {
                    final int currentSecond = i;
                    player.server.execute(() -> renderStepEffect(player, currentSecond));
                    Thread.sleep(1000);
                }
                player.server.execute(() -> finalizeAscension(player));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderStepEffect(ServerPlayer player, int second) {
        ServerLevel level = player.serverLevel();
        player.sendSystemMessage(Component.literal("§6§l>>> 神格融合中 " + (second * 10 + 10) + "% <<<"), true);
        for(int j = 0; j < 3; j++) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(player.getX() + (player.getRandom().nextDouble() - 0.5) * 8,
                        player.getY(),
                        player.getZ() + (player.getRandom().nextDouble() - 0.5) * 8);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }
        ParticleOptions particle = this.id.contains("sea") ? ParticleTypes.SOUL :
                (this.id.contains("asura") ? ParticleTypes.FLAME : ParticleTypes.GLOW);
        level.sendParticles(particle, player.getX(), player.getY() + 1, player.getZ(), 300, 1.5, 3.0, 1.5, 0.15);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.2f, 0.5f + (second * 0.15f));
    }

    private void finalizeAscension(ServerPlayer player) {
        if (this.isCumulative) {
            for (int i = 1; i <= 9; i++) {
                grantStageRewards(i, player);
            }
        } else {
            grantStageRewards(9, player);
        }

        if (!finalRewards.isEmpty()) {
            finalRewards.forEach(r -> r.grant(player));
        }

        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 5, 0, 0, 0, 1.0);
        player.serverLevel().sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 50, 0, 0, 0, 1.0);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.5f);

        Component chatMsg = Component.literal("§d§l[神界通报] §b§l" + player.getScoreboardName() + " §f历经磨难，终成 §6§l" + this.name + " §f尊位！");
        Component titleMsg = Component.literal("§6§l" + this.name + " §b§l传承完成");
        Component subtitleMsg = Component.literal("§f恭喜玩家 §e" + player.getScoreboardName() + " §f成就神位");
        if (player.server != null) {
            player.server.getPlayerList().broadcastSystemMessage(chatMsg, false);
            for (ServerPlayer allPlayer : player.server.getPlayerList().getPlayers()) {
                allPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 70, 20));
                allPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(titleMsg));
                allPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitleMsg));
            }
        }

        NetworkHandler.sendToClient(new PacketChangeCamera(0), player);
        player.sendSystemMessage(Component.literal("§a§l神格已成，凡躯已脱！"));
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
            ItemStack stack = new ItemStack(item, count);
            // 背包塞不下就掉在地上，不要静默吞掉玩家的奖励
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
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
        rewardPools.computeIfAbsent(stage, k -> new ArrayList<>())
                .add(player -> grantAttr(player, attrKey, value));
        rewardDescriptions.computeIfAbsent(stage, k -> new ArrayList<>()).add(formatted);
        return this;
    }


    public GodInfo addFinalAttrReward(String attrKey, float value, String desc) {
        String formatted = formatRewardDesc(desc);
        this.finalRewards.add(player -> grantAttr(player, attrKey, value));
        this.finalRewardDescriptions.add(formatted);
        return this;
    }

    /**
     * 发放一条属性奖励。
     *
     * <p>考中奖励和神位奖励共用这一份逻辑 —— 之前两处各写一遍，结果
     * {@code addFinalAttrReward} 漏了 {@code dengji} 分支，
     * 于是每个神配置里的「神位奖励：百级」都石沉大海（玩家封神后等级纹丝不动）。</p>
     */
    private static void grantAttr(Player player, String attrKey, float value) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            switch (attrKey) {
                case "gongji" -> cap.setGongji(cap.getGongji() + value);
                case "maxshengming" -> cap.setMaxshengming(cap.getMaxshengming() + value);
                case "maxjingshenli" -> cap.setMaxjingshenli(cap.getMaxjingshenli() + value);
                case "fangyu" -> cap.setFangyu(cap.getFangyu() + value);
                case "baojishanghai" -> cap.setBaojishanghai(cap.getBaojishanghai() + value);
                case "dengji" -> grantLevels(player, cap, (int) value);
                default -> {
                }
            }
            SynsAPI.synsPlayerAttribute(player);
        });
    }

    /**
     * {@code dengji} 奖励：真正把等级顶上去（不是加属性）。
     *
     * <p><b>为什么看 {@code isGod()}：</b>{@code PlayerUpgradeSystem.isTupoDengji} 里
     * 99 级在**封神之前**是硬上限（"已经满级，请封神后再突破"），
     * 所以考试途中拿到的 +1 级卡在 99 时只能折算成属性强化；
     * 而飞升动画结束时 {@code isGod} 已经为 true，这时才允许把 99 级顶到 100 级。</p>
     */
    private static void grantLevels(Player player, PlayerAttributeCapability cap, int levels) {
        if (!(player instanceof ServerPlayer serverPlayer) || levels <= 0) {
            return;
        }
        int currentLevel = cap.getDengji();
        for (int i = 0; i < levels; i++) {
            if (currentLevel >= 99 && !cap.isGod()) {
                applyLevelStatsOnly(serverPlayer, cap, 99);
            } else {
                int nextLevel = currentLevel + 1;
                PlayerUpgradeSystem.processSuccessfulUpgrade(serverPlayer, cap, nextLevel);
                currentLevel = nextLevel;
            }
        }
    }

    private static void applyLevelStatsOnly(ServerPlayer player, PlayerAttributeCapability capability, int level) {
        capability.setMaxshengming(capability.getMaxshengming() + (level * 1.4f) * 0.7f);
        capability.setFangyu(capability.getFangyu() + (level * 0.3f) * 0.7f);
        capability.setGongji(capability.getGongji() + (level * 0.5f) * 0.65f);
        capability.setMaxjingshenli(capability.getMaxjingshenli() + (level * 2f) * 1.2f);
        capability.setMaxjingyan(capability.getMaxjingyan() + (level * 1.4f) * 1.3f);
        capability.setShengming(capability.getShengming() + 1f);
        capability.setWuchuan(capability.getWuchuan() + 1f);
        capability.setShanbi(capability.getShanbi() + 1f);
        capability.setKangbao(capability.getKangbao() + 1f);
        player.sendSystemMessage(Component.literal("§d§l【神赐】 §f由于你已达99级巅峰，无法升级百级，但各类属性已强化！"));
    }

    public GodTask getRandomTask(int stage, RandomSource random) {
        List<GodTask> pool = examPools.get(stage);
        if (pool == null || pool.isEmpty()) return null;
        return pool.get(random.nextInt(pool.size()));
    }
}