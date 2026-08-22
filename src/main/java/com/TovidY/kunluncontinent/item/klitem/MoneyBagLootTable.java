package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MoneyBagLootTable {

    /**
     * 奖励条目包装类
     */
    public record DropEntry(Item item, int minCount, int maxCount, int weight) {}

    /**
     * 钱袋类型枚举
     */
    public enum BagType {
        SMALL,   // 小钱袋
        MIDDLE,  // 中钱袋
        BIG      // 大钱袋
    }

    /**
     * 根据钱袋类型和随机数生成掉落物 List
     */
    public static List<ItemStack> generateLoot(BagType bagType, RandomSource random) {
        List<ItemStack> result = new ArrayList<>();
        List<DropEntry> entries = getEntriesForBag(bagType);

        if (entries.isEmpty()) return result;

        // 计算总权重
        int totalWeight = entries.stream().mapToInt(DropEntry::weight).sum();

        // 决定本次右键产出多少次奖励
        int rollTimes = getRollTimes(bagType, random);

        for (int i = 0; i < rollTimes; i++) {
            int roll = random.nextInt(totalWeight);
            int current = 0;

            for (DropEntry entry : entries) {
                current += entry.weight();
                if (roll < current) {
                    // 决定该币种的数量
                    int count = entry.minCount() + random.nextInt(entry.maxCount() - entry.minCount() + 1);
                    if (count > 0) {
                        result.add(new ItemStack(entry.item(), count));
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 概率表配置模板（经严格削弱调整）
     */
    private static List<DropEntry> getEntriesForBag(BagType bagType) {
        switch (bagType) {
            case SMALL:
                // 小钱袋概率模板（总权重 1000）：
                // 铜魂币 (权重 890 / 89.0%)：数量 1 ~ 5
                // 银魂币 (权重 100 / 10.0%)：数量 1 ~ 1
                // 金魂币 (权重  10 /  1.0%)：数量 1 ~ 1（极低概率彩蛋）
                return List.of(
                        new DropEntry(ModItems.COPPER_SOUL_COIN.get(), 1, 5, 890),
                        new DropEntry(ModItems.SILVER_SOUL_COIN.get(), 1, 1, 100),
                        new DropEntry(ModItems.GOLDEN_SOUL_COIN.get(), 1, 1, 10)
                );

            case MIDDLE:
                // 中钱袋概率模板（总权重 100）：
                // 铜魂币 (权重 65 / 65%)：数量 5 ~ 15
                // 银魂币 (权重 30 / 30%)：数量 1 ~ 3
                // 金魂币 (权重  5 /  5%)：数量 1 ~ 1
                return List.of(
                        new DropEntry(ModItems.COPPER_SOUL_COIN.get(), 5, 15, 65),
                        new DropEntry(ModItems.SILVER_SOUL_COIN.get(), 1, 3, 30),
                        new DropEntry(ModItems.GOLDEN_SOUL_COIN.get(), 1, 1, 5)
                );

            case BIG:
                // 大钱袋概率模板（总权重 100）：
                // 铜魂币 (权重 35 / 35%)：数量 15 ~ 25
                // 银魂币 (权重 50 / 50%)：数量 3 ~ 6
                // 金魂币 (权重 15 / 15%)：数量 1 ~ 2
                return List.of(
                        new DropEntry(ModItems.COPPER_SOUL_COIN.get(), 15, 25, 35),
                        new DropEntry(ModItems.SILVER_SOUL_COIN.get(), 3, 6, 50),
                        new DropEntry(ModItems.GOLDEN_SOUL_COIN.get(), 1, 2, 15)
                );

            default:
                return List.of();
        }
    }

    /**
     * 抽取次数（抽取不同币种组合的轮数）
     */
    private static int getRollTimes(BagType bagType, RandomSource random) {
        return switch (bagType) {
            case SMALL -> 1;                       // 小钱袋只触发 1 次
            case MIDDLE -> 1 + random.nextInt(2); // 中钱袋 1~2 次
            case BIG -> 2;                        // 大钱袋固定 2 次
        };
    }
}