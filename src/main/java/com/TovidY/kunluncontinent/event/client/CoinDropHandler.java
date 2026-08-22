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

    // 内存数据，不进行 NBT 保存。用 Boolean 表示：Boolean.TRUE=强制百分百掉落，Boolean.FALSE=强制关闭掉落
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
        if (nianxian <= 0) return;
        Boolean testStatus = (player != null) ? COIN_DROP_TEST_MAP.get(player.getUUID()) : null;
        if (testStatus != null) {
            if (!testStatus) {
                return;
            }
        } else {
            // 正常逻辑：35% 基础概率，随年限增加
            double baseTriggerChance = 0.10;
            double extraChance = getBonusChanceByNianxian(nianxian);
            double totalTriggerChance = Math.min(0.45, baseTriggerChance + extraChance);
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
            // 正常逻辑：1% 基础概率，最高15%
            double bagChance = 0.01 + getBagBonusByNianxian(nianxian);
            isBag = RANDOM.nextDouble() < bagChance;
        }

        Item dropItem;
        if (isBag) {
            dropItem = rollMoneyBag(nianxian, testStatus);
        } else {
            dropItem = rollSoulCoin(nianxian, testStatus);
        }

        if (dropItem != null) {
            int count = rollDropCount(nianxian, testStatus);
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

    private static Item rollSoulCoin(long nianxian, Boolean testStatus) {
        // 测试模式下，概率平分以方便测试每一种品质
        if (Boolean.TRUE.equals(testStatus)) {
            double r = RANDOM.nextDouble();
            if (r < 0.33) return ModItems.GOLDEN_SOUL_COIN.get();
            if (r < 0.66) return ModItems.SILVER_SOUL_COIN.get();
            return ModItems.COPPER_SOUL_COIN.get();
        }

        double r = RANDOM.nextDouble();
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

    private static Item rollMoneyBag(long nianxian, Boolean testStatus) {
        if (Boolean.TRUE.equals(testStatus)) {
            double r = RANDOM.nextDouble();
            if (r < 0.33) return ModItems.BIG_MONEY_BAG.get();
            if (r < 0.66) return ModItems.MIDDLE_MONEY_BAG.get();
            return ModItems.SMALL_MONEY_BAG.get();
        }

        double r = RANDOM.nextDouble();
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

    private static int rollDropCount(long nianxian, Boolean testStatus) {
        if (Boolean.TRUE.equals(testStatus)) return 3;
        if (nianxian >= 10000000) return 3 + RANDOM.nextInt(3);
        if (nianxian >= 100000)  return 2 + RANDOM.nextInt(2);
        if (nianxian >= 1000)    return 1 + RANDOM.nextInt(2);
        return 1;
    }
}