package com.TovidY.kunluncontinent.tower.floor;

import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挑战塔关卡表（共 100 层，全部显式手写，无公式生成）。
 *
 * <p>每层 = (基础年限, 限时秒, 全属性倍率, 词条等级, 刷怪配置...)。</p>
 * <ul>
 *   <li><b>基础年限</b>：决定怪物全属性的基础值（{@code MobAttributeCapability.initNianxian}）；</li>
 *   <li><b>全属性倍率</b>：本层所有怪物的 11 项属性整体再乘该系数；</li>
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
 */
public class TowerFloorRegistry {

    public static class FloorData {
        public final List<MonsterConfig> monsters;
        public final int timeLimitSeconds;
        public final long baseNianxian;
        /** 本层怪物全属性倍率（1.0 = 原样）。 */
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

    // 常用怪物 id，缩短下面表格的书写
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
                new MonsterConfig(SKELETON, 1, 2)
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
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 1, 2, "迅速")
        );

        registerFloor(17, 1900000L, 45, 2.5f, 2,
                new MonsterConfig(HUSK, 3, 2),
                new MonsterConfig(ZOMBIE, 2, 3, "壁垒")
        );

        registerFloor(18, 2000000L, 50, 2.6f, 2,
                new MonsterConfig(ZOMBIE, 2, 4),
                new MonsterConfig(SKELETON, 2, 2, "泥沼"),
                new MonsterConfig(STRAY, 1, 1)
        );

        // ---- 第 20 层【数值爆炸】：凋零骷髅登场，词条 3 级 ----
        registerFloor(19, 2150000L, 55, 3.2f, 3,
                new MonsterConfig(ZOMBIE, 3, 3, "浑厚"),
                new MonsterConfig(HUSK, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        // =====================================================================
        // 第三阶段（第 21~30 层）：词条 3 级，雪魔登场
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
                new MonsterConfig(SKELETON, 2, 3, "免伤")
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
                new MonsterConfig(STRAY, 2, 2)
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
                new MonsterConfig(STRAY, 2, 2)
        );

        // ---- 第 35 层【属性爆炸】 ----
        registerFloor(34, 5300000L, 85, 5.0f, 4,
                new MonsterConfig(ZOMBIE, 3, 4, "狂暴"),
                new MonsterConfig(HUSK, 2, 2, "浑厚", "壁垒"),
                new MonsterConfig(SKELETON, 3, 3, "瞬移", "恐惧"),
                new MonsterConfig(STRAY, 1, 2, "迅速")
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
                new MonsterConfig(STRAY, 2, 2, "迅速")
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
                new MonsterConfig(ZOMBIE, 3, 3),
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

        registerFloor(56, 10200000L, 117, 5.6f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(57, 10400000L, 118, 5.7f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(58, 10600000L, 119, 5.8f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        registerFloor(59, 10800000L, 120, 5.8f, 6,
                new MonsterConfig(ZOMBIE, 3, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 1, 2, "恐惧")
        );

        // ---- 第 60 层【数值爆炸】，词条 7 级 ----
        registerFloor(60, 11000000L, 121, 8.0f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(61, 11200000L, 122, 6.0f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(62, 11400000L, 123, 6.1f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(63, 11600000L, 124, 6.1f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(64, 11800000L, 125, 6.2f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        // ---- 第 65 层【属性爆炸】 ----
        registerFloor(65, 12000000L, 126, 7.6f, 7,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(66, 12200000L, 127, 6.4f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(67, 12400000L, 128, 6.4f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(68, 12600000L, 129, 6.5f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(69, 12800000L, 130, 6.6f, 7,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(SKELETON, 2, 2)
        );

        registerFloor(70, 13000000L, 131, 9.0f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(71, 13200000L, 132, 6.8f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(72, 13400000L, 133, 6.9f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(73, 13600000L, 134, 7.0f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(74, 13800000L, 135, 7.0f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        // ---- 第 75 层【属性爆炸】 ----
        registerFloor(75, 14000000L, 136, 8.5f, 8,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(76, 14200000L, 137, 7.2f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(77, 14400000L, 138, 7.3f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(78, 14600000L, 139, 7.3f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        registerFloor(79, 14800000L, 140, 7.4f, 8,
                new MonsterConfig(ZOMBIE, 4, 3),
                new MonsterConfig(STRAY, 2, 2),
                new MonsterConfig(HUSK, 2, 2, "壁垒")
        );

        // ---- 第 80 层【数值爆炸】，词条 9 级 ----
        registerFloor(80, 15000000L, 141, 10.1f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(81, 15200000L, 142, 7.6f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(82, 15400000L, 143, 7.6f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(83, 15600000L, 144, 7.7f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(84, 15800000L, 145, 7.8f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        // ---- 第 85 层【属性爆炸】 ----
        registerFloor(85, 16000000L, 146, 9.5f, 9,
                new MonsterConfig(ZOMBIE, 6, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(86, 16200000L, 147, 7.9f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(87, 16400000L, 148, 8.0f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(88, 16600000L, 149, 8.1f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        registerFloor(89, 16800000L, 150, 8.2f, 9,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(SKELETON, 2, 2),
                new MonsterConfig(WITHER_SKELETON, 3, 2, "恐惧")
        );

        // ---- 第 90 层【数值爆炸】，词条 10 级（满级词条） ----
        registerFloor(90, 17000000L, 151, 11.1f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)

        );

        registerFloor(91, 17200000L, 152, 8.3f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)

        );

        registerFloor(92, 17400000L, 153, 8.4f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)

        );

        registerFloor(93, 17600000L, 154, 8.4f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(94, 17800000L, 155, 8.5f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        // ---- 第 95 层【属性爆炸】 ----
        registerFloor(95, 18000000L, 156, 10.3f, 10,
                new MonsterConfig(ZOMBIE, 6, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(96, 18200000L, 157, 8.7f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(97, 18400000L, 158, 8.7f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        registerFloor(98, 18600000L, 159, 8.8f, 10,
                new MonsterConfig(ZOMBIE, 5, 3),
                new MonsterConfig(STRAY, 2, 2)
        );

        // ---- 第 100 层【终极数值爆炸】：全属性 ×12，词条满级，通关必出神位 ----
        registerFloor(99, 18800000L, 160, 12.0f, 10,
                new MonsterConfig(ZOMBIE, 5, 4, "狂暴"),
                new MonsterConfig(STRAY, 3, 3, "恐惧"),
                new MonsterConfig(WITHER_SKELETON, 2, 3, "瞬移")
        );
    }

    /** 完整注册（含全属性倍率 + 词条等级）。 */
    private static void registerFloor(int floorIndex, long nianxian, int timeLimitSeconds,
                                      float attributeMultiplier, int skillLevel, MonsterConfig... monsters) {
        REGISTRY.put(floorIndex, new FloorData(List.of(monsters), timeLimitSeconds, nianxian, attributeMultiplier, skillLevel));
    }

    /** 兼容写法：不填倍率与词条等级 = 1.0 / 1 级。 */
    private static void registerFloor(int floorIndex, long nianxian, int timeLimitSeconds, MonsterConfig... monsters) {
        registerFloor(floorIndex, nianxian, timeLimitSeconds, 1.0f, 1, monsters);
    }

    public static FloorData getFloorData(int floor) {
        return REGISTRY.getOrDefault(floor, REGISTRY.get(0));
    }

    /** 已配置的总层数（= 最高索引 + 1），供"开放层数上限"直接引用。 */
    public static int getTotalFloors() {
        return REGISTRY.isEmpty() ? 0 : Collections.max(REGISTRY.keySet()) + 1;
    }
}
