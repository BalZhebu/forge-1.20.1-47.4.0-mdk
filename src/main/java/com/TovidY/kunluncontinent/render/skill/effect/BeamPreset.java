package com.TovidY.kunluncontinent.render.skill.effect;

/**
 * 带状能量体的<b>外观预设</b> —— 对应 {@code skill_beam.fsh} 的一组 uniform。
 *
 * <p>竖直光柱和沿视线的裂隙 / 射线共用同一套几何与着色器，靠这些参数区分。
 * 刻意<b>不加</b> {@code @OnlyIn(Dist.CLIENT)}（纯数据，服务端以后可能要查）。</p>
 *
 * <h3>字段含义</h3>
 * <ul>
 *   <li>{@code halfWidth} / {@code length} —— 带的半宽与长度（格）</li>
 *   <li>{@code vertical} —— {@code true} 竖直光柱（方向恒为 +Y，起点贴施法者脚下）；
 *       {@code false} 沿施法者视线（裂隙 / 射线）</li>
 *   <li>{@code hollow} —— {@code false} 中心亮（光柱）；{@code true} 中心暗、两侧亮（撕裂的口子）</li>
 *   <li>{@code startHeight} —— 起点相对施法者脚下的高度（格）。光柱 0、射线取眼睛高度</li>
 *   <li>{@code jagged} —— 折线抖动强度，0 = 平直。裂隙要抖、光柱不要</li>
 *   <li>{@code flow} —— 沿长度方向的能量流动速度</li>
 *   <li>{@code coreColor} / {@code edgeColor} —— 0xRRGGBB。
 *       ⚠️ {@code hollow} 为真时两者在剖面上是<b>反的</b>：中心显示 {@code edgeColor}、
 *       两侧显示 {@code coreColor} —— 调色时留意</li>
 * </ul>
 */
