package com.TovidY.kunluncontinent.item.baseskillist.tianshi.two;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.tianshi.AngelWings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 第二魂技 · <b>天使之翼</b>：开关型技能，展开后直接获得飞行能力。
 *
 * <h3>和武魂真身一样的开关手感</h3>
 * <ul>
 *   <li>释放 → 展开（{@link #hasUpfrontCost()} 为 false：不扣一次性消耗）；</li>
 *   <li>展开时再按 → 收起（{@link #tryToggleOff}，走释放流程最前面，不被冷却拦）；</li>
 *   <li>飞行时的精神力消耗沿用 {@code updatePlayerFly} 里那段（每 10 tick 按速度档位扣），
 *       这里<b>不</b>另算一份。</li>
 * </ul>
 *
 * <h3>它额外带来什么</h3>
 * <p>飞行状态本来会削减 40%~10% 的伤害（{@code CombatEventHandler#getFlyingDamageMultiplier}），
 * 展开天使之翼期间这个削减<b>取消</b> —— 见那里的判断。</p>
 */
public class SkillTianshi2 extends BaseSkillItem {

    @Override public float getBaseCost() { return 0f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.two.description";
    }

    /** 开关型：成本在飞行期间按 tick 扣，不做「够不够扣一次」的前置校验。 */
    @Override
    public final boolean hasUpfrontCost() {
        return false;
    }

    /** 已展开时再按 = 收起。 */
    @Override
    public boolean tryToggleOff(ServerPlayer player) {
        if (!AngelWings.isActive(player)) {
            return false;
        }
        PlayerAttributeCapability cap = player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return false;
        }
        AngelWings.deactivate(player, cap);
        player.displayClientMessage(Component.literal("§e§l【天使之翼】§7圣翼收拢"), true);
        return true;
    }

    @Override
    public boolean handleRelease(Level level, Player player, int nianxian) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return false;
        }
        PlayerAttributeCapability cap = player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return false;
        }
        // 门槛与自动关闭线一致：精神力已经见底就开不起来（否则开了立刻被关掉，白刷冷却）
        float max = ModAttributeAPI.getMaxjingshenli(player);
        if (max > 0.0f && cap.getJingshenli() <= max * AngelWings.AUTO_CLOSE_RATIO) {
            player.displayClientMessage(Component.literal("§c精神力太低，无法展开天使之翼"), true);
            return false;
        }
        AngelWings.activate(sp, cap);
        return true;
    }
}
