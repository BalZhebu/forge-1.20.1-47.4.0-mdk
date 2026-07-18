package com.TovidY.kunluncontinent.tower;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kunluncontinent")
public class TowerPreBuilder {

    private static boolean isGenerating = false;
    private static int currentTargetTower = 0;

    private static int TOTAL_TOWERS = 100;

    private static ServerLevel targetLevel = null;
    private static int tickCounter = 0;

    /**
     * 监听世界加载事件：当服务器启动，加载到我们的爬塔维度时触发检查
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension().equals(com.TovidY.kunluncontinent.worldgen.ModDimensions.TOWER_REALM_LEVEL_KEY)) {
                // 检查硬盘标记：如果没有生成过，立刻启动渐进式生成！
                if (!TowerSaveData.get(serverLevel).isGenerated()) {
                    startGeneration(serverLevel);
                }
            }
        }
    }

    private static void startGeneration(ServerLevel level) {
        if (isGenerating) return;
        targetLevel = level;
        isGenerating = true;
        currentTargetTower = 0;
        tickCounter = 0;

        // ==================== 【全新核心：环境与性能优化拦截】 ====================
        // FMLEnvironment.dist 会返回当前的物理运行环境：
        // - net.minecraftforge.api.distmarker.Dist.DEDICATED_SERVER: 纯服务端核心（开服包）
        // - net.minecraftforge.api.distmarker.Dist.CLIENT: 客户端（单人游戏或局域网联机）
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            TOTAL_TOWERS = 10; // 客户端/局域网：只生成 10 座塔，大幅度减轻卡顿与硬盘开销
            System.out.println("[昆仑大陆] 检测到当前运行于【客户端内置环境】，幻境塔总数缩减至: 10座");
        } else {
            TOTAL_TOWERS = 50; // 独立服务器：直接拉满 100 座
            System.out.println("[昆仑大陆] 检测到当前运行于【独立服务器环境】，开启全量筑造，幻境塔总数: 50座");
        }
        // =====================================================================

        System.out.println("[昆仑大陆] === 检测到首次创世，开启【渐进式解压】预生成机制 ===");
        System.out.println("[昆仑大陆] " + TOTAL_TOWERS + "座幻境塔开始按计划筑造，请稍候...");
    }

    /**
     * 监听服务器心跳
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!isGenerating || event.phase != TickEvent.Phase.END || targetLevel == null) {
            return;
        }

        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;

            buildSingleTower(currentTargetTower);
            currentTargetTower++;

            int progress = (int) (((float) currentTargetTower / TOTAL_TOWERS) * 100);
            String progressMsg = "§6[昆仑大陆] §f正在筑造幻境乾坤（游戏卡顿为正常现象）... §a" + progress + "% " + getProgressBar(progress    );
            event.getServer().getPlayerList().getPlayers().forEach(player -> {
                player.displayClientMessage(Component.literal(progressMsg), true);
            });
            System.out.println("[昆仑大陆] 幻境塔物理建造中... 当前进度: " + progress + "% (" + currentTargetTower + "/" + TOTAL_TOWERS + ")");

            if (currentTargetTower % 10 == 0) {
                System.out.println("[昆仑大陆] 正在强行释放内存缓存并写入磁盘...");
                targetLevel.save(null, true, false);
            }
            System.out.println("[昆仑大陆] 幻境塔物理建造中... 当前进度: " + progress + "% (" + currentTargetTower + "/" + TOTAL_TOWERS + ")");

            if (currentTargetTower >= TOTAL_TOWERS) {
                isGenerating = false;
                TowerSaveData.get(targetLevel).setGenerated(true);
                targetLevel.save(null, true, false);

                event.getServer().getPlayerList().getPlayers().forEach(player -> {
                    player.sendSystemMessage(Component.literal("§b[昆仑大陆] === 幻境全部生成完毕，游戏空间通道已稳定！ ==="));
                });

                System.out.println("[昆仑大陆] === " + TOTAL_TOWERS + "座幻境塔全量安全预建完毕！ ===");
                targetLevel = null;
            }
        }
    }

    /**
     * 辅助方法：生成一个简易的可视化进度条 [■■□□□]
     */
    private static String getProgressBar(int progress) {
        int totalBars = 10;
        int filledBars = progress / 10;
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) bar.append("§a■");
            else bar.append("§7□");
        }
        bar.append("§8]");
        return bar.toString();
    }

    /**
     * 核心：读取并放置你的 tower.nbt 结构
     */
    private static void buildSingleTower(int towerId) {
        BlockPos centerPos = new BlockPos(towerId * 300, 64, 0);
        ChunkPos chunkPos = new ChunkPos(centerPos);

        try {
            targetLevel.setChunkForced(chunkPos.x, chunkPos.z, true);
            StructureTemplateManager templateManager = targetLevel.getStructureManager();
            ResourceLocation structuresLoc = new ResourceLocation("kunluncontinent", "tower");
            Optional<StructureTemplate> templateOpt = templateManager.get(structuresLoc);

            if (templateOpt.isPresent()) {
                StructureTemplate template = templateOpt.get();

                StructurePlaceSettings settings = new StructurePlaceSettings()
                        .setRotation(Rotation.NONE)
                        .setMirror(Mirror.NONE)
                        .setIgnoreEntities(false);

                template.placeInWorld(targetLevel, centerPos, centerPos, settings, targetLevel.random, 2);
            } else {
                System.err.println("[昆仑大陆] 严重错误：请联系作者！");
            }

        } catch (Exception e) {
            System.err.println("[昆仑大陆] 警告：第 " + towerId + " 座塔生成时遭遇未知异常: " + e.getMessage());
        } finally {
            targetLevel.setChunkForced(chunkPos.x, chunkPos.z, false);
        }
    }

    /**
     * 外部扩容专用：无视心跳和渐进式机制，直接现场立刻盖一座塔
     */
    public static void buildSingleTowerDirectly(ServerLevel level, int towerId) {
        // 临时把外部传入的维度关卡赋予上下文，供 buildSingleTower 内部使用
        ServerLevel prevLevel = targetLevel;
        targetLevel = level;

        try {
            buildSingleTower(towerId);
            targetLevel.save(null, true, false);
        } catch (Exception e) {
            System.err.println("[昆仑大陆] 动态扩容塔 " + towerId + " 失败: " + e.getMessage());
        } finally {
            targetLevel = prevLevel;
        }
    }
    /**
     * 获取当前系统记录的最大塔总数（让账本类可以知道初始是 15 还是 100）
     */
    public static int getTotalTowers() {
        return TOTAL_TOWERS;
    }

    // 外部接口：如果正在生成中，禁止玩家挑战或传送
    public static boolean isGenerating() {
        return isGenerating;
    }

}