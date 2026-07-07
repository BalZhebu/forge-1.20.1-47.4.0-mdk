package com.TovidY.kunluncontinent.tower.block;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.tower.TowerPreBuilder;
import com.TovidY.kunluncontinent.tower.TowerStateManager;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class StoneStampBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public StoneStampBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    protected static final VoxelShape NORTH_SOUTH_SHAPE = Stream.of(
            Block.box(0.0D, 0.0D, 4.0D, 16.0D, 6.0D, 12.0D),
            Block.box(1.0D, 6.0D, 6.0D, 15.0D, 30.0D, 10.0D),
            Block.box(2.0D, 30.0D, 6.0D, 14.0D, 32.0D, 10.0D)
    ).reduce(Shapes::or).get();

    protected static final VoxelShape EAST_WEST_SHAPE = Stream.of(
            Block.box(4.0D, 0.0D, 0.0D, 12.0D, 6.0D, 16.0D),
            Block.box(6.0D, 6.0D, 1.0D, 10.0D, 30.0D, 15.0D),
            Block.box(6.0D, 30.0D, 2.0D, 10.0D, 32.0D, 14.0D)
    ).reduce(Shapes::or).get();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.EAST || facing == Direction.WEST) {
            return EAST_WEST_SHAPE;
        }
        return NORTH_SOUTH_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = level.getServer();
            if (server == null) return InteractionResult.SUCCESS;

            ResourceKey<Level> currentDimension = level.dimension();

            if (currentDimension.equals(ModDimensions.TOWER_REALM_LEVEL_KEY)) {
                serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    if (attr.isTowerChallenging()) {
                        attr.setTowerChallenging(false);
                        int currentFloor = attr.getCurrentTowerFloor();
                        attr.setCurrentTowerFloor(Math.max(0, currentFloor - 1));
                        serverPlayer.sendSystemMessage(Component.literal("§c[幻境法则] 临阵脱逃，道心受损！历练判定失败，层数出现跌落！"));
                    }
                    attr.setTowerLastActiveTick(0);
                });
                // ===================================

                ResourceKey<Level> respawnDim = serverPlayer.getRespawnDimension();
                ServerLevel respawnLevel = server.getLevel(respawnDim != null ? respawnDim : Level.OVERWORLD);
                if (respawnLevel == null) {
                    respawnLevel = server.overworld();
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
                        serverPlayer.sendSystemMessage(Component.literal("§e[昆仑大陆] 你的出生点已失效，正在遣送回初始出生点..."));
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
                    serverPlayer.sendSystemMessage(Component.literal("§c[昆仑大陆] 警告：无法加载爬塔虚空维度，请联系作者！"));
                    TowerStateManager.releaseTower(serverPlayer);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

}