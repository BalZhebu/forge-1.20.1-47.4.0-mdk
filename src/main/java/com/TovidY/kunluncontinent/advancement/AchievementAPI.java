package com.TovidY.kunluncontinent.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * 成就（进度）统一调用入口。
 *
 * <p>玩法代码里只调用本类的语义化方法，不需要关心成就的注册路径、JSON 条件或触发器细节。
 * 事件 ID 常量与 datagen（{@code ModAdvancementProvider}）共用同一套，避免两边写串。</p>
 *
 * <p>典型用法：</p>
 * <pre>{@code
 * AchievementAPI.onAwakenWuhun(player);                  // 觉醒武魂
 * AchievementAPI.onAbsorbHunhuan(player, nianxian, n);   // 吸收魂环（含年限分档 + 九环圆满）
 * AchievementAPI.onAlchemySuccess(nearbyPlayer, quality);// 炼丹成功（含品质分档）
 * AchievementAPI.onReincarnate(player, times);           // 转生（times = 累计次数，1/2 分档）
 * AchievementAPI.onTowerCleared(player, newFloor);        // 魂塔通关（10/30/50/100 层分档）
 * AchievementAPI.onGodExamStart(player);                 // 开启神考
 * AchievementAPI.onGodExamStagePassed(player, stage);    // 通过某一考
 * AchievementAPI.onGodAscended(player);                  // 成就神位
 * AchievementAPI.onEnterThunderRealm(player);            // 踏入雷界
 * AchievementAPI.onEnterCelestialRealm(player);          // 踏入神界
 * AchievementAPI.onEnterPolarIceRealm(player);           // 踏入极冰界
 * AchievementAPI.onEnterTowerRealm(player);              // 踏入魂塔秘境
 * AchievementAPI.onEatDanyao(player, count);             // 服用丹药（count>=2 视为批量）
 * }</pre>
 */
public final class AchievementAPI {

    private AchievementAPI() {
    }

    // ==================================================================================
    //  事件 ID（必须与 datagen 中使用的字符串完全一致）
    // ==================================================================================

    /** 第一次觉醒武魂。 */
    public static final String WUHUN_AWAKEN = "wuhun_awaken";
    /** 觉醒仪式上出了两个武魂（双生武魂）。 */
    public static final String WUHUN_DUO = "wuhun_duo";
    /** 觉醒仪式上出了三个武魂（三生武魂）。 */
    public static final String WUHUN_TRIPLE = "wuhun_triple";
    /** 第一次吸收魂环。 */
    public static final String HUNHUAN_FIRST = "hunhuan_first";
    /** 集齐九枚魂环。 */
    public static final String HUNHUAN_NINE = "hunhuan_nine";
    /** 吸收千年魂环。 */
    public static final String HUNHUAN_THOUSAND = "hunhuan_thousand";
    /** 吸收万年魂环。 */
    public static final String HUNHUAN_MYRIAD = "hunhuan_myriad";
    /** 吸收十万年魂环。 */
    public static final String HUNHUAN_HUNDRED_THOUSAND = "hunhuan_hundred_thousand";
    /** 吸收百万年魂环。 */
    public static final String HUNHUAN_MILLION = "hunhuan_million";
    /** 获得神赐魂环（千万年级）。 */
    public static final String HUNHUAN_DIVINE = "hunhuan_divine";

    /** 第一次成功炼丹（药散及以上，破碎不算）。 */
    public static final String ALCHEMY_FIRST = "alchemy_first";
    /** 炼出灵丹。 */
    public static final String ALCHEMY_SPIRIT = "alchemy_spirit";
    /** 炼出宝丹。 */
    public static final String ALCHEMY_TREASURE = "alchemy_treasure";
    /** 炼出仙丹（仙品）。 */
    public static final String ALCHEMY_IMMORTAL = "alchemy_immortal";

    /** 第一次转生。 */
    public static final String REINCARNATION_FIRST = "reincarnation_first";
    /** 第一次挑战魂塔。 */
    public static final String TOWER_FIRST = "tower_first";
    /** 魂塔通关十层。 */
    public static final String TOWER_TEN = "tower_ten";
    /** 接下神之试炼。 */
    public static final String GOD_EXAM_START = "god_exam_start";
    /** 第一次踏入雷界。 */
    public static final String THUNDER_REALM_ENTER = "thunder_realm_enter";

    // ---------- 维度探索 ----------
    /** 第一次踏入神界（天境）。 */
    public static final String CELESTIAL_REALM_ENTER = "celestial_realm_enter";
    /** 第一次踏入极冰界。 */
    public static final String POLAR_ICE_REALM_ENTER = "polar_ice_realm_enter";
    /** 第一次踏入魂塔秘境。 */
    public static final String TOWER_REALM_ENTER = "tower_realm_enter";

