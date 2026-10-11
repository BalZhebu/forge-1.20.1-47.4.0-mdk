package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 武魂真身系列（各武魂的第 7 魂技）的公共基类。
 *
 * <h3>它是「开关」而不是「一次性技能」</h3>
 * <ul>
 *   <li>释放 → 开启，持续增幅，直到 {@link ZhenshenState} 判定该关；</li>
 *   <li>已开启时再释放 → 关闭（{@link #tryToggleOff}）；</li>
 *   <li>开启<b>不扣</b>一次性能耗（{@link #hasUpfrontCost()} 返回 false），
 *       成本是开启期间 10 倍的武魂维持费；</li>
 *   <li>开启进冷却，关闭不刷新冷却（靠 {@link #handleRelease} 的返回值实现：
 *       关闭走的是 {@link #tryToggleOff}，压根到不了刷新冷却那一步）。</li>
 * </ul>
 *
 * <h3>子类只需要给三样东西</h3>
 * <p>{@link #getZhenshenEffect()}、{@link #playActivateFx}、{@link #playDeactivateFx}。
 * 其余状态机逻辑全在 {@link ZhenshenState}，四个真身共用，别再各写一份。</p>
 */
public abstract class BaseZhenshenSkill extends BaseSkillItem {

    /** 该真身对应的增益（破魂真身 / 八荒真身 / 裂虎真身 / 巨猿真身）。 */
    protected abstract MobEffect getZhenshenEffect();

    /** 开启瞬间的表现：粒子 + 音效 + 提示。 */
    protected abstract void playActivateFx(ServerLevel level, ServerPlayer player);

    /** 关闭瞬间的表现。 */
    protected abstract void playDeactivateFx(ServerLevel level, ServerPlayer player);

    /** 开启期间贴在 ActionBar 上的状态文案。 */
    protected abstract String getActiveMessage();

    // ==================================================================================
    //  开关
    // ==================================================================================

    /** 开关型技能：成本按 tick 扣，不做「够不够扣一次 550」的前置校验。 */
    @Override
    public final boolean hasUpfrontCost() {
        return false;
    }

    /**
     * 已开启时再按 = 关闭。
     * <p>调用点在 {@code CPacketReleaseSkill} 的最前面：开启后玩家立刻处于冷却中，
     * 若走正常流程，关闭会被冷却检查拦下，所以必须在这里提前处理。</p>
     */
    @Override
    public final boolean tryToggleOff(ServerPlayer player) {
        if (!ZhenshenState.isActive(player)) return false;

        ZhenshenState.deactivate(player);
        if (player.level() instanceof ServerLevel level) {
            playDeactivateFx(level, player);
        }
        player.displayClientMessage(Component.literal(getActiveMessage() + " §7—— 已关闭"), true);
        return true;
    }

    @Override
    public final boolean handleRelease(Level level, Player player, int nianxian) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp) || !(level instanceof ServerLevel sl)) {
            return false;
        }

        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) return false;

        // 真身是武魂的延伸，武魂没开就不能开
        String wuhun = cap.getWuhunName();
        if (wuhun == null) {
            player.displayClientMessage(Component.literal("§c请先开启武魂！"), true);
            return false;
        }

        if (!ZhenshenState.canActivate(player, cap)) {
            player.displayClientMessage(Component.literal("§c精神力过低，无法开启武魂真身"), true);
            return false;
        }

        ZhenshenState.activate(sp, getZhenshenEffect(), wuhun);
        playActivateFx(sl, sp);
        return true;   // true = 真的开了，调用方据此刷新冷却
    }

    // ==================================================================================
    //  NPC / 非玩家路径
    // ==================================================================================

    /**
     * NPC 走的是 {@code executeEffectForNpc} → 本方法（FakePlayer 分支）。
     * <p>FakePlayer 不适合套开关状态机，这里退回「贴一次限时增益」的旧行为，
     * 保证 NPC 用第 7 魂技时表现不变。玩家不会走到这里 ——
     * 玩家的路径是重写后的 {@link #handleRelease}，它不调 {@code executeEffect}。</p>
     */
    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(getZhenshenEffect(), 800, 0));
        }
    }

    // ==================================================================================
    //   UI
    // ==================================================================================

    /** 开关型技能没有一次性消耗，描述里不该出现「消耗 X 点精神力」。 */
    @Override
    public Component getDynamicDescription(float costMultiplier) {
        return Component.translatable(getDescriptionKey());
    }
}
