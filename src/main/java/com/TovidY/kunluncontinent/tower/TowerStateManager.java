package com.TovidY.kunluncontinent.tower;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 幻境塔的<b>占用账本</b>：记录每一座塔当前被哪位玩家占用。
 *
 * <h3>这次改了什么</h3>
 * <ul>
 *   <li><b>不再缓存"总塔数"</b>：原来 {@code currentTotalTowers} 在类初始化时读一次
 *       {@code TowerPreBuilder.getTotalTowers()} 就固定了。而预生成实际只建
 *       10（客户端）/ 50（服务器）座 —— 如果账本先于预生成器初始化（例如玩家在大世界
 *       就按下了石印），它就会以为有 100 座塔，把玩家分到<b>根本没建过的空塔</b>里，
 *       传送过去直接掉虚空。现在每次分配都现读 {@link TowerPreBuilder#getBuiltTowers()}，
 *       只扫"确实已经建好"的区间。</li>
 *   <li><b>扩容建塔建到塔维度去</b>：原来用 {@code player.serverLevel()}（= 玩家当前所在维度，
 *       通常是大世界）当目标维度，池子满了就会把一座塔<b>盖在大世界的 x=towerId*300 处</b>。
 *       现在固定取塔维度。</li>
 *   <li>日志从 {@code System.out} 换成 {@link KlMain#LOGGER}。</li>
 * </ul>
 *
 * <p>分配/释放都加了 {@code synchronized}（与原实现一致）：账本被玩家进塔、死亡、
 * 换维度多条路径同时动，虽然都在主线程，但保持串行更不容易出错。</p>
 */
public final class TowerStateManager {

    /** 每座塔当前是谁在挑战（KEY: 塔 id，VALUE: 玩家 UUID）。 */
    private static final Map<Integer, UUID> TOWER_OCCUPANCY = new HashMap<>();

    private TowerStateManager() {
    }

    /**
     * 为准备进入的玩家挑一座空闲塔并立刻占用。
     *
     * @return 塔 id；<b>-1</b> 表示现在一座都进不去（还在预生成 / 塔维度没加载）
     */
    public static synchronized int allocateFreeTower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();

        // 1. 防呆：已经在某座塔里的（掉线重连等）直接回原塔
        for (Map.Entry<Integer, UUID> entry : TOWER_OCCUPANCY.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                return entry.getKey();
            }
        }

        // 2. 只在【已建成】的区间里找空位 —— 未建好的塔分出去就是掉虚空
        int built = TowerPreBuilder.getBuiltTowers();
        for (int i = 0; i < built; i++) {
            if (!TOWER_OCCUPANCY.containsKey(i)) {
                TOWER_OCCUPANCY.put(i, playerUUID);
                KlMain.LOGGER.info("[昆仑大陆] 幻境塔分配：{} → 第 {} 座", player.getName().getString(), i + 1);
                return i;
            }
        }

        // 3. 池子满了：预生成还没跑完就先别给（否则给的是空塔）
        if (TowerPreBuilder.isGenerating()) {
            KlMain.LOGGER.info("[昆仑大陆] 幻境塔分配失败：{} 想进塔，但预生成尚未完成（已建 {} 座）",
                    player.getName().getString(), built);
            return -1;
        }

        // 4. 现场扩容：⚠️ 目标维度必须是【塔维度】，不是玩家当前所在的大世界
        ServerLevel towerLevel = player.server.getLevel(ModDimensions.TOWER_REALM_LEVEL_KEY);
        if (towerLevel == null) {
            KlMain.LOGGER.error("[昆仑大陆] 幻境塔分配失败：塔维度未加载，无法为 {} 扩建",
                    player.getName().getString());
            return -1;
        }

        int newTowerId = built;
        KlMain.LOGGER.info("[昆仑大陆] 幻境塔已满（{} 座），为 {} 现场扩建第 {} 座",
                built, player.getName().getString(), newTowerId + 1);

        TowerPreBuilder.buildSingleTowerDirectly(towerLevel, newTowerId);
        TOWER_OCCUPANCY.put(newTowerId, playerUUID);
        return newTowerId;
    }

    /**
     * 玩家离开（挑战完成 / 死亡 / 退出）时释放他的塔。
     *
     * <p>人走茶凉：塔立刻回到公共池，后面的人可以复用。</p>
     */
    public static synchronized void releaseTower(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        TOWER_OCCUPANCY.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(playerUUID)) {
                KlMain.LOGGER.info("[昆仑大陆] 幻境塔释放：{} 离开第 {} 座，该塔转为空闲",
                        player.getName().getString(), entry.getKey() + 1);
                return true;
            }
            return false;
        });
    }

    /** 清空所有占用（服务器关服/重载时用）。 */
    public static synchronized void clearAllOccupancy() {
        TOWER_OCCUPANCY.clear();
    }
}
