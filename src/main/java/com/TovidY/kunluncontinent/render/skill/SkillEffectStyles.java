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

    // =====================================================================
    //  天使系（1~9 魂技）
    //  ⚠️ 这几条全部复用现有 5 条 fsh（sigil / beam / blade / burst / shock），
    //     只加预设与 id —— 没有新 GLSL，所以不用跑 check_shaders.py
    // =====================================================================

    /**
     * 圣光斩（天使 1）：绕施法者扫出的 120° 金色弧光，半径 7 格。
     *
     * <p>实现 {@code ArcEffect} + fsh {@code skill_arc}；{@code radius} = 弧带半径（格）。
     * 用的是<b>竖直柱面弧带</b>而不是水平扇形 —— 水平扇形在平视时会退化成一条线。</p>
     */
    public static final int SLASH_ANGEL = 15;

    /**
     * 天使之翼（天使 2）：展开瞬间贴在<b>施法者身上</b>的光爆。
     *
     * <p>实现 {@code BurstEffect}（屏幕对齐、体内爆裂）；{@code radius} = 四边形半边长（格）。
     * 锚点是施法者本人。</p>
     */
    public static final int BURST_ANGEL_WINGS = 16;

    /** 圣愈之光（天使 3）：半径 5 的地面治疗阵。锚点钉在释放点（{@code atPoint}）。 */
    public static final int SIGIL_ANGEL_HEAL = 17;

    /** 审判之矛（天使 4）：20 格金色长枪（枪尖会"飞出去"）。实现 {@code BeamEffect} + {@code skill_spear}。 */
    public static final int BEAM_JUDGMENT_SPEAR = 18;

    /** 神圣庇护（天使 5）：脚下十四边庇护阵，锚点跟随施法者。 */
    public static final int SIGIL_ANGEL_BLESS = 19;

    /** 武魂真身（天使 7）：8 格竖直白金光柱，锚点跟随施法者。 */
    public static final int BEAM_ANGEL_PILLAR = 20;

    /**
     * 圣光锁链（天使 6）：半径 30 的锁链大阵。锚点钉在释放点。
     *
     * <p>⚠️ 这是本套里视觉半径最大的地面阵（30 格），
     * 但几何仍然只是一个四边形，逐像素图案在 fsh 里算 —— 不会因为半径大就多画顶点。</p>
     */
    public static final int SIGIL_ANGEL_CHAIN = 21;

    /** 武魂真身（天使 7）开启瞬间的「神临」阵，锚点跟随施法者。 */
    public static final int SIGIL_ANGEL_ZHENSHEN = 22;

    /**
     * 圣裁之域（天使 8）：半径 20、持续 400 tick 的常驻领域阵。
     *
     * <p>生命周期必须和服务端的 {@code HolyJudgmentField} 对齐（20 秒），
     * 锚点用 {@code atPoint} 钉死 —— 领域不会跟着玩家跑。</p>
     */
    public static final int SIGIL_ANGEL_JUDGMENT = 23;

    /** 终焉救赎（天使 9）：36 格天降光柱。锚点钉在落点。 */
    public static final int BEAM_JUDGMENT_COLUMN = 24;

    /**
     * 终焉救赎（天使 9）：从落点向外推开的冲击光波，半径 = 判定半径 30。
     *
     * <p>⚠️ 参数 {@code radius} 是<b>光波最终扩散到的半径</b>。
     * 实现 {@code ShockwaveEffect}（{@code skill_shock}），颜色沿用该 fsh 内置的金色。</p>
     */
    public static final int SHOCK_ANGEL = 25;

    /**
     * 终焉救赎（天使 9）：天降的巨大发光体（9 格宽、从 34 格高处砸下来）。
     *
     * <p>实现 {@code FallEffect} + fsh {@code skill_fall}；{@code radius} 被忽略。
     * 锚点钉在落点，时长 {@code FallEffect.DURATION_TICKS}。</p>
     */
    public static final int FALL_ANGEL_SWORD = 26;

    /**
     * 圣裁之域（天使 8）：每一波扫到目标时，贴在它身上的金色命中光。
     *
     * <p>实现 {@code BurstEffect}（屏幕对齐、体内爆裂）；{@code radius} = 四边形半边长（格），
     * 锚点是<b>被命中的生物</b>。一波可能对十几个目标各发一次，所以时长给得很短。</p>
     */
    public static final int BURST_ANGEL_HIT = 27;

    private SkillEffectStyles() {
    }

    // =====================================================================
    //  七杀剑（器武魂·纯输出）：全部复用 sigil / beam / burst / shock 四条 fsh
    // =====================================================================
    /** 第一魂技「七杀剑」召唤地阵。 */
    public static final int SIGIL_QISHA_SUMMON = 20;
    /** 第六魂技「七杀剑阵」大阵。 */
    public static final int SIGIL_QISHA_FIELD = 21;
    /** 第七魂技「七杀真身」脚下血阵。 */
    public static final int SIGIL_QISHA_ZHENSHEN = 22;
    /** 第五/八/九魂技的血色射线（沿视线）。 */
    public static final int BEAM_QISHA_RAY = 23;
    /** 第七/九魂技的血色光柱（竖直）。 */
    public static final int BEAM_QISHA_PILLAR = 24;
    /** 通用血色爆光（施法者身上）。 */
    public static final int BURST_QISHA = 25;
    /** 印记 / 命中时目标身上的血爆。 */
    public static final int BURST_QISHA_MARK = 26;
    /** 扩散血环（斩击 / 突进）。 */
    public static final int SHOCK_QISHA = 27;
    /** 第二魂技「血刃斩」红色剑波（横向弧斩）。 */
    public static final int SLASH_QISHA = 28;
    /** 「七」字六芒星法阵（第二套：自带几何，不依赖 skill_sigil）。 */
    public static final int SIGIL_QISHA_GLYPH = 29;
    /** 暗红剑芒天降（单个目标落点）。 */
    public static final int FALL_QISHA_SWORD = 30;
    /** 五芒星法阵（第 8 魂技锁定阵）。 */
    public static final int SIGIL_QISHA_PENTA = 31;
    /** 八芒星法阵（第 6 魂技结界内层）。 */
    public static final int SIGIL_QISHA_OCTA = 32;
    /** 三角阵（第 9 魂技）。 */
    public static final int SIGIL_QISHA_TRI = 33;

    // =====================================================================
    //  四大武魂（破魂枪 / 八荒戟 / 磐石巨猿 / 裂金虎）1~7 魂技的着色器样式
    //  —— 全部复用 sigil / beam / burst / shock / arc 五条 fsh，只加预设
    // =====================================================================
    public static final int SHOCK_POHUN = 34;
    public static final int SIGIL_POHUN_SUMMON = 35;
    public static final int SIGIL_POHUN_FIELD = 36;
    public static final int BEAM_POHUN_RAY = 37;
    public static final int BEAM_POHUN_PILLAR = 38;
    public static final int BURST_POHUN = 39;
    public static final int SHOCK_BAHUANG = 40;
    public static final int SIGIL_BAHUANG_SUMMON = 41;
    public static final int SIGIL_BAHUANG_FIELD = 42;
    public static final int BEAM_BAHUANG_RAY = 43;
    public static final int BEAM_BAHUANG_PILLAR = 44;
    public static final int BURST_BAHUANG = 45;
    public static final int SLASH_BAHUANG = 46;
    public static final int SHOCK_JUYUAN = 47;
    public static final int SIGIL_JUYUAN_SUMMON = 48;
    public static final int SIGIL_JUYUAN_FIELD = 49;
    public static final int BEAM_JUYUAN_RAY = 50;
    public static final int BURST_JUYUAN = 51;
    public static final int SHOCK_TIGER = 52;
    public static final int BEAM_TIGER_RAY = 53;
}
