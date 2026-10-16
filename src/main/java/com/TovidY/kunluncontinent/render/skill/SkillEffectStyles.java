package com.TovidY.kunluncontinent.render.skill;

/**
 * 技能场域特效的<b>样式 id</b>。
 *
 * <p>刻意放在一个<b>双端都可见</b>的普通类里（没有 {@code @OnlyIn(Dist.CLIENT)}）：
 * 服务端发 {@code S2CSkillFieldPacket} 时要用这些数字，
 * 而真正的实现类 {@link SkillEffects} / {@code SkillEffectRenderer} 是纯客户端的，
 * 服务端类一旦直接引用它们，专用服务器上就会 {@code NoClassDefFoundError}。</p>
 */
public final class SkillEffectStyles {

    /** 八荒寂灭（八荒戟第九魂技）· 寂灭地阵。锚点跟随施法者。 */
    public static final int BAHUANG9_FIELD = 0;

    /**
     * 降雷（八荒戟第八魂技）· 雷柱。锚点跟随<b>被劈的目标</b>。
     *
     * <p>这个样式每个目标发一次包（50 格内十几个目标就是十几个实例），
     * {@code radius} 字段在这里的语义是「雷柱的半宽（格）」，不是场域半径。</p>
     */
    public static final int BAHUANG8_BOLT = 1;

    // =====================================================================
    //  地面符文阵（着色器 skill_sigil，实现 SigilEffect，外观见 SigilPreset）
    //  radius 字段 = 阵法视觉半径（格）
    // =====================================================================

    /** 憾岳（磐石巨猿 8）：土黄九边巨坑，锚点跟随施法者。 */
    public static final int SIGIL_HANYUE = 2;

    /** 不周倾（磐石巨猿 9）：金褐十二边拔地阵，锚点跟随施法者。 */
    public static final int SIGIL_BUZHOU = 3;

    /** 裂天（裂金虎 8）：亮金十二边 + 三道爪痕。 */
    public static final int SIGIL_LIETIAN = 4;

    /** 猛虎破界（裂金虎 9）：金白十六边 + 四道爪痕 + 三重冲击环。 */
    public static final int SIGIL_POJIE = 5;

    // =====================================================================
    //  带状能量体（着色器 skill_beam，实现 BeamEffect，外观见 BeamPreset）
    //  ⚠️ radius 字段在这些样式里被忽略 —— 尺寸全在预设里
    // =====================================================================

    /** 憾岳的拍击岩柱：竖直 6 格，锚点跟随施法者。 */
    public static final int BEAM_STONE_PILLAR = 6;

    /** 不周倾的拔地巨柱：竖直 9 格。 */
    public static final int BEAM_BUZHOU_PILLAR = 7;

    /** 猛虎破界的破界光柱：竖直 8 格。 */
    public static final int BEAM_POJIE_PILLAR = 8;

    // =====================================================================
    //  带状几何但用专用着色器（实现同样是 BeamEffect，只是换了 fsh）
    //  ⚠️ radius 字段同样被忽略
    // =====================================================================

    /**
     * 裂空的虚空裂缝：沿施法者视线 20 格。
     *
     * <p>⚠️ 走专用着色器 {@code skill_rift} —— 那条用的是<b>普通 alpha 混合</b>，
     * 所以裂缝内部能真正压暗背景（画成"黑洞"）。通用的 {@code skill_beam}
     * 是加法混合，加法混合下画不出黑洞，只能画发光带。</p>
     */
    public static final int POHUN8_RIFT = 9;

    /**
     * 弑神的斩神光刃：沿视线 12 格。
     *
     * <p>⚠️ 走专用着色器 {@code skill_blade} —— 月牙轮廓 + 上下锐利刃口 + 白热核心线，
     * 比通用能量带更有"劈斩"的气势。</p>
     */
    public static final int POHUN9_BLADE = 10;

    // =====================================================================
    //  扩散光波（着色器 skill_shock，实现 ShockwaveEffect）
    //  radius 字段 = 光波最终扩散到的半径（格）
    // =====================================================================

    /**
     * 不周倾落地：从施法者脚下向外推开的冲击光波。
     *
     * <p>光波的推进速度和 {@code PWPlayerTickEvent#handleLieDiLanding} 里
     * 伤害扩散的速度是<b>同一份</b>，所以"光波扫到哪、哪才受伤"。</p>
     */
    public static final int SHOCK_BUZHOU = 11;

    // =====================================================================
    //  贴在生物身上的屏幕对齐特效（着色器 skill_burst / skill_vortex/_light，
    //  实现 BurstEffect，外观见 BurstEffect.Style）
    //  radius 字段 = 四边形的半边长（格）
    //  ⚠️ 锚点是**被命中的生物**，不是施法者
    // =====================================================================

    /**
     * 裂天（裂金虎 8）：在被命中生物<b>体内</b>炸开的金色光芒。
     *
     * <p>每次脉冲对每个目标各发一次。深度测试是开的，所以爆点核心会被生物
     * 自己的模型挡住，只有撑出轮廓的部分露出来 —— 这是刻意的（"体内"质感）。</p>
     */
    public static final int BURST_LIETIAN = 12;

    /**
     * 猛虎破界（裂金虎 9）：目标<b>身后</b>张开的黑色旋涡。
     *
     * <p>走专用着色器 {@code skill_vortex}，那条用的是<b>普通 alpha 混合</b>
     * （不是加法），否则黑色画不出来。</p>
     */
    public static final int VORTEX_POJIE = 13;

    /**
     * 猛虎破界（裂金虎 9）：旋涡打出的金色光芒。
     *
     * <p><b>和伤害结算在同一 tick 发出</b>，做到"光到即伤"。</p>
     */
    public static final int VORTEX_POJIE_LIGHT = 14;

    private SkillEffectStyles() {
    }
}
