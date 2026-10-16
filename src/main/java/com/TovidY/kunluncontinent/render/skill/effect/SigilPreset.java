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
}
