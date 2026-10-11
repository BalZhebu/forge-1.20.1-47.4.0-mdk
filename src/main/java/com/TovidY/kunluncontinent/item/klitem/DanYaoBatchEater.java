package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;

/**
 * 「蹲下右键」一键服丹：<b>只吃手上这一格，有多少吃多少</b>。
 *
 * <h3>作用域：仅当前这一叠，绝不碰背包其他格</h3>
 * <p>曾经写成「扫描背包所有同类丹药」，结果背包里 3 叠 64 颗时一次吃掉了 192 颗 ——
 * 这明显不是玩家预期。玩家蹲下右键时丹药就在手上，
 * 意图就是<b>把手上的这一叠解决掉</b>，其他格子与自己无关。</p>
 *
 * <h3>⚠️ 遍历粒度：单格内逐颗吃</h3>
 * 必须用 {@code while} 持续从这一格取丹，只写一次 {@code shrink(1)} 会变成「一叠只吃一颗」。
 *
 * <p>{@code ItemStack} 是<b>引用</b>，对 {@code heldStack} 连续 {@code shrink}
 * 会直接反映到玩家手上那格。</p>
 *
 * <h3>消耗规则</h3>
 * <ul>
 *   <li><b>经验丹</b>：只吃到刚好够升一级为止，这一格剩下的丹药原样保留。</li>
 *   <li><b>无经验丹</b>（精神力丹等）：没有升级概念，把这一格全部吃完。</li>
 * </ul>
 */
public final class DanYaoBatchEater {

    /** 单次服用上限，防止极端堆叠把服务端卡死。 */
    private static final int MAX_EAT_PER_CALL = 256;

    private DanYaoBatchEater() {
    }

    /**
     * 只吃玩家手上那一叠同类丹药。
     *
     * @param heldStack 手上这一格（主手或副手，取决于触发时用的手）
     */
    public static void eatAllOf(ServerPlayer player, DanYaoItem danyao, ItemStack heldStack) {
        if (heldStack == null || heldStack.isEmpty()
                || heldStack.getItem() != danyao
                || DanYaoItem.Quality.get(heldStack) == DanYaoItem.Quality.damaged) {
            player.sendSystemMessage(Component.literal("无可服用丹药"));
            return;
        }

        if (danyao.getRawJingyan() > 0.0f) {
            eatUntilLevelUp(player, danyao, heldStack);
        } else {
            eatWholeStack(player, danyao, heldStack);
        }
    }

    /**
     * 经验丹：这一格吃到刚好够升一级为止。
     * <p><b>单格 while 逐颗吃</b>是本次核心，缺了就只掉 1 颗。</p>
     */
    private static void eatUntilLevelUp(ServerPlayer player, DanYaoItem danyao, ItemStack stack) {
        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) return;

        int usedBefore = player.getStats().getValue(Stats.ITEM_USED.get(danyao));
        boolean hasCap = danyao.getMaxused() > 0;
        int levelBefore = (int) cap.getDengji();
        float needed = cap.getMaxjingyan() - cap.getJingyan();

        int eaten = 0;
        float gained = 0.0f;

        while (!stack.isEmpty() && eaten < MAX_EAT_PER_CALL) {
            if (hasCap && usedBefore + eaten >= danyao.getMaxused()) break;
            if (!player.getAbilities().instabuild && levelBefore < danyao.getMinLevel()) break;
            // ⭐ 经验已够升级 → 立刻停手，这一格剩下的丹药一颗不碰
            if (gained >= needed) break;

            float gain = danyao.getRawJingyan() * DanYaoItem.Quality.get(stack).multiplier;
            player.awardStat(Stats.ITEM_USED.get(danyao));
            danyao.applyNonExpEffects(stack, player);
            stack.shrink(1);
            eaten++;
            gained += gain;
        }

        if (gained > 0.0f) {
            cap.setJingyan(cap.getJingyan() + gained);
            PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);
        }
        SynsAPI.synsPlayerAttribute(player);

        int levelAfter = (int) cap.getDengji();
        String tip = "§d服丹 §f×§e" + eaten + " §f(+§b" + fmt(gained) + "§f)";
        if (levelAfter > levelBefore) tip += " §a突破 §e" + levelAfter + " §a级";
        else if (eaten > 0) tip += " §7经验不足";
        player.sendSystemMessage(Component.literal(tip));

        // 成就：服用丹药 / 批量服丹
        if (eaten > 0) {
            AchievementAPI.onEatDanyao(player, eaten);
        }
    }

    /** 无经验丹药：把这一格全部吃完。 */
    private static void eatWholeStack(ServerPlayer player, DanYaoItem danyao, ItemStack stack) {
        int usedBefore = player.getStats().getValue(Stats.ITEM_USED.get(danyao));
        boolean hasCap = danyao.getMaxused() > 0;

        int eaten = 0;
        while (!stack.isEmpty() && eaten < MAX_EAT_PER_CALL) {
            if (hasCap && usedBefore + eaten >= danyao.getMaxused()) break;
            if (!player.getAbilities().instabuild) {
                int lv = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                        .map(c -> (int) c.getDengji()).orElse(0);
                if (lv < danyao.getMinLevel()) break;
            }
            player.awardStat(Stats.ITEM_USED.get(danyao));
            danyao.applyNonExpEffects(stack, player);
            stack.shrink(1);
            eaten++;
        }

        SynsAPI.synsPlayerAttribute(player);
        if (eaten > 0) {
            player.sendSystemMessage(Component.literal("§d服丹 §f×§e" + eaten));
            AchievementAPI.onEatDanyao(player, eaten);
        }
    }

    private static String fmt(float v) {
        if (Math.abs(v) >= 1000f) return String.valueOf((long) v);
        return String.format("%.1f", v);
    }
}
