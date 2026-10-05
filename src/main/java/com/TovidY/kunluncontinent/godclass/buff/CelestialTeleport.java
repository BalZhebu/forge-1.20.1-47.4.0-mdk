package com.TovidY.kunluncontinent.godclass.buff;

import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * 「进入 / 离开神界」的<b>双向传送</b>。
 *
 * <p>规则（用户需求）：</p>
 * <ol>
 *   <li><b>不在神界</b> → 传送进神界：神界有重生点用它，没有就找一块露天陆地。</li>
 *   <li><b>已在神界</b> → 传送回主世界：同样有重生点用它，没有就用主世界世界出生点。</li>
 *   <li>掉进神界虚空被自动传送 → 走<b>与"离开神界"完全相同</b>的逻辑。</li>
 * </ol>
 *
 * <p>面板上只有<b>一个按钮</b>，按当前所在维度自动切换语义（省空间）。</p>
 *
 * <p>⭐ <b>为什么用"预算式强制生成"</b>：这是本类最关键的设计。踩过的坑：</p>
 * <blockquote>
 * ① 一开始用 {@code level.getHeight()} 裸读 → 区块未加载时<b>同步强制加载</b>，
 * 撒上千个点 =加载几千个区块 → 服务端卡死 + 高度图返 0 → 全判虚空。
 * ② 改成"只搜已加载区块" → 安全了，但玩家从主世界点按钮时神界<b>一个区块都没加载</b>
 * → 永远返回 null → 「找不到落脚之处」。<b>死结。</b>
 * </blockquote>
 * 所以改成：<b>按固定预算主动生成</b>（{@link #MAX_FORCE_CHUNKS} 个区块封顶），
 * 每生成一批就检查一次，找到立刻停手。既不会卡死，也能在空维度里找到岛。
 */
public final class CelestialTeleport {

    private CelestialTeleport() {
    }

    /** 搜索半径上限（格）。神界岛屿稀疏，2048 足够。 */
    private static final int SEARCH_RADIUS = 2048;

    /** 螺旋搜索初始步长（格），每轮 ×1.5。 */
    private static final int SEARCH_STEP_START = 16;

    /** 螺旋搜索最多放大多少轮（16 → 16×1.5^10 ≈ 1547）。 */
    private static final int SEARCH_ROUNDS = 10;

    /**
     * ⭐ 强制生成的<b>区块预算上限</b>。
     *
     * <p>16×16=256 格，够覆盖玩家传送点周围一大片；生成这些区块约几十毫秒，
     * 不会卡死。超过预算就放弃并提示玩家 —— 宁可"明确失败"也不要卡服。</p>
     */
    private static final int MAX_FORCE_CHUNKS = 160;

    // ==================== 对外入口 ====================

    /**
     * ⭐ 面板按钮的唯一入口 —— <b>按当前维度自动决定进/出</b>。
     *
     * @return 是否成功传送
     */
    public static boolean toggle(ServerPlayer player) {
        if (isInCelestial(player)) {
            return leaveCelestial(player);
        }
        return enterCelestial(player);
    }

    /** 面板按钮该显示什么文字（在神界内就显示"离开神界"）。 */
    public static String buttonLabel(ServerPlayer player) {
        return isInCelestial(player) ? "§6离开神界" : "§b进入神界";
    }

    /** 玩家当前是否在神界。 */
    public static boolean isInCelestial(ServerPlayer player) {
        return player.level().dimension() == ModDimensions.CELESTIAL_REALM_LEVEL_KEY;
    }

    // ==================== 进入神界 ====================

    public static boolean enterCelestial(ServerPlayer player) {
        ServerLevel celestial = player.server.getLevel(ModDimensions.CELESTIAL_REALM_LEVEL_KEY);
        if (celestial == null) {
            player.sendSystemMessage(Component.literal("§c[神界] 天境尚未开启。"));
            return false;
        }
        if (player.level() == celestial) {
            player.sendSystemMessage(Component.literal("§e[神界] 你已身处天境。"));
            return false;
        }

        // ① 神界有重生点 → 直接过去（3 参重载 = 不发消息）
        if (player.getRespawnDimension() == ModDimensions.CELESTIAL_REALM_LEVEL_KEY
                && player.getRespawnPosition() != null) {
            return doTeleport(player, celestial, player.getRespawnPosition());
        }

        // ② 没有 → 找安全落点
        BlockPos safe = findSafeLand(celestial, player.blockPosition());
        if (safe == null) {
            player.sendSystemMessage(Component.literal(
                    "§c[神界] 附近 " + (SEARCH_RADIUS * 2) + " 格内未找到可直接落脚的陆地岛屿。"
                            + "§7请用 §f/execute in kunluncontinent:celestial_realm run tp @s ~ ~ 100 ~ ~ "
                            + "§7先过去看看岛屿分布。"));
            return false;
        }

        return doTeleport(player, celestial, safe);
    }

    // ==================== 离开神界 ====================

    /**
     * 离开神界 → 回<b>主世界</b>。
     *
     * <p>⚠️ 供「掉入虚空自动送回」<b>复用同一份逻辑</b>（用户要求两者一致）：
     * 有主世界重生点就回那里，没有就回世界出生点。</p>
     */
    public static boolean leaveCelestial(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        if (overworld == null) return false;

        BlockPos target = player.getRespawnPosition();
        boolean inOverworld = player.getRespawnDimension() == Level.OVERWORLD;
        if (target == null || !inOverworld) {
            target = overworld.getSharedSpawnPos();
        }

        return doTeleport(player, overworld, target);
    }

    // ==================== 落点搜索 ====================

    /**
     * 找一个<b>露天且脚下有地</b>的落点。
     *
     * <p><b>阿基米德螺旋</b>从近到远（步长 ×1.5 每轮），圆心取玩家当前 x/z。
     * 遇到未加载的区块时<b>按预算强制生成</b>，生成完立刻检查。</p>
     *
     * @param center 搜索圆心（玩家当前位置，只取 x/z）
     */
    public static BlockPos findSafeLand(ServerLevel level, BlockPos center) {
        int usedChunks = 0;                    // ⭐ 预算计数
        int step = SEARCH_STEP_START;

        for (int round = 0; round < SEARCH_ROUNDS && step <= SEARCH_RADIUS; round++) {
            int samples = Math.max(16, (int) (2.0 * Math.PI * step / 16.0D));

            for (int s = 0; s < samples; s++) {
                double ang = 2.0 * Math.PI * s / samples;
                int dx = (int) (Math.cos(ang) * step);
                int dz = (int) (Math.sin(ang) * step);
                int x = center.getX() + dx;
                int z = center.getZ() + dz;

                int cx = x >> 4;
                int cz = z >> 4;

                if (!isChunkReady(level, cx, cz)) {
                    // ⛔ 预算用尽 → 直接放弃，不再生成更多区块
                    if (usedChunks >= MAX_FORCE_CHUNKS) {
                        return null;
                    }
                    usedChunks++;
                    if (!generateChunk(level, cx, cz)) {
                        continue;   // 生成失败（很少见），试下一个点
                    }
                }

                BlockPos hit = checkColumn(level, new BlockPos(x, 0, z));
                if (hit != null) return hit;
            }
            step = (int) (step * 1.5D);
        }
        return null;
    }

    /**
     * 同步生成一个区块到"地形+高度图"阶段。
     *
     * <p>用 {@code getChunk(x, z, ChunkStatus.FULL, true)}：
     * {@code ChunkStatus.FULL} 保证高度图和方块状态都已就绪，最后一个 {@code true}
     * 允许强制生成未加载的区块。</p>
     *
     * <p>⚠️ 这是<b>同步阻塞</b>调用 —— 单个区块生成通常几毫秒，
     * 所以靠 {@link #MAX_FORCE_CHUNKS} 预算把总耗时锁死在可接受范围内。</p>
     */
    private static boolean generateChunk(ServerLevel level, int cx, int cz) {
        try {
            level.getChunkSource().getChunk(cx, cz,
                    net.minecraft.world.level.chunk.ChunkStatus.FULL, true);
            return true;
        } catch (RuntimeException e) {
            return false;   // 生成失败（极少数情况，如维度状态异常）
        }
    }

    /**
     * 该坐标的区块是否<b>已完成地形生成</b>。
     */
    private static boolean isChunkReady(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(x, z)) return false;
        var chunk = level.getChunkSource().getChunk(x, z, false);
        return chunk != null && !chunk.isEmpty();
    }

    /**
     * 检查某一列：返回可站立的落脚点，不合格返回 null。
     */
    private static BlockPos checkColumn(ServerLevel level, BlockPos column) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ());
        // 神界维度 height=256，岛顶一般在 60~200。低于 minBuildHeight 或超出上界 = 虚空
        if (surfaceY <= level.getMinBuildHeight() || surfaceY >= level.getMaxBuildHeight()) {
            return null;
        }

        BlockPos ground = new BlockPos(column.getX(), surfaceY - 1, column.getZ());
        BlockState below = level.getBlockState(ground);
        if (below.isAir() || !below.isSolidRender(level, ground)) {
            return null;   // 脚下是虚空/水 → 不能站
        }

        BlockPos feet = new BlockPos(column.getX(), surfaceY, column.getZ());
        if (!level.getBlockState(feet).isAir()) return null;        // 头顶要空
        if (!level.getBlockState(feet.above()).isAir()) return null; // 再上一格也要空

        return feet;
    }

    // ==================== 传送核心 ====================

    /** 实际执行传送（不发任何消息）。 */
    private static boolean doTeleport(ServerPlayer player, ServerLevel target, BlockPos pos) {
        return doTeleport(player, target, pos, null);
    }

    /**
     * 实际执行传送（含清掉落物保护与特效）。
     *
     * @param msg 聊天栏提示；<b>传 {@code null} 或空串则完全不发消息</b>
     */
    private static boolean doTeleport(ServerPlayer player, ServerLevel target, BlockPos pos, String msg) {
        // ⚠️ 必须"先清空头顶再传送" —— 反过来的话传送后改方块可能来不及，
        //    玩家正好卡在树叶里会窒息。
        BlockPos clear = pos.above();
        if (!target.getBlockState(clear).isAir()) {
            target.setBlock(clear, Blocks.AIR.defaultBlockState(), 2);
        }

        player.teleportTo(target,
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                player.getYRot(), player.getXRot());
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);

        target.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        target.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                60, 0.4D, 1.0D, 0.4D, 0.2D);

        if (msg != null && !msg.isEmpty()) {
            player.sendSystemMessage(Component.literal(msg));
        }
        return true;
    }
}
