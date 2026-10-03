package com.TovidY.kunluncontinent.item.baseskillist;

/**
 * 一条"声明式魂技"的完整描述：数值 + 文案 + 特效。
 *
 * <p>一个 {@link SkillSpec} 配上 {@link ModItems#skillVariant}（注册）就是一条新魂技，
 * 不用再写技能类；物品图标复用同槽位既有魂技的贴图，不新增美术资源。</p>
 *
 * @param descKey           技能描述的语言键（必须含一个 {@code %s}，会被实际消耗填充）
 * @param baseCost          基础精神力消耗（实际消耗 = 此值 × 年限消耗倍率）
 * @param damageMultiplier  伤害倍率（最终伤害 = 攻击力 × 此值 × 年限倍率；纯增益技能填 0）
 * @param castTime          吟唱时间（tick），0 = 瞬发
 * @param cooldownTicks     冷却（tick），20 tick = 1 秒
 * @param effect            特效与伤害逻辑（服务端执行）
 */
public record SkillSpec(String descKey, float baseCost, float damageMultiplier,
                        int castTime, int cooldownTicks, SkillEffect effect) {
}
