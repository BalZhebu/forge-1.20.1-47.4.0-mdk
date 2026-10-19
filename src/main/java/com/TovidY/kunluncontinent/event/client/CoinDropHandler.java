package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoinDropHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    private static final Map<UUID, Boolean> COIN_DROP_TEST_MAP = new ConcurrentHashMap<>();
    /**
     * 设置玩家的掉落测试状态
     * @param mode "true" -> 强制100%掉落 | "false" -> 强制0%掉落 | "reset" -> 恢复正常概率
     */
    public static void setTestMode(UUID playerUuid, String mode) {
        if ("true".equalsIgnoreCase(mode)) {
            COIN_DROP_TEST_MAP.put(playerUuid, Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(mode)) {
            COIN_DROP_TEST_MAP.put(playerUuid, Boolean.FALSE);
        } else if ("reset".equalsIgnoreCase(mode)) {
            COIN_DROP_TEST_MAP.remove(playerUuid);
        }
    }

    /**
     * 获取玩家当前的测试状态 (null 表示正常机制)
     */
    public static Boolean getTestStatus(UUID playerUuid) {
        return COIN_DROP_TEST_MAP.get(playerUuid);
    }

    public static void tryDropCoins(LivingEntity entity, long nianxian, Player player) {
        doDropCoins(entity, nianxian, player, 0.0D, 0.0D, 0.0D, 0);
    }

    /**
     * 挑战塔专用：触发率 / 钱袋率 / 品质 / 数量都随层数线性提升。
     *
     * <p>加成用<b>加在原有数值上的绝对量</b>而不是乘系数：
     * 原来的触发率已经被 {@code min(0.45, ...)} 压过一次，乘系数在低年限怪身上几乎看不出变化；
     * 加法才能让"第 1 层 → 第 100 层"有一条稳定可见的曲线。</p>
     *
     * @param chanceBonus 触发率加成（0.10+年限加成 之上的绝对百分比）
     * @param bagBonus    钱袋率加成
     * @param qualityShift 品质偏移：直接从随机数里减掉，越大越容易出高品质
     * @param extraCount  额外数量
     */
    public static void tryDropCoinsBoosted(LivingEntity entity, long nianxian, Player player,
                                           double chanceBonus, double bagBonus,
                                           double qualityShift, int extraCount) {
        doDropCoins(entity, nianxian, player, chanceBonus, bagBonus, qualityShift, extraCount);
    }

    /** 唯一实现：普通掉落四项加成全传 0，塔内掉落按层数传值。 */
    private static void doDropCoins(LivingEntity entity, long nianxian, Player player,
                                    double chanceBonus, double bagBonus,
                                    double qualityShift, int extraCount) {
        if (nianxian <= 0) return;
        Boolean testStatus = (player != null) ? COIN_DROP_TEST_MAP.get(player.getUUID()) : null;
        if (testStatus != null) {
            if (!testStatus) {
                return;
            }
        } else {
            double baseTriggerChance = 0.10;
            double extraChance = getBonusChanceByNianxian(nianxian);
            // 先按原版上限压到 0.45，再叠加层数加成（普通掉落加成恒为 0，行为与改造前完全一致）
            double base = Math.min(0.45, baseTriggerChance + extraChance);
            double totalTriggerChance = Math.min(0.80, base + chanceBonus);
            if (RANDOM.nextDouble() > totalTriggerChance) {
                return;
            }
        }
        // 2.判定掉落钱袋还是硬币
        boolean isBag;
        if (Boolean.TRUE.equals(testStatus)) {
            // 处于 100% 测试模式下，钱袋触发概率提升至 50%（便于快速测试钱袋）
            isBag = RANDOM.nextDouble() < 0.50;
        } else {
            // 正常逻辑：1% 基础概率，最高15%；塔内再叠加层数加成
            double bagChance = Math.min(0.50, 0.01 + getBagBonusByNianxian(nianxian) + bagBonus);
            isBag = RANDOM.nextDouble() < bagChance;
        }

        Item dropItem;
        if (isBag) {
            dropItem = rollMoneyBag(nianxian, testStatus, qualityShift);
        } else {
            dropItem = rollSoulCoin(nianxian, testStatus, qualityShift);
        }

        if (dropItem != null) {
            int count = rollDropCount(nianxian, testStatus, extraCount);
            ItemStack stack = new ItemStack(dropItem, count);
            entity.spawnAtLocation(stack);
        }
    }

    private static double getBonusChanceByNianxian(long nianxian) {
        if (nianxian >= 10000000) return 0.50;
        if (nianxian >= 1000000)  return 0.40;
        if (nianxian >= 100000)   return 0.30;
        if (nianxian >= 10000)    return 0.20;
        if (nianxian >= 1000)     return 0.10;
        if (nianxian >= 100)      return 0.05;
        return 0.0;
    }

    private static double getBagBonusByNianxian(long nianxian) {
        if (nianxian >= 10000000) return 0.14;
        if (nianxian >= 1000000)  return 0.11;
        if (nianxian >= 100000)   return 0.08;
        if (nianxian >= 10000)    return 0.05;
        if (nianxian >= 1000)     return 0.03;
        if (nianxian >= 100)      return 0.01;
        return 0.0;
    }

    /**
     * 抽取魂币品质。
     *
     * @param qualityShift 品质偏移（塔内按层数从随机数里减掉，越大越容易出高品质）
     */
    private static Item rollSoulCoin(long nianxian, Boolean testStatus, double qualityShift) {
        // 测试模式下，概率平分以方便测试每一种品质
        if (Boolean.TRUE.equals(testStatus)) {
            double r = RANDOM.nextDouble();
            if (r < 0.33) return ModItems.GOLDEN_SOUL_COIN.get();
            if (r < 0.66) return ModItems.SILVER_SOUL_COIN.get();
            return ModItems.COPPER_SOUL_COIN.get();
        }

        double r = RANDOM.nextDouble() - qualityShift;
        if (nianxian >= 1000000) {
            if (r < 0.70) return ModItems.GOLDEN_SOUL_COIN.get();
            return ModItems.SILVER_SOUL_COIN.get();
        } else if (nianxian >= 10000) {
            if (r < 0.20) return ModItems.GOLDEN_SOUL_COIN.get();
            if (r < 0.80) return ModItems.SILVER_SOUL_COIN.get();
            return ModItems.COPPER_SOUL_COIN.get();
        } else if (nianxian >= 100) {
            if (r < 0.02) return ModItems.GOLDEN_SOUL_COIN.get();
            if (r < 0.35) return ModItems.SILVER_SOUL_COIN.get();
            return ModItems.COPPER_SOUL_COIN.get();
        } else {
            if (r < 0.05) return ModItems.SILVER_SOUL_COIN.get();
            return ModItems.COPPER_SOUL_COIN.get();
        }
    }

    private static Item rollMoneyBag(long nianxian, Boolean testStatus, double qualityShift) {
        if (Boolean.TRUE.equals(testStatus)) {
            double r = RANDOM.nextDouble();
            if (r < 0.33) return ModItems.BIG_MONEY_BAG.get();
            if (r < 0.66) return ModItems.MIDDLE_MONEY_BAG.get();
            return ModItems.SMALL_MONEY_BAG.get();
        }

        double r = RANDOM.nextDouble() - qualityShift;
        if (nianxian >= 1000000) {
            if (r < 0.20) return ModItems.BIG_MONEY_BAG.get();
            if (r < 0.50) return ModItems.MIDDLE_MONEY_BAG.get();
            return ModItems.SMALL_MONEY_BAG.get();
        } else if (nianxian >= 10000) {
            if (r < 0.10) return ModItems.BIG_MONEY_BAG.get();
            if (r < 0.40) return ModItems.MIDDLE_MONEY_BAG.get();
            return ModItems.SMALL_MONEY_BAG.get();
        } else {
            if (r < 0.01) return ModItems.BIG_MONEY_BAG.get();
            if (r < 0.25) return ModItems.MIDDLE_MONEY_BAG.get();
            return ModItems.SMALL_MONEY_BAG.get();
        }
    }

    private static int rollDropCount(long nianxian, Boolean testStatus, int extraCount) {
        if (Boolean.TRUE.equals(testStatus)) return 3 + extraCount;
        if (nianxian >= 10000000) return 3 + RANDOM.nextInt(3) + extraCount;
        if (nianxian >= 100000)  return 2 + RANDOM.nextInt(2) + extraCount;
        if (nianxian >= 1000)    return 1 + RANDOM.nextInt(2) + extraCount;
        return 1 + extraCount;
    }
}