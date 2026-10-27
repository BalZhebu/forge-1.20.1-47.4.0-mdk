package com.TovidY.kunluncontinent.capability.hunhuanattributes;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 魂环「年限提升」计算器 —— 神考全过后给全部魂环 +5 万年用的。
 *
 * <h3>为什么不能直接 {@code ring.setNianxian(old + 50000)}</h3>
 * <p>{@code setNianxian} 会走 {@code updateAttributesByNianxian()}，而那条公式里有两处随机：</p>
 * <ul>
 *   <li>{@code g} 有一段 {@code ThreadLocalRandom.nextInt(...)} 的浮动（越大年限浮动越大）；</li>
 *   <li>吸收时 {@code HunhuanWeakener} 还会按档位再乘一个 0.40~1.00 的随机保留比例。</li>
 * </ul>
 * <p>直接重算 = 把玩家当年抽到的运气和吃的削减<b>全部洗掉重骰</b>，
 * 属性有可能因为一次坏运气反而下降。所以这里用「<b>确定性基线比值</b>」：</p>
 *
 * <pre>
 *   新属性 = 旧属性 × ( 新年限的确定性基线 / 旧年限的确定性基线 )
 * </pre>
 *
 * <p>「确定性基线」= 把 {@code g} 里的随机项取 0 后的那套公式。
 * 它是随年限<b>单调递增</b>的，所以比值恒 &gt; 1；而旧属性里携带的
 * 「随机浮动 × 吸收削减」被整体保留下来 —— 玩家当年运气好还是差，放大后依然好或差。</p>
 *
 * <h3>新手区 ×0.35 削弱</h3>
 * <p>{@code MobAttributeCapability#checkAndApplyWeakness} 会给「主世界 0,0 附近 1000 格内、
 * 年限 &lt; 5000」的魂兽打上 {@code isWeakened}，属性整体 ×0.35。</p>
 * <p>这里把旧基线<b>乘以 0.35</b>、新基线按<b>不削弱</b>算 —— 于是分母变小、分子不变，
 * 一次乘算就同时完成了「年限提升」和「解除新手削弱」。新年限（≥ 5 万）本来也早已超出削弱条件。</p>
 *
 * <h3>为什么不用手动把属性加到玩家身上</h3>
 * <p>玩家总属性由 {@code ModAttributeAPI} 汇总：
 * {@code 裸值 + 加点 + getWuhunBonus(所有魂环求和) + 魂骨 + 永久 + 药水 + 护甲}。
 * 魂环属性一改，玩家属性<b>自动</b>跟着变，所以这里只需要改环 + 发包刷新，
 * 再往 {@code cap} 上加一遍会变成双倍。</p>
 */
public final class HunhuanNianxianUpgrader {

    /** 每枚魂环增加的年限。 */
    public static final long BONUS_NIANXIAN = 50_000L;

    /** 与 {@code MobAttributeCapability#applyGrowth} 里的削弱系数保持一致。 */
    private static final float NEWBIE_WEAKEN_FACTOR = 0.35f;

    private HunhuanNianxianUpgrader() {
    }

    // ==================================================================================
    //  确定性基线
    // ==================================================================================

    /**
     * 某一年的「确定性基线」。
     *
     * <p>⚠️ 这张分档表是魂环属性的<b>唯一定义</b> ——
     * {@code MobAttributeCapability#updateAttributesByNianxian} 也调用它，
     * 只是额外加上 {@code gRand} 的随机浮动。改数值只改这里。</p>
     *
     * @param g      攻击增长基数（不含随机）
     * @param l      副属性增长基数
     * @param gMult  攻击倍率
     * @param hpMult 最大生命倍率
     * @param hpBase 当前生命系数
     * @param gRand  攻击随机浮动上限（0 = 无浮动）
     */
    public record Baseline(float g, float l, float gMult, float hpMult, float hpBase, int gRand) {

        /** 带随机浮动的攻击基数 —— 生成属性时用。 */
        public float gJittered(ThreadLocalRandom random) {
            return gRand > 0 ? g + random.nextInt(gRand) : g;
        }

        public float wugongBase() {
            return g * gMult;
        }

        public float wufangBase() {
            return g / 3f;
        }

        public float maxHpBase() {
            return hpMult * g;
        }

        public float curHpBase() {
            return hpBase * g;
        }

        public float critRateBase() {
            return l;
        }

        public float critDamageBase() {
            return 5f * l;
        }

        public float kangbaoBase() {
            return 3f * l;
        }

        public float xixueBase() {
            return l / 4f;
        }
    }

    /**
     * 取某个年限的确定性基线（不含随机、不含吸收削减、不含新手削弱）。
     *
     * <p>分档边界与 {@code MobAttributeCapability#updateAttributesByNianxian} 完全一致：
     * 100 / 1千 / 1万 / 10万 / 100万 / 1000万 / 1亿。</p>
     */
    public static Baseline baselineOf(long nianxian) {
        if (nianxian < 100) {
            return new Baseline(2f + nianxian / 7f, 5f + nianxian / 20f, 1f, 10f, 10f, 10);
        }
        if (nianxian < 1_000) {
            return new Baseline(35f + nianxian / 10f, 15f + nianxian / 1000f * 10f, 2f, 10f, 10f, 50);
        }
        if (nianxian < 10_000) {
            return new Baseline(150f + nianxian / 20f, 20f + nianxian / 10_000f * 10f, 2f, 25f, 10f, 250);
        }
        if (nianxian < 100_000) {
            return new Baseline(400f + nianxian / 67f, 40f + nianxian / 100_000f * 10f, 3f, 40f, 10f, 750);
        }
        if (nianxian < 1_000_000) {
            return new Baseline(888f + nianxian / 133f, 50f + nianxian / 1_000_000f * 10f, 5f, 60f, 10f, 3_750);
        }
        if (nianxian < 10_000_000) {
            return new Baseline(2560f + nianxian / 500f, 100f + nianxian / 10_000_000f * 20f, 10f, 200f, 150f, 15_000);
        }
        if (nianxian < 100_000_000) {
            return new Baseline(6400f + nianxian / 1000f, 200f + nianxian / 100_000_000f * 50f, 25f, 500f, 400f, 100_000);
        }
        return new Baseline(15000f + nianxian / 2000f, 500f + nianxian / 1_000_000_000f * 100f, 60f, 1200f, 1000f, 500_000);
    }

    // ==================================================================================
    //  单枚魂环升级
    // ==================================================================================

    /**
     * 给一枚魂环加 {@link #BONUS_NIANXIAN} 年，并把属性按基线比值放大。
     *
     * <p>只改数值，不动 {@code shenci / shenciGod / sourceType / towerSkillsTag} 等字段
     * （是就地修改，不是新建对象）。</p>
     *
     * @return 是否真的改了（年限为 0 的异常环会跳过）
     */
    public static boolean upgradeRing(MobAttributeCapability ring) {
        if (ring == null) {
            return false;
        }
        long oldNianxian = ring.getNianxian();
        if (oldNianxian <= 0) {
            // 年限 0 说明这枚环没被正常初始化过，动它只会把 0 乘成 0
            return false;
        }
        long newNianxian = oldNianxian + BONUS_NIANXIAN;
        if (newNianxian < oldNianxian) {
            return false;   // long 溢出兜底
        }

        Baseline from = baselineOf(oldNianxian);
        Baseline to = baselineOf(newNianxian);
        float oldWeaken = ring.isWeakened() ? NEWBIE_WEAKEN_FACTOR : 1f;

        // ---- 主属性：攻击 / 防御 / 生命 ----
        ring.setWugong(scale(ring.getGongjiWithoutTemp(), from.wugongBase() * oldWeaken, to.wugongBase()));
        ring.setWufang(scale(ring.getFangyu(), from.wufangBase() * oldWeaken, to.wufangBase()));
        ring.setMaxshengming(scale(ring.getMaxshengming(), from.maxHpBase() * oldWeaken, to.maxHpBase()));
        ring.setShengming(scale(ring.getShengming(), from.curHpBase() * oldWeaken, to.curHpBase()));

        // ---- 副属性：暴击率 / 暴击伤害 / 抗暴 / 吸血 / 命中 / 闪避 / 物穿 ----
        ring.setBaojilv(scale(ring.getBaojilv(), from.critRateBase() * oldWeaken, to.critRateBase()));
        ring.setBaojishanghai(scale(ring.getBaojishanghai(), from.critDamageBase() * oldWeaken, to.critDamageBase()));
        ring.setKangbao(scale(ring.getKangbao(), from.kangbaoBase() * oldWeaken, to.kangbaoBase()));
        ring.setXixue(scale(ring.getXixue(), from.xixueBase() * oldWeaken, to.xixueBase()));
        ring.setMingzhong(scale(ring.getMingzhong(), from.critRateBase() * oldWeaken, to.critRateBase()));
        ring.setShanbi(scale(ring.getShanbi(), from.critRateBase() * oldWeaken, to.critRateBase()));
        ring.setWuchuan(scale(ring.getWuchuan(), from.critRateBase() * oldWeaken, to.critRateBase()));

        // ⚠️ 用 setNianxianWithoutRecalc：走 setNianxian 会重新随机、把上面算好的值冲掉
        ring.setNianxianWithoutRecalc(newNianxian);
        // 新年限远超削弱区间，标记留着会让这枚环永远背着 ×0.35
        ring.setWeakened(false);
        return true;
    }

    /**
     * 等比放大：{@code current × (newBase / oldBase)}。
     *
     * <p>{@code oldBase <= 0} 时原值返回（理论上不会发生，g 恒 &gt; 0）。</p>
     */
    private static float scale(float current, float oldBase, float newBase) {
        if (oldBase <= 0f || current == 0f) {
            return current;
        }
        return current * (newBase / oldBase);
    }

    // ==================================================================================
    //  玩家整体
    // ==================================================================================

    /**
     * 一次升级结果，给聊天提示用。
     *
     * @param wuhunCount 受影响武魂数
     * @param ringCount  受影响魂环数
     * @param skipped    是否因为已经领过而跳过
     */
    public record Result(int wuhunCount, int ringCount, boolean skipped) {
        public boolean changed() {
            return !skipped && ringCount > 0;
        }
    }

    /**
     * 把玩家<b>所有武魂</b>的<b>所有魂环</b>年限 +5 万。
     *
     * <p>幂等：{@link PlayerAttributeCapability#isGodNianxianBonus()} 为 true 时直接跳过，
     * 避免神考代码被重复触发（debug 命令 / 积攒型奖励重发）导致叠好几次。</p>
     */
    public static Result upgradeAll(ServerPlayer player) {
        PlayerAttributeCapability cap = player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return new Result(0, 0, true);
        }
        if (cap.isGodNianxianBonus()) {
            return new Result(0, 0, true);
        }

        Map<String, List<MobAttributeCapability>> map = cap.getMonsterCapabilityLists();
        if (map == null || map.isEmpty()) {
            return new Result(0, 0, false);
        }

        int wuhunCount = 0;
        int ringCount = 0;
        for (List<MobAttributeCapability> rings : map.values()) {
            if (rings == null || rings.isEmpty()) {
                continue;
            }
            boolean touched = false;
            for (MobAttributeCapability ring : rings) {
                if (upgradeRing(ring)) {
                    ringCount++;
                    touched = true;
                }
            }
            if (touched) {
                wuhunCount++;
            }
        }

        cap.setGodNianxianBonus(true);
        // 玩家总属性是 ModAttributeAPI 现算的（getWuhunBonus 求和），所以改完环发一次包就够了
        SynsAPI.synsPlayerAttribute(player);
        // 魂环的年限决定客户端渲染的配色 / 贴图，必须单独重播一次魂环数据
        PlayerHunhuanAPI.broadcastWuhunRings(player, false);
        return new Result(wuhunCount, ringCount, false);
    }
}
