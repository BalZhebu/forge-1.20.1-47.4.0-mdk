package com.TovidY.kunluncontinent.item.neidanitems;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NeidanDropHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    public static void tryDropNeidan(LivingEntity entity, MobAttributeCapability cap, Player player) {
        tryDropNeidanInternal(entity, cap, player, 0.20D, 1.0F, 1.0F);
    }

    /**
     * 挑战塔专用：掉落率与品质都随层数提升。
     *
     * <p>内丹的<b>等级</b>（NEIDAN1~8）依旧由魂兽年限决定，不随层数跳档 ——
     * 层数只影响「掉不掉」和「掉出来是凡品还是仙品」。</p>
     *
     * @param chance     触发概率（调用方已算好上限）
     * @param qualityMul 良品及以上的权重倍率（1.0 = 原始分布）
     */
    public static void tryDropNeidanBoosted(LivingEntity entity, MobAttributeCapability cap, Player player,
                                            double chance, float qualityMul) {
        tryDropNeidanInternal(entity, cap, player, chance, qualityMul, qualityMul);
    }

    /**
     * 唯一实现：普通掉落传 (0.20, 1.0, 1.0)，塔内掉落按层数传倍率。
     *
     * @param gradeWeightMul 良品~绝品的权重倍率
     * @param xianMul        仙品额外概率的倍率
     */
    private static void tryDropNeidanInternal(LivingEntity entity, MobAttributeCapability cap, Player player,
                                              double chance, float gradeWeightMul, float xianMul) {
        CompoundTag playerData = player.getPersistentData();
        boolean forceDrop = playerData.getBoolean("KL_Neidan_Prob_Cheat");
        if (!forceDrop && RANDOM.nextDouble() > chance) return;
        long nianxian = cap.getNianxian();
        int tier = getTier(nianxian);
        if (tier == 0) return;
        // 品质作弊开关：先试「FANPIN/LIANGPIN…」这种大写写法，失败再走正常权重
        NeidanQuality quality;
        if (playerData.contains("KL_Neidan_Quality_Cheat")) {
            try {
                String cheatVal = playerData.getString("KL_Neidan_Quality_Cheat").replace("PIN", "");
                quality = NeidanQuality.valueOf(cheatVal);
            } catch (Exception e) {
                quality = calculateQuality(tier, player, gradeWeightMul, xianMul);
            }
        } else {
            quality = calculateQuality(tier, player, gradeWeightMul, xianMul);
        }
        if (quality == null) return;

        dropItem(entity, tier, quality);
    }

    private static int getTier(long nianxian) {
        if (nianxian >= 100000000) return 8;
        if (nianxian >= 10000000) return 7;
        if (nianxian >= 1000000) return 6;
        if (nianxian >= 100000) return 5;
        if (nianxian >= 10000) return 4;
        if (nianxian >= 1000) return 3;
        if (nianxian >= 100) return 2;
        if (nianxian >= 1) return 1;
        return 0;
    }

    /**
     * 抽取内丹品质。
     *
     * @param gradeWeightMul 良品~绝品的权重倍率（1.0 = 原始分布；塔内按层数上调）
     * @param xianMul        仙品额外概率的倍率（1.0 = 原始概率）
     */
    private static NeidanQuality calculateQuality(int tier, Player player, float gradeWeightMul, float xianMul) {
        CompoundTag playerData = player.getPersistentData();

        if (playerData.contains("KL_Neidan_Quality_Cheat")) {
            String cheat = playerData.getString("KL_Neidan_Quality_Cheat");
            switch (cheat) {
                case "fanpin":   return NeidanQuality.FAN;
                case "liangpin": return NeidanQuality.LIANG;
                case "shangpin": return NeidanQuality.SHANG;
                case "zhenpin":  return NeidanQuality.ZHEN;
                case "juepin":   return NeidanQuality.JUE;
                case "xianpin":  return NeidanQuality.XIAN;
            }
        }

        boolean canDropXian = tier >= 3;

        double xianProb = 0;
        if (tier == 3) xianProb = 0.003;
        else if (tier == 4) xianProb = 0.004;
        else if (tier == 5) xianProb = 0.005;
        else if (tier == 6) xianProb = 0.008;
        else if (tier == 7) xianProb = 0.010;
        else if (tier == 8) xianProb = 0.015;
        if (canDropXian && RANDOM.nextDouble() <= xianProb * xianMul) {
            return NeidanQuality.XIAN;
        }
        int totalWeight = 0;
        for (NeidanQuality q : NeidanQuality.values()) {
            if (q == NeidanQuality.XIAN) continue;
            totalWeight += weightOf(q, gradeWeightMul);
        }

        int r = RANDOM.nextInt(Math.max(1, totalWeight));
        int current = 0;
        for (NeidanQuality q : NeidanQuality.values()) {
            if (q == NeidanQuality.XIAN) continue;
            current += weightOf(q, gradeWeightMul);
            if (r < current) return q;
        }
        return NeidanQuality.FAN;
    }

    /** 单档权重：凡品保持原样，其余按倍率放大（至少 1，避免被四舍五入抹平）。 */
    private static int weightOf(NeidanQuality quality, float mul) {
        if (quality == NeidanQuality.FAN) return quality.baseWeight;
        return Math.max(1, Math.round(quality.baseWeight * mul));
    }

    private static void dropItem(LivingEntity entity, int tier, NeidanQuality quality) {
        Item item = switch (tier) {
            case 1 -> ModItems.NEIDAN1.get();
            case 2 -> ModItems.NEIDAN2.get();
            case 3 -> ModItems.NEIDAN3.get();
            case 4 -> ModItems.NEIDAN4.get();
            case 5 -> ModItems.NEIDAN5.get();
            case 6 -> ModItems.NEIDAN6.get();
            case 7 -> ModItems.NEIDAN7.get();
            case 8 -> ModItems.NEIDAN8.get();
            default -> null;
        };

        if (item != null) {
            ItemStack stack = new ItemStack(item);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("Quality", quality.name());

            entity.spawnAtLocation(stack);
        }
    }
}
