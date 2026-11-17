package com.TovidY.kunluncontinent.tower.floor;

import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挑战塔关卡表。
 *
 * <p><b>第 1~100 层全部显式手写</b>（见下面 static 块）；<b>第 101 层起不再手写</b>，
 * 由 {@link #generateFloor(int)} 按最后十几层的规律用公式往外延伸，可以一直打到
 * {@link #MAX_AVAILABLE_FLOOR} 层 —— 见下方「无限延伸」小节。</p>
 *
 * <p>每层 = (基础年限, 限时秒, 核心三围倍率, 词条等级, 刷怪配置...)。</p>
 * <ul>
 *   <li><b>基础年限</b>：决定怪物全属性的基础值（{@code MobAttributeCapability.initNianxian}）；</li>
 *   <li><b>核心三围倍率</b>：本层所有怪物的<b>生命 / 防御 / 攻击</b>再乘该系数。
 *       ⚠️ 只放大这三项 —— 闪避/吸血/暴击等比率型副属性若也乘倍率会直接把平衡搞崩
 *       （详见 {@code MobAttributeCapability#applyAttributeMultiplier} 的注释）；</li>
 *   <li><b>词条等级</b>：一个数字管整层（1级=原始强度，每级+10%），显示自动拼罗马数字；
 *       个别词条想脱离整层等级，在名字后手写罗马后缀（{@code "狂暴 V"}）即可覆盖；</li>
 *   <li><b>刷怪配置</b>：每条 {@link MonsterConfig} 自带数量参数，一层的怪物总数 = 各条 count 之和。</li>
 * </ul>
 *
 * <p><b>爆炸节奏：</b>每第 5/15/25...95 层一次<b>属性爆炸</b>，每第 10/20/30...100 层一次
 * <b>更强的数值爆炸</b>——爆炸层倍率明显高于前后层，过后回落渐进基线。</p>
 *
 * <p><b>神位概率</b>（在 {@code KLivingDeathEvent}）：基础 0.5% × 层数，整五层 ×1.5、
 * 整十层 ×2 —— 曲线恰好第 100 层 = 100%（必出），已预留。</p>
 *
 * <h3>第二批词条的登场层（"随层数解锁"）</h3>
 * <p>词条本体在 {@code TowerSkillPool}，每条带 {@code getMinFloor()}：
 * 随机抽取会自动跳过还没到登场层的条目，所以低层不会莫名其妙冒出高层词条；
 * 下表是<b>手写指定</b>的登场层（也是该词条第一次能被玩家看到的地方）。</p>
 * <ul>
 *   <li>第 12 层 —— 被动【虚影】（额外闪避）</li>
 *   <li>第 16 层 —— 被动【回春】（周期回血）</li>
 *   <li>第 18 层 —— 主动【践踏】（跃击 + 冲击波击退）</li>
 *   <li>第 20 层 —— 被动【噬血】（击中回血，数值爆炸层）</li>
 *   <li>第 24 层 —— 被动【甲鳞】（受伤减免，属性爆炸层）</li>
 *   <li>第 28 层 —— 主动【缚魂】（把玩家拽过来）</li>
 *   <li>第 33 层 —— 主动【冰封】（缓慢 + 挖掘疲劳）</li>
 *   <li>第 35 层 —— 被动【殉爆】（死亡余波，属性爆炸层）</li>
 *   <li>第 40 层 —— 主动【玄甲】（自身抗性提升，数值爆炸层）</li>
 *   <li>第 46 层 —— 主动【血祭】（自伤换范围真伤）</li>
 *   <li>第 100 层 —— 血祭 / 冰封 / 殉爆 / 玄甲 全上，终局阵容</li>
 * </ul>
 * <p>共同约束：<b>这批词条绝不改动任何方块</b>（不放冰、不点燃、不生成 Explosion 实体）。</p>
 *
 * <h3>无限延伸（第 101 层起）</h3>
 * <p>第 100 层不再是终点：打完就继续往上，层号本身<b>没有终点</b>，
 * 但各项数值按各自的「度」收敛，不是无脑翻倍：</p>
 * <ul>
 *   <li><b>年限（决定怪物全属性基础值）</b>：分段递减 —— 第 136 层前每层 +100 万、
 *       第 137~146 层每层 +50 万、第 147 层起每层 +10 万，
 *       在 <b>1 亿 2 千万</b>（{@link #MAX_NIANXIAN}）处封顶 —— <b>第 396 层</b>才吃满；</li>
 *   <li><b>核心三围倍率</b>：<u>不封顶</u>，按每层 +0.07 的线性基线一直涨，
 *       整五层（×1.20）/整十层（×1.34）照旧给一次爆炸。这是唯一的无限成长轴；</li>
 *   <li><b>词条等级</b>：{@code 1 + 层号/10}，第 90 层起就满了（10 级），之后恒为满级；</li>
 *   <li><b>限时</b>：延续「每层 +1 秒」，但在 600 秒（第 539 层）封顶；</li>
 *   <li><b>阵容</b>：以第 90~100 层的「僵尸群 + 流浪者群」为模板，
 *       每 10 层一个小台阶加数量与词条，数值爆炸层额外补凋零骷髅。</li>
 * </ul>
 * <p>⚠️ 神位<b>不</b>在这里给：第 100 层通关必出神位（{@code KLivingDeathEvent}），
 * 之后玩家 {@code godName} 非空就再也不会触发。第 101 层起纯粹是继续挑战 + 掉落
 * （掉落走 {@code TowerDropReward}，成神玩家照样拿魂环魂骨）。</p>
 */
public class TowerFloorRegistry {

    // ==================================================================================
    //  无限延伸的四条「度」（改上限只改这里）
    // ==================================================================================

    /** 手写配表层数：第 1 ~ 100 层。第 101 层起全部由公式生成。 */
    public static final int HANDWRITTEN_FLOORS = 100;

    /**
     * 层号硬上限（第 {@code MAX_AVAILABLE_FLOOR} 层）。
     *
     * <p>只是为了防止 int 溢出和无意义的巨层，体验上等于「无限」：
     * 年限在第 396 层才封顶，之后只剩三围倍率在涨。</p>
     */
    public static final int MAX_AVAILABLE_FLOOR = 10000;

    /** 怪物年限绝对上限：1 亿 2 千万。到顶后不再增长（属性基础值也就此定型）。 */
    public static final long MAX_NIANXIAN = 120_000_000L;

    /** 生成层的年限基数：<b>0 基索引 55</b>（第 56 层）= 1000 万，之后每层 +100 万（与手写段完全一致）。 */
    private static final long NIANXIAN_BASE_FLOOR_55 = 10_000_000L;
    private static final long NIANXIAN_STEP = 1_000_000L;

    // ── 年限三段式：越接近封顶，每层涨得越少（把 1.2 亿这个顶推远一点） ──
    /** 索引 135 = 第 136 层，恰好 9000 万；之后每层只 +50 万。 */
    private static final int NIANXIAN_TIER1_END = 135;
    /** 索引 145 = 第 146 层，恰好 9500 万；之后每层只 +10 万。 */
    private static final int NIANXIAN_TIER2_END = 145;
    private static final long NIANXIAN_AT_90M = 90_000_000L;
    private static final long NIANXIAN_AT_95M = 95_000_000L;
    private static final long NIANXIAN_STEP_MID = 500_000L;
    private static final long NIANXIAN_STEP_LATE = 100_000L;

    /** 生成层的三围倍率：以<b>索引 99</b>（第 100 层）反推出的基线衔接，之后每层 +0.07。 */
    private static final double MULT_BASELINE_AT_99 = 8.955D;
    private static final double MULT_STEP = 0.07D;
    /** 整五层 / 整十层的爆炸系数（与手写段一致）。 */
    private static final double MULT_ATTR_BOOM = 1.20D;
    private static final double MULT_NUM_BOOM = 1.34D;

    public static class FloorData {
        public final List<MonsterConfig> monsters;
        public final int timeLimitSeconds;
        public final long baseNianxian;
        /** 本层怪物「生命 / 防御 / 攻击」倍率（1.0 = 原样；副属性不受影响）。 */
        public final float attributeMultiplier;
        /** 本层默认词条等级（1~10），作用于所有未手写罗马后缀的指定词条。 */
        public final int skillLevel;

        public FloorData(List<MonsterConfig> monsters, int timeLimitSeconds, long baseNianxian,
                         float attributeMultiplier, int skillLevel) {
            this.monsters = monsters;
            this.timeLimitSeconds = timeLimitSeconds;
            this.baseNianxian = baseNianxian;
            this.attributeMultiplier = attributeMultiplier;
            this.skillLevel = skillLevel;
        }
    }

    private static final Map<Integer, FloorData> REGISTRY = new HashMap<>();

    private static ResourceLocation klMob(String path) {
        return new ResourceLocation("kunluncontinent", path);
    }

    private static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft", "zombie");
    private static final ResourceLocation HUSK = new ResourceLocation("minecraft", "husk");
    private static final ResourceLocation SKELETON = new ResourceLocation("minecraft", "skeleton");
    private static final ResourceLocation STRAY = new ResourceLocation("minecraft", "stray");
    private static final ResourceLocation WITHER_SKELETON = new ResourceLocation("minecraft", "wither_skeleton");

    static {
        registerFloor(0, 888888L, 30, 1.0f, 1,
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        registerFloor(1, 900000L, 30, 1.0f, 1,
                new MonsterConfig(ZOMBIE, 2, 1)
        );

        registerFloor(2, 920000L, 45, 1.0f, 1,
                new MonsterConfig(ZOMBIE, 1, 2),
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        registerFloor(3, 940000L, 40, 1.0f, 1,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        // ---- 第 5 层【属性爆炸】 ----
        registerFloor(4, 960000L, 60, 1.4f, 1,
                new MonsterConfig(ZOMBIE, 3, 2),
                new MonsterConfig(ZOMBIE, 1, 2, "恐惧")
        );

        registerFloor(5, 970000L, 60, 1.5f, 1,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        registerFloor(6, 988888L, 60, 1.6f, 1,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(HUSK, 1, 2)
        );

        registerFloor(7, 990000L, 60, 1.7f, 1,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(SKELETON, 1, 1),
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        registerFloor(8, 999999L, 50, 1.8f, 1,
                new MonsterConfig(ZOMBIE, 2, 3),
                new MonsterConfig(HUSK, 1, 2),
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        // ---- 第 10 层【数值爆炸】：第一个大关口 ----
        registerFloor(9, 1000001L, 80, 2.2f, 1,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(SKELETON, 2, 2, "恐惧"),
                new MonsterConfig(ZOMBIE, 1, 2, "狂暴", "瞬移")
        );

        // =====================================================================
        // 第二阶段（第 11~20 层）：词条 2 级
        // =====================================================================
        registerFloor(10, 1100000L, 50, 1.9f, 2,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(HUSK, 1, 2)
        );

        registerFloor(11, 1200000L, 45, 2.0f, 2,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(SKELETON, 1, 1, "虚影")
        );

        registerFloor(12, 1300000L, 40, 2.1f, 2,
                new MonsterConfig(ZOMBIE, 2, 3),
                new MonsterConfig(STRAY, 1, 1)
        );

        registerFloor(13, 1400000L, 30, 2.2f, 2,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(SKELETON, 1, 2),
                new MonsterConfig(ZOMBIE, 1, 1)
        );

        // ---- 第 15 层【属性爆炸】 ----
        registerFloor(14, 1600000L, 40, 2.6f, 2,
                new MonsterConfig(ZOMBIE, 3, 2),
                new MonsterConfig(HUSK, 2, 2)
        );

        registerFloor(15, 1700000L, 40, 2.3f, 2,
                new MonsterConfig(ZOMBIE, 2, 2),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(16, 1800000L, 45, 2.4f, 2,
                new MonsterConfig(ZOMBIE, 3, 3, "回春"),
                new MonsterConfig(STRAY, 1, 2, "迅速")
        );

        registerFloor(17, 1900000L, 45, 2.5f, 2,
                new MonsterConfig(HUSK, 3, 2),
                new MonsterConfig(ZOMBIE, 2, 3, "壁垒")
        );

        registerFloor(18, 2000000L, 50, 2.6f, 2,
                new MonsterConfig(ZOMBIE, 2, 4),
                new MonsterConfig(SKELETON, 2, 1, "泥沼", "践踏"),
                new MonsterConfig(STRAY, 1, 1)
        );

        // ---- 第 20 层【数值爆炸】：凋零骷髅登场，词条 3 级 ----
        registerFloor(19, 2150000L, 55, 3.2f, 3,
                new MonsterConfig(ZOMBIE, 3, 3, "浑厚"),
                new MonsterConfig(HUSK, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧", "噬血")
        );

        // =====================================================================
        // 第三阶段（第 21~30 层）：词条 3 级
        // =====================================================================
        registerFloor(20, 2300000L, 55, 2.7f, 3,
                new MonsterConfig(ZOMBIE, 3, 4),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(SKELETON, 2, 3, "瞬移")
        );

        registerFloor(21, 2450000L, 55, 2.8f, 3,
                new MonsterConfig(ZOMBIE, 2, 3, "壁垒", "浑厚"),
                new MonsterConfig(HUSK, 3, 2),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(22, 2600000L, 60, 2.9f, 3,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 3, 3, "恐惧"),
                new MonsterConfig(STRAY, 1, 1, "迅速")
        );

        registerFloor(23, 2800000L, 60, 3.0f, 3,
                new MonsterConfig(ZOMBIE, 3, 4, "狂暴"),
                new MonsterConfig(HUSK, 2, 2),
                new MonsterConfig(SKELETON, 2, 3)
        );

        // ---- 第 25 层【属性爆炸】，词条 4 级 ----
        registerFloor(24, 3000000L, 65, 3.8f, 4,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2, "泥沼"),
                new MonsterConfig(SKELETON, 2, 2, "免伤", "甲鳞")
        );

        registerFloor(25, 3200000L, 65, 3.1f, 4,
                new MonsterConfig(ZOMBIE, 3, 3, "浑厚", "壁垒"),
                new MonsterConfig(HUSK, 2, 3),
                new MonsterConfig(SKELETON, 3, 2),
                new MonsterConfig(STRAY, 1, 1)
        );

        registerFloor(26, 3400000L, 65, 3.2f, 4,
                new MonsterConfig(ZOMBIE, 3, 4, "狂暴", "迅速"),
                new MonsterConfig(SKELETON, 3, 3)
        );

        registerFloor(27, 3600000L, 70, 3.3f, 4,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 3, 3, "瞬移", "恐惧"),
                new MonsterConfig(STRAY, 2, 2, "缚魂")
        );

        registerFloor(28, 3800000L, 70, 3.4f, 4,
                new MonsterConfig(ZOMBIE, 3, 4, "浑厚"),
                new MonsterConfig(HUSK, 3, 2, "壁垒"),
                new MonsterConfig(SKELETON, 2, 3),
                new MonsterConfig(STRAY, 1, 1)
        );

        // ---- 第 30 层【数值爆炸】 ----
        registerFloor(29, 4000000L, 75, 4.4f, 4,
                new MonsterConfig(ZOMBIE, 4, 4, "狂暴"),
                new MonsterConfig(SKELETON, 3, 3, "泥沼")
        );

        // =====================================================================
        // 第四阶段（第 31~40 层）：词条 4 级
        // =====================================================================
        registerFloor(30, 4200000L, 75, 3.5f, 4,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(HUSK, 3, 3, "壁垒"),
                new MonsterConfig(STRAY, 2, 2, "迅速"),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(31, 4400000L, 80, 3.6f, 4,
                new MonsterConfig(ZOMBIE, 4, 4, "狂暴", "浑厚"),
                new MonsterConfig(SKELETON, 3, 2, "瞬移")
        );

        registerFloor(32, 4700000L, 80, 3.7f, 4,
                new MonsterConfig(ZOMBIE, 3, 4),
                new MonsterConfig(HUSK, 3, 3, "壁垒"),
                new MonsterConfig(SKELETON, 2, 3, "恐惧"),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(33, 5000000L, 80, 3.8f, 4,
                new MonsterConfig(ZOMBIE, 3, 4),
                new MonsterConfig(HUSK, 2, 2, "浑厚", "壁垒"),
                new MonsterConfig(SKELETON, 3, 3, "瞬移"),
                new MonsterConfig(STRAY, 2, 2, "冰封")
        );

        // ---- 第 35 层【属性爆炸】 ----
        registerFloor(34, 5300000L, 85, 5.0f, 4,
                new MonsterConfig(ZOMBIE, 3, 4, "狂暴"),
                new MonsterConfig(HUSK, 2, 2, "浑厚", "壁垒"),
                new MonsterConfig(SKELETON, 3, 3, "瞬移", "恐惧"),
                new MonsterConfig(STRAY, 1, 2, "迅速", "殉爆")
        );

        registerFloor(35, 5600000L, 85, 3.9f, 4,
                new MonsterConfig(ZOMBIE, 3, 4),
                new MonsterConfig(WITHER_SKELETON, 2, 2, "壁垒"),
                new MonsterConfig(SKELETON, 2, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(36, 5900000L, 85, 4.0f, 4,
                new MonsterConfig(ZOMBIE, 4, 4, "狂暴", "浑厚"),
                new MonsterConfig(SKELETON, 3, 2, "瞬移")
        );

        registerFloor(37, 6200000L, 90, 4.1f, 4,
                new MonsterConfig(ZOMBIE, 3, 4),
                new MonsterConfig(HUSK, 3, 3, "壁垒"),
                new MonsterConfig(WITHER_SKELETON, 2, 3, "恐惧"),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(38, 6500000L, 90, 4.2f, 4,
                new MonsterConfig(ZOMBIE, 4, 4, "狂暴"),
                new MonsterConfig(SKELETON, 3, 3, "泥沼"),
                new MonsterConfig(STRAY, 1, 1)
        );

        // ---- 第 40 层【数值爆炸】，词条 5 级 ----
        registerFloor(39, 6800000L, 100, 5.4f, 5,
                new MonsterConfig(ZOMBIE, 3, 4, "狂暴"),
                new MonsterConfig(HUSK, 2, 2, "浑厚", "壁垒"),
                new MonsterConfig(WITHER_SKELETON, 3, 3, "瞬移", "恐惧"),
                new MonsterConfig(STRAY, 2, 2, "迅速", "玄甲")
        );

        // =====================================================================
        // 第五阶段（第 41~50 层）：词条 5 级，年限 700万 → 880万
        // =====================================================================
        registerFloor(40, 7000000L, 101, 4.3f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(41, 7200000L, 102, 4.4f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(42, 7400000L, 103, 4.5f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(43, 7600000L, 104, 4.5f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(44, 7800000L, 105, 4.6f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        // ---- 第 45 层【属性爆炸】 ----
        registerFloor(45, 8000000L, 106, 5.6f, 5,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(46, 8200000L, 107, 4.8f, 5,
                new MonsterConfig(ZOMBIE, 3, 3, "血祭"),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(47, 8400000L, 108, 4.9f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(48, 8600000L, 109, 4.9f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        registerFloor(49, 8800000L, 110, 5.0f, 5,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(HUSK, 1, 2, "壁垒")
        );

        // ---- 第 50 层【数值爆炸】，词条 6 级 ----
        registerFloor(50, 9000000L, 111, 6.9f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(51, 9200000L, 112, 5.1f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(52, 9400000L, 113, 5.2f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(53, 9600000L, 114, 5.3f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(54, 9800000L, 115, 5.4f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        // ---- 第 55 层【属性爆炸】 ----
        registerFloor(55, 10000000L, 116, 6.6f, 6,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(56, 11000000L, 117, 5.6f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(57, 12000000L, 118, 5.7f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(58, 13000000L, 119, 5.8f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(59, 14000000L, 120, 5.8f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        // ---- 第 60 层【数值爆炸】，词条 7 级 ----
        registerFloor(60, 15000000L, 121, 8.0f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(61, 16000000L, 122, 6.0f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(62, 17000000L, 123, 6.1f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(63, 18000000L, 124, 6.1f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(64, 19000000L, 125, 6.2f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        // ---- 第 65 层【属性爆炸】 ----
        registerFloor(65, 20000000L, 126, 7.6f, 7,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(66, 21000000L, 127, 6.4f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(67, 22000000L, 128, 6.4f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(68, 23000000L, 129, 6.5f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(69, 24000000L, 130, 6.6f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(70, 25000000L, 131, 9.0f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(71, 26000000L, 132, 6.8f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(72, 27000000L, 133, 6.9f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(73, 28000000L, 134, 7.0f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(74, 29000000L, 135, 7.0f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        // ---- 第 75 层【属性爆炸】 ----
        registerFloor(75, 30000000L, 136, 8.5f, 8,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(76, 31000000L, 137, 7.2f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(77, 32000000L, 138, 7.3f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(78, 33000000L, 139, 7.3f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(79, 34000000L, 140, 7.4f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        // ---- 第 80 层【数值爆炸】，词条 9 级 ----
        registerFloor(80, 35000000L, 141, 10.1f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(81, 36000000L, 142, 7.6f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(82, 37000000L, 143, 7.6f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(83, 38000000L, 144, 7.7f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(84, 39000000L, 145, 7.8f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        // ---- 第 85 层【属性爆炸】 ----
        registerFloor(85, 40000000L, 146, 9.5f, 9,
                new MonsterConfig(ZOMBIE, 6, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(86, 41000000L, 147, 7.9f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(87, 42000000L, 148, 8.0f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(88, 43000000L, 149, 8.1f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(89, 44000000L, 150, 8.2f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        // ---- 第 90 层【数值爆炸】，词条 10 级（满级词条） ----
        registerFloor(90, 45000000L, 151, 11.1f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)

        );

        registerFloor(91, 46000000L, 152, 8.3f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)

        );

        registerFloor(92, 47000000L, 153, 8.4f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)

        );

        registerFloor(93, 48000000L, 154, 8.4f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(94, 49000000L, 155, 8.5f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        // ---- 第 95 层【属性爆炸】 ----
        registerFloor(95, 50000000L, 156, 10.3f, 10,
                new MonsterConfig(ZOMBIE, 6, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(96, 51000000L, 157, 8.7f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(97, 52000000L, 158, 8.7f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(98, 53000000L, 159, 8.8f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        // ---- 第 100 层【终极数值爆炸】：生命/防御/攻击 ×12，词条满级，通关必出神位 ----
        registerFloor(99, 54000000L, 160, 12.0f, 10,
                new MonsterConfig(ZOMBIE, 5, 3, "狂暴", "血祭"),
                new MonsterConfig(STRAY, 3, 3, "恐惧", "冰封"),
                new MonsterConfig(WITHER_SKELETON, 2, 3, "瞬移", "殉爆", "玄甲")
        );
    }

    /** 完整注册（含核心三围倍率 + 词条等级）。 */
    private static void registerFloor(int floorIndex, long nianxian, int timeLimitSeconds,
                                      float attributeMultiplier, int skillLevel, MonsterConfig... monsters) {
        REGISTRY.put(floorIndex, new FloorData(List.of(monsters), timeLimitSeconds, nianxian, attributeMultiplier, skillLevel));
    }

    /** 兼容写法：不填倍率与词条等级 = 1.0 / 1 级。 */
    private static void registerFloor(int floorIndex, long nianxian, int timeLimitSeconds, MonsterConfig... monsters) {
        registerFloor(floorIndex, nianxian, timeLimitSeconds, 1.0f, 1, monsters);
    }

    // ==================================================================================
    //  第 101 层起：按公式延伸
    // ==================================================================================

    /**
     * 取某一层的配置。
     *
     * <p>手写层（0~99）直接命中表；<b>101 层起按需生成并缓存</b>，所以同一层反复进出
     * 拿到的是同一份数据（怪物阵容不会每帧变）。</p>
     *
     * @param floor 0 基索引（0 = 第 1 层）
     */
    public static FloorData getFloorData(int floor) {
        int f = Mth.clamp(floor, 0, MAX_AVAILABLE_FLOOR - 1);
        FloorData data = REGISTRY.get(f);
        if (data != null) {
            return data;
        }
        data = generateFloor(f);
        REGISTRY.put(f, data);
        return data;
    }

    /**
     * 第 101 层起的怪物年限，<b>三段递减</b>，最终 1 亿 2 千万封顶。
     *
     * <table>
     *   <tr><th>层号</th><th>年限</th><th>每层增量</th></tr>
     *   <tr><td>100 → 136</td><td>5400 万 → 9000 万</td><td>+100 万</td></tr>
     *   <tr><td>137 → 146</td><td>9050 万 → 9500 万</td><td>+50 万</td></tr>
     *   <tr><td>147 → 396</td><td>9510 万 → 1.2 亿</td><td>+10 万</td></tr>
     * </table>
     *
     * <p>分段的意义：原来一律 +100 万，第 166 层就吃满 1.2 亿了，后期层数几乎没在成长；
     * 改成递减之后封顶推到<b>第 396 层</b>，高层每一层都还看得见实数变化。</p>
     *
     * @param floor 0 基索引
     */
    private static long nianxianForFloor(int floor) {
        long nianxian;
        if (floor <= NIANXIAN_TIER1_END) {
            // 第 136 层及以前：每层 +100 万
            nianxian = NIANXIAN_BASE_FLOOR_55 + (long) (floor - 55) * NIANXIAN_STEP;
        } else if (floor <= NIANXIAN_TIER2_END) {
            // 第 137~146 层：每层 +50 万
            nianxian = NIANXIAN_AT_90M + (long) (floor - NIANXIAN_TIER1_END) * NIANXIAN_STEP_MID;
        } else {
            // 第 147 层起：每层 +10 万
            nianxian = NIANXIAN_AT_95M + (long) (floor - NIANXIAN_TIER2_END) * NIANXIAN_STEP_LATE;
        }
        return Math.min(MAX_NIANXIAN, nianxian);
    }

    /**
     * 生成手写段之外的一层。
     *
     * <p>所有曲线都接着第 90~100 层的趋势走，不在 100/101 之间留跳变。</p>
     */
    private static FloorData generateFloor(int floor) {
        // ── 年限：分段递减增长，1 亿 2 千万封顶 ──
        long nianxian = nianxianForFloor(floor);

        // ── 限时：第 41 层起就是「层号 + 61」，继续 +1 秒/层；600 秒封顶 ──
        // 第 539 层以后不再加时：一层最多 16 只怪，10 分钟已经非常宽裕，
        // 再往上加就只剩「层数越大时间越离谱」这一种效果了。
        int timeLimit = Math.min(600, floor + 61);

        // ── 词条等级：1 + 层号/10，第 90 层起满级 ──
        int skillLevel = Math.min(TowerSkillPool.MAX_LEVEL, 1 + floor / 10);

        // ── 三围倍率：线性基线 + 整五/整十爆炸，不封顶 ──
        // ⚠️ 传入的是 0 基索引：手写段「第 100 层」= 索引 99（99 % 10 == 9），
        //    「第 95 层」= 索引 94（94 % 10 == 4）。这里必须跟着索引走，不能按层号取余，
        //    否则爆炸节奏会整体错位一层（第 101 层会被误判成数值爆炸层）。
        double multiplier = MULT_BASELINE_AT_99 + (floor - 99) * MULT_STEP;
        if (floor % 10 == 9) {
            multiplier *= MULT_NUM_BOOM;
        } else if (floor % 10 == 4) {
            multiplier *= MULT_ATTR_BOOM;
        }
        float attributeMultiplier = Math.round(multiplier * 10f) / 10f;

        return new FloorData(generateMonsters(floor), timeLimit, nianxian,
                attributeMultiplier, skillLevel);
    }

    /**
     * 第 101 层起的刷怪阵容。
     *
     * <p>模板取自第 90~100 层：僵尸群当肉盾、流浪者群当输出，数值爆炸层再补凋零骷髅。
     * 每 10 层一个台阶缓慢加数量，避免后期一层刷出上百只把服务端打死。</p>
     */
    private static List<MonsterConfig> generateMonsters(int floor) {
        int tier = (floor - HANDWRITTEN_FLOORS) / 10;      // 每 10 层 +1 台阶
        int zombieCount = 5 + Math.min(4, tier);           // 5 → 9，封顶
        int strayCount  = 2 + Math.min(2, tier / 3);       // 2 → 4，封顶
        // 同上：0 基索引的 %10==9 / %10==4 才是「整十层 / 整五层」
        boolean attrBoom = floor % 10 == 4;
        boolean numBoom  = floor % 10 == 9;

        List<MonsterConfig> list = new ArrayList<>(3);
        if (numBoom) {
            // 整十层：数值爆炸，满词条阵容
            list.add(new MonsterConfig(ZOMBIE, zombieCount + 1, 3, "狂暴", "血祭"));
            list.add(new MonsterConfig(STRAY, strayCount, 3, "恐惧", "冰封"));
            list.add(new MonsterConfig(WITHER_SKELETON, 2, 3, "瞬移", "殉爆", "玄甲"));
        } else if (attrBoom) {
            // 整五层：属性爆炸，多一只带狂暴的僵尸
            list.add(new MonsterConfig(ZOMBIE, zombieCount + 1, 3, "狂暴"));
            list.add(new MonsterConfig(STRAY, strayCount, 2));
            if (tier >= 3) {
                list.add(new MonsterConfig(WITHER_SKELETON, 1, 3, "恐惧"));
            }
        } else {
            list.add(new MonsterConfig(ZOMBIE, zombieCount, 3));
            list.add(new MonsterConfig(STRAY, strayCount, 2));
            // 第 150 层起，普通层也常驻一只凋零骷髅
            if (tier >= 5) {
                list.add(new MonsterConfig(WITHER_SKELETON, 1, 3, "恐惧"));
            }
        }
        return list;
    }

    /** 手写配表层数（第 1~100 层）。⚠️ 这不是「可挑战上限」，用 {@link #getMaxAvailableFloor()}。 */
    public static int getTotalFloors() {
        return HANDWRITTEN_FLOORS;
    }

    /** 可挑战的最高层号（1 基）。 */
    public static int getMaxAvailableFloor() {
        return MAX_AVAILABLE_FLOOR;
    }
}
