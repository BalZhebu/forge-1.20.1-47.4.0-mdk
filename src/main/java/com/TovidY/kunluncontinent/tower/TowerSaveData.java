package com.TovidY.kunluncontinent.tower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class TowerSaveData extends SavedData {
    private boolean isGenerated = false;

    /**
     * 已经建成的塔数（塔 id 合法区间 {@code [0, builtTowers)}）。
     *
     * <p>必须存盘：预生成被中途打断（崩服/关服）时，重启后要从这里<b>续建</b>，
     * 否则会从 0 再来一遍 —— 等于把已经建好的塔用同一个坐标再盖一次。</p>
     */
    private int builtTowers = 0;

    // 读取硬盘上的标记
    public static TowerSaveData load(CompoundTag tag) {
        TowerSaveData data = new TowerSaveData();
        data.isGenerated = tag.getBoolean("is_tower_generated");
        data.builtTowers = tag.getInt("built_towers");
        return data;
    }

    // 将标记写入硬盘
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("is_tower_generated", this.isGenerated);
        tag.putInt("built_towers", this.builtTowers);
        return tag;
    }

    public boolean isGenerated() { return isGenerated; }
    public void setGenerated(boolean generated) {
        this.isGenerated = generated;
        this.setDirty(); // 标记数据已改变，提醒原版引擎存盘
    }

    public int getBuiltTowers() { return builtTowers; }
    public void setBuiltTowers(int built) {
        if (built == this.builtTowers) {
            return;
        }
        this.builtTowers = Math.max(0, built);
        this.setDirty();
    }

    /**
     * 获取当前维度的动态标记实例
     */
    public static TowerSaveData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TowerSaveData::load, TowerSaveData::new, "kunlun_tower_status");
    }
}
