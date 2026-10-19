package com.TovidY.kunluncontinent.tower;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.event.client.CoinDropHandler;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanDropHandler;
import com.TovidY.kunluncontinent.tower.floor.TowerFloorRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 挑战塔的「神考奖励」通道 —— 塔内怪物唯一的合法掉落来源。
 *
 * <h3>背景</h3>
 * <p>塔内怪物（{@code persistentData.TowerSpawned = true}）默认<b>什么都不掉</b>
 * （{@code KLivingDeathEvent#onMobDrops} 会清空原版战利品，死亡流程也会提前 return）。
 * 这里给"已经在走神考"的玩家开一条专属通道：击杀塔怪照样产出魂环 / 魂骨 / 魂币 / 内丹。</p>
 *
 * <h3>谁能拿</h3>
 * <ul>
 *   <li>玩家<b>有神考任务</b>（{@code godName} 非空且阶段 1~9）或<b>已经封神</b>（{@code isGod}）；</li>
 *   <li>并且击杀发生在<b>真正的挑战中</b>（{@code isTowerChallenging}），
 *       或者这只怪是 {@code /kunluncontinent tower spawn} 刷出来的测试怪
 *       （带 {@code TowerTestSpawn} 标签）—— 避免用调试指令刷怪刷奖励。</li>
 * </ul>
 *
 * <h3>层数怎么加成</h3>
 * <p>层号是 0 基索引（0 = 第 1 层，99 = 第 100 层）。<b>线性</b>成长，参数全部集中在下面几个常量里：</p>
 * <ul>
 *   <li><b>魂环</b>：<u>不加成</u>，每次击杀最多 1 个，且严格走 {@code KLConfig} 的年限概率；</li>
 *   <li><b>魂骨</b>：基础概率（按年限档位读配置）+ {@link #HUNGU_BONUS_PER_FLOOR} × 层号，封顶 {@link #HUNGU_CHANCE_CAP}；</li>
 *   <li><b>魂币</b>：触发率 +0.4%/层、钱袋率 +0.25%/层、数量每 20 层 +1、品质每层压 0.2% 的随机数；</li>
 *   <li><b>内丹</b>：掉落率 +0.4%/层（基础 20% → 第 100 层 60%），品质权重随层数上调。</li>
 * </ul>
 */
public final class TowerDropReward {

    private TowerDropReward() {
    }

    // ==================================================================================
    //  线性成长参数（调平衡只改这里）
    // ==================================================================================

    /** 魂骨：每层 +0.5% 绝对概率 → 第 100 层 +49.5%。 */
    private static final double HUNGU_BONUS_PER_FLOOR = 0.005D;

    /** 魂骨概率上限：留 15% 的"脸黑"余量，不做成 100% 必掉。 */
    public static final double HUNGU_CHANCE_CAP = 0.85D;

    /** 魂币触发率：每层 +0.4%（叠加在原版 0.10~0.45 的基础上）。 */
    private static final double COIN_CHANCE_PER_FLOOR = 0.004D;

    /** 魂币钱袋率：每层 +0.25%。 */
    private static final double COIN_BAG_PER_FLOOR = 0.0025D;

    /** 魂币品质：每层把随机数压低 0.2%（层数越高越容易出银/金币）。 */
    private static final double COIN_QUALITY_PER_FLOOR = 0.002D;

    /** 魂币数量：每 20 层额外 +1 个。 */
    private static final int COIN_COUNT_PER_FLOORS = 20;

    /** 内丹掉落率：每层 +0.4%（基础 20% → 第 100 层 60%）。 */
    private static final double NEIDAN_CHANCE_PER_FLOOR = 0.004D;

    /** 内丹掉落率上限。 */
    private static final double NEIDAN_CHANCE_CAP = 0.75D;

    /** 内丹品质：每层 +1% 的高品质权重（第 100 层约 ×2）。 */
    private static final double NEIDAN_QUALITY_PER_FLOOR = 0.01D;

    // ==================================================================================
    //  资格与层号
    // ==================================================================================

    /**
     * 这个玩家是否"有神考任务"。
     *
     * <p>刻意<u>不</u>看神考是否完成：只要手里握着神考（进行中）或已经封神，都算。
     * 按 {@code PlayerAttributeCapability} 的口径：
     * {@link PlayerAttributeCapability#hasActiveTask()}（阶段 1~9）或
     * {@link PlayerAttributeCapability#isGod()}。</p>
     */
    public static boolean hasGodExam(ServerPlayer player) {
        if (player == null) return false;
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.isGod() || cap.hasActiveTask())
                .orElse(false);
    }

    /**
     * 本次击杀是否应该发奖：有神考 + （正在挑战 或 打的是调试刷出来的测试怪）。
     */
    public static boolean rewardEligible(ServerPlayer player, Entity mob) {
        if (!hasGodExam(player)) return false;
        if (mob.getPersistentData().getBoolean("TowerTestSpawn")) return true;
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::isTowerChallenging)
                .orElse(false);
    }

    /**
     * 取这次击杀归属的层号（0 基）。
     *
     * <p>优先读怪物身上的 {@code TowerFloor}（刷怪时写入的，最准）；
     * 老存档里没有这个标签就退回玩家当前所在层；再不行按 0 处理。</p>
     */
    public static int resolveFloor(ServerPlayer player, Entity mob) {
        CompoundTag tag = mob.getPersistentData();
        int floor = tag.contains("TowerFloor") ? tag.getInt("TowerFloor")
                : player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::getCurrentTowerFloor)
                .orElse(0);
        // 防御性钳制：配表外的层号会让加成曲线跑飞
        return Math.max(0, Math.min(TowerFloorRegistry.getTotalFloors() - 1, floor));
    }

    // ==================================================================================
    //  各类掉落的层数加成
    // ==================================================================================

    /** 魂骨：层数带来的**额外绝对概率**（不含上限）。 */
    public static double hunguBonus(int floor) {
        return HUNGU_BONUS_PER_FLOOR * Math.max(0, floor);
    }

    /** 魂币：触发率 / 钱袋率 / 数量 / 品质全部按层数提升。 */
    public static void dropCoins(LivingEntity deadMob, long nianxian, ServerPlayer player, int floor) {
        int f = Math.max(0, floor);
        CoinDropHandler.tryDropCoinsBoosted(deadMob, nianxian, player,
                COIN_CHANCE_PER_FLOOR * f,
                COIN_BAG_PER_FLOOR * f,
                COIN_QUALITY_PER_FLOOR * f,
                f / COIN_COUNT_PER_FLOORS);
    }

    /** 内丹：掉落率与品质随层数提升（内丹的等级仍由魂兽年限决定，不随层数跳档）。 */
    public static void dropNeidan(LivingEntity deadMob, MobAttributeCapability cap, ServerPlayer player, int floor) {
        int f = Math.max(0, floor);
        double chance = Math.min(NEIDAN_CHANCE_CAP, 0.20D + NEIDAN_CHANCE_PER_FLOOR * f);
        float qualityMul = (float) (1.0D + NEIDAN_QUALITY_PER_FLOOR * f);
        NeidanDropHandler.tryDropNeidanBoosted(deadMob, cap, player, chance, qualityMul);
    }

    /** 仅用于日志/调试：把本层各项加成打成一行文本。 */
    public static String describe(int floor) {
        int f = Math.max(0, floor);
        return "第 " + (f + 1) + " 层加成：魂骨 +"
                + String.format("%.1f%%", hunguBonus(f) * 100)
                + "，魂币触发 +" + String.format("%.1f%%", COIN_CHANCE_PER_FLOOR * f * 100)
                + "，内丹率 " + String.format("%.0f%%", Math.min(NEIDAN_CHANCE_CAP, 0.20D + NEIDAN_CHANCE_PER_FLOOR * f) * 100);
    }
}
