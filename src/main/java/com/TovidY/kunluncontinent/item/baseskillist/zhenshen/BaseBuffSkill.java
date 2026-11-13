package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 「增益开关」系列技能的公共基类 —— 与 {@link BaseZhenshenSkill} 同一套开关思路，
 * 但由 {@link BuffToggleState} 管理，<b>多个增益可以同时挂</b>。
 *
 * <h3>行为</h3>
 * <ul>
 *   <li>释放 → 开启，持续获得该增益，<b>维持费大幅增加</b>（{@link #getUpkeepMultiplier()}）；</li>
 *   <li>开启期间再释放 → 关闭（{@link #tryToggleOff}）；</li>
 *   <li>精神力跌破下限 / 武魂关闭 / <b>切换武魂</b> → 自动关闭；</li>
 *   <li>开启<b>不扣</b>一次性能耗（{@link #hasUpfrontCost()} false）。</li>
 * </ul>
 *
 * <h3>子类给五样东西</h3>
 * <p>{@link #getToggleId()}、{@link #resolveBuffEffect}（可随环境变化）、
 * {@link #getBuffAmplifier()}、{@link #getUpkeepMultiplier()}、
 * {@link #getActiveMessage()} + {@link #playActivateFx}/{@link #playDeactivateFx}。</p>
 */
public abstract class BaseBuffSkill extends BaseSkillItem {

    protected BaseBuffSkill() {
        // 注册动态效果解析器：啸月这类"白天/夜晚给不同增益"的技能在刷新时回调
        BuffToggleState.registerResolver(getToggleId(), this::resolveBuffEffect);
    }

    /** 这个开关的唯一 id（NBT 里用它区分多个增益）。 */
    protected abstract String getToggleId();

    /** 当前该给什么增益。可随环境变化（如啸月按昼夜返回不同效果）。 */
    protected abstract MobEffect resolveBuffEffect(ServerPlayer player);

    /** 增益等级（MobEffectInstance 的 amplifier）。 */
    protected abstract int getBuffAmplifier();

    /** 开启期间的维持费增量：8 = 武魂维持消耗 +7 倍。 */
    protected abstract float getUpkeepMultiplier();

    /** 开启期间贴在 ActionBar 上的状态文案（简短，如「石肤」「啸月」）。 */
    protected abstract String getActiveMessage();

    /** 开启瞬间的表现。 */
    protected abstract void playActivateFx(ServerLevel level, ServerPlayer player);

    /** 关闭瞬间的表现。 */
    protected abstract void playDeactivateFx(ServerLevel level, ServerPlayer player);

    // ==================================================================================
    //  开关
    // ==================================================================================

    /** 开关型技能：维持费按 tick 结算，没有一次性消耗。 */
    @Override
    public final boolean hasUpfrontCost() {
        return false;
    }

    /**
     * 已开启时再按 = 关闭（调用点在 {@code CPacketReleaseSkill} 最前面，
     * 绕过开启后立刻进入的冷却检查）。
     */
    @Override
    public boolean tryToggleOff(ServerPlayer player) {
        if (!BuffToggleState.isActive(player, getToggleId())) {
            return false;
        }
        BuffToggleState.deactivate(player, getToggleId());
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
        if (cap == null) {
            return false;
        }

        // 武魂没开就不能挂增益
        if (cap.getWuhunName() == null) {
            player.displayClientMessage(Component.literal("§c请先开启武魂！"), true);
            return false;
        }

        if (BuffToggleState.isActive(player, getToggleId())) {
            // 理论上 tryToggleOff 已处理，这里兜底
            tryToggleOff(sp);
            return false;
        }

        if (!BuffToggleState.canActivate(player, cap)) {
            player.displayClientMessage(Component.literal("§c精神力过低，无法开启该增益"), true);
            return false;
        }

        // 同一个武魂下的增益开关可以叠加（石肤 + 搬山 + 石铠 同时开），维持费按条目累加
        BuffToggleState.activate(sp, getToggleId(), resolveBuffEffect(sp),
                getBuffAmplifier(), getUpkeepMultiplier(), cap.getWuhunName());
        playActivateFx(sl, sp);
        return true;   // true = 真的开了，调用方据此刷新冷却
    }

    // ==================================================================================
    //  NPC / 非玩家路径：退回「贴一次限时增益」
    // ==================================================================================

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            MobEffect effect = resolveBuffEffect(sp);
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, 400, getBuffAmplifier()));
        }
    }

    // ==================================================================================
    //  UI
    // ==================================================================================

    /** 开关型技能没有一次性消耗，描述里不该出现「消耗 X 点精神力」。 */
    @Override
    public Component getDynamicDescription(float costMultiplier) {
        return Component.translatable(getDescriptionKey());
    }
}
