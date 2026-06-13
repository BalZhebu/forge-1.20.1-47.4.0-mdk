package com.TovidY.kunluncontinent.tower.block;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.tower.TowerPreBuilder;
import com.TovidY.kunluncontinent.tower.TowerStateManager;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class StoneStampBlock extends Block {
    public StoneStampBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = level.getServer();
            if (server == null) return InteractionResult.SUCCESS;

            ResourceKey<Level> currentDimension = level.dimension();

            if (currentDimension.equals(ModDimensions.TOWER_REALM_LEVEL_KEY)) {

                // === 【新增：挑战中离场判定失败】 ===
                serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    if (attr.isTowerChallenging()) {
                        attr.setTowerChallenging(false);
                        int currentFloor = attr.getCurrentTowerFloor();
                        attr.setCurrentTowerFloor(Math.max(0, currentFloor - 1));
                        serverPlayer.sendSystemMessage(Component.literal("§c[幻境法则] 临阵脱逃，道心受损！历练判定失败，层数出现跌落！"));

                        // TODO: 如果你后续有清理全场怪物的逻辑，也可以在这里调用：
                        // TowerStateManager.clearTowerMonsters(serverPlayer);
                    }
                    attr.setTowerLastActiveTick(0);
                });
                // ===================================

                ResourceKey<Level> respawnDim = serverPlayer.getRespawnDimension();
                ServerLevel respawnLevel = server.getLevel(respawnDim != null ? respawnDim : Level.OVERWORLD);
                if (respawnLevel == null) {
                    respawnLevel = server.overworld(); // 绝对安全的兜底：直接抓原版主世界
                }
                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                double targetX, targetY, targetZ;
                float targetAngle = serverPlayer.getRespawnAngle();

                if (respawnPos != null) {
                    java.util.Optional<net.minecraft.world.phys.Vec3> safePos = Player.findRespawnPositionAndUseSpawnBlock(
                            respawnLevel, respawnPos, targetAngle, serverPlayer.isRespawnForced(), false
                    );
                    if (safePos.isPresent()) {
                        targetX = safePos.get().x;
                        targetY = safePos.get().y;
                        targetZ = safePos.get().z;
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("§e[昆仑大陆] 你的本命法脉（床）已失效，正在遣送回初始出生点..."));
                        BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                        targetX = sharedSpawn.getX() + 0.5;
                        targetY = sharedSpawn.getY();
                        targetZ = sharedSpawn.getZ() + 0.5;
                    }
                } else {
                    BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                    targetX = sharedSpawn.getX() + 0.5;
                    targetY = sharedSpawn.getY();
                    targetZ = sharedSpawn.getZ() + 0.5;
                }
                serverPlayer.sendSystemMessage(Component.literal("§b[昆仑大陆] 幻境历练结束，心神回归大世界..."));
                TowerStateManager.releaseTower(serverPlayer);
                serverPlayer.teleportTo(
                        respawnLevel,
                        targetX, targetY, targetZ,
                        targetAngle,
                        serverPlayer.getXRot()
                );
            } else {
                if (TowerPreBuilder.isGenerating()) {
                    serverPlayer.sendSystemMessage(Component.literal("§c[昆仑大陆] 幻境乾坤未稳，空间正在筑造中，请稍后再试..."));
                    return InteractionResult.SUCCESS;
                }

                int allocatedId = TowerStateManager.allocateFreeTower(serverPlayer);

                if (allocatedId == -1) {
                    serverPlayer.sendSystemMessage(Component.literal("§e[昆仑大陆] 当前幻境承载魂师过多，需稍后再试..."));
                    return InteractionResult.SUCCESS;
                }

                ServerLevel towerLevel = server.getLevel(ModDimensions.TOWER_REALM_LEVEL_KEY);
                if (towerLevel != null) {
                    double structureStartX = allocatedId * 300.0;
                    double structureStartZ = 0.0;
                    double destX = structureStartX + 15.0 + 0.5;
                    double destY = 67.0;
                    double destZ = structureStartZ + 12.0 + 0.5;

                    serverPlayer.sendSystemMessage(Component.literal("§a[昆仑大陆] 冥冥中感应到第 " + (allocatedId + 1) + " 个位面幻境，正在向道场接引..."));

                    serverPlayer.teleportTo(
                            towerLevel,
                            destX, destY, destZ,
                            0.0F,
                            serverPlayer.getXRot()
                    );
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("§c[昆仑大陆] 错误：无法加载爬塔虚空维度，请检查世界配置！"));
                    TowerStateManager.releaseTower(serverPlayer);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}