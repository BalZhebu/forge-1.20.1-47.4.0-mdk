package com.TovidY.kunluncontinent.drop;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 掉落表执行器 —— 遍历 {@link DropRegistry} 里所有规则、逐条结算。
 *
 * <p>每条规则<b>独立</b>判定，所以同一只怪可以同时命中多条（例：500 年以上的水生怪
 * 既掉返气草种子又掉深海精华）。</p>
 *
 * <p>年限从被击杀实体的 {@link MobAttributeCapability} 取；没有该 capability
 * （例如普通原版怪）时按 0 处理，于是所有带年限门槛的规则都会自动跳过。</p>
 */
public final class DropExecutor {

    private DropExecutor() {
    }

    /**
     * 跑一遍掉落表。
     *
     * @param entity 被击杀的生物
     * @param random 随机源（用实体的 random 更稳）
     */
    public static void apply(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) return;

        long nianxian = readNianxian(entity);
        RandomSource random = entity.getRandom();

        for (DropRule rule : DropRegistry.rules()) {
            ItemStack stack = rule.roll(entity, nianxian, random);
            if (stack == null || stack.isEmpty()) continue;
            var itemEntity = entity.spawnAtLocation(stack);
            if (itemEntity != null && rule.glowing()) {
                itemEntity.setGlowingTag(true);
            }
        }
    }

    /** 读被击杀实体的年限；没有魂兽 capability 返回 0。 */
    public static long readNianxian(LivingEntity entity) {
        return entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                .map(MobAttributeCapability::getNianxian)
                .orElse(0L);
    }
}