package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import com.TovidY.kunluncontinent.block.klblock.LiandanluBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class DanfangFurnaceRandomizer {

    public static final int MIN_TIER = 3;
    public static final int MAX_TIER = 7;
    /** 落在低阶组（{@link #MIN_TIER}~{@link #LOW_TIER_MAX}）的概率。 */
    public static final float LOW_TIER_CHANCE = 0.6f;
    public static final int LOW_TIER_MAX = 4;

    /**
     * 兜底扫描间隔（tick）。100 tick = 5 秒。
     *
     * <p>即时扫描已经在"玩家跨区块"时做了，这个只是安全网 —— 覆盖玩家站着不动、
     * 而附近刚好生成了丹房（比如手打 {@code /place}）的情况。所以不需要每秒一次。</p>
     */
    private static final int SAFETY_SCAN_INTERVAL = 100;

    /** 以玩家所在区块为中心，向四周各扫几个区块。13×10×12 的丹房跨最多 2×2 区块，3 足够。 */
    private static final int CHUNK_RADIUS = 3;

    /**
     * 丹炉 NBT 标记：已掷过阶位。
     *
     * <p>为什么需要它：模板里写死的占位方块就是 {@code liandanlu4}，而 4 阶仍在
     * {@link #MIN_TIER}~{@link #MAX_TIER} 范围内。若随机到 4，「换方块」是空操作，
     * 光靠方块类型无法区分「已处理」和「还没处理」→ 会被反复重掷、最终永远出不了 4 阶。
     * 所以随机到 4 阶时必须额外打标记。</p>
     *
     * <p>随机到非 4 阶时不需要标记：{@code setBlock} 后旧方块实体连同标记一起被销毁，
     * 而新方块类型已经不等于 {@code liandanlu4}，天然不会被再次收集。</p>
     */
    private static final String NBT_ROLLED = "DanfangTierRolled";

    /** 玩家上次扫过的区块（打包成 long 存 persistentData），用来做"跨区块才扫"。 */
    private static final String NBT_SCAN_CHUNK = "DanfangScanChunk";

    /** 废弃丹房结构 ID，用于把「真的是丹房里的炉子」和「玩家自己摆的丹炉」区分开。 */
    private static final ResourceKey<Structure> DANFANG_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang"));

    private static final Random RANDOM = new Random();

    // ==================================================================================
    //  触发
    // ==================================================================================

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // 先做双端便宜判断，客户端玩家到这里就返回
        if (!(event.player instanceof ServerPlayer player)) return;

        boolean enteredNewChunk = markChunk(player);
        if (!enteredNewChunk && player.tickCount % SAFETY_SCAN_INTERVAL != 0) return;
        scanNear(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) scanNear(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) scanNear(player);
    }

    /**
     * 玩家是否跨进了新区块；是的话记下来并返回 {@code true}。
     *
     * <p>状态存在 persistentData 里而不是静态表 —— 不引入跨玩家的静态状态，
     * 退出重进也不会丢。初始值 0 会在登录时被 {@link #onPlayerLoggedIn} 的即时扫描补上。</p>
     */
    private static boolean markChunk(ServerPlayer player) {
        int cx = player.getBlockX() >> 4;
        int cz = player.getBlockZ() >> 4;
        long packed = ((long) cx << 32) ^ (cz & 0xFFFFFFFFL);
        CompoundTag data = player.getPersistentData();
        if (data.getLong(NBT_SCAN_CHUNK) == packed) return false;
        data.putLong(NBT_SCAN_CHUNK, packed);
        return true;
    }

    private static void scanNear(ServerPlayer sp) {
        if (!(sp.level() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;   // 丹房只在主世界

        int cx = sp.getBlockX() >> 4;
        int cz = sp.getBlockZ() >> 4;

        // 先收集，再动手：setBlock 会增删区块的方块实体表，
        // 边遍历边改会抛 ConcurrentModificationException。
        List<BlockPos> furnaces = new ArrayList<>();

        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
                if (chunk == null) continue;
                collectFurnaces(level, chunk, furnaces);
            }
        }

        for (BlockPos pos : furnaces) replaceFurnace(level, pos);
    }

    // ==================================================================================
    //  收集
    // ==================================================================================

    /**
     * 扫一个区块的方块实体表，挑出「还是四阶、没掷过、且确实在丹房结构里」的丹炉。
     *
     * <p>最后那一条结构判定是为了<b>不碰玩家自己摆的丹炉</b> —— 玩家特意放一个四阶炉，
     * 结果被随机成了六阶，那是 bug 不是特性。判定很便宜（就是查一下该坐标在不在结构里），
     * 而且只有真的扫到丹炉才会走。</p>
     */
    private static void collectFurnaces(ServerLevel level, LevelChunk chunk, List<BlockPos> out) {
        Map<BlockPos, BlockEntity> map = chunk.getBlockEntities();
        if (map.isEmpty()) return;

        for (Map.Entry<BlockPos, BlockEntity> entry : map.entrySet()) {
            BlockEntity be = entry.getValue();
            if (!(be instanceof LiandanluBlockEntity)) continue;

            BlockPos pos = entry.getKey();
            // 换过方块或打过标记后就不再匹配，天然幂等
            if (be.getPersistentData().getBoolean(NBT_ROLLED)) continue;
            if (!chunk.getBlockState(pos).is(ModBlocks.LIANDANLU4.get())) continue;
            if (!inDanfang(level, pos)) continue;

            out.add(pos.immutable());
        }
    }

    /** 该坐标是否落在 {@code kunluncontinent:abandoned_danfang} 结构的某个 piece 内。 */
    private static boolean inDanfang(ServerLevel level, BlockPos pos) {
        return level.structureManager().getStructureWithPieceAt(pos, DANFANG_KEY).isValid();
    }

    // ==================================================================================
    //  丹炉换阶
    // ==================================================================================

    private static void replaceFurnace(ServerLevel level, BlockPos pos) {
        BlockState old = level.getBlockState(pos);
        if (!old.is(ModBlocks.LIANDANLU4.get())) return;

        int tier = pickTier();
        var holder = ModBlocks.LIANDANLULIST.get(tier - 1);
        Block next = holder == null ? null : holder.get();
        if (next == null) return;

        if (next == old.getBlock()) {
            // 随机到 4 阶 = 模板默认值，不需要换方块；
            // 但必须打标记，否则下一轮扫描会把它当成「还没处理」反复重掷。
            markRolled(level, pos);
            KlMain.LOGGER.debug("[Kunlun] 废弃丹房炼丹炉随机化：4 阶 -> 4 阶（保持）@ {}", pos);
            return;
        }

        BlockState nextState = next.defaultBlockState();
        // 保留朝向，别把炉子转个方向
        if (nextState.hasProperty(LiandanluBlock.FACING) && old.hasProperty(LiandanluBlock.FACING)) {
            nextState = nextState.setValue(LiandanluBlock.FACING, old.getValue(LiandanluBlock.FACING));
        }

        level.setBlock(pos, nextState, Block.UPDATE_ALL);
        KlMain.LOGGER.info("[Kunlun] 废弃丹房炼丹炉随机化：4 阶 -> {} 阶 @ {}", tier, pos);
    }

    /** 在方块实体的 {@code ForgeData} 里打「已掷阶位」标记（随区块正常存档）。 */
    private static void markRolled(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return;
        be.getPersistentData().putBoolean(NBT_ROLLED, true);
        be.setChanged();
    }

    /** 随机阶位：60% 出 3~4 阶，40% 出 5~7 阶（均匀随机会让废弃丹房频繁刷出高阶炉，太扎眼）。 */
    private static int pickTier() {
        int min = Math.max(1, MIN_TIER);
        int max = Math.min(ModBlocks.LIANDANLULIST.size(), MAX_TIER);
        if (min >= max) return min;

        int lowTo = Math.max(min, Math.min(max, LOW_TIER_MAX));

        if (RANDOM.nextFloat() < LOW_TIER_CHANCE) {
            return min + RANDOM.nextInt(lowTo - min + 1);
        }
        int highFrom = lowTo + 1;
        if (highFrom > max) {
            return min + RANDOM.nextInt(lowTo - min + 1);
        }
        return highFrom + RANDOM.nextInt(max - highFrom + 1);
    }
}
