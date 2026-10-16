package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 「一键释放多个魂技」的服务端调度器。
 *
 * <h3>背景</h3>
 * <p>玩家可以把左边那一列快捷绑定里的<b>多个槽位绑到同一个物理键</b>上，
 * 按一下就会同时触发多个 {@code CPacketReleaseDirectSkill}。如果服务端老老实实
 * 在同一 tick 里连着跑 9 遍释放流程，会一次性撞出一堆问题：</p>
 * <ol>
 *   <li><b>吟唱互斥刷屏</b>：第一个吟唱技能占住吟唱位后，后面 8 个全部被拒，
 *       每人甩一条「正在吟唱中...」到 ActionBar 上；</li>
 *   <li><b>真身被误关</b>：{@code tryToggleOff} 是释放流程的第一步，
 *       一键释放会顺手把开着的武魂真身关掉；</li>
 *   <li><b>看不到结果</b>：9 条 ActionBar 互相覆盖，玩家只知道"点了没反应"；</li>
 *   <li><b>特效尖峰</b>：9 个技能的粒子/音效挤在同一 tick，容易掉帧；</li>
 *   <li><b>选中槽被改</b>：每条直放包都会写一次"当前选中槽位"。</li>
 * </ol>
 *
 * <h3>做法</h3>
 * <p>把「一次按到多个槽位」合并成一个 {@link CPacketReleaseBatch}，由这里逐个放：</p>
 * <ul>
 *   <li>瞬发技能：每 {@value #TICKS_BETWEEN_SKILLS} tick 放一个；</li>
 *   <li>吟唱技能：<b>一批只唱一次</b> —— 第一个遇到的吟唱技能开始吟唱
 *       （时长锁死 {@link CPacketReleaseSkill#BATCH_CAST_TICKS} = 3.5 秒），
 *       队列停下来等它唱完（或被打断）再继续；之后的吟唱技能一律跳过
 *       （{@code SKIPPED_CAST}）。</li>
 * </ul>
 * <p>逐个放还顺带保证了顺序语义正确 —— 每个技能的精神力校验看到的是
 * <b>前一个技能已经扣完</b>的余额，而不是同一 tick 里的初始值。</p>
 *
 * <h3>规则（不在这里，在 {@link CPacketReleaseSkill#tryRelease}）</h3>
 * <p>本类只负责「什么时候放、放完怎么汇报」。哪些技能该跳过（开关型 / 本批已唱过）、
 * 精神力够不够、在不在冷却 —— 全部走 {@code tryRelease} 的批量模式，
 * 与单发路径共用同一份实现。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkillBatchRelease {

    /**
     * 相邻两个瞬发技能之间隔多少 tick。
     *
     * <p>{@code 2} = 0.1 秒。既能消掉同 tick 的特效尖峰，又快到感觉是"一次按下去"。</p>
     */
    public static final int TICKS_BETWEEN_SKILLS = 2;

    /**
     * 整批释放的硬性上限（tick）。
     *
     * <p>常态最坏情况：一次 3.5 秒吟唱 + 8 个瞬发 ≈ 90 tick。
     * 给到 {@code 800}（40 秒）纯属兜底 —— 万一吟唱状态异常没清掉，
     * 也不至于让队列永远挂在静态表里。</p>
     */
    private static final long MAX_BATCH_TICKS = 800L;

    /**
     * 等吟唱结束的额外宽限（tick）。
     *
     * <p>正常情况吟唱到点就结束；如果超过 {@code BATCH_CAST_TICKS + 这个值}
     * 还没结束，说明状态异常，直接当作已结束放行，不让整批卡死。</p>
     */
    private static final long CAST_WAIT_SLACK = 60L;

    /** 单个槽位的合法范围。 */
    private static final int SLOT_COUNT = 9;

    private static final Map<UUID, Batch> RUNNING = new HashMap<>();

    private SkillBatchRelease() {
    }

    /** 一次进行中的批量释放。 */
    private static final class Batch {
        /** 发起时玩家开的武魂 —— 中途换了武魂，槽位含义就变了，必须中止。 */
        final String wuhun;
        final Deque<Integer> pending;
        final Map<Integer, CPacketReleaseSkill.Result> results = new LinkedHashMap<>();
        /** 整批开始的游戏刻，用于超时兜底。 */
        final long startTick;
        /** 下一个技能的释放时刻。 */
        long nextTick;
        /** 正在等这个槽位的吟唱结束；{@code -1} = 没在等。 */
        int waitingCastSlot = -1;
        /** 那次吟唱开始的游戏刻。 */
        long castStartTick;
        /**
         * 本批是否已经发起过吟唱。
         *
         * <p>一批只唱一次 —— 吟唱位本来也只有一个。只有真正"开始吟唱"了才置位，
         * 所以某个吟唱技能因为冷却 / 蓝不够没能唱起来时，后面的吟唱技能还有机会。</p>
         */
        boolean castUsed = false;

        Batch(String wuhun, Collection<Integer> slots, long firstTick) {
            this.wuhun = wuhun;
            this.pending = new ArrayDeque<>(slots);
            this.nextTick = firstTick;
            this.startTick = firstTick;
        }
    }

    /**
     * 开始一次批量释放。
     *
     * <p>已经在跑的话直接忽略 —— 不排队也不覆盖。一次批量本身就要跑好几秒，
     * 覆盖会让前一批的结果永远汇报不出来。</p>
     */
    public static void start(ServerPlayer player, PlayerAttributeCapability cap, Collection<Integer> slots) {
        if (RUNNING.containsKey(player.getUUID())) {
            return;
        }
        String wuhun = cap.getWuhunName();
        if (wuhun == null) {
            player.displayClientMessage(Component.literal("§c请先开启武魂！"), true);
            return;
        }
        List<Integer> ordered = new ArrayList<>();
        for (Integer raw : slots) {
            if (raw == null) {
                continue;
            }
            int slot = raw;
            if (slot >= 0 && slot < SLOT_COUNT && !ordered.contains(slot)) {
                ordered.add(slot);
            }
        }
        if (ordered.isEmpty()) {
            return;
        }
        ordered.sort(Integer::compareTo);
        RUNNING.put(player.getUUID(), new Batch(wuhun, ordered, player.level().getGameTime()));
    }

    /** 每个玩家每 tick 调用一次：该放就放，该等就等，放完汇报。 */
    public static void tick(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        Batch batch = RUNNING.get(player.getUUID());
        if (batch == null) {
            return;
        }

        // 中途换武魂 → 槽位对应的技能已经不是原来那些了，直接中止
        if (!batch.wuhun.equals(cap.getWuhunName())) {
            RUNNING.remove(player.getUUID());
            player.displayClientMessage(Component.literal("§7武魂已切换，一键释放中止"), true);
            return;
        }

        // 整批超时兜底
        if (gameTime - batch.startTick > MAX_BATCH_TICKS) {
            RUNNING.remove(player.getUUID());
            SynsAPI.synsPlayerAttribute(player);
            player.displayClientMessage(Component.literal("§7一键释放超时，已中止"), true);
            announce(player, batch);
            return;
        }

        if (gameTime < batch.nextTick) {
            return;
        }

        if (batch.waitingCastSlot >= 0) {
            // 正在等一次吟唱结束
            if (cap.getCurrentCastingSkill() != null
                    && gameTime - batch.castStartTick <= CPacketReleaseSkill.BATCH_CAST_TICKS + CAST_WAIT_SLACK) {
                return;   // 还在唱
            }
            // 判定这次吟唱有没有唱完：正常唱完约等于锁定时长；明显偏短就是被打断了
            long elapsed = gameTime - batch.castStartTick;
            batch.results.put(batch.waitingCastSlot,
                    elapsed + 2 >= CPacketReleaseSkill.BATCH_CAST_TICKS
                            ? CPacketReleaseSkill.Result.SUCCESS
                            : CPacketReleaseSkill.Result.CAST_INTERRUPTED);
            batch.waitingCastSlot = -1;
            batch.nextTick = gameTime + TICKS_BETWEEN_SKILLS;
        } else {
            Integer slot = batch.pending.poll();
            if (slot != null) {
                // 一批只唱一次：已经唱过了就让后面的吟唱技能直接跳过
                CPacketReleaseSkill.ReleaseMode mode = batch.castUsed
                        ? CPacketReleaseSkill.ReleaseMode.BATCH_NO_CAST
                        : CPacketReleaseSkill.ReleaseMode.BATCH_ALLOW_CAST;
                CPacketReleaseSkill.Result result = CPacketReleaseSkill.tryRelease(player, cap, slot, mode);
                batch.results.put(slot, result);
                if (result == CPacketReleaseSkill.Result.STARTED_CASTING) {
                    batch.castUsed = true;
                    batch.waitingCastSlot = slot;
                    batch.castStartTick = gameTime;
                    batch.nextTick = gameTime + CPacketReleaseSkill.BATCH_CAST_TICKS;
                } else {
                    batch.nextTick = gameTime + TICKS_BETWEEN_SKILLS;
                }
            }
        }

        // 统一收尾：没排队了、也不在等吟唱 → 整批结束
        if (batch.pending.isEmpty() && batch.waitingCastSlot < 0) {
            RUNNING.remove(player.getUUID());
            SynsAPI.synsPlayerAttribute(player);
            announce(player, batch);
        }
    }

    /** 中断某个玩家的批量释放（死亡 / 退出时调用，避免残留条目）。 */
    public static void cancel(UUID uuid) {
        RUNNING.remove(uuid);
    }

    /** 退出登录时清掉，否则那个玩家的条目会一直挂在静态表里。 */
    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancel(event.getEntity().getUUID());
    }

    /**
     * 汇总提示：<b>一条</b> ActionBar 说清"成功了几个、剩下的分别为什么没放出来"。
     *
     * <p>取代原来的 9 条互相覆盖的消息 —— 那也是"一键释放感觉没反应"的主因。</p>
     */
    private static void announce(ServerPlayer player, Batch batch) {
        int success = 0;
        int cooldown = 0;
        int noMp = 0;
        int casting = 0;
        int interrupted = 0;
        int skippedCast = 0;
        int skippedToggle = 0;
        int empty = 0;
        int other = 0;

        for (CPacketReleaseSkill.Result result : batch.results.values()) {
            if (result == CPacketReleaseSkill.Result.SUCCESS) {
                success++;
            } else if (result == CPacketReleaseSkill.Result.ON_COOLDOWN) {
                cooldown++;
            } else if (result == CPacketReleaseSkill.Result.NOT_ENOUGH_MP) {
                noMp++;
            } else if (result == CPacketReleaseSkill.Result.CASTING) {
                casting++;
            } else if (result == CPacketReleaseSkill.Result.CAST_INTERRUPTED) {
                interrupted++;
            } else if (result == CPacketReleaseSkill.Result.SKIPPED_CAST) {
                skippedCast++;
            } else if (result == CPacketReleaseSkill.Result.SKIPPED_TOGGLE) {
                skippedToggle++;
            } else if (result == CPacketReleaseSkill.Result.EMPTY_SLOT
                    || result == CPacketReleaseSkill.Result.NO_WUHUN) {
                empty++;
            } else {
                other++;
            }
        }

        StringBuilder text = new StringBuilder("§b一键释放 §7· §a成功 ").append(success);
        if (cooldown > 0) {
            text.append(" §7| §e冷却 ").append(cooldown);
        }
        if (noMp > 0) {
            text.append(" §7| §9精神力不足 ").append(noMp);
        }
        if (interrupted > 0) {
            text.append(" §7| §c被打断 ").append(interrupted);
        }
        if (casting > 0) {
            text.append(" §7| §6吟唱中 ").append(casting);
        }
        if (skippedCast > 0) {
            text.append(" §7| §8略过吟唱 ").append(skippedCast).append("§7(一批只唱一次)");
        }
        if (skippedToggle > 0) {
            text.append(" §7| §8略过开关 ").append(skippedToggle);
        }
        if (empty > 0) {
            text.append(" §7| §8空槽 ").append(empty);
        }
        if (other > 0) {
            text.append(" §7| §c无效 ").append(other);
        }
        player.displayClientMessage(Component.literal(text.toString()), true);
    }
}
