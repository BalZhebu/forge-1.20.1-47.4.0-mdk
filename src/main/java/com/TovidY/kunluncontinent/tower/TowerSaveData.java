package com.TovidY.kunluncontinent.tower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class TowerSaveData extends SavedData {
    private boolean isGenerated = false;

    // 读取硬盘上的标记
    public static TowerSaveData load(CompoundTag tag) {
        TowerSaveData data = new TowerSaveData();
        data.isGenerated = tag.getBoolean("is_tower_generated");
        return data;
    }

    // 将标记写入硬盘
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("is_tower_generated", this.isGenerated);
        return tag;
    }

    public boolean isGenerated() { return isGenerated; }
    public void setGenerated(boolean generated) {
        this.isGenerated = generated;
        this.setDirty(); // 标记数据已改变，提醒原版引擎存盘
    }

    /**
     * 获取当前维度的动态标记实例
     */
    public static TowerSaveData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TowerSaveData::load, TowerSaveData::new, "kunlun_tower_status");
    }
}