    // ---------- 神考进阶 ----------
    /** 通过神考第一考。 */
    public static final String GOD_EXAM_STAGE1 = "god_exam_stage1";
    /** 通过神考第五考（半程）。 */
    public static final String GOD_EXAM_STAGE5 = "god_exam_stage5";
    /** 成就神位（九考全过）。 */
    public static final String GOD_ASCENDED = "god_ascended";

    // ---------- 魂塔层数 ----------
    /** 魂塔通关三十层。 */
    public static final String TOWER_THIRTY = "tower_thirty";
    /** 魂塔通关五十层。 */
    public static final String TOWER_FIFTY = "tower_fifty";
    /** 魂塔通关百层（登顶必出神位）。 */
    public static final String TOWER_HUNDRED = "tower_hundred";

    // ---------- 丹药 / 聚灵 ----------
    /** 第一次服用丹药。 */
    public static final String DANYAO_FIRST = "danyao_first";
    /** 批量服用丹药（蹲下右键一次吃多颗）。 */
    public static final String DANYAO_BATCH = "danyao_batch";

    // ---------- 魂骨 ----------
    /** 第一次装备魂骨。 */
    public static final String BONE_FIRST = "bone_first";
    /** 七处魂骨槽全部装满。 */
    public static final String BONE_SEVEN = "bone_seven";

    // ---------- 神祇进阶 ----------
    /** 获得封神时赐予的神赐魂环。 */
    public static final String GOD_RING_BLESSED = "god_ring_blessed";

    // ---------- 转生 / 武魂 ----------
    /** 完成第二次转生。 */
    public static final String REINCARNATION_SECOND = "reincarnation_second";
    /** 第一次开启武魂真身。 */
    public static final String WUHUN_ZHENSHEN = "wuhun_zhenshen";
    /** 吸收一枚特殊生物魂环（自带专属魂技）。 */
    public static final String SPECIAL_SOUL_RING = "special_soul_ring";

    // ---------- 装备 ----------
    /** 穿齐四件同套本模组盔甲（触发四件套加成）。 */
    public static final String ARMOR_SET_FULL = "armor_set_full";

    // ==================================================================================
    //  分档规则（新增档位只需在这里加一行，运行时会自动多触发一个成就）
    // ==================================================================================

    /** 魂环年限档位：达到阈值即点亮对应成就。 */
    public record NianxianTier(int threshold, String eventId) {
    }

    public static final List<NianxianTier> NIANXIAN_TIERS = List.of(
            new NianxianTier(1_000, HUNHUAN_THOUSAND),
            new NianxianTier(10_000, HUNHUAN_MYRIAD),
            new NianxianTier(100_000, HUNHUAN_HUNDRED_THOUSAND),
            new NianxianTier(1_000_000, HUNHUAN_MILLION),
            new NianxianTier(10_000_000, HUNHUAN_DIVINE)
    );

    /** 丹药品质档位：ordinal 对应 {@code DanYaoQuality}（0=破碎 1=药散 2=药丹 3=灵丹 4=宝丹 5=仙丹）。 */
    public record QualityTier(int minQuality, String eventId) {
    }

    public static final List<QualityTier> QUALITY_TIERS = List.of(
            new QualityTier(1, ALCHEMY_FIRST),
            new QualityTier(3, ALCHEMY_SPIRIT),
            new QualityTier(4, ALCHEMY_TREASURE),
            new QualityTier(5, ALCHEMY_IMMORTAL)
    );

    /** 一枚武魂最多承载的魂环数量（九魂技对应九环）。 */
    public static final int MAX_RINGS = 9;

    /**
     * 魂塔层数档位：达到阈值即点亮对应成就。
     * <p>十层已由 {@link #TOWER_TEN} 覆盖，这里只补更高的档。</p>
     */
    public record TowerTier(int floor, String eventId) {
    }

    public static final List<TowerTier> TOWER_TIERS = List.of(
            new TowerTier(30, TOWER_THIRTY),
            new TowerTier(50, TOWER_FIFTY),
            new TowerTier(100, TOWER_HUNDRED)
    );

    // ==================================================================================
    //  基础触发
    // ==================================================================================

