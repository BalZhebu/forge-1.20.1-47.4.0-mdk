package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.TovidY.kunluncontinent.worldgen.ModDimensions.POLAR_ICE_REALM_TYPE;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UltraOptimizedLightningEvent {

    private static final int CHANCE_DENOMINATOR = 200;
    private static final int SEARCH_RADIUS = 20;
    private static final int MIN_TRIGGER_HEIGHT = 210;
    // 原 ±2,±2,±2 范围，曼哈顿距离上限 = 6
    private static final int ROD_SEARCH_MANHATTAN = 6;

    private static final float LIGHTNING_SOUND_VOLUME = 2.0F;
    private static final float LIGHTNING_SOUND_PITCH = 1.0F;

    /**
     * chunkKey -> 该 chunk 内的避雷针坐标列表（持久内存缓存，建一次永久有效）
     */
    private static final Map<Long, List<BlockPos>> ROD_CACHE = new HashMap<>();
    /** 本 tick 已处理的铁锭位置，防止同一物品被多次触发 */
    private static final Map<BlockPos, Long> RECENTLY_PROCESSED = new HashMap<>();
    /** 是否已完成全图避雷针扫描（只执行一次） */
    private static volatile boolean cacheInitialized = false;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.level.isClientSide
                || event.level.getGameTime() % 20 != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) event.level;
        if (!level.dimensionTypeId().equals(POLAR_ICE_REALM_TYPE)) return;

        // 首次进入维度时扫描全图避雷针写入缓存（之后每次 tick 只做内存查找）
        if (!cacheInitialized) {
            buildFullCache(level);
            cacheInitialized = true;
        }

        if (level.random.nextInt(CHANCE_DENOMINATOR) != 0) return;

        // 清理超过 100 tick（约 5 秒）的处理记录，防止 Map 无限增长
        long cutoff = level.getGameTime() - 100;
        RECENTLY_PROCESSED.entrySet().removeIf(e -> e.getValue() < cutoff);

        for (ServerPlayer player : level.players()) {
            if (tryStrikeNearPlayer(level, player)) {
                return; // 一次判定全局只触发一次
            }
        }
    }

    /**
     * 按需扫描玩家周围 chunk 内的避雷针，写入内存缓存。
     * 使用 getChunkNow() 获取已加载的 chunk，不会触发异步加载。
     */
    private static void buildFullCache(ServerLevel level) {
        int radiusChunks = (SEARCH_RADIUS / 16) + 1;
        for (ServerPlayer player : level.players()) {
            int px = player.blockPosition().getX();
            int pz = player.blockPosition().getZ();
            int baseChunkX = px >> 4;
            int baseChunkZ = pz >> 4;
            for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
                for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                    int cx = baseChunkX + dx;
                    int cz = baseChunkZ + dz;
                    long key = packChunkKey(cx, cz);
                    if (ROD_CACHE.containsKey(key)) continue;
                    net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;
                    List<BlockPos> rods = new ArrayList<>();
                    net.minecraft.world.level.chunk.LevelChunkSection[] sections = chunk.getSections();
                    if (sections == null) continue;
                    for (int sx = 0; sx < sections.length; sx++) {
                        var section = sections[sx];
                        if (section == null) continue;
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {
                                    if (section.getBlockState(x, y, z).is(Blocks.LIGHTNING_ROD)) {
                                        int worldY = sx * 16 + y;
                                        rods.add(new BlockPos(cx * 16 + x, worldY, cz * 16 + z));
                                    }
                                }
                            }
                        }
                    }
                    if (!rods.isEmpty()) {
                        ROD_CACHE.put(key, rods);
                    }
                }
            }
        }
    }

    private static boolean tryStrikeNearPlayer(ServerLevel level, ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        AABB searchArea = new AABB(playerPos).inflate(SEARCH_RADIUS);

        // 筛选符合条件的临星铁锭（含防重复检查）
        List<ItemEntity> targetIngots = level.getEntitiesOfClass(ItemEntity.class, searchArea, item -> {
            if (!item.getItem().is(ModItems.RINSEI_INGOT.get())) return false;
            if (item.getY() <= MIN_TRIGGER_HEIGHT) return false;
            return !RECENTLY_PROCESSED.containsKey(item.blockPosition());
        });

        if (targetIngots.isEmpty()) return false;

        for (ItemEntity ingot : targetIngots) {
            if (hasNearbyLightningRod(ingot.blockPosition())) {
                triggerLightning(level, ingot);
                return true;
            }
        }
        return false;
    }

    /**
     * 检查物品附近是否有避雷针——纯内存查找，不查方块状态。
     * 检查物品所在 chunk 及周边 1 格（共 9 个 chunk）内的缓存列表。
     */
    private static boolean hasNearbyLightningRod(BlockPos ingotPos) {
        int cx = ingotPos.getX() >> 4;
        int cz = ingotPos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                List<BlockPos> rods = ROD_CACHE.get(packChunkKey(cx + dx, cz + dz));
                if (rods == null) continue;
                for (BlockPos rod : rods) {
                    if (rod.distManhattan(ingotPos) <= ROD_SEARCH_MANHATTAN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void triggerLightning(ServerLevel level, ItemEntity ingot) {
        BlockPos strikePos = ingot.blockPosition().above();

        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning == null) return;
        lightning.moveTo(Vec3.atBottomCenterOf(strikePos));
        lightning.setVisualOnly(false);
        level.addFreshEntity(lightning);

        ItemStack ingotStack = ingot.getItem();
        ItemEntity fragment = new ItemEntity(level, ingot.getX(), ingot.getY(), ingot.getZ(),
                new ItemStack(ModItems.LIGHTNING_FRAGMENTS.get(), ingotStack.getCount()));
        fragment.setGlowingTag(true);
        fragment.setInvulnerable(true);
        fragment.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(fragment);

        ingot.discard();
        // 标记已处理，防止本 tick 内重复触发
        RECENTLY_PROCESSED.put(ingot.blockPosition(), level.getGameTime());

        level.playSound(null, strikePos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS,
                LIGHTNING_SOUND_VOLUME, LIGHTNING_SOUND_PITCH);
    }

    private static long packChunkKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }
}
