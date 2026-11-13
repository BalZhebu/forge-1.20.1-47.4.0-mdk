package com.TovidY.kunluncontinent.render.skill.effect;

/**
 * 地面符文阵的<b>外观预设</b> —— 对应 {@code skill_sigil.fsh} 的一组 uniform。
 *
 * <p>阵法图案完全由这套参数决定，所以同一套着色器能画出好几个技能各自不同的阵，
 * 不用每个技能写一份 fsh。</p>
 *
 * <h3>字段含义</h3>
 * <ul>
 *   <li>{@code polySides} / {@code polyRadius} —— 外圈等分刻度数与半径（归一化 0~1）</li>
 *   <li>{@code starPoints} / {@code starInner} / {@code starOuter} —— 星芒角数与内外半径</li>
 *   <li>{@code runeRadius} —— 内圈法阵半径</li>
 *   <li>{@code slashCount} —— 爪痕条数，0 = 没有爪痕（磐石系没有爪，裂金虎系有）</li>
 *   <li>{@code shockCount} —— 冲击环数量，最多 3</li>
 *   <li>{@code coreColor} / {@code edgeColor} —— 0xRRGGBB</li>
 * </ul>
 *
 * <p>⚠️ 参数有几何约束：{@code starOuter < polyRadius}、{@code starInner < starOuter}、
 * {@code runeRadius < starInner}。反了不会崩，但图案会互相穿插变得很乱。</p>
 *
 * <p>刻意<b>不加</b> {@code @OnlyIn(Dist.CLIENT)} —— 它只是纯数据，
 * 标了客户端专属反而会让服务端以后想查尺寸时踩坑。</p>
 */
