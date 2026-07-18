package com.TovidY.kunluncontinent.capability.mobattributes;

import java.util.Random;

public class HunhuanWeakener {
    private static final Random RANDOM = new Random();

    /**
     * 单个年限档位：年限低于 maxNianxian（不含）时，保留比例落在 [ratioMin, ratioMax] 区间内随机取值。
     * 每个属性的档位表都是完全独立的数组实例，互不共享、互不引用。
     */
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
     * 对魂环属性进行精细化、多维度的削弱
     */
    public static MobAttributeCapability weaken(MobAttributeCapability original) {
        // 创建深拷贝新实例，不影响实体本身
        MobAttributeCapability weakened = new MobAttributeCapability();
        weakened.deserializeNBT(original.serializeNBT());

        // 神赐魂环默认不削弱
        if (original.isShenci()) {
            return weakened;
        }

        long nianxian = original.getNianxian();

        // ==================== 各个属性独立削弱计算（各用各的表，互不干扰） ====================
        weakened.setWugong(applyTier(original.getGongji(), nianxian, WUGONG_TIERS));
        weakened.setWufang(applyTier(original.getFangyu(), nianxian, WUFANG_TIERS));

        float weakenedMaxHp = applyTier(original.getMaxshengming(), nianxian, MAX_SHENGMING_TIERS);
        weakened.setMaxshengming(weakenedMaxHp);

        weakened.setBaojilv(applyTier(original.getBaojilv(), nianxian, BAOJILV_TIERS));
        weakened.setBaojishanghai(applyTier(original.getBaojishanghai(), nianxian, BAOJISHANGHAI_TIERS));
        weakened.setKangbao(applyTier(original.getKangbao(), nianxian, KANGBAO_TIERS));

        weakened.setXixue(applyTier(original.getXixue(), nianxian, XIXUE_TIERS));
        weakened.setMingzhong(applyTier(original.getMingzhong(), nianxian, MINGZHONG_TIERS));
        weakened.setShanbi(applyTier(original.getShanbi(), nianxian, SHANBI_TIERS));
        weakened.setWuchuan(applyTier(original.getWuchuan(), nianxian, WUCHUAN_TIERS));

        return weakened;
    }

    /**
     * 纯粹的“按年限查找档位区间”流程，不包含任何属性专属数值。
     * 每次调用传入哪张表，就完全按哪张表计算，表与表之间没有任何关联。
     */
    private static float applyTier(float originalVal, long nianxian, Tier[] tiers) {
        for (Tier tier : tiers) {
            if (nianxian < tier.maxNianxian()) {
                return (float) (originalVal * getRandomRatio(tier.ratioMin(), tier.ratioMax()));
            }
        }
        return originalVal;
    }

    private static double getRandomRatio(double min, double max) {
        if (min >= max) return min;
        return min + RANDOM.nextDouble() * (max - min);
    }
}