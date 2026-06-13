package com.TovidY.kunluncontinent.tower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TowerStateManager {
    // 【核心账本】记录每座塔当前是谁在挑战 (KEY: 塔ID 0-99, VALUE: 玩家的UUID)
    private static final Map<Integer, UUID> TOWER_OCCUPANCY = new HashMap<>();

    /**
     * 为准备进入的玩家寻找并抢占一座空闲的塔
     * @return 如果找到空闲塔，返回 0-99 的 ID；如果满员，返回 -1
     */
    public static synchronized int allocateFreeTower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        // 防呆机制：如果这个修士已经在某座塔里了（比如掉线重连），直接送他回他原本占用的那座塔
        for (Map.Entry<Integer, UUID> entry : TOWER_OCCUPANCY.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                return entry.getKey();
            }
        }

        // 线性扫描 100 座塔，寻找绝对空闲的道场
        for (int i = 0; i < 100; i++) {
            if (!TOWER_OCCUPANCY.containsKey(i)) {
                TOWER_OCCUPANCY.put(i, playerUUID); // 立刻登记锁定占领！
                System.out.println("[昆仑大陆] 分配成功：玩家 " + player.getName().getString() + " 抢占了第 " + (i + 1) + " 号幻境塔");
                return i;
            }
        }

        return -1; // 100座全满了
    }

    /**
     * 当玩家挑战成功离开、死亡或者退出游戏时，必须调用此方法释放塔的占用
     */
    public static synchronized void releaseTower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        // 遍历账本，把这个玩家名下的塔全部清空腾出来
        TOWER_OCCUPANCY.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(playerUUID)) {
                System.out.println("[昆仑大陆] 释放成功：玩家 " + player.getName().getString() + " 离开了第 " + (entry.getKey() + 1) + " 号幻境塔");
                return true;
            }
            return false;
        });
    }

    /**
     * 专属清理逻辑：只斩杀属于当前玩家的幻境怪物
     */


    /**
     * 调试/强制重置账本使用（比如开服或重置时调用）
     */
    public static synchronized void clearAllOccupancy() {
        TOWER_OCCUPANCY.clear();
    }
}