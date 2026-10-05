package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.render.DamageIndicatorRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * ⭐ 伤害结算的<b>兜底修正器</b>（适配其他 mod 时的保险层）。
 *
 * <p><b>要解决什么问题</b>：玩家反馈"装了其他 mod 后伤害失效" ——
 * 伤害飘字显示正常（说明我们的公式算对了），但怪物只掉 1 滴血（等于裸手伤害）。</p>
 *
 * <p><b>根因</b>：{@code CombatEventHandler} 只在 {@link LivingHurtEvent} 里
 * {@code event.setAmount(finalDamage)}。而 Forge 的事件链是：</p>
 * <pre>
 *   LivingHurtEvent  →（其他 mod 可能在这里 setCanceled / setAmount 覆盖）
 *   LivingDamageEvent→（护甲/抗性等在这里继续改）
 *   实际扣血
 * </pre>
 * 其他 mod 只要在 {@code LivingHurtEvent} 上做以下任一动作，我们的数值就被冲掉：
 * <ol>
 *   <li><b>{@code setCanceled(true)}</b>（无敌/护盾类 mod 最常见）
 *       → 事件被取消 → 我们设的 amount 根本不生效 → 走不到扣血，只剩"手打1点"。</li>
 *   <li><b>{@code setAmount(1.0F)}</b>（覆盖式伤害改写）
 *       → 我们的最终伤害被替换成 1。</li>
 * </ol>
 *
 * <p><b>本类的做法</b>：在 {@code CombatEventHandler} 算完后把"我算出来的伤害"记到
 * {@link #PENDING}；再在 {@link LivingDamageEvent}（<b>优先级 LOWEST，最后执行</b>）
 * 检查实际伤害是否被腰斩，是就用我们的值补回去。</p>
 *
 * <p>为什么用 {@code LivingDamageEvent} 而不是 {@code LivingHurtEvent}：
 * {@code LivingHurtEvent} 被取消就不会走到 Damage；<b>能走到 Damage 说明伤害确实要结算</b>，
 * 这时把被腰斩的数值补回来才安全，不会造成"无敌 mod 失效"。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class CombatDamageBackupHandler {

    /**
     * 本 tick 我们算出的伤害：{@code 受击实体 -> 我们算出的值}。
     *
     * <p>用 {@link WeakHashMap} 避免实体卸载后内存泄漏 —— 实体被GC 时条目自动消失。</p>
     */
    private static final Map<LivingEntity, Float> PENDING = new WeakHashMap<>();

    /**
     * 记录本次攻击的最终伤害，供 {@link LivingDamageEvent} 兜底比对。
     *
     * <p>由 {@code CombatEventHandler#onLivingHurt} 在算出finalDamage 后调用。</p>
     */
    public static void record(LivingEntity target, float finalDamage) {
        if (target == null || !target.isAlive()) return;
        PENDING.put(target, finalDamage);
    }

    /**
     * 兜底：实际扣血被其他 mod 腰斩时补回来。
     *
     * <p>用 {@code LOWEST} 优先级 —— 事件总线里<b>最后执行</b>，
     * 这样其他 mod 的 {@code setAmount} 都已经跑完了，我们改的就是最终值。</p>
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        // 只处理玩家造成的伤害
        if (!(event.getSource().getEntity() instanceof Player)) return;

        LivingEntity target = event.getEntity();
        Float expected = PENDING.remove(target);   // 取出并清理，避免陈旧数据误伤
        if (expected == null) return;

        float actual = event.getAmount();

        // 判定：我们的值明显更大，却被腰斩到 40% 以下 → 判定被其他 mod 改坏
        // 不用"实际 < 期望"而用比例，是为了容忍合理的护甲/抗性削减
        if (expected > 1.0F && actual < expected * 0.4F) {
            event.setAmount(expected);
        }
    }

    /**
     * 每 tick 末尾清理<b>残留记录</b>。
     *
     * <p>正常流程下 {@code PENDING} 每条都会在 Damage 阶段被remove，
     * 但若目标<b>中途无敌/受伤取消</b>（事件没走到 Damage），那条记录会留下，
     * 下一次攻击同一目标时可能被误用 → 所以每 tick 兜底清空。</p>
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        PENDING.clear();
    }
}
