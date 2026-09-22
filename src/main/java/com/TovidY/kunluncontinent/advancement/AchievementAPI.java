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
 * AchievementAPI.onReincarnate(player);                  // 转生
 * AchievementAPI.onTowerCleared(player, newFloor);        // 魂塔通关
 * AchievementAPI.onGodExamStart(player);                 // 开启神考
 * AchievementAPI.onEnterThunderRealm(player);            // 踏入雷界
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

    /** 转生成功。 */
    public static void onReincarnate(Player player) {
        trigger(player, REINCARNATION_FIRST);
    }

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
    }

    /** 接下神之试炼。 */
    public static void onGodExamStart(Player player) {
        trigger(player, GOD_EXAM_START);
    }

    /** 第一次踏入雷界。 */
    public static void onEnterThunderRealm(Player player) {
        trigger(player, THUNDER_REALM_ENTER);
    }
}