public record SigilPreset(
        float polySides, float polyRadius,
        float starPoints, float starInner, float starOuter,
        float runeRadius,
        int slashCount, float slashAngle,
        int shockCount,
        int coreColor, int edgeColor) {

    /** 憾岳（磐石巨猿 8）：土黄九边巨坑 + 七芒星，无爪痕。 */
    public static final SigilPreset HANYUE = new SigilPreset(
            9.0F, 0.72F,
            7.0F, 0.22F, 0.62F,
            0.34F,
            0, 0.0F,
            2,
            0xD98432, 0x4A2A10);

    /** 不周倾（磐石巨猿 9）：金褐十二边 + 八芒星，拔地阵法。 */
    public static final SigilPreset BUZHOU = new SigilPreset(
            12.0F, 0.80F,
            8.0F, 0.26F, 0.66F,
            0.40F,
            0, 0.0F,
            2,
            0xF2C75C, 0x5C3A16);

    /** 裂天（裂金虎 8）：亮金十二边 + 六芒星 + 三道爪痕。 */
    public static final SigilPreset LIETIAN = new SigilPreset(
            12.0F, 0.78F,
            6.0F, 0.30F, 0.70F,
            0.36F,
            3, 0.0F,
            2,
            0xFFD84D, 0x6B4A08);

    /** 猛虎破界（裂金虎 9）：金白十六边 + 六芒星 + 四道爪痕 + 三重冲击环。 */
    public static final SigilPreset POJIE = new SigilPreset(
            16.0F, 0.86F,
            6.0F, 0.32F, 0.78F,
            0.44F,
            4, 0.0F,
            3,
            0xFFE58A, 0x7A4E0A);

    // =====================================================================
    //  天使系（白金配色，无爪痕 —— 天使的图案是"芒"不是"爪"）
    //  半径由发包方给（radius 字段），预设只管图案疏密
    // =====================================================================

    /** 圣愈之光（天使 3）：十二边 + 六芒，图案偏内收，像一朵收拢的光。 */
    public static final SigilPreset ANGEL_HEAL = new SigilPreset(
            12.0F, 0.78F,
            6.0F, 0.34F, 0.70F,
            0.28F,
            0, 0.0F,
            2,
            0xFFF7D8, 0x8A6A1E);

    /** 神圣庇护（天使 5）：十四边 + 八芒 + 三重冲击环，向外推的开阔感。 */
    public static final SigilPreset ANGEL_BLESS = new SigilPreset(
            14.0F, 0.82F,
            8.0F, 0.36F, 0.74F,
            0.30F,
            0, 0.0F,
            3,
            0xFFFCEA, 0x9C7A22);

    /** 圣光锁链（天使 6）：十六边 + 六芒，铺满 30 格，外圈刻度当"锁链环"。 */
    public static final SigilPreset ANGEL_CHAIN = new SigilPreset(
            16.0F, 0.88F,
            6.0F, 0.40F, 0.80F,
            0.34F,
            0, 0.0F,
            2,
            0xFFEDB6, 0x7A5A12);

    /** 武魂真身（天使 7）：十八边 + 八芒，最密，开启瞬间的"神临"阵。 */
    public static final SigilPreset ANGEL_ZHENSHEN = new SigilPreset(
            18.0F, 0.90F,
            8.0F, 0.42F, 0.82F,
            0.36F,
            0, 0.0F,
            3,
            0xFFF7D6, 0xA8862A);

    /** 圣裁之域（天使 8）：二十边 + 八芒 + 三重环，半径 20 的常驻领域阵。 */
    public static final SigilPreset ANGEL_JUDGMENT = new SigilPreset(
            20.0F, 0.92F,
            8.0F, 0.44F, 0.86F,
            0.38F,
            0, 0.0F,
            3,
            0xFFF5CC, 0xB08A20);

    /** 召唤（七杀剑 1）：血色七边 + 七芒星，无爪痕，环最内收敛。 */
    public static final SigilPreset QISHA_SUMMON = new SigilPreset(
            7.0F, 0.70F,
            7.0F, 0.24F, 0.58F,
            0.32F,
            0, 0.0F,
            2,
            0xC21A2B, 0x2A0508);

    /** 剑阵（七杀剑 6）：十六边 + 七芒星 + 三重冲击环，最大的一圈。 */
    public static final SigilPreset QISHA_FIELD = new SigilPreset(
            16.0F, 0.84F,
            7.0F, 0.30F, 0.74F,
            0.44F,
            0, 0.0F,
            3,
            0xE02438, 0x35060B);

    /** 真身（七杀剑 7）：九边 + 七芒 + 内圈符文，紧凑贴身。 */
    public static final SigilPreset QISHA_ZHENSHEN = new SigilPreset(
            9.0F, 0.66F,
            7.0F, 0.22F, 0.54F,
            0.30F,
            0, 0.0F,
            2,
            0xFF3B4E, 0x2A0508);

    // ---- 破魂枪（虚空紫金）----
    public static final SigilPreset POHUN_SUMMON = new SigilPreset(
            8.0F, 0.70F, 7.0F, 0.24F, 0.58F, 0.32F, 0, 0.0F, 2,
            0xB05CFF, 0x14061F);
    public static final SigilPreset POHUN_FIELD = new SigilPreset(
            16.0F, 0.82F, 7.0F, 0.30F, 0.72F, 0.42F, 0, 0.0F, 3,
            0xC9A2FF, 0x1A0B2A);

    // ---- 八荒戟（熔岩金）----
    public static final SigilPreset BAHUANG_SUMMON = new SigilPreset(
            8.0F, 0.72F, 8.0F, 0.26F, 0.60F, 0.34F, 0, 0.0F, 2,
            0xF2A63B, 0x4A1A08);
    public static final SigilPreset BAHUANG_FIELD = new SigilPreset(
            20.0F, 0.84F, 8.0F, 0.30F, 0.74F, 0.44F, 0, 0.0F, 3,
            0xFFB347, 0x4A1A08);

    // ---- 磐石巨猿（大地黄）----
    public static final SigilPreset JUYUAN_SUMMON = new SigilPreset(
            9.0F, 0.70F, 7.0F, 0.24F, 0.58F, 0.32F, 0, 0.0F, 2,
            0xD9A13B, 0x3A2A10);
    public static final SigilPreset JUYUAN_FIELD = new SigilPreset(
            16.0F, 0.82F, 9.0F, 0.28F, 0.70F, 0.42F, 0, 0.0F, 3,
            0xE8B44C, 0x3A2A10);

    // =====================================================================
    //  深海祭坛（召唤深渊领主）：青蓝系三层阵
    // =====================================================================
    /** 外层巨阵：24 边 + 12 芒 + 内符文 + 三重冲击环。 */
    public static final SigilPreset DEEPSEA_ALTAR = new SigilPreset(
            24.0F, 0.88F,
            12.0F, 0.26F, 0.76F,
            0.46F,
            0, 0.0F,
            3,
            0x7FE9FF, 0x062A3E);

    /** 中层阵：8 边 + 6 芒，贴身一圈。 */
    public static final SigilPreset DEEPSEA_INNER = new SigilPreset(
            8.0F, 0.72F,
            6.0F, 0.22F, 0.60F,
            0.34F,
            0, 0.0F,
            2,
            0xBFF7FF, 0x0B3A52);

    /** 内层符文阵：20 边 + 5 芒 + 三道爪痕（被巨兽抓过）。 */
    public static final SigilPreset DEEPSEA_RUNE = new SigilPreset(
            20.0F, 0.80F,
            5.0F, 0.24F, 0.68F,
            0.40F,
            3, 0.6F,
            2,
            0x5AD8FF, 0x08203A);

    // ---- 深海法阵塔：一层一个形状，越往上越大 ----
    /** 塔基：正五边形 + 五芒星。 */
    public static final SigilPreset DEEPSEA_PENTA = new SigilPreset(
            5.0F, 0.74F,
            5.0F, 0.30F, 0.70F,
            0.30F,
            0, 0.0F,
            2,
            0x9FF6FF, 0x0A2A3E);

    /** 塔身：正六边形 + 六芒星（两个交叠三角）。 */
    public static final SigilPreset DEEPSEA_HEXA = new SigilPreset(
            6.0F, 0.78F,
            6.0F, 0.28F, 0.72F,
            0.34F,
            0, 0.0F,
            3,
            0x7FE9FF, 0x07203A);

    /** 塔顶封印：十二边 + 三芒 + 三道爪痕，最小最亮。 */
    public static final SigilPreset DEEPSEA_SEAL = new SigilPreset(
            12.0F, 0.80F,
            3.0F, 0.20F, 0.62F,
            0.30F,
            3, 0.8F,
            1,
            0xBFF7FF, 0x0B3042);
}
