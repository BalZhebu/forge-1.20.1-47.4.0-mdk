package com.TovidY.kunluncontinent.tower; // 请对齐你原本的包名

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TowerStateManager {
    // 【核心账本】记录每座塔当前是谁在挑战 (KEY: 塔ID, VALUE: 玩家的UUID)
    // 初始可能是 0-99，满了之后会自动追加 100, 101, 102...
    private static final Map<Integer, UUID> TOWER_OCCUPANCY = new HashMap<>();

    // 自定义一个当前服务器最大的塔总数变量，初始值对应独立服务器的 100
    private static int currentTotalTowers = TowerPreBuilder.getTotalTowers();

    /**
     * 为准备进入的玩家寻找并抢占一座空闲的塔
     * @return 返回分配的塔 ID（如果满了会自动现场扩建并返回新 ID）
     */
    public static synchronized int allocateFreeTower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        // 1. 防呆机制：如果这个修士已经在某座塔里了（比如掉线重连），直接送他回他原本占用的那座塔
        for (Map.Entry<Integer, UUID> entry : TOWER_OCCUPANCY.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                return entry.getKey();
            }
        }

        // 2. 动态扫描当前的公共池（i < currentTotalTowers）
        // 关键点：因为离开时会 removeIf，所以前人退出来的扩容塔（比如第 101 座）会在这里直接被后续其他人扫描并复用！
        for (int i = 0; i < currentTotalTowers; i++) {
            if (!TOWER_OCCUPANCY.containsKey(i)) {
                TOWER_OCCUPANCY.put(i, playerUUID); // 立刻登记锁定占领！
                System.out.println("[昆仑大陆] 分配成功：玩家 " + player.getName().getString() + " 抢占了现成的第 " + (i + 1) + " 号幻境塔");
                return i;
            }
        }

        ServerLevel towerLevel = player.serverLevel(); // 获取当前虚空维度的上下文
        int newTowerId = currentTotalTowers;

        System.out.println("[昆仑大陆] 警告：当前幻境负载！正在为玩家 " + player.getName().getString() + " 动态筑造第 " + (newTowerId + 1) + " 座幻境塔...");

        TowerPreBuilder.buildSingleTowerDirectly(towerLevel, newTowerId);

        currentTotalTowers++;

        TOWER_OCCUPANCY.put(newTowerId, playerUUID);
        System.out.println("[昆仑大陆] 扩容成功：玩家 " + player.getName().getString() + " 成功入驻新筑造的第 " + (newTowerId + 1) + " 号幻境塔");

        return newTowerId;
    }

    /**
     * 当玩家挑战成功离开、死亡或者退出游戏时，必须调用此方法释放塔的占用
     * 你原本的代码已经非常完美，不需要做任何变动！人走茶凉，塔自动变回公开状态！
     */
    public static synchronized void releaseTower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        TOWER_OCCUPANCY.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(playerUUID)) {
                System.out.println("[昆仑大陆] 释放成功：玩家 " + player.getName().getString() + " 离开了第 " + (entry.getKey() + 1) + " 号幻境塔，该塔已转为公开可用");
                return true;
            }
            return false;
        });
    }

    public static synchronized void clearAllOccupancy() {
        TOWER_OCCUPANCY.clear();
    }

}