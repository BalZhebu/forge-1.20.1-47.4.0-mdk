package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.godclass.buff.CelestialTeleport;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 神界（celestial_realm）维度的死亡 / 虚空处理。
 *
 * <p>三条规则：</p>
 * <ol>
 *   <li><b>神界死亡 → 送回主世界</b>：不管玩家把重生点设在哪，只要死在神界，
 *       强制把重生点改写成"主世界重生点"（没设过就主世界出生点）。</li>
 *   <li><b>在神界用床设置重生点 → 正常在神界复活</b>。
 *       床会把 {@code respawnDimension} 设成神界，此时死亡<b>不拦截</b>。</li>
 *   <li><b>跳下虚空 → 送回主世界重生点</b>（同 1，没有就主世界出生点）。</li>
 *   <li><b>睡觉 → 睡满 5 秒直接快进到天亮</b>（见 {@link #tryFastForwardNight}）。</li>
 * </ol>
 *
 * <p><b>为什么不在虚空时直接 kill 玩家</b>：原版 {@code checkBelowWorld} 会发
 * {@code fellOutOfWorld} 伤害并死亡，那条死亡路径同样会触发规则 1 ——
 * 两条都做的话是重复且浪费的。所以这里直接在阈值内<b>主动改写重生点并传送</b>，
 * 不走死亡流程（玩家不会掉血、不会死、不会掉掉落物）。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class CelestialRealmRespawnHandler {

    /**
     * 虚空判定高度。<b>必须比维度的 minBuildHeight 低</b>才有效。
     *
     * <p>神界维度是 {@code min_y=0 / height=128}，也就是能build到 Y=-64。
     * 原版 {@code checkBelowWorld} 的阈值是 {@code minBuildHeight - 64} = -128。
     * 我们取 <b>-80</b>：比 -64 更低（保证是真掉出世界了才触发），
     * 但比 -128 高（这样玩家还没被原版清理掉，我们抢先接管，<b>不会死</b>）。</p>
     */
    private static final double VOID_Y = -80.0D;

    /**
     * 睡满多少 tick 之后才快进时间。
     *
     * <p>原版 {@code Player#isSleepingLongEnough()} 的阈值就是 <b>100</b>
     * （即黑屏淡入 5 秒），保持一致，玩家体感就和主世界睡觉一样。</p>
     */
    private static final int SLEEP_TICKS = 100;

    // ==================== 规则 1 & 2：死亡重生 ====================

    /**
     * 玩家死亡后、重生落点<b>计算之前</b>触发 —— 这是唯一能改写重生点的地方。
     *
     * <p>顺序很关键：{@code PlayerRespawnEvent} 早于实际传送，
     * 改 {@code respawnDimension/respawnPosition} 才会生效。
     * 如果等到 {@code PlayerPostRespawnEvent} 就已经传送完了。</p>
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 死亡发生在哪个维度就用哪个维度判断
        //（重生点可能在主世界，但"死在哪"才是规则的触发条件）
        if (!isCelestial(player)) return;

        // 规则 2：床把重生点设在神界 → 尊重它，留在神界复活
        // （神界 dimension_type 的 bed_works = true，床能正常设重生点）
        if (isCelestialDimension(player.getRespawnDimension())) {
            return;
        }

        // 规则 1：其余情况（没床 / 床在主世界）→ 一律回主世界重生点；
        //        若连重生点都没设过，sendToOverworldSpawn 内部会退到主世界世界出生点。
        sendToOverworldSpawn(player, "§7你离开了天境…");
    }

    // ==================== 规则 3：跳下虚空 ====================

    /**
     * 玩家 tick 检测：掉到 {@link #VOID_Y} 以下就直接送回主世界，<b>不死</b>。
     *
     * <p>用 {@code TickEvent.PlayerTickEvent} 的 END 阶段，避免和其他逻辑抢。</p>
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!isCelestial(player)) return;

        if (player.getY() < VOID_Y) {
            // ⭐ 与「按钮离开神界」完全同一条逻辑（用户要求）。
            //   注意这里**不能**用 sendToOverworldSpawn —— 那个会额外改写 respawnPosition，
            //   而玩家只是掉下去、还活着，重生点（神界的床）应该继续有效。
            CelestialTeleport.leaveCelestial(player);
            return;
        }

        // 规则 4：睡觉满 100 tick 后把时间快进到天亮
        tryFastForwardNight(player);
    }

    /**
     * 规则 4：<b>神界睡觉直接跳到天亮</b>。
     *
     * <p>原版跳夜依赖 {@code ServerLevel#tick} 里的
     * {@code sleepStatus.areEnoughDeepSleeping(...)}，其中
     * {@code isSleepingLongEnough()} 要求 {@code isSleeping() && sleepCounter >= 100}。
     * 天境是"无固定时间"的自然维度，理论该走同一条路，但实际玩家躺下后夜晚不推进 ——
     * 原版那条判定在自建维度上不可靠（{@code tick()} 每 tick 都会重算 sleepStatus，
     * 一旦有玩家因各种原因处于非 active 状态就永远凑不够人数）。</p>
     *
     * <p>所以这里直接按<b>单个玩家的睡眠进度</b>快进，不依赖任何人数判定：
     * 睡满 {@link #SLEEP_TICKS}（与原版 100 一样 = 5 秒黑屏）就把时间设到天亮。</p>
     *
     * <p>用 {@code setDayTime} 而不是 {@code setDayTime(0)}：一个 MC 日长 24000 tick，
     * 天亮对应 0/24000，取"当前时间 - 当前时间 % 24000"能保留已过天数。</p>
     */
    private static void tryFastForwardNight(ServerPlayer player) {
        if (!player.isSleeping()) return;                  // 没睡 → 不管
        if (player.getSleepTimer() < SLEEP_TICKS) return;  // 黑屏没结束 → 等

        // 已经是白天/清晨就不用动（原版就是"跳过剩余夜晚"，不是回到正午）
        if (!player.level().isNight()) return;

        ServerLevel celestial = (ServerLevel) player.level();
        long dayTime = celestial.getDayTime();
        // % 24000 在 0~23999，取整到当天开始 = 天亮；+1000 稍微推进一点避免"刚睡醒又入夜"
        long morning = (dayTime - dayTime % 24000L) + 1000L;
        celestial.setDayTime(morning);
    }

    // ==================== 工具方法 ====================

    /** 玩家当前所在维度是不是神界。 */
    private static boolean isCelestial(ServerPlayer player) {
        return player.level().dimension() == ModDimensions.CELESTIAL_REALM_LEVEL_KEY;
    }

    /** 传进来的维度 key 是不是神界。 */
    private static boolean isCelestialDimension(ResourceKey<Level> key) {
        return key == ModDimensions.CELESTIAL_REALM_LEVEL_KEY;
    }

    /**
     * 把玩家强制送到"主世界重生点"，并<b>改写其重生点</b>。
     *
     * <p>⚠️ <b>死亡路径专用</b>，与「按钮离开神界」不同：
     * 玩家死在神界时，<b>必须把 {@code respawnPosition} 改写成主世界</b>，
     * 否则下次在神界死亡又会被拉回那个已经失效的神界重生点。</p>
     *
     * <p>虚空传送不需要改重生点（人还活着，重生点仍有效），
     * 所以那条路直接调 {@link CelestialTeleport#leaveCelestial}。</p>
     *
     * @param message 传送到主世界时给玩家看的提示
     */
    private static void sendToOverworldSpawn(ServerPlayer player, String message) {
        ServerLevel overworld = player.server.overworld();
        if (overworld == null) return;   // 理论上不会发生，防御性返回

        // 1. 算目标位置：**只在"重生点确实在主世界"时复用它的坐标**。
        //    否则（例如玩家在末地/下界设过重生点）会拿"别的维度的坐标"传送到主世界 → 传到荒野。
        //    这种情况一律退回主世界世界出生点。
        BlockPos target = player.getRespawnPosition();
        boolean spawnInOverworld = player.getRespawnDimension() == Level.OVERWORLD;
        if (target == null || !spawnInOverworld) {
            target = overworld.getSharedSpawnPos();
        }

        // 2. 改写重生点为"主世界 + target"，否则下次在神界死亡又会回到失效位置
        player.setRespawnPosition(Level.OVERWORLD, target, player.getYRot(), false, false);

        // 3. 传送（不重置能力、不发死亡消息）
        player.teleportTo(
                overworld,
                target.getX() + 0.5D,
                target.getY(),
                target.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot());

        player.displayClientMessage(Component.literal(message), false);
    }
}
