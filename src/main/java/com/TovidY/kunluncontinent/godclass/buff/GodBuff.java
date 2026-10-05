package com.TovidY.kunluncontinent.godclass.buff;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * 一个神位被动。
 *
 * <p>实现类只需给出：神位 id、面板上显示的增幅文案、以及真正的效果逻辑
 * （在 {@link #onLivingHurt} / {@link #onTargetTick} 等钩子里）。</p>
 *
 * <p>⚠️ 钩子方法<b>默认什么都不做</b>，实现类按需覆盖，避免每个新神位
 * 都必须实现全部方法。</p>
 */
public abstract class GodBuff {

    private final String godId;
    private final ChatFormatting color;
    private final String title;
    private final String desc;

    protected GodBuff(String godId, ChatFormatting color, String title, String desc) {
        this.godId = godId;
        this.color = color;
        this.title = title;
        this.desc = desc;
    }

    /** 对应 {@code GodRegistry.GODS} 的 key，如 {@code sea_god}。 */
    public final String godId() {
        return godId;
    }

    /** 面板上"被动增幅"区块的一行标题。 */
    public final String title() {
        return title;
    }

    /** 面板上"被动增幅"区块的详细说明。 */
    public final String desc() {
        return desc;
    }

    public final ChatFormatting color() {
        return color;
    }

    /** 面板上带颜色的完整一行，如「§3☾ 海神之威：……」。 */
    public final Component toLine() {
        return Component.literal("§" + color.getChar() + "▸ " + title + "：§f" + desc);
    }

    // ==================== 可选钩子（默认空实现） ====================

    /**
     * 攻击者造成伤害时的钩子。
     *
     * @return 新的伤害值；返回 {@link Float#NaN} 表示不修改
     */
    public float onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        return Float.NaN;
    }

    /**
     * 生物每 tick 的钩子（用于「让生物逃跑/不靠近」这类持续行为）。
     *
     * <p>只在 {@code event.phase == END} 时调用。</p>
     */
    public void onMobTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
    }

    /**
     * ⭐ 生物「换攻击目标」的钩子 —— <b>解除敌意的正确入口</b>。
     *
     * <p>{@code LivingChangeTargetEvent} 在生物要把某个目标写进 AI 时触发，
     * 既覆盖 AI 主动索敌（{@code NearestAttackableTargetGoal}），
     * 也覆盖 {@code setTarget()} 手动指定。它是 <b>@Cancelable</b> 的 ——
     * {@code setCanceled(true)} 表示"这次换目标作废，目标保持不变"。
     * 事件驱动、零 tick 开销，比每 tick 扫实体可靠得多。</p>
     */
    public void onTargeting(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
    }

    /**
     * 判定：这只生物是否<b>免受</b>该神位被动影响。
     *
     * <p>例如海神的"永不敌对"要排除魔鲸，就得在这里返回 true。</p>
     */
    public boolean isImmune(net.minecraft.world.entity.LivingEntity target) {
        return false;
    }
}
