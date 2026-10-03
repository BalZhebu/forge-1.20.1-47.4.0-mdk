package com.TovidY.kunluncontinent.drop;

import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 生物筛选器 —— 声明式掉落表里"什么生物会掉"的那一半。
 *
 * <p>内置了几种常用维度，可以链式组合：</p>
 * <pre>
 *   DropFilter.ofMobType(MobType.WATER)      // 水生
 *   DropFilter.nameContains("fish", "ocean") // 名字含关键词
 *   DropFilter.is(EntityType.ZOMBIE)         // 原版/模组某类实体
 *   DropFilter.all()                          // 任何生物（配minNianxian用）
 *   DropFilter.none()                        // 永不掉（用于关掉某条规则）
 * </pre>
 */
public final class DropFilter {

    private final Predicate<LivingEntity> predicate;
    private final String desc;

    private DropFilter(Predicate<LivingEntity> predicate, String desc) {
        this.predicate = predicate;
        this.desc = desc;
    }

    // ==================== 基础 ====================

    /** 任何生物都匹配。配合 {@code minNianxian} 就是"所有生物在X 年以上掉落"。 */
    public static DropFilter all() {
        return new DropFilter(e -> true, "所有生物");
    }

    /** 永不匹配（用来临时停用某条规则而不删代码）。 */
    public static DropFilter none() {
        return new DropFilter(e -> false, "无");
    }

    /** 任意自定义判断。 */
    public static DropFilter of(Predicate<LivingEntity> predicate, String desc) {
        return new DropFilter(predicate, desc);
    }

    // ==================== 常用维度 ====================

    /** 按生物属性类型（海/陆/空中）。 */
    public static DropFilter mobType(net.minecraft.world.entity.MobType type) {
        return new DropFilter(e -> e.getMobType() == type, "属性=" + type);
    }

    /** 注册名包含任一关键词（不区分大小写）。最省事的写法。 */
    public static DropFilter nameContains(String... keywords) {
        List<String> list = List.of(keywords);
        return new DropFilter(e -> {
            String id = e.getType().toShortString().toLowerCase();
            for (String k : list) {
                if (id.contains(k.toLowerCase())) return true;
            }
            return false;
        }, "名字含" + list);
    }

    /** 指定实体类型（varargs 便于一次写多个）。 */
    @SafeVarargs
    public static DropFilter is(net.minecraft.world.entity.EntityType<? extends LivingEntity>... types) {
        Set<net.minecraft.world.entity.EntityType<? extends LivingEntity>> set = Set.of(types);
        return new DropFilter(set::contains, "实体" + set);
    }

    /** 当前<b>在水中</b>（会被丢进水里的陆怪也算）。 */
    public static DropFilter inWater() {
        return new DropFilter(LivingEntity::isInWaterOrBubble, "在水中");
    }

    // ==================== 组合 ====================

    /** 与自己取交集。 */
    public DropFilter and(Predicate<LivingEntity> other) {
        Predicate<LivingEntity> self = this.predicate;
        return new DropFilter(e -> self.test(e) && other.test(e), desc + "且");
    }

    /** 与自己取并集。 */
    public DropFilter or(DropFilter other) {
        return new DropFilter(e -> this.predicate.test(e) || other.predicate.test(e),
                desc + "或" + other.desc);
    }

    // ==================== 内部 ====================

    public boolean test(LivingEntity e) {
        return e != null && predicate.test(e);
    }

    public String desc() {
        return desc;
    }
}