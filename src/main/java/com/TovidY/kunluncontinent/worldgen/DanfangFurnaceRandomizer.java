package com.TovidY.kunluncontinent.worldgen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import com.TovidY.kunluncontinent.block.klblock.LiandanluBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 废弃丹房的后处理：① 把模板里写死的四阶丹炉随机成 3~7 阶；② 兜底填充老存档的空奖励箱。
 *
 * <h3>奖励箱的正解在结构 NBT 里</h3>
 * <p>结构 NBT（{@code data/kunluncontinent/structures/abandoned_danfang.nbt}）里的两个
 * {@code minecraft:chest} 已带上 {@code LootTable: "kunluncontinent:chests/abandoned_danfang"}
 * （见 {@code .tools/patch_danfang_loot.py}）。原理（已核对 1.20.1
 * {@code StructureTemplate.placeInWorld} 第 244~252 行）：放置结构时对带 nbt 的方块执行
 * {@code blockentity.load(nbt)}，且若该方块实体是 {@link RandomizableContainerBlockEntity}，
 * 还会补写一个<b>随机 {@code LootTableSeed}</b>。于是：</p>
 * <ul>
 *   <li>每个丹房的箱子内容天然不同；</li>
 *   <li>首次开箱才掷战利品，不会提前刷出；</li>
 *   <li>零运行时开销。</li>
 * </ul>
 * <p>⚠️ 因此<b>绝不能</b>在扫描里无脑调 {@code chest.isEmpty()} /
 * {@code chest.getItem()}：{@link RandomizableContainerBlockEntity#isEmpty()} 内部会先
 * {@code unpackLootTable()}，把原版战利品提前结算，等于「玩家路过就被开箱」。
 * 本类只在{@link #inDanfang 确认在废弃丹房结构内}之后才碰箱子。</p>
 *
 * <h3>丹炉为什么必须用 Java</h3>
 * <p>「随机阶数」不是方块状态能表达的：每阶是独立方块，每阶的
 * {@link LiandanluBlockEntity} 由 {@code LiandanluBlock(..., tier)} 硬编码，
 * 结构模板无法表达随机。所以只能在结构生成后换方块。</p>
 *
 * <h3>触发时机（旧实现的坑）</h3>
 * <p>旧实现只挂 {@code PlayerLoggedInEvent} / {@code PlayerChangedDimensionEvent}，
 * 玩家「走进」丹房不触发任何事件 → 表现为「生成了但没变」。现在改为：</p>
 * <ul>
 *   <li>{@link #onPlayerTick} —— 每 {@link #SCAN_INTERVAL} tick 扫一次玩家周边区块，
 *       覆盖自然生成、{@code /place} 生成、老存档已存在的三种情况；</li>
 *   <li>{@link #onPlayerLoggedIn} / {@link #onPlayerChangedDimension} —— 立即扫一次。</li>
 * </ul>
 * <p>扫描只遍历已加载区块的方块实体表（{@code LevelChunk#getBlockEntities}），
 * 不逐格读方块，开销可忽略。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class DanfangFurnaceRandomizer {

    public static final int MIN_TIER = 3;
    public static final int MAX_TIER = 7;
    /** 落在低阶组（{@link #MIN_TIER}~{@link #LOW_TIER_MAX}）的概率。 */
    public static final float LOW_TIER_CHANCE = 0.6f;
    public static final int LOW_TIER_MAX = 4;

    /** 扫描间隔（tick）。20 tick = 1 秒。 */
    private static final int SCAN_INTERVAL = 20;
    /** 以玩家所在区块为中心，向四周各扫几个区块。13×10×12 的丹房跨最多 2×2 区块，3 足够。 */
    private static final int CHUNK_RADIUS = 3;

    /** 箱子 NBT 标记：本类已处理过，别再碰。 */
    private static final String NBT_FILLED = "DanfangLootFilled";

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

    /** 废弃丹房结构 ID，用于把「真的是丹房」和「玩家自己摆的丹炉」区分开。 */
    private static final ResourceKey<Structure> DANFANG_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    new ResourceLocation(KlMain.MOD_ID, "abandoned_danfang"));

    /**
     * 奖励表 ID。内容由 {@code datagen.lootprovider.ModChestLootTablesProvider}
     * 跑 {@code runData} 生成，<b>不要手写 JSON</b>。
     */
    private static final ResourceLocation LOOT_TABLE_ID =
            new ResourceLocation(KlMain.MOD_ID, "chests/abandoned_danfang");

    private static final Random RANDOM = new Random();

    // ==================================================================================
    //  触发
    // ==================================================================================

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.tickCount % SCAN_INTERVAL != 0) return;
        scanNear(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        scanNear(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        scanNear(event.getEntity());
    }

    private static void scanNear(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(sp.level() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;   // 丹房只在主世界

        int cx = sp.getBlockX() >> 4;
        int cz = sp.getBlockZ() >> 4;

        // 先收集，再动手：setBlock 会增删区块的方块实体表，
        // 边遍历边改会抛 ConcurrentModificationException。
        List<BlockPos> furnaces = new ArrayList<>();
        List<BlockPos> chests = new ArrayList<>();

        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx + dx, cz + dz);
                if (chunk == null) continue;
                collectChunk(level, chunk, furnaces, chests);
            }
        }

        for (BlockPos pos : furnaces) replaceFurnace(level, pos);
        for (BlockPos pos : chests) fillLegacyChest(level, pos);
    }

    // ==================================================================================
    //  收集
    // ==================================================================================

    /**
     * 扫一个区块的方块实体表。
     *
     * <p>箱子只在该区块<b>含炼丹炉</b>时才进一步做结构判定 —— 废弃丹房必定有炉子，
     * 这样能挡掉绝大多数无关区块，避免对玩家基地里成百上千个箱子反复查结构。</p>
     */
    private static void collectChunk(ServerLevel level, LevelChunk chunk,
                                     List<BlockPos> furnaces, List<BlockPos> chests) {
        Map<BlockPos, BlockEntity> map = chunk.getBlockEntities();
        if (map.isEmpty()) return;

        boolean hasFurnace = false;
        List<BlockPos> looseChests = null;

        for (Map.Entry<BlockPos, BlockEntity> entry : map.entrySet()) {
            BlockEntity be = entry.getValue();
            BlockPos pos = entry.getKey();

            if (be instanceof LiandanluBlockEntity) {
                hasFurnace = true;
                // 只看「还是四阶且没掷过」的：换过方块或打过标记后就不再匹配，天然幂等
                if (!be.getPersistentData().getBoolean(NBT_ROLLED)
                        && chunk.getBlockState(pos).is(ModBlocks.LIANDANLU4.get())) {
                    furnaces.add(pos.immutable());
                }
            } else if (be instanceof RandomizableContainerBlockEntity
                    && !be.getPersistentData().getBoolean(NBT_FILLED)) {
                if (looseChests == null) looseChests = new ArrayList<>(4);
                looseChests.add(pos.immutable());
            }
        }

        if (!hasFurnace || looseChests == null) return;

        // 到这里才允许查结构 + 触碰容器状态
        for (BlockPos pos : looseChests) {
            if (inDanfang(level, pos)) chests.add(pos);
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
            KlMain.LOGGER.info("[Kunlun] 废弃丹房炼丹炉随机化：4 阶 -> 4 阶（保持）@ {}", pos);
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

    private static void fillLegacyChest(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest)) return;
        if (!inDanfang(level, pos)) return;

        var data = chest.getPersistentData();
        if (data.getBoolean(NBT_FILLED)) return;

        // 注意：这一步会让自带 LootTable 的箱子按随机种子结算一次
        if (!chest.isEmpty()) {
            data.putBoolean(NBT_FILLED, true);
            chest.setChanged();
            return;
        }

        LootTable table = level.getServer().getLootData().getLootTable(LOOT_TABLE_ID);
        if (table == null) return;   // 表不存在，下次再看

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.CHEST);

        table.getRandomItems(params, stack -> {
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                if (chest.getItem(slot).isEmpty()) {
                    chest.setItem(slot, stack);
                    return;
                }
            }
        });

        data.putBoolean(NBT_FILLED, true);
        chest.setChanged();
    }
}
