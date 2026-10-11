package com.TovidY.kunluncontinent.block.portal.polarice;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录每个维度里「玩家的传送门」坐标，即玩家手动用雪flake + 框架激活的那一座。
 *
 * <h3>为什么需要它</h3>
 * 原来的 {@code PolarIceTeleporter} / {@code ThunderRealmTeleporter} 是
 * 「在目标点 16 格内找传送门，找不到就<b>新建</b>」。而
 * {@code BlockPos.betweenClosed(±16,±16,±16)} 是 <b>33³ = 35937</b> 格的无序遍历 ——
 * 玩家在极冰界每走远一点，目标点附近就没有门了，于是又造一座新门；
 * 回头走时新门在身后，同样找不到，于是再造一座。
 * 结果就是<b>越用越多</b>，严重拖慢服务端。
 *
 * <p>本类把坐标按维度记下来：玩家在主世界手动搭的门，
 * 一整趟玩法（进门 → 传送 → 回来）<b>永远回到同一座门</b>，不会再生成新的。</p>
 *
 * <p>为什么不做成「每个玩家各记一个」：那会让同一座门被多个玩家共用/抢占，
 * 而且玩家自己都说不清楚「我在远处开的门算不算家」。按维度记一个「固定的家」
 * 语义最简单，也最符合直觉。</p>
 */
public class PortalSaveData extends SavedData {

    private static final String FILE_ID = "kunlun_portal_home";

    /** 维度 id → 该维度玩家的固定传送门坐标（未记录时 value 为 null）。 */
    private final Map<String, long[]> homePosByDim = new HashMap<>();

    public static PortalSaveData load(CompoundTag tag) {
        PortalSaveData data = new PortalSaveData();
        var compound = tag.getCompound("Homes");
        for (String dim : compound.getAllKeys()) {
            long[] arr = compound.getLongArray(dim);
            if (arr.length == 3) {
                data.homePosByDim.put(dim, arr);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag homes = new CompoundTag();
        for (Map.Entry<String, long[]> e : homePosByDim.entrySet()) {
            homes.putLongArray(e.getKey(), e.getValue());
        }
        tag.put("Homes", homes);
        return tag;
    }

    public static PortalSaveData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(PortalSaveData::load, PortalSaveData::new, FILE_ID);
    }

    /** 取该维度记录的固定传送门；没有记录返回 {@code null}。 */
    public BlockPos getHome(String dimensionId) {
        long[] arr = homePosByDim.get(dimensionId);
        return arr == null ? null : BlockPos.of(arr[0]);
    }

    /** 记录该维度的固定传送门。 */
    public void setHome(String dimensionId, BlockPos pos) {
        homePosByDim.put(dimensionId, new long[]{pos.asLong(), 0L, 0L});
        setDirty();
    }

    /** 清空该维度的记录（传送门被玩家拆除时用）。 */
    public void clearHome(String dimensionId) {
        if (homePosByDim.remove(dimensionId) != null) {
            setDirty();
        }
    }
}
