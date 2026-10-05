package com.TovidY.kunluncontinent.drop;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * 一条声明式掉落规则。<b>整个类不可变</b>，用链式 API 拼出来。
 *
 * <p>典型用法（新增一条掉落 = 写这一行）：</p>
 * <pre>
 *   DropRule.of("深海精华", ModItems.DEEPSEA_JINGHUA)
 *          .filter(DropFilter.nameContains("fish", "ocean"))
 *          .chance(0.06)
 *          .range(1, 0);// 任何年限
 *
 *   DropRule.of("返气草种子", ModItems.FANQICAO_SEEDS)
 *          .filter(DropFilter.all())
 *          .chance(0.01)
 *          .minNianxian(500)                // 所有生物 500 年以上都可能掉
 *          .minNianxian(500).maxNianxian(10_000)
 *          .count(1, 5);                    // 掉 1~5 个
 * </pre>
 */
public final class DropRule {

    private final String id;
    private final Supplier<? extends Item> item;
    private final DropFilter filter;
    private final double chance;
    private final long minNianxian;      // 0 = 不限
    private final long maxNianxian;      // 0 = 不限
    /** 每增加 N 年，概率增加 chancePerYear（>=0）。0=固定概率。 */
    private final double chancePerYear;
    private final long chancePerYearPer;
    private final double maxChance;      // 概率上限（>0 时生效）
    private final int minCount;
    private final int maxCount;
    private final boolean glowing;

    private DropRule(Builder b) {
        this.id = b.id;
        this.item = b.item;
        this.filter = b.filter;
        this.chance = b.chance;
        this.minNianxian = b.minNianxian;
        this.maxNianxian = b.maxNianxian;
        this.chancePerYear = b.chancePerYear;
        this.chancePerYearPer = b.chancePerYearPer;
        this.maxChance = b.maxChance;
        this.minCount = b.minCount;
        this.maxCount = b.maxCount;
        this.glowing = b.glowing;
    }

    // ==================== 构建 ====================

    public static Builder of(String id, Supplier<? extends Item> item) {
        return new Builder(id, item);
    }

    /** 链式构建器。每一条规则用它。 */
    public static final class Builder {
        private final String id;
        private final Supplier<? extends Item> item;
        private DropFilter filter = DropFilter.all();
        private double chance = 1.0;
        private long minNianxian = 0;
        private long maxNianxian = 0;
        private double chancePerYear = 0;
        private long chancePerYearPer = 1;
        private double maxChance = 0;
        private int minCount = 1;
        private int maxCount = 1;
        private boolean glowing = false;

        private Builder(String id, Supplier<? extends Item> item) {
            this.id = id;
            this.item = item;
        }

        /** 限定生物种类。默认 {@link DropFilter#all()}。 */
        public Builder filter(DropFilter f) {
            this.filter = f;
            return this;
        }

        /** 掉落概率 0~1。 */
        public Builder chance(double c) {
            this.chance = c;
            return this;
        }

        /** 最低年限门槛（{@code 0} = 不限）。"所有生物 500 年以上"就靠这个。 */
        public Builder minNianxian(long v) {
            this.minNianxian = v;
            return this;
        }

        /** 最高年限（含）。{@code 0} = 不限。 */
        public Builder maxNianxian(long v) {
            this.maxNianxian = v;
            return this;
        }

        /** 一次设置年限区间。 */
        public Builder range(long min, long max) {
            this.minNianxian = min;
            this.maxNianxian = max;
            return this;
        }

        /** 掉 1~n 个。 */
        public Builder count(int min, int max) {
            this.minCount = min;
            this.maxCount = max;
            return this;
        }

        /**
         * 概率随年限递增：{@code chance + (年限 - minNianxian) / perYear × chancePerYear}。
         *
         * <p>配合 {@code capChance} 用。例：返气草种子
         * {@code chance(0.01).minNianxian(500).perYear(1000, 0.001).capChance(0.08)}
         * 就是"500 年 1%，每 1000 年 +0.1%，最高 8%"。</p>
         */
        public Builder perYear(long perYear, double chancePerYear) {
            this.chancePerYear = chancePerYear;
            this.chancePerYearPer = perYear;
            return this;
        }

        /** 概率上限（含）。 */
        public Builder capChance(double max) {
            this.maxChance = max;
            return this;
        }

        /** 掉出的物品发光（用于稀有物提示）。 */
        public Builder glowing() {
            this.glowing = true;
            return this;
        }

        public DropRule build() {
            return new DropRule(this);
        }
    }

    // ==================== 判定与结算 ====================

    public String id() {
        return id;
    }

    /**
     * 判定是否掉落；命中就返回要掉的 ItemStack，没掉返回 {@code null}。
     *
     * <p><b>不负责 spawn</b> —— 由 {@link DropExecutor} 统一结算，
     * 这样"要不要发光"这类表现层的事只在一个地方处理。</p>
     *
     * @param entity   被击杀的生物
     * @param nianxian 该生物的年限（无 capability 时传 0）
     * @param random   随机源
     */
    public ItemStack roll(LivingEntity entity, long nianxian, RandomSource random) {
        // 1) 生物种类
        if (!filter.test(entity)) return null;
        // 2) 年限区间
        if (minNianxian > 0 && nianxian < minNianxian) return null;
        if (maxNianxian > 0 && nianxian > maxNianxian) return null;
        // 3) 概率（可随年限递增，带上限）
        if (random.nextDouble() >= effectiveChance(nianxian)) return null;
        return new ItemStack(item.get(), rollCount(random));
    }

    /** 实际生效的概率 = 基础概率 + 年限增量（封顶）。 */
    public double effectiveChance(long nianxian) {
        double c = chance;
        if (chancePerYear > 0) {
            long base = minNianxian > 0 ? minNianxian : 0;
            c += (nianxian - base) / (double) chancePerYearPer * chancePerYear;
        }
        if (maxChance > 0) c = Math.min(maxChance, c);
        return Math.max(0, Math.min(1, c));
    }

    public boolean glowing() {
        return glowing;
    }

    private int rollCount(RandomSource r) {
        if (maxCount <= minCount) return minCount;
        return minCount + r.nextInt(maxCount - minCount + 1);
    }

    // ==================== 调试 ====================

    @Override
    public String toString() {
        return String.format("%s [%s] 年限%s~%s 概率%.3f%% 数量%d~%d",
                id, filter.desc(),
                minNianxian == 0 ? "不限" : String.valueOf(minNianxian),
                maxNianxian == 0 ? "不限" : String.valueOf(maxNianxian),
                chance * 100, minCount, maxCount);
    }
}