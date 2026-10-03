package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.hunhuanattributes.HunhuanAttributeHelper;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * <b>武魂永久基础属性</b> —— 把武魂魂环的一部分属性"抽成"出来，永久加给玩家自己。
 *
 * <h3>设计目的</h3>
 * 原设定里玩家裸值成长极慢（99 级裸攻击仅 1980），而 9 枚十万年魂环就有~29 万，
 * 导致"关武魂 = 弱 99.3%"，玩家不开武魂就完全不能打。
 * 本系统让玩家<b>把武魂的一部分实力固化成本</b>，即使关掉武魂也保留一部分战力。
 *
 * <h3>继承比例（按等级线性提升，<b>绝不写死</b>）</h3>
 * <pre>
 *   比例 = 10% + (等级 - 1) / 98 × 40%
 *   Lv1 = 10%   Lv50 ≈ 30%   Lv99 = 50%
 * </pre>
 *
 * <h3>⭐ 防"左脚踩右脚"刷属性的四道锁（本类的核心）</h3>
 * <ol>
 *   <li><b>独立存储</b>：抽成结果写进 {@code wuhunPermanentStats}，
 *       <b>绝不</b>回写 {@code getGongji()} 这类裸值字段 —— 裸值是"基线"的一部分，
 *       回写就等于"抽成→加裸值→再抽成"的正反馈。</li>
 *   <li><b>不参与下一次抽成</b>：抽成基线只取 {@code getWuhunBonus}
 *       （= 纯魂环总和），而 {@code getWuhunBonus} 只读
 *       {@code monsterCapabilityLists}，<b>不含</b>本属性 → 天然无环。</li>
 *   <li><b>单向递增 + 取max</b>：已得的值只增不减（等级降低、卸环都不会掉回）。
 *       换武魂只会在"新基线算出更高值"时补差额，不可能双份叠加。</li>
 *   <li><b>每 tick 至多结算一次</b>：用 {@link #SETTLED_FLAG} 标记，
 *       同一次 {@code grantPermanentByLevel} 调用里即使被多处调用也只生效一次。</li>
 * </ol>
 *
 * <p>另有数值净化：非有限值（NaN/Inf）、负数、超出 {@link #HARD_CAP} 的一律拒绝。</p>
 */
public final class WuhunPermanent {

    private WuhunPermanent() {
    }

    // ==================== 数值保护 ====================

    /** 继承比例下限（1 级）。 */
    private static final float MIN_RATE = 0.15f;
    /** 继承比例上限（99 级封顶）。 */
    private static final float MAX_RATE = 0.55f;
    /** 玩家最高等级（与 PlayerUpgradeSystem 的99 上限一致）。 */
    private static final int MAX_LEVEL = 99;
    /**
     * 单条永久属性的硬上限。
     *
     * <p>按"9 枚千万年魂环 × 50%"量级取整，给足余量又不可能失控。
     * 魂环年限最高 1e10，单枚 wugong 约 200 万+，9 枚 ×50% 约 900 万 —— 取 1e9。</p>
     */
    public static final float HARD_CAP = 1_000_000_000f;

    private static final Random RANDOM = new Random();

    // ==================== 比例 ====================

    /**
     * 按玩家等级算继承比例，<b>线性插值、绝不写死</b>。
     *
     * @param level 玩家等级（1~99，超出自动夹取）
     * @return 0.10 ~ 0.50
     */
    public static float rateForLevel(int level) {
        int lv = Math.max(1, Math.min(MAX_LEVEL, level));
        if (MAX_LEVEL <= 1) return MAX_RATE;
        return MIN_RATE + (lv - 1f) / (MAX_LEVEL - 1f) * (MAX_RATE - MIN_RATE);
    }

    /**
     * 按当前等级重算永久属性（<b>取 max，单向递增</b>）。<b>只在升级流程里调</b>。
     *
     * <p>安全性：</p>
     * <ul>
     *   <li>基线取 {@link HunhuanAttributeHelper#getWuhunBonus} —— 纯魂环总和，
     *       <b>不含</b>已有的永久属性 → 无自反馈；</li>
     *   <li>写入用 {@code merge(key, v, max)} —— 只会变多不会变少，
     *       反复调用结果幂等；</li>
     *   <li>每条都过 {@link #HARD_CAP} 与有限性校验。</li>
     * </ul>
     *
     * @return 本次真正新增的项数（0 = 没变化）
     */
    public static int grantPermanentByLevel(ServerPlayer player, PlayerAttributeCapability cap) {
        // —— 锁 4：同一次调用链内只结算一次 ——
        if (isSettled(cap)) return 0;
        markSettled(cap);

        int level = cap.getDengji();
        float rate = rateForLevel(level);

        int changed = 0;
        // 走玩家侧的同一个 getter 口径（会正确处理 NPC / 关闭武魂等情况）
        for (AttributePointSpec spec : AttributePointSpec.values()) {
            float base = wuhunBonusOf(player, spec.getKey());
            if (!Float.isFinite(base) || base <= 0f) continue;

            float want = base * rate;
            if (!Float.isFinite(want) || want <= 0f) continue;
            if (want > HARD_CAP) want = HARD_CAP;

            Map<String, Float> map = cap.getWuhunPermanentStats();
            float have = map.getOrDefault(spec.getKey(), 0f);
            // 锁 3：只增不减，且不会因为重复结算而翻倍
            if (want > have) {
                map.put(spec.getKey(), want);
                changed++;
            }
        }
        return changed;
    }

    /**
     * 取"当前武魂魂环在某个属性上的总和"，<b>不含</b>永久属性。
     *
     * <p>刻意不直接调 {@code ModAttributeAPI} 的聚合 getter —— 那个会连带算上
     * 魂骨/装备/药水/永久属性，用作基线就会引入外部变量。</p>
     */
    private static float wuhunBonusOf(ServerPlayer player, String key) {
        Function<MobAttributeCapability, Float> getter = switch (key) {
            case "gongji" -> MobAttributeCapability::getGongji;
            case "fangyu" -> MobAttributeCapability::getFangyu;
            case "maxshengming" -> MobAttributeCapability::getMaxshengming;
            case "baojilv" -> MobAttributeCapability::getBaojilv;
            case "baojishanghai" -> MobAttributeCapability::getBaojishanghai;
            case "shengminghuifu" -> MobAttributeCapability::getShengminghuifu;
            case "xixue" -> MobAttributeCapability::getXixue;
            case "shanbi" -> MobAttributeCapability::getShanbi;
            case "mingzhong" -> MobAttributeCapability::getMingzhong;
            case "wuchuan" -> MobAttributeCapability::getWuchuan;
            case "kangbao" -> MobAttributeCapability::getKangbao;
            default -> null;
        };
        if (getter == null) return 0f;
        return HunhuanAttributeHelper.getWuhunBonus(player, getter);
    }

    // ==================== 查询 ====================

    /** 某条永久属性当前值（未获得返回 0）。 */
    public static float get(PlayerAttributeCapability cap, String key) {
        float v = cap.getWuhunPermanentStats().getOrDefault(key, 0f);
        return Float.isFinite(v) && v > 0f ? v : 0f;
    }

    public static float getGongji(PlayerAttributeCapability cap) {
        return get(cap, "gongji");
    }

    public static float getMaxshengming(PlayerAttributeCapability cap) {
        return get(cap, "maxshengming");
    }

    // ==================== 转生 ====================

    /**
     * 转生：永久属性<b>按 5% 打折</b>（与其它属性 {@code /20} 的继承口径一致），
     * <b>绝不是完全保留</b>。
     *
     * <p>注意"未开武魂转生很亏"这个问题：未开武魂时本属性本来就是 0，
     * 打折后仍是 0，数值上不亏；但玩家的<b>感受</b>上会亏（等于白存了）。
     * 这一点已记录为待优化项（见类注释），暂不擅自改规则。</p>
     *
     * @return 打折后保留下来的项数
     */
    public static int zhuanshengKeep(PlayerAttributeCapability newCap, PlayerAttributeCapability oldCap) {
        Map<String, Float> old = oldCap.getWuhunPermanentStats();
        if (old.isEmpty()) {
            newCap.getWuhunPermanentStats().clear();
            return 0;
        }
        newCap.getWuhunPermanentStats().clear();
        int kept = 0;
        for (Map.Entry<String, Float> e : old.entrySet()) {
            float v = e.getValue() / 20f;      // 5%
            if (Float.isFinite(v) && v > 0f) {
                newCap.getWuhunPermanentStats().put(e.getKey(), v);
                kept++;
            }
        }
        return kept;
    }

    // ==================== 防重入 ====================

    /**
     * "本次调用链已结算过"的标记。
     *
     * <p><b>刻意不用 configFlags 的位</b>：那个 long 里的 12 个 bit 已经被玩家配置开关占满，
     * 借用高位会和 {@code PlayerAttributeCapability} 的其它逻辑纠缠。
     * 这里用一个独立的 WeakHashMap，key 是 capability 本身（玩家下线即回收）。</p>
     */
    private static final Map<PlayerAttributeCapability, Boolean> SETTLED = new java.util.WeakHashMap<>();

    private static boolean isSettled(PlayerAttributeCapability cap) {
        return SETTLED.containsKey(cap);
    }

    private static void markSettled(PlayerAttributeCapability cap) {
        SETTLED.put(cap, Boolean.TRUE);
    }

    /**
     * 清除"已结算"标记。<b>每次真正结算前必须调一次</b>，
     * 否则永久属性只会增长一次就再也不涨。
     */
    public static void clearSettledFlag(PlayerAttributeCapability cap) {
        SETTLED.remove(cap);
    }
}