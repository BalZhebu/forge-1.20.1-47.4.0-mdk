package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;

public class QualityCalculator {
    public static int calculate(IItemHandler inv) {
        // 1. 初始权重 (破碎概率最高，红色极低)
        float[] weights = {100f, 50f, 20f, 10f, 5f, 1f};

        // 2. 检查 0-4 槽内丹品质 (假设内丹 NBT 里存了 Quality)
        for (int i = 0; i < 5; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            int neidanQuality = stack.getOrCreateTag().getInt("Quality");
            // 每一个等级的内丹品质，提升对应档位的权重
            for (int q = 1; q <= neidanQuality; q++) {
                weights[Math.min(q, 5)] += 15f;
            }
        }

        // 3. 检查 17 槽丹渣块
        if (inv.getStackInSlot(17).is(ModBlocks.DROSS_BLOCK.get().asItem())) {
            weights[0] /= 2; // 破碎概率减半
            weights[3] += 20; // 紫色概率提升
            weights[4] += 10; // 金色概率提升
        }

        // 4. 权重随机抽奖
        float totalWeight = 0;
        for (float w : weights) totalWeight += w;
        float r = new Random().nextFloat() * totalWeight;

        float current = 0;
        for (int i = 0; i < weights.length; i++) {
            current += weights[i];
            if (r <= current) return i; // 返回 0-5 的品级索引
        }
        return 0;
    }
}
