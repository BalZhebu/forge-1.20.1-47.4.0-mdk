package com.TovidY.kunluncontinent.capability.playerattributes;

/**
 * 修炼（打坐）的<b>数值公式唯一真相源</b>。
 *
 * <h3>为什么抽出来</h3>
 * 原本蒲团挂机（{@code PWPlayerTickEvent}）与一键跳过（{@code PacketXiulianChoice}）
 * <b>各写了一份公式，且不一致</b>：
 * <ul>
 *   <li>挂机：三段硬分档 {@code <=30 → 5 分钟}、{@code <=70 → 7 分钟}、
 *       {@code >70 → 10*1.08^(lvl-70) 分钟}</li>
 *   <li>跳过：{@code <=30 → 5}、{@code <=89 → 7}、{@code >89 → 10} 分钟
 *       —— <b>89 这个分界明显是笔误</b>（原公式是 70），
 *       导致「跳过」比「挂机」多给 27%~70% 经验，玩家会跳过刷级</li>
 * </ul>
 * 现在两条路径统一调用本类，<b>挂 1 分钟和跳过 1 分钟拿到的经验必然相同</b>。
 */
public final class XiulianLogic {

    /** 基础分钟数。 */
    private static final float BASE_MINUTES = 5.0f;
    /** 每级线性递增的分钟数。 */
    private static final float LINEAR_PER_LEVEL = 0.1f;
    /** 超过 {@link #CURVE_START_LEVEL} 后启用的曲线惩罚系数。 */
    private static final float CURVE_COEFF = 0.02f;
    /** 曲线惩罚起始等级。 */
    private static final int CURVE_START_LEVEL = 30;

    /** 修炼时间上限：10 分钟（600 秒）。 */
    public static final int TIME_MAX = 600;

    /** 修炼时间耗尽后的冷却时长：3 分钟（3600 tick）。 */
    public static final int COOLDOWN_TICKS = 3600;

    private XiulianLogic() {
    }

    /**
     * 计算「从当前等级升到下一级」所需的<b>挂机分钟数</b>（连续公式）。
     *
     * <p>公式：{@code 5 + lvl*0.1 + max(0, lvl-30)^1.5 * 0.02} 分钟。</p>
     *
     * <p><b>连续性</b>：1 级 5.10 分钟 → 99 级 26.36 分钟，全程单调递增。
     * 30→31 与 70→71 处的跳变都被压到约 <b>1.5%</b>，
     * 不会出现「升到 31 级突然变慢一半」这类数值事故。</p>
     *
     * <p><b>调用方务必使用本方法</b>，不要再写自己的分档 ——
     * 挂机与跳过必须共用同一份，否则玩家会发现「跳过比站着挂快」。</p>
     */
    public static float minutesPerLevel(int level) {
        float minutes = BASE_MINUTES + level * LINEAR_PER_LEVEL;
        if (level > CURVE_START_LEVEL) {
            double over = level - CURVE_START_LEVEL;
            minutes += (float) (Math.pow(over, 1.5) * CURVE_COEFF);
        }
        return minutes;
    }

    /**
     * 挂机 / 跳过 {@code seconds} 秒能获得多少经验。
     *
     * @param level    当前等级
     * @param seconds  挂机秒数
     * @param maxJingyan 当前的最大经验（升级门槛）
     */
    public static float expFor(int level, int seconds, float maxJingyan) {
        return maxJingyan / (minutesPerLevel(level) * 60f) * seconds;
    }
}