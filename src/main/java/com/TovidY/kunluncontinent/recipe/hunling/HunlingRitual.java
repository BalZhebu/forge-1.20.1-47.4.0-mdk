package com.TovidY.kunluncontinent.recipe.hunling;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 汇灵台仪式的公共契约 —— 不管最终产物是一件物品还是一只生物，仪式过程是一样的：
 * <b>必需品摆台、副物品摆基座、按时间扣精神力、按时间吃副物品</b>。
 *
 * <p>所以状态机只认这个接口，产物怎么落地由实现类自己决定：</p>
 * <ul>
 *   <li>{@link HunlingRitualRecipe} —— 产出物品，完成后台面上的必需品变成成品；</li>
 *   <li>{@link HunlingSpawnRitualRecipe} —— 召唤生物，完成后台下清空、台上冒出生物。</li>
 * </ul>
 *
 * <p>{@link #matchesRitual} 已经写成默认实现（必需品 1 件 + 基座物品总数必须正好等于配方要求），
 * 实现类不用再管匹配。</p>
 */
public interface HunlingRitual {

    /** 必需品：最后摆到汇灵台上的那件。 */
    ItemStack getCatalyst();

    /** 副物品：摆在基座上的那些。 */
    List<RitualIngredient> getRitualIngredients();

    /** 精神力总消耗（按 {@code 总量 / 时长} 逐 tick 扣完）。 */
    int getSpiritCost();

    /** 持续时间，单位 tick。 */
    int getDurationTicks();

    /** 匹配时是否连 NBT 一起比。 */
    boolean isStrictNbt();

    /** 副物品总件数（= 基座上应有的物品总数）。 */
    default int getTotalItemCount() {
        int total = 0;
        for (RitualIngredient ing : getRitualIngredients()) {
            total += ing.count();
        }
        return total;
    }

    /**
     * 判定这次仪式能不能成立。
     *
     * @param catalystStack 玩家手里的必需品
     * @param baseStacks    范围内所有基座上的物品（空槽会被跳过）
     */
    default boolean matchesRitual(ItemStack catalystStack, List<ItemStack> baseStacks) {
        if (catalystStack.isEmpty() || !matchesUnit(getCatalyst(), catalystStack)) {
            return false;
        }

        List<ItemStack> pool = new ArrayList<>();
        for (ItemStack stack : baseStacks) {
            if (!stack.isEmpty()) {
                pool.add(stack);
            }
        }
        // 数量必须正好：多摆一件也不算，否则「多出来的那件」到底消耗不消耗说不清
        if (pool.size() != getTotalItemCount()) {
            return false;
        }

        boolean[] used = new boolean[pool.size()];
        for (RitualIngredient requirement : getRitualIngredients()) {
            int need = requirement.count();
            for (int i = 0; i < pool.size() && need > 0; i++) {
                if (used[i]) {
                    continue;
                }
                if (requirement.matches(pool.get(i), isStrictNbt())) {
                    used[i] = true;
                    need--;
                }
            }
            if (need > 0) {
                return false;
            }
        }
        return true;
    }

    /** 单件比对（必需品用）。 */
    default boolean matchesUnit(ItemStack template, ItemStack actual) {
        if (isStrictNbt()) {
            return ItemStack.isSameItemSameTags(template, actual);
        }
        return template.is(actual.getItem());
    }
}