    /** 触发一个成就事件。非服务端玩家会被安全忽略。 */
    public static void trigger(Player player, String eventId) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModTriggers.KUNLUN_TRIGGER.trigger(serverPlayer, eventId);
        }
    }

    // ==================================================================================
    //  语义化入口
    // ==================================================================================

    /** 觉醒武魂（含双生武魂）。 */
    public static void onAwakenWuhun(Player player) {
        trigger(player, WUHUN_AWAKEN);
    }

    /** 一次觉醒出两个及以上武魂。 */
    public static void onAwakenDuo(Player player) {
        trigger(player, WUHUN_DUO);
    }

    /** 一次觉醒出三个武魂。 */
    public static void onAwakenTriple(Player player) {
        trigger(player, WUHUN_TRIPLE);
    }

    /**
     * 吸收魂环。
     *
     * @param nianxian   本次吸收的魂环年限
     * @param totalRings 该武魂当前已吸收的魂环总数
     */
    public static void onAbsorbHunhuan(Player player, double nianxian, int totalRings) {
        trigger(player, HUNHUAN_FIRST);
        for (NianxianTier tier : NIANXIAN_TIERS) {
            if (nianxian >= tier.threshold()) {
                trigger(player, tier.eventId());
            }
        }
        if (totalRings >= MAX_RINGS) {
            trigger(player, HUNHUAN_NINE);
        }
    }

    /**
     * 炼丹成功。
     *
     * @param quality {@code DanYaoQuality} 的 ordinal，破碎（0）不会点亮任何成就
     */
    public static void onAlchemySuccess(Player player, int quality) {
        for (QualityTier tier : QUALITY_TIERS) {
            if (quality >= tier.minQuality()) {
                trigger(player, tier.eventId());
            }
        }
    }

    /**
     * 转生成功（已经带上本次之后的转生次数）。
     *
     * @param times 累计转生次数（1 = 第一次，2 = 第二次……）
     */

    /** 开始挑战魂塔。 */
    public static void onTowerStart(Player player) {
        trigger(player, TOWER_FIRST);
    }

    /**
     * 魂塔通关一层。
     *
     * @param newFloor 通关后达到的新层数
     */
    public static void onTowerCleared(Player player, int newFloor) {
        trigger(player, TOWER_FIRST);
        if (newFloor >= 10) {
            trigger(player, TOWER_TEN);
        }
        // 三十 / 五十 / 百层
        for (TowerTier tier : TOWER_TIERS) {
            if (newFloor >= tier.floor()) {
                trigger(player, tier.eventId());
            }
        }
    }

    /** 接下神之试炼。 */
    public static void onGodExamStart(Player player) {
        trigger(player, GOD_EXAM_START);
    }

    /**
     * 通过神考某一考。
     *
     * @param stage 刚完成的考号（1~9）
     */
    public static void onGodExamStagePassed(Player player, int stage) {
        if (stage == 1) trigger(player, GOD_EXAM_STAGE1);
        if (stage == 5) trigger(player, GOD_EXAM_STAGE5);
    }

    /** 成就神位（九考全过）。 */
    public static void onGodAscended(Player player) {
        trigger(player, GOD_ASCENDED);
    }

    /** 第一次踏入雷界。 */
    public static void onEnterThunderRealm(Player player) {
        trigger(player, THUNDER_REALM_ENTER);
    }

    /** 第一次踏入神界（天境）。 */
    public static void onEnterCelestialRealm(Player player) {
        trigger(player, CELESTIAL_REALM_ENTER);
    }

    /** 第一次踏入极冰界。 */
    public static void onEnterPolarIceRealm(Player player) {
        trigger(player, POLAR_ICE_REALM_ENTER);
    }

    /** 第一次踏入魂塔秘境。 */
    public static void onEnterTowerRealm(Player player) {
        trigger(player, TOWER_REALM_ENTER);
    }

    /** 服用丹药（含批量服丹，单颗也会触发第一次）。 */
    public static void onEatDanyao(Player player, int count) {
        trigger(player, DANYAO_FIRST);
        if (count >= 2) {
            trigger(player, DANYAO_BATCH);
        }
    }

    /**
     * 魂骨槽发生变化后调用（装备 / 卸下都算）。
     *
     * @param boneCount 当前 7 个魂骨槽里非空的数量
     */
    public static void onBoneChanged(Player player, int boneCount) {
        if (boneCount <= 0) {
            return;
        }
        trigger(player, BONE_FIRST);
        if (boneCount >= 7) {
            trigger(player, BONE_SEVEN);
        }
    }

    /** 获得神赐魂环（封神时自动赐予的那一枚）。 */
    public static void onGodRingBlessed(Player player) {
        trigger(player, GOD_RING_BLESSED);
    }

    /**
     * 转生成功（已经带上本次之后的转生次数）。
     *
     * @param times 累计转生次数（1 = 第一次，2 = 第二次……）
     */
    public static void onReincarnate(Player player, int times) {
        trigger(player, REINCARNATION_FIRST);
        if (times >= 2) {
            trigger(player, REINCARNATION_SECOND);
        }
    }

    /** 第一次开启武魂真身。 */
    public static void onZhenshenActivate(Player player) {
        trigger(player, WUHUN_ZHENSHEN);
    }

    /** 吸收了一枚特殊生物魂环。 */
    public static void onSpecialSoulRing(Player player) {
        trigger(player, SPECIAL_SOUL_RING);
    }

    /** 四件套穿齐。 */
    public static void onArmorSetFull(Player player) {
        trigger(player, ARMOR_SET_FULL);
    }
}
