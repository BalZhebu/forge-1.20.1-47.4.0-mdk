package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class AlchemicalCalculator {
    public static int calculateResultQuality(IItemHandler handler) {
        // 索引说明：0碎, 1散, 2丹, 3灵, 4宝, 5仙
        double[] weights = {40.0, 30.0, 15.0, 10.0, 4.0, 1.0};

        // 1. 丹渣块角色：绝对稳定剂（直接抹除破碎权重）
        boolean hasDrossBlock = handler.getStackInSlot(17).is(ModBlocks.DROSS_BLOCK.get().asItem());
        if (hasDrossBlock) {
            double brokenWeight = weights[0];
            weights[0] = 0;
            weights[1] += brokenWeight; // 转移给药散
        }

        // 2. 扫描内丹，计算“提质压力”
        double pressure = 0;
        boolean hasXian = false;
        boolean hasJue = false;

        for (int i = 0; i < 5; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof NeidanItem) {
                String q = stack.getOrCreateTag().getString("Quality");
                pressure += switch (q) {
                    case "XIAN" -> { hasXian = true; yield 60.0; } // 仙品提供极强压力
                    case "JUE"  -> { hasJue = true; yield 40.0; } // 绝品
                    case "ZHEN" -> 20.0;
                    case "SHANG"-> 10.0;
                    default     -> 0.0;
                };
            }
        }

        // 3. 执行推土机逻辑：将低级权重向高级推
        for (int i = 0; i < 4; i++) {
            double shift = Math.min(weights[i], pressure);
            weights[i] -= shift;
            weights[i + 1] += shift;
            pressure -= shift;
            if (pressure <= 0) break;
        }

        // 4. 最终保底映射（硬性覆盖玩家要求的数值）
        if (hasXian) {
            // 仙品内丹炼制：灵35%, 宝60%, 仙5%
            weights[0]=0; weights[1]=0; weights[2]=0;
            weights[3]=35.0; weights[4]=60.0; weights[5]=5.0;
        } else if (hasJue && weights[2] <= 0) {
            // 绝品内丹炼制且低级已清空：灵84%, 宝15%, 仙1%
            weights[0]=0; weights[1]=0; weights[2]=0;
            weights[3]=84.0; weights[4]=15.0; weights[5]=1.0;
        }

        // 5. 权重抽奖
        double totalWeight = 0;
        for (double w : weights) totalWeight += w;
        double r = Math.random() * totalWeight;
        double count = 0;
        for (int i = 0; i < weights.length; i++) {
            count += weights[i];
            if (r <= count) return i;
        }
        return 1; // 兜底返回药散
    }
}