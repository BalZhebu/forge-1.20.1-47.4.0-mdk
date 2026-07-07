package com.TovidY.kunluncontinent.tower;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.server.PacketSyncTowerTimer;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.tower.floor.TowerFloorRegistry;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//幻境法则
@Mod.EventBusSubscriber(modid = "kunluncontinent", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TowerRestrictionHandler {

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
                Vec3 movement = player.getDeltaMovement();
                player.setDeltaMovement(movement.x, 0.0D, movement.z);
            }
        }
    }

    /**
     * 拦截右键互动（挂有神之凝视时：禁止右键方块、空气、实体）
     */
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
            event.setCanceled(true);
        }
    }

    /**
     * 拦截左键破坏和左键打人（可以安全取消）
     */
    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity().hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension().equals(ModDimensions.TOWER_REALM_LEVEL_KEY)) {
            if (event.getPlayer() instanceof ServerPlayer player && !player.isCreative()) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c[幻境法则] 这里的乾坤受古阵保护，不可损毁！"), true);
            }
        }
    }

    /**
     * 2. 禁止放置方块
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension().equals(ModDimensions.TOWER_REALM_LEVEL_KEY)) {
            Entity entity = event.getEntity();
            if (entity instanceof ServerPlayer player && !player.isCreative()) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c[幻境法则] 此地禁止造作，请凭真本事登楼！"), true);
            }
        }
    }

    /**
     * 3. 实时检测：越界判定、发呆挂机与时限计时器一网打尽
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        if (event.player instanceof ServerPlayer player) {

            // 【融合新增功能】：在 tick 中双重拦截跳跃状态（防止某些连跳 Mod 绕过 JumpEvent）
            if (player.hasEffect(ModEffects.THE_GAZE_OF_GOD.get())) {
                if (player.getDeltaMovement().y > 0) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                }
            }

            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                if (attr.isTowerChallenging()) {
                    long currentTick = player.server.getTickCount();

                    var floorData = TowerFloorRegistry.getFloorData(attr.getCurrentTowerFloor());
                    int maxSeconds = floorData.timeLimitSeconds;

                    long challengeTicks = currentTick - attr.getTowerLastActiveTick();
                    int elapsedSeconds = (int) (challengeTicks / 20);
                    int remainingSeconds = maxSeconds - elapsedSeconds;

                    if (currentTick % 20 == 0) {
                        NetworkHandler.sendToClient(new PacketSyncTowerTimer(Math.max(0, remainingSeconds), true), player);
                    }

                    if (remainingSeconds <= 0) {
                        attr.setTowerChallenging(false);
                        attr.setTowerLastActiveTick(0);

                        int currentFloor = attr.getCurrentTowerFloor();
                        attr.setCurrentTowerFloor(Math.max(0, currentFloor - 1));

                        player.sendSystemMessage(Component.literal("§4[幻境法则] 时限已到，你未能历练成功！历练失败！"));

                        NetworkHandler.sendToClient(new PacketSyncTowerTimer(0, false), player);

                        TowerSpawnerEngine.clearTowerMonstersForPlayer(player);
                        returnPlayerToSpawn(player);
                        return;
                    }
                }
            });

            ServerLevel level = player.serverLevel();
            if (level.dimension().equals(ModDimensions.TOWER_REALM_LEVEL_KEY)) {
                if (player.isCreative() || player.isSpectator()) return;

                double x = player.getX();
                double z = player.getZ();
                int towerId = (int) Math.round(x / 300.0);
                double centerX = towerId * 300.0 + 15.0;
                double centerZ = 15.0;
                double distanceSq = Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2);

                if (distanceSq > Math.pow(18, 2) || player.getY() < 50) {
                    player.sendSystemMessage(Component.literal("§4[幻境法则] 你已跌出幻境范围，历练强行失败！"));
                    player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                        attr.setTowerChallenging(false);
                        attr.setTowerLastActiveTick(0);
                        TowerSpawnerEngine.clearTowerMonstersForPlayer(player);
                    });
                    NetworkHandler.sendToClient(new PacketSyncTowerTimer(0, false), player);
                    returnPlayerToSpawn(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                if (attr.isTowerChallenging()) {
                    attr.setTowerChallenging(false);
                    attr.setTowerLastActiveTick(0);
                    int currentFloor = attr.getCurrentTowerFloor();
                    attr.setCurrentTowerFloor(Math.max(0, currentFloor - 1));
                    player.sendSystemMessage(Component.literal("§4[幻境法则] 检测到你上次非正常离开幻境，阵法反噬，历练强行失败！"));
                    NetworkHandler.sendToClient(new PacketSyncTowerTimer(0, false), player);
                    returnPlayerToSpawn(player);
                    SynsAPI.synsPlayerAttribute(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                if (attr.isTowerChallenging()) {
                    TowerStateManager.releaseTower(player);
                    TowerSpawnerEngine.clearTowerMonstersForPlayer(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getFrom().equals(ModDimensions.TOWER_REALM_LEVEL_KEY) && event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                attr.setTowerChallenging(false);
                attr.setTowerLastActiveTick(0);
            });
            NetworkHandler.sendToClient(new PacketSyncTowerTimer(0, false), player);
        }
    }

    /**
     * 5. 禁止丢弃物品（已升级：爬塔维度中 或者是 挂了神之凝视 Buff 均不可丢弃物品）
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            boolean inTowerRealm = player.level().dimension().equals(ModDimensions.TOWER_REALM_LEVEL_KEY);
            boolean hasGazeEffect = player.hasEffect(ModEffects.THE_GAZE_OF_GOD.get());

            if ((inTowerRealm || hasGazeEffect) && !player.isCreative()) {
                event.setCanceled(true);
                if (!player.getInventory().add(event.getEntity().getItem())) {
                    player.drop(event.getEntity().getItem(), false);
                }

                if (hasGazeEffect) {
                    player.sendSystemMessage(Component.literal("§c[神之凝视] 身体已被神威锁死，无法丢弃物品！"), true);
                } else {
                    player.sendSystemMessage(Component.literal("§c[幻境法则] 历练重地，不可乱丢杂物，专心应敌！"), true);
                }
            }
        }
    }

    /**
     * 安全把玩家踢回他的大世界重生点
     */
    public static void returnPlayerToSpawn(ServerPlayer player) {
        TowerStateManager.releaseTower(player);
        ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());
        if (respawnLevel == null) respawnLevel = player.server.overworld();
        BlockPos spawnPos = player.getRespawnPosition();
        if (spawnPos == null) spawnPos = respawnLevel.getSharedSpawnPos();

        player.teleportTo(
                respawnLevel,
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );
    }
}