package com.TovidY.kunluncontinent.tower;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 幻境塔的<b>渐进式预生成器</b>：首次进入塔维度时，按节奏把 {@code tower} 结构复制到
 * {@code (id * 300, 64, 0)}。
 *
 * <h3>它到底有多少开销（实测口径）</h3>
 * <ul>
 *   <li>结构本身 <b>31 × 11 × 31 ≈ 1.06 万方块</b>，只跨 <b>2×2 = 4 个 chunk</b>；</li>
 *   <li>客户端/局域网 10 座、独立服务器 50 座 → 合计 <b>40 / 200 个 chunk、约 10.6 万 / 53 万次 setBlock</b>；</li>
 *   <li>建完之后稳态开销 ≈ 0：{@code onServerTick} 第一行就 {@code isGenerating} 早退。</li>
 * </ul>
 * <p>所以<b>总量并不大</b>，真正让人感觉卡的是<b>尖峰</b>：放置时如果 chunk 还没生成好，
 * {@code setBlock} 会触发**主线程同步生成地形**，一下就是几十~上百毫秒。</p>
 *
 * <h3>因此本类的做法（三阶段 + 分帧）</h3>
 * <pre>
 *   ① 投递位面票据（addRegionTicket）—— 只登记，不阻塞，让 chunk 系统按它自己的预算去生成；
 *   ② 每 tick 用 getChunk(..., FULL, false) 非阻塞地探一下“4 个 chunk 都到齐了吗”；
 *   ③ 到齐了才 placeInWorld —— 这时已经没有地形生成，只剩 1 万次纯方块写入（几毫秒）。
 * </pre>
 * <p>地形生成被摊进原版 chunk 系统的每 tick 预算里，所以不再有“每秒卡一下”的尖峰；
 * 代价是总时长略微变长（多等几 tick），这个交换是划算的。</p>
 *
 * <h3>其它优化</h3>
 * <ul>
 *   <li>结构模板<b>只读一次</b>并缓存（原来每建一座都 {@code templateManager.get()} 走一遍仓库）；</li>
 *   <li>进度保存从「每 10 座全维度 flush 存盘 + 结束再来一次」改成<b>只在结束时 flush 一次</b>，
 *       中途只 {@code setDirty}（自动保存会带走）；</li>
 *   <li>用<b>自定义票据</b>而不是 {@code setChunkForced}：不再往 {@code forcedchunks} 里留持久记录，
 *       中途崩服也不会让这 4 个 chunk 永久强加载；</li>
 *   <li>已建成的塔数写进 {@link TowerSaveData}，崩服重启后<b>从断点续建</b>，不会把旧塔覆盖一遍。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class TowerPreBuilder {

    /** 塔间距（方块）。⚠️ 必须与 {@code StoneStampBlock} 里的 {@code allocatedId * 300} 一致。 */
    public static final int TOWER_SPACING = 300;

    /** 塔底座 Y。 */
    public static final int TOWER_BASE_Y = 64;

    private static final ResourceLocation TEMPLATE_ID = new ResourceLocation(KlMain.MOD_ID, "tower");

    /** 两座塔之间的间隔 tick（20 = 1 秒）。给服务器留出喘息时间。 */
    private static final int TICKS_PER_TOWER = 20;

    /** 等 chunk 就绪的上限：超过就强行放置（兜底，防止某次生成失败导致整个流程卡死）。 */
    private static final int MAX_WAIT_TICKS = 200;

    /** 进度播报间隔（座）。每座都发一条 actionbar 是纯浪费。 */
    private static final int PROGRESS_EVERY = 5;

    /**
     * 本模组专用的 chunk 票据。
     *
     * <p>用自定义类型而不是 {@code setChunkForced}：强加载票据会被写进 {@code forcedchunks} 文件，
     * 中途崩服就会把那几个 chunk <b>永久强加载</b>；票据只在内存里，进程结束就没了。</p>
     */
    private static final TicketType<ChunkPos> TOWER_TICKET =
            TicketType.create("kunluncontinent_tower", Comparator
                    .comparingInt((ChunkPos cp) -> cp.x)
                    .thenComparingInt(cp -> cp.z));

    private static boolean isGenerating = false;
    /** 本次预生成的目标座数（客户端 10 / 独立服务器 50）。 */
    private static int TOTAL_TOWERS = 100;
    /** 已经建成的塔数（塔 id 区间是 {@code [0, builtTowers)}）。 */
    private static int builtTowers = 0;

    private static ServerLevel targetLevel = null;
    private static int tickCounter = 0;

    // ---- 单座塔的三阶段状态 ----
    private static int pendingTowerId = -1;
    private static List<ChunkPos> pendingChunks = List.of();
    private static int waitTicks = 0;

    /** 结构模板缓存：{@code StructureTemplate} 是不可变数据，读一次就够了。 */
    private static StructureTemplate cachedTemplate = null;
    private static boolean templateLoadFailed = false;

    // ==================================================================================
    //  启动
    // ==================================================================================

    /**
     * 监听世界加载：塔维度首次创建时启动渐进式预生成。
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.dimension().equals(com.TovidY.kunluncontinent.worldgen.ModDimensions.TOWER_REALM_LEVEL_KEY)) {
            return;
        }
        TowerSaveData data = TowerSaveData.get(serverLevel);
        if (data.isGenerated()) {
            // 已经建完：把断点信息读回来给 TowerStateManager 用（否则重启后会以为只有 TOTAL_TOWERS 座）
            builtTowers = Math.max(builtTowers, data.getBuiltTowers());
            return;
        }
        startGeneration(serverLevel);
    }

    private static void startGeneration(ServerLevel level) {
        if (isGenerating) {
            return;
        }
        targetLevel = level;
        isGenerating = true;
        tickCounter = 0;
        pendingTowerId = -1;
        pendingChunks = List.of();
        waitTicks = 0;

        // 上次建到一半就崩了：从断点继续，别把已经建好的塔再盖一遍
        builtTowers = Math.max(0, TowerSaveData.get(level).getBuiltTowers());

        // 客户端（含单人/局域网）只建 10 座；独立服务器 50 座。
        // ⚠️ 注意这决定的是「预生成多少」，不是「最多能进多少人」——
        //    超出之后 TowerStateManager 会现场扩建。
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            TOTAL_TOWERS = 10;
        } else {
            TOTAL_TOWERS = 50;
        }

        KlMain.LOGGER.info("[昆仑大陆] 幻境塔预生成启动：目标 {} 座，从第 {} 座继续（每 {} tick 一座，分帧放置）",
                TOTAL_TOWERS, builtTowers + 1, TICKS_PER_TOWER);

        if (builtTowers >= TOTAL_TOWERS) {
            finishGeneration();
        }
    }

    // ==================================================================================
    //  心跳：三阶段状态机
    // ==================================================================================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!isGenerating || event.phase != TickEvent.Phase.END || targetLevel == null) {
            return;
        }

        // ---- 阶段 0：还没开始搬这一座 → 到点了就投票据 ----
        if (pendingTowerId < 0) {
            if (++tickCounter < TICKS_PER_TOWER) {
                return;
            }
            tickCounter = 0;
            beginTower(builtTowers);
            return;
        }

        // ---- 阶段 1：等 chunk 就绪（不阻塞！）----
        if (!chunksReady(targetLevel, pendingChunks)) {
            if (++waitTicks < MAX_WAIT_TICKS) {
                return;
            }
            KlMain.LOGGER.warn("[昆仑大陆] 第 {} 座塔等待 chunk 超时（{} tick），强行放置",
                    pendingTowerId + 1, MAX_WAIT_TICKS);
        }

        // ---- 阶段 2：放置 + 收尾 ----
        finishTower(event);
    }

    /** 阶段 1 收尾：登记票据（不阻塞），让 chunk 系统自己按预算生成。 */
    private static void beginTower(int towerId) {
        pendingTowerId = towerId;
        pendingChunks = chunksFor(targetLevel, towerId);
        waitTicks = 0;
        for (ChunkPos cp : pendingChunks) {
            targetLevel.getChunkSource().addRegionTicket(TOWER_TICKET, cp, 0, cp);
        }
    }

    /** 阶段 3：chunk 已就绪 → 只做方块写入，几毫秒的事。 */
    private static void finishTower(TickEvent.ServerTickEvent event) {
        int towerId = pendingTowerId;
        try {
            placeTemplate(targetLevel, towerId);
            builtTowers = Math.max(builtTowers, towerId + 1);
            // 记断点：只标脏，不 flush —— 让自动保存带走，避免主线程磁盘 IO
            TowerSaveData.get(targetLevel).setBuiltTowers(builtTowers);
        } catch (Exception e) {
            KlMain.LOGGER.error("[昆仑大陆] 第 {} 座幻境塔生成失败", towerId + 1, e);
            // 失败也要推进，否则会卡在同一座塔上无限重试
            builtTowers = Math.max(builtTowers, towerId + 1);
            TowerSaveData.get(targetLevel).setBuiltTowers(builtTowers);
        } finally {
            for (ChunkPos cp : pendingChunks) {
                targetLevel.getChunkSource().removeRegionTicket(TOWER_TICKET, cp, 0, cp);
            }
            pendingChunks = List.of();
            pendingTowerId = -1;
            waitTicks = 0;
        }

        // 进度播报：每 PROGRESS_EVERY 座（或最后一座）才发一次
        if (builtTowers % PROGRESS_EVERY == 0 || builtTowers >= TOTAL_TOWERS) {
            int progress = (int) (((float) builtTowers / Math.max(1, TOTAL_TOWERS)) * 100);
            String msg = "§6[昆仑大陆] §f幻境塔初始化中... §a" + progress + "% " + progressBar(progress);
            event.getServer().getPlayerList().getPlayers().forEach(p -> p.displayClientMessage(Component.literal(msg), true));
            KlMain.LOGGER.info("[昆仑大陆] 幻境塔 {}/{}（{}%）", builtTowers, TOTAL_TOWERS, progress);
        }

        if (builtTowers >= TOTAL_TOWERS) {
            finishGeneration();
        }
    }

    /** 全部建完：标记 + <b>只在这里</b>做一次 flush 存盘，保证"标记已生成"和塔数据一起落盘。 */
    private static void finishGeneration() {
        isGenerating = false;
        TowerSaveData.get(targetLevel).setBuiltTowers(builtTowers);
        TowerSaveData.get(targetLevel).setGenerated(true);
        targetLevel.save(null, true, false);

        if (targetLevel.getServer() != null) {
            targetLevel.getServer().getPlayerList().getPlayers().forEach(p ->
                    p.sendSystemMessage(Component.literal("§b[昆仑大陆] 幻境塔初始化已完成")));
        }
        KlMain.LOGGER.info("[昆仑大陆] === 幻境塔预生成完毕：{} 座 ===", builtTowers);
        targetLevel = null;
    }

    // ==================================================================================
    //  放置
    // ==================================================================================

    /** 同步放置一座塔（调用方必须保证 chunk 已就绪，否则会退化成主线程同步生成地形）。 */
    private static void placeTemplate(ServerLevel level, int towerId) {
        StructureTemplate template = template(level);
        if (template == null) {
            return;
        }
        BlockPos centerPos = new BlockPos(towerId * TOWER_SPACING, TOWER_BASE_Y, 0);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false);
        template.placeInWorld(level, centerPos, centerPos, settings, level.random, 2);
    }

    /** 结构模板缓存（失败只报一次错，不刷屏）。 */
    private static StructureTemplate template(ServerLevel level) {
        if (cachedTemplate != null) {
            return cachedTemplate;
        }
        if (templateLoadFailed) {
            return null;
        }
        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> opt = manager.get(TEMPLATE_ID);
        if (opt.isEmpty()) {
            templateLoadFailed = true;
            KlMain.LOGGER.error("[昆仑大陆] 严重错误：找不到结构 {}，幻境塔无法生成！", TEMPLATE_ID);
            return null;
        }
        cachedTemplate = opt.get();
        return cachedTemplate;
    }

    /**
     * 这座塔会覆盖哪些 chunk。
     *
     * <p>按<b>结构实际尺寸</b>算，而不是只强加载中心 chunk —— 31×11×31 的塔会跨 2×2 个 chunk，
     * 原来只强加载中心那一个，剩下 3 个是在 {@code placeInWorld} 内部被同步加载的，
     * 那才是卡顿的真正来源。</p>
     */
    private static List<ChunkPos> chunksFor(ServerLevel level, int towerId) {
        StructureTemplate template = template(level);
        int sizeX = template != null ? template.getSize().getX() : 32;
        int sizeZ = template != null ? template.getSize().getZ() : 32;

        int x0 = towerId * TOWER_SPACING;
        int z0 = 0;
        int cx0 = x0 >> 4, cz0 = z0 >> 4;
        int cx1 = (x0 + sizeX - 1) >> 4, cz1 = (z0 + sizeZ - 1) >> 4;

        List<ChunkPos> list = new ArrayList<>((cx1 - cx0 + 1) * (cz1 - cz0 + 1));
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                list.add(new ChunkPos(cx, cz));
            }
        }
        return list;
    }

    /** 非阻塞探测：这些 chunk 是否都已经到 FULL。 */
    private static boolean chunksReady(ServerLevel level, List<ChunkPos> chunks) {
        ChunkSource source = level.getChunkSource();
        for (ChunkPos cp : chunks) {
            if (source.getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null) {
                return false;
            }
        }
        return true;
    }

    // ==================================================================================
    //  外部接口
    // ==================================================================================

    /**
     * <b>玩家现场扩容</b>：池子满了立刻盖一座（不能分帧，玩家在等）。
     *
     * <p>这里先把塔覆盖的所有 chunk <b>同步</b>加载到 FULL，再放置 ——
     * 否则 {@code placeInWorld} 边生成边写方块，玩家会明显卡一下，
     * 偶尔还会出现"半座塔"的方块更新错乱。</p>
     *
     * @param level 必须是<b>塔维度</b>，不是玩家当前所在的维度
     */
    public static synchronized void buildSingleTowerDirectly(ServerLevel level, int towerId) {
        List<ChunkPos> chunks = chunksFor(level, towerId);
        for (ChunkPos cp : chunks) {
            // requireChunk = true：阻塞直到这 4 个 chunk 生成完（玩家本来就在等，只能同步）
            level.getChunk(cp.x, cp.z, ChunkStatus.FULL, true);
        }
        try {
            placeTemplate(level, towerId);
            builtTowers = Math.max(builtTowers, towerId + 1);
            TowerSaveData.get(level).setBuiltTowers(builtTowers);
        } catch (Exception e) {
            KlMain.LOGGER.error("[昆仑大陆] 动态扩建第 {} 座幻境塔失败", towerId + 1, e);
        }
    }

    /** 已经建成的塔数（塔 id 合法区间是 {@code [0, builtTowers)}）。 */
    public static int getBuiltTowers() {
        return builtTowers;
    }

    /** 预生成目标座数（仅作诊断用；真正能分到多少座看 {@link #getBuiltTowers()}）。 */
    public static int getTotalTowers() {
        return TOTAL_TOWERS;
    }

    /** 是否正在预生成（外部据此禁止玩家挑战）。 */
    public static boolean isGenerating() {
        return isGenerating;
    }

    /** 进度条 [■■■□□□□□□□]。 */
    private static String progressBar(int progress) {
        int filled = Math.max(0, Math.min(10, progress / 10));
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "§a■" : "§7□");
        }
        return bar.append("§8]").toString();
    }
}