public record BeamPreset(
        float halfWidth, float length,
        boolean vertical, boolean hollow,
        float startHeight,
        float jagged, float flow,
        int coreColor, int edgeColor) {

    /** 拍击岩柱（磐石巨猿 8「憾岳」）：6 格高，轻微抖动的土黄岩柱。 */
    public static final BeamPreset STONE_PILLAR = new BeamPreset(
            1.6F, 6.0F, true, false,
            0.0F,
            0.10F, 1.2F,
            0xE8A54B, 0x3A2410);

    /** 拔地巨柱（磐石巨猿 9「不周倾」）：9 格高，几乎不抖。 */
    public static final BeamPreset BUZHOU_PILLAR = new BeamPreset(
            2.0F, 9.0F, true, false,
            0.0F,
            0.06F, 0.9F,
            0xF7D06A, 0x4A3113);

    /** 破界光柱（裂金虎 9「猛虎破界」）：8 格高金白细柱，流动快。 */
    public static final BeamPreset POJIE_PILLAR = new BeamPreset(
            1.2F, 8.0F, true, false,
            0.0F,
            0.05F, 1.4F,
            0xFFF0B0, 0x6A4A12);

    /**
     * 虚空裂缝（破魂枪 8「裂空」）：沿视线 20 格。
     *
     * <p>配合专用着色器 {@code skill_rift}：轮廓是梭形（两端收成尖），
     * {@code halfWidth} 是<b>中段最大半宽</b>，两端会收到 0。
     * {@code hollow} / {@code jagged} / {@code flow} 在这条 shader 里用不到（被忽略）。</p>
     */
    public static final BeamPreset VOID_RIFT = new BeamPreset(
            1.7F, 20.0F, false, true,
            1.0F,
            0.55F, 1.6F,
            0xB05CFF, 0x05010C);

    /**
     * 斩神光刃（破魂枪 9「弑神」）：沿视线 12 格。
     *
     * <p>配合专用着色器 {@code skill_blade}：月牙轮廓 + 上下刃口 + 白热核心线。
     * {@code halfWidth} 是刃的最大半宽 —— 从"极细射线"改成"劈出去的光刃"，所以给得大。</p>
     */
    public static final BeamPreset DEATH_RAY = new BeamPreset(
            3.5F, 12.0F, false, false,
            1.62F,
            0.0F, 2.2F,
            0xFFEDB8, 0x9E1220);

    // =====================================================================
    //  天使系（白金配色）
    // =====================================================================

    /**
     * 审判之矛（天使 4）：20 格长枪。
     *
     * <p>走专用着色器 {@code skill_spear} —— 同一个 {@code BeamEffect} 几何，
     * fsh 里画枪杆 / 枪头 / 尾缨，并按 {@code Age} 让枪尖"射出去"。
     * ⚠️ 内部图案全按 {@code beamUV} 的 0~1 归一化算，所以 {@code halfWidth}
     * 只决定"枪有多粗"，改长度也不会让枪变形。</p>
     */
    public static final BeamPreset JUDGMENT_LANCE = new BeamPreset(
            1.6F, 20.0F, false, false,
            1.20F,
            0.0F, 2.4F,
            0xFFF6D0, 0xD8A93A);

    /** 武魂真身（天使 7）：8 格高、几乎不抖的白金光柱，从脚下升起。 */
    public static final BeamPreset ANGEL_PILLAR = new BeamPreset(
            1.8F, 8.0F, true, false,
            0.0F,
            0.04F, 1.6F,
            0xFFF8DC, 0xC8A038);

    /**
     * 终焉救赎（天使 9）：36 格高的天降光柱。
     *
     * <p>比真身那条粗得多（{@code halfWidth} 3.0）—— 它要撑满 30 格判定范围的气势。</p>
     */
    public static final BeamPreset JUDGMENT_COLUMN = new BeamPreset(
            3.0F, 36.0F, true, false,
            0.0F,
            0.02F, 1.2F,
            0xFFFFF2, 0xD8B34A);

    /** 血色射线（七杀剑 5/8/9）：沿视线，细而锐，中心暗两侧亮。 */
    public static final BeamPreset QISHA_RAY = new BeamPreset(
            0.55F, 10.0F, false, true,
            1.2F,
            0.05F, 2.4F,
            0xE02438, 0x2A0508);

    /** 血色光柱（七杀剑 7/9）：竖直 6 格，边缘收束。 */
    public static final BeamPreset QISHA_PILLAR = new BeamPreset(
            1.3F, 6.0F, true, false,
            0.0F,
            0.08F, 1.8F,
            0xFF4A5A, 0x2A0508);

    /** 破魂枪射线：虚空紫。 */
    public static final BeamPreset POHUN_RAY = new BeamPreset(
            0.9F, 14.0F, false, true, 1.2F, 0.30F, 2.0F,
            0xB05CFF, 0x05010C);
    /** 破魂枪光柱：虚空紫竖柱。 */
    public static final BeamPreset POHUN_PILLAR = new BeamPreset(
            1.3F, 5.5F, true, false, 0.0F, 0.08F, 1.8F,
            0xC9A2FF, 0x14061F);
    /** 八荒戟射线：熔岩金。 */
    public static final BeamPreset BAHUANG_RAY = new BeamPreset(
            1.0F, 14.0F, false, true, 1.2F, 0.25F, 2.2F,
            0xFFB347, 0x3A1204);
    /** 八荒戟光柱：熔岩金竖柱。 */
    public static final BeamPreset BAHUANG_PILLAR = new BeamPreset(
            1.5F, 6.5F, true, false, 0.0F, 0.08F, 2.0F,
            0xFFB347, 0x3A1204);
    /** 磐石巨猿射线：大地黄。 */
    public static final BeamPreset JUYUAN_RAY = new BeamPreset(
            1.1F, 12.0F, false, false, 1.2F, 0.10F, 1.6F,
            0xE8B44C, 0x3A2A10);
    /** 裂金虎射线：金白爪光。 */
    public static final BeamPreset TIGER_RAY = new BeamPreset(
            0.7F, 12.0F, false, true, 1.1F, 0.18F, 2.6F,
            0xFFE98A, 0x8A5A00);

    /**
     * 深海召唤·收尾「天降能量柱」：28 格高、更粗、几乎不抖。
     *
     * <p>锚点在祭坛顶面，光柱从这里向上 28 格 —— 从玩家视角看就是
     * 「一道光从天上砸下来接在祭坛上」。超过清场穹顶（12 格）的部分会被地形挡住，
     * 只留下穹顶里那一截，正好是"从天而降"的观感。</p>
     */
    public static final BeamPreset DEEPSEA_HEAVEN = new BeamPreset(
            3.0F, 28.0F, true, false,
            0.0F,
            0.04F, 2.6F,
            0xDFFBFF, 0x0A4A63);

    /** 深海召唤：12 格高青白光柱，轻微抖动、能量上涌。 */
    public static final BeamPreset DEEPSEA_PILLAR = new BeamPreset(
            2.2F, 12.0F, true, false,
            0.0F,
            0.05F, 1.6F,
            0x9FF6FF, 0x06222E);
}
