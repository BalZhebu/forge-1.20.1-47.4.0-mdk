package com.TovidY.kunluncontinent.capability.mobattributes;

import java.util.Random;

public class HunhuanWeakener {
    private static final Random RANDOM = new Random();

    /**
     * NPC 魂环的专属属性增强倍率（整体调低削弱）。
     * 1.0 表示与玩家效果一致。
     * 1.3 表示 NPC 保留的属性比例在基础削弱结果上再提升 30%（例如原本只保留 50% 属性，乘以 1.3 后保留 65%）。
     * 如果你想让 NPC 完全不削弱，甚至可以直接设为 2.0 或更高！
     */
    private static final double NPC_RATIO_MULTIPLIER = 1.50;

    private record Tier(long maxNianxian, double ratioMin, double ratioMax) {}

    // ==========================================
    // 1. 武功 (物理攻击) —— 独立档位表
    // ==========================================
    private static final Tier[] WUGONG_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.85, 0.95),
            new Tier(10000, 0.70, 0.85),
            new Tier(100000, 0.65, 0.80),
            new Tier(1000000, 0.60, 0.75),
            new Tier(10000000, 0.55, 0.60),
            new Tier(Long.MAX_VALUE, 0.50, 0.50),
    };

    // ==========================================
    // 2. 武防 (物理防御) —— 独立档位表
    // ==========================================
    private static final Tier[] WUFANG_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.80, 0.90),
            new Tier(10000, 0.70, 0.85),
            new Tier(100000, 0.60, 0.75),
            new Tier(1000000, 0.50, 0.65),
            new Tier(10000000, 0.45, 0.55),
            new Tier(Long.MAX_VALUE, 0.40, 0.45),
    };

    // ==========================================
    // 3. 最大生命 —— 独立档位表
    // ==========================================
    private static final Tier[] MAX_SHENGMING_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.85, 0.95),
            new Tier(10000, 0.75, 0.90),
            new Tier(100000, 0.65, 0.80),
            new Tier(1000000, 0.55, 0.75),
            new Tier(10000000, 0.50, 0.65),
            new Tier(Long.MAX_VALUE, 0.45, 0.60),
    };

    // ==========================================
    // 5. 暴击率 —— 独立档位表
    // ==========================================
    private static final Tier[] BAOJILV_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.90, 1.00),
            new Tier(10000, 0.80, 0.95),
            new Tier(100000, 0.70, 0.85),
            new Tier(1000000, 0.60, 0.80),
            new Tier(10000000, 0.50, 0.70),
            new Tier(Long.MAX_VALUE, 0.40, 0.60),
    };

    // ==========================================
    // 6. 暴击伤害 —— 独立档位表
    // ==========================================
    private static final Tier[] BAOJISHANGHAI_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.90, 1.00),
            new Tier(10000, 0.80, 0.95),
            new Tier(100000, 0.70, 0.85),
            new Tier(1000000, 0.60, 0.80),
            new Tier(10000000, 0.50, 0.70),
            new Tier(Long.MAX_VALUE, 0.40, 0.60),
    };

    // ==========================================
    // 7. 抗暴 —— 独立档位表
    // ==========================================
    private static final Tier[] KANGBAO_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.90, 1.00),
            new Tier(10000, 0.80, 0.95),
            new Tier(100000, 0.70, 0.85),
            new Tier(1000000, 0.60, 0.80),
            new Tier(10000000, 0.50, 0.70),
            new Tier(Long.MAX_VALUE, 0.40, 0.60),
    };

    // ==========================================
    // 8. 吸血 —— 独立档位表
    // ==========================================
    private static final Tier[] XIXUE_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.85, 0.95),
            new Tier(10000, 0.75, 0.90),
            new Tier(100000, 0.65, 0.80),
            new Tier(1000000, 0.55, 0.70),
            new Tier(10000000, 0.45, 0.60),
            new Tier(Long.MAX_VALUE, 0.35, 0.50),
    };

    // ==========================================
    // 9. 命中 —— 独立档位表
    // ==========================================
    private static final Tier[] MINGZHONG_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.85, 0.95),
            new Tier(10000, 0.75, 0.90),
            new Tier(100000, 0.65, 0.80),
            new Tier(1000000, 0.55, 0.70),
            new Tier(10000000, 0.45, 0.60),
            new Tier(Long.MAX_VALUE, 0.35, 0.50),
    };

    // ==========================================
    // 10. 闪避 —— 独立档位表
    // ==========================================
    private static final Tier[] SHANBI_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.85, 0.95),
            new Tier(10000, 0.75, 0.90),
            new Tier(100000, 0.65, 0.80),
            new Tier(1000000, 0.55, 0.70),
            new Tier(10000000, 0.45, 0.60),
            new Tier(Long.MAX_VALUE, 0.35, 0.50),
    };

    // ==========================================
    // 11. 无视防御 (物理穿透) —— 独立档位表
    // ==========================================
    private static final Tier[] WUCHUAN_TIERS = {
            new Tier(10, 1.0, 1.0),
            new Tier(100, 1.0, 1.0),
            new Tier(1000, 0.85, 0.95),
            new Tier(10000, 0.75, 0.90),
            new Tier(100000, 0.65, 0.80),
            new Tier(1000000, 0.55, 0.70),
            new Tier(10000000, 0.45, 0.60),
            new Tier(Long.MAX_VALUE, 0.35, 0.50),
    };

    /**
     * 【核心改动】：重载 weaken 方法，支持传入 isNpc 标识
     * @param original 原始魂环属性
     * @param isNpc 是否为 NPC 的魂环
     */
    public static MobAttributeCapability weaken(MobAttributeCapability original, boolean isNpc) {
        MobAttributeCapability weakened = new MobAttributeCapability();
        weakened.deserializeNBT(original.serializeNBT());

        // 神赐魂环默认不削弱
        if (original.isShenci()) {
            return weakened;
        }

        long nianxian = original.getNianxian();

        // 如果是 NPC，应用 NPC 专属的系数（减少削弱，保留更多属性）；玩家则保持 1.0 原始倍率
        double multiplier = isNpc ? NPC_RATIO_MULTIPLIER : 1.0;

        // ==================== 各个属性独立削弱计算 ====================
        weakened.setWugong(applyTier(original.getGongji(), nianxian, WUGONG_TIERS, multiplier));
        weakened.setWufang(applyTier(original.getFangyu(), nianxian, WUFANG_TIERS, multiplier));

        float weakenedMaxHp = applyTier(original.getMaxshengming(), nianxian, MAX_SHENGMING_TIERS, multiplier);
        weakened.setMaxshengming(weakenedMaxHp);

        weakened.setBaojilv(applyTier(original.getBaojilv(), nianxian, BAOJILV_TIERS, multiplier));
        weakened.setBaojishanghai(applyTier(original.getBaojishanghai(), nianxian, BAOJISHANGHAI_TIERS, multiplier));
        weakened.setKangbao(applyTier(original.getKangbao(), nianxian, KANGBAO_TIERS, multiplier));

        weakened.setXixue(applyTier(original.getXixue(), nianxian, XIXUE_TIERS, multiplier));
        weakened.setMingzhong(applyTier(original.getMingzhong(), nianxian, MINGZHONG_TIERS, multiplier));
        weakened.setShanbi(applyTier(original.getShanbi(), nianxian, SHANBI_TIERS, multiplier));
        weakened.setWuchuan(applyTier(original.getWuchuan(), nianxian, WUCHUAN_TIERS, multiplier));

        return weakened;
    }

    /**
     * 保持原有接口兼容性（默认 isNpc = false，给玩家使用）
     */
    public static MobAttributeCapability weaken(MobAttributeCapability original) {
        return weaken(original, false);
    }

    /**
     * 按年限查找档位区间，并根据 multiplier 增强保留比例
     */
    private static float applyTier(float originalVal, long nianxian, Tier[] tiers, double multiplier) {
        for (Tier tier : tiers) {
            if (nianxian < tier.maxNianxian()) {
                // 计算随机保留比例并乘上 NPC 加成倍率，最高不超过 1.0 (即 100% 原始属性)
                double finalRatio = Math.min(1.0, getRandomRatio(tier.ratioMin(), tier.ratioMax()) * multiplier);
                return (float) (originalVal * finalRatio);
            }
        }
        return originalVal;
    }

    private static double getRandomRatio(double min, double max) {
        if (min >= max) return min;
        return min + RANDOM.nextDouble() * (max - min);
    }
}