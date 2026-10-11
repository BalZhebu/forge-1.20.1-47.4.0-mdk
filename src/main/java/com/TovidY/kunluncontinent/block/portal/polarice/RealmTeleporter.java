package com.TovidY.kunluncontinent.block.portal.polarice;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

/**
 * 极冰界 / 雷界 传送门的<b>公共实现</b>。
 *
 * <h3>⚠️ 为什么抽成基类</h3>
 * 之前 {@code PolarIceTeleporter} 与 {@code ThunderRealmTeleporter} 是两份<b>逐行雷同</b>的代码，
 * 所以同一个 BUG 会在两处同时出现、且改一处就漏另一处。现在差异只有
 * 「门内方块 / 框架方块 / 本维度 key」三个参数，逻辑收敛到本类。
 *
 * <h3>查找策略（按顺序，全部走完才新建）</h3>
 * <ol>
 *   <li><b>该维度记录的固定传送门</b>（玩家自己用雪flake 搭的那座，
 *       存在 {@link PortalSaveData}）—— 绝大多数情况命中于此，O(1)。</li>
 *   <li><b>目标点附近逐层外扩搜索</b>：16 → 64 → 128 格，每层找到即停。
 *       找到<b>最近</b>的门而不是第一个遇到的。</li>
 *   <li><b>已加载区块内搜索</b>：玩家手动在别处开的门也能被找回，
 *       不会强制加载新区块（{@code hasChunkAt} 过滤）。</li>
 *   <li>以上都没有才 <b>新建</b>一座，并写入 {@link PortalSaveData} 作为该维度的家。</li>
 * </ol>
 *
 * <p><b>关键修复</b>：旧实现是「16 格内没有就无条件新建」，
 * 玩家每走远一点就造一座新门 → <b>越用越多</b>。
 * 现在有了 1~3 级兜底，只在世界真的没有任何门时才新建。</p>
 */
public abstract class RealmTeleporter implements ITeleporter {

    /** 门内方块（实心那层）。 */
    protected abstract Block portalBlock();

    /** 框架方块。 */
    protected abstract Block frameBlock();

    /** 本 Teleporter 服务的<b>目标</b>维度。 */
    protected abstract ResourceKey<Level> targetDimension();

    protected final ServerLevel level;

    protected RealmTeleporter(ServerLevel level) {
        this.level = level;
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos destinationPos = locatePortal(destWorld, entity.blockPosition());
        if (destinationPos == null) {
            destinationPos = createPortal(destWorld, entity.blockPosition());
        }
        return new PortalInfo(
                new Vec3(destinationPos.getX() + 0.5D, destinationPos.getY(), destinationPos.getZ() + 0.5D),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    // ==================================================================================
    //  查找：四级兜底
    // ==================================================================================

    /** 逐层搜索半径，命中即停，避免一上来就 33³ 无谓遍历。 */
    private static final int[] SEARCH_RADII = {16, 64, 128};

    private BlockPos locatePortal(ServerLevel destWorld, BlockPos startPos) {
        // ① 该维度的固定传送门（玩家自己搭的那座）
        PortalSaveData data = PortalSaveData.get(destWorld);
        BlockPos home = data.getHome(destWorld.dimension().location().toString());
        if (home != null && isPortalBlock(destWorld, home)) {
            return safeSpawn(destWorld, home);
        }
        if (home != null) {
            // 记录的门被拆了 → 清掉记录，避免每次传送都白跑一趟
            data.clearHome(destWorld.dimension().location().toString());
        }

        // ② 附近逐层外扩，每层取**最近**的一座
        for (int radius : SEARCH_RADII) {
            BlockPos found = findNearest(destWorld, startPos, radius);
            if (found != null) return safeSpawn(destWorld, found);
        }

        // ③ 已加载区块内搜索（玩家在别处手动开的门）
        BlockPos inLoaded = findInLoadedChunks(destWorld, startPos);
        if (inLoaded != null) {
            data.setHome(destWorld.dimension().location().toString(), inLoaded);
            return safeSpawn(destWorld, inLoaded);
        }

        return null;
    }

    private boolean isPortalBlock(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(portalBlock());
    }

    /**
     * 在 {@code ±radius} 的立方体内找<b>离 startPos 最近</b>的门。
     * <p>Y 轴限制在 ±16，避免上下界/深层洞穴这种垂直跨度把搜索量放大。</p>
     */
    private BlockPos findNearest(ServerLevel level, BlockPos start, int radius) {
        BlockPos best = null;
        long bestDist = Long.MAX_VALUE;
        int ySpan = Math.min(radius, 16);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -ySpan; dy <= ySpan; dy++) {
                    BlockPos p = start.offset(dx, dy, dz);
                    if (!level.getBlockState(p).is(portalBlock())) continue;
                    long d = (long) dx * dx + (long) dy * dy + (long) dz * dz;
                    if (d < bestDist) {
                        bestDist = d;
                        best = p.immutable();
                    }
                }
            }
        }
        return best;
    }

    /**
     * 在<b>已加载</b>区块内找最近的门。
     * <p>用 {@code hasChunkAt} 过滤，绝不为了找门而强制加载新区块 ——
     * 否则玩家每次传送都可能触发一整片区块加载。</p>
     */
    private BlockPos findInLoadedChunks(ServerLevel level, BlockPos start) {
        int radius = 128;
        BlockPos best = null;
        long bestDist = Long.MAX_VALUE;
        int ySpan = 16;

        for (int dx = -radius; dx <= radius; dx += 16) {
            for (int dz = -radius; dz <= radius; dz += 16) {
                BlockPos p = start.offset(dx, 0, dz);
                if (!level.hasChunkAt(p)) continue;
                for (int dy = -ySpan; dy <= ySpan; dy++) {
                    BlockPos q = p.offset(0, dy, 0);
                    if (!level.getBlockState(q).is(portalBlock())) continue;
                    long d = (long) dx * dx + (long) dy * dy + (long) dz * dz;
                    if (d < bestDist) {
                        bestDist = d;
                        best = q.immutable();
                    }
                }
            }
        }
        return best;
    }

    /**
     * 门内站位向上抬到空位，避免玩家被塞进实心框架里卡住。
     */
    private BlockPos safeSpawn(ServerLevel level, BlockPos portalPos) {
        BlockPos p = portalPos;
        for (int i = 0; i < 3; i++) {
            BlockState state = level.getBlockState(p);
            if (state.isAir() && level.getBlockState(p.above()).isAir()) break;
            p = p.above();
        }
        return p;
    }

    // ==================================================================================
    //  新建：整个世界真的一个门都没有时才走这里
    // ==================================================================================

    protected BlockPos createPortal(ServerLevel destWorld, BlockPos pos) {
        int y = destWorld.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        if (y < 1) y = 70;
        BlockPos spawnPos = new BlockPos(pos.getX(), y, pos.getZ());

        // 4x5 结构：外圈框架，内径 2x3 门
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                BlockPos target = spawnPos.offset(dx, dy, 0);
                boolean isFrame = (dx == -1 || dx == 2 || dy == 0 || dy == 4);
                if (isFrame) {
                    destWorld.setBlockAndUpdate(target, frameBlock().defaultBlockState());
                } else {
                    destWorld.setBlockAndUpdate(target, portalBlock().defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X));
                }
            }
        }

        // ⭐ 记为该维度的固定传送门，否则玩家走远后又会造新的
        PortalSaveData.get(destWorld).setHome(destWorld.dimension().location().toString(),
                spawnPos.offset(1, 0, 0));
        return spawnPos.offset(0, 1, 0);
    }
}
