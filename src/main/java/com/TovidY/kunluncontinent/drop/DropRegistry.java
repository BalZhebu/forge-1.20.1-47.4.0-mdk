package com.TovidY.kunluncontinent.drop;

import com.TovidY.kunluncontinent.item.ModItems;

import java.util.ArrayList;
import java.util.List;

/**
 * ⭐ <b>全项目唯一的掉落表登记处</b> —— 想新增任何"击杀掉 X"的东西，<b>只改这个文件</b>。
 *
 * <p>用法（照抄一行改参数即可）：</p>
 * <pre>
 *   // ① 所有生物 500 年以上，1% 概率掉 1~5 个返气草种子
 *   register(DropRule.of("返气草种子", ModItems.FANQICAO_SEEDS)
 *           .filter(DropFilter.all())
 *           .chance(0.01)
 *           .minNianxian(500)
 *           .count(1, 5)
 *           .build());
 *
 *   // ② 指定生物（水生）+ 6% 概率掉 1 个深海精华
 *   register(DropRule.of("深海精华", ModItems.DEEPSEA_JINGHUA)
 *           .filter(DropFilter.nameContains("fish", "ocean", "whale", "shark"))
 *           .chance(0.06)
 *           .build());
 *
 *   // ③ 只有僵尸、且 1万年以上，0.5% 掉钻石
 *   register(DropRule.of("僵尸宝石", Items.DIAMOND)
 *           .filter(DropFilter.is(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER))
 *           .chance(0.005)
 *           .minNianxian(10_000)
 *           .glowing()
 *           .build());
 *
 *   // ④ 年限区间限定
 *   register(DropRule.of("百年限定", ModItems.XXX)
 *           .range(100, 999)      // 只在 100~999 年之间掉
 *           .chance(0.2)
 *           .build());
 * </pre>
 *
 * <p><b>规则求值</b>：所有规则<b>各自独立判定</b>，一条命中不影响其它 —— 所以
 * "所有生物 500 年以上掉种子" 和 "水生 6% 掉精华" 可以同时存在，互不干扰。</p>
 */
public final class DropRegistry {

    private DropRegistry() {
    }

    private static final List<DropRule> RULES = new ArrayList<>();

    /** 登记一条规则（会自动注册，{@code build()} 不用再调）。 */
    public static void register(DropRule.Builder builder) {
        RULES.add(builder.build());
    }

    /** 登记一条已构建好的规则。 */
    public static void register(DropRule rule) {
        RULES.add(rule);
    }

    public static List<DropRule> rules() {
        return RULES;
    }

    // ==================== 正式掉落表 ====================

    static {
        // ---------- 深海精华：水生生物 6% ----------
        // 只认注册名关键词 + MobType.WATER，**不再用 isInWaterOrBubble 兜底**
        // （原写法会把"被推进水里的陆怪"也算成水生）。
        register(DropRule.of("深海精华", ModItems.DEEPSEA_JINGHUA)
                .filter(DropFilter.mobType(net.minecraft.world.entity.MobType.WATER)
                        .or(DropFilter.nameContains("fish", "ocean", "whale", "shark",
                                "dolphin", "squid", "cod", "salmon", "tropical", "puffer")))
                .chance(0.06)
                .build());

        //击杀50000年以上生物可有5%的概率掉落重置卷轴
        register(DropRule.of("重置卷轴", ModItems.RESET_SCROLL)
                .filter(DropFilter.all())
                .chance(0.05)
                .minNianxian(50000)
                .build());

        // ---------- 返气草种子：所有生物 500 年以上 ----------
        // 概率随年限递增：500 年 1%，每 1000 年 +0.1%，最高 8%（等价于旧的手写逻辑）。
        register(DropRule.of("返气草种子", ModItems.FANQICAO_SEEDS)
                .filter(DropFilter.all())
                .chance(0.01)
                .minNianxian(500)
                .perYear(1000, 0.001)
                .capChance(0.08)
                .count(1, 5)
                .build());
    }
}
