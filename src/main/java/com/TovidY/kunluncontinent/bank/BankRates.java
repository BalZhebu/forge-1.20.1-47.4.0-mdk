package com.TovidY.kunluncontinent.bank;

/**
 * 银行汇率：<b>纯函数，没有任何 tick</b>。
 *
 * <h3>为什么不需要定时刷新</h3>
 * <p>汇率只由「<b>汇率日序号</b>」决定：{@code rate = f(dayIndex)}。
 * 于是「每游戏日 5 点刷新」这件事自动成立 —— 时间一过 5:00，{@code dayIndex} 加一，
 * 算出来的汇率自然就是新的。<b>没有定时器、没有存档字段、没有网络包</b>，
 * 也不需要每天往所有玩家发包（这一点比"每天 5 点跑一圈"省太多）。</p>
 *
 * <h3>5:00 是怎么算出来的</h3>
 * <p>Minecraft 的 {@code dayTime % 24000}：0 = 06:00、6000 = 12:00、12000 = 18:00、18000 = 00:00。
 * 所以 05:00 对应 23000。{@code dayIndex = (dayTime + 1000) / 24000}：
 * 22999 → 0（还是今天）、23000 → 1（已经跨日）。</p>
 *
 * <h3>两个方向的价格为什么不共用同一个数</h3>
 * <p>如果「金→银」和「银→金」都用当天汇率，跨日就能套利：
 * 高价日（1 金 = 75 银）卖金，低价日（1 金 = 30 银）买金 → 75 银能买回 2.5 金，直接印钱。</p>
 * <p>所以：</p>
 * <ul>
 *   <li><b>向下换</b>（金→银、银→铜）用当日浮动汇率，玩家可以“等个好日子卖”；</li>
 *   <li><b>向上换</b>（银→金、铜→银）用<b>固定买价</b>，而且买价高于历史最高卖价
 *       （90 &gt; 75、60 &gt; 50）→ 任何方向、任何日子都不可能套利，最多"卖得贵一点"。</li>
 * </ul>
 */
public final class BankRates {

    /** 1 金能卖多少银：30 ~ 75。 */
    public static final int SILVER_PER_GOLD_MIN = 30;
    public static final int SILVER_PER_GOLD_MAX = 75;

    /** 1 银能卖多少铜：20 ~ 50。 */
    public static final int COPPER_PER_SILVER_MIN = 20;
    public static final int COPPER_PER_SILVER_MAX = 50;

    /**
     * 买 1 金要付多少银（固定）。
     *
     * <p>= 最高卖价 75 × 1.2，取整到 90。高于任何一天的卖价，所以不存在同日套利，
     * 跨日最坏情况（在 75 那天卖、再买回）也亏 17%，不会成为印钞机。</p>
     */
    public static final int GOLD_BUY_PRICE_SILVER = 90;

    /** 买 1 银要付多少铜（固定）。= 最高卖价 50 × 1.2。 */
    public static final int SILVER_BUY_PRICE_COPPER = 60;

    /** 换算多久刷新一次（tick 数，仅用于界面显示文案）。 */
    public static final int REFRESH_AT_DAY_TICK = 23000; // 05:00

    private BankRates() {
    }

    /**
     * 当前属于第几个「汇率日」（以 05:00 为界）。
     *
     * @param dayTime {@code Level#getDayTime()}
     */
    public static long dayIndex(long dayTime) {
        return Math.floorDiv(dayTime + (24000L - REFRESH_AT_DAY_TICK), 24000L);
    }

    /** 当日「1 金 = ? 银」。 */
    public static int silverPerGold(long dayIndex) {
        return lerp(unit(dayIndex, 1), SILVER_PER_GOLD_MIN, SILVER_PER_GOLD_MAX);
    }

    /** 当日「1 银 = ? 铜」。 */
    public static int copperPerSilver(long dayIndex) {
        return lerp(unit(dayIndex, 7), COPPER_PER_SILVER_MIN, COPPER_PER_SILVER_MAX);
    }

    // ==================================================================================
    //  曲线
    // ==================================================================================

    /**
     * 把「日序号」映射到 [0,1]。
     *
     * <p>60% 慢波 + 40% 每日抖动：慢波让汇率是「慢慢漂」，玩家有"等一个好日子"的空间；
     * 剩下的抖动让相邻两天不会完全一样，避免一眼算穿。</p>
     */
    private static double unit(long dayIndex, int salt) {
        double wave = 0.5 + 0.5 * Math.sin(dayIndex * 0.9 + salt * 2.1);
        double jitter = hash01(dayIndex, salt);
        return 0.6 * wave + 0.4 * jitter;
    }

    private static int lerp(double v, int min, int max) {
        double clamped = Math.max(0.0, Math.min(1.0, v));
        return min + (int) Math.round(clamped * (max - min));
    }

    /** 稳定的 64 位散列 → [0,1)，同一天同一 salt 永远同一个值。 */
    private static double hash01(long dayIndex, int salt) {
        long x = dayIndex * 0x9E3779B97F4A7C15L + salt * 0xBF58476D1CE4E5B9L;
        x ^= (x >>> 30);
        x *= 0xBF58476D1CE4E5B9L;
        x ^= (x >>> 27);
        x *= 0x94D049BB133111EBL;
        x ^= (x >>> 31);
        return (x >>> 11) * 0x1.0p-53;
    }
}
