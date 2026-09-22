package com.TovidY.kunluncontinent.tower.block;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncTowerTimer;
import com.TovidY.kunluncontinent.tower.TowerSpawnerEngine;
import com.TovidY.kunluncontinent.tower.floor.TowerFloorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;


public class SummonTowerBlock extends Block {

    private static final int MAX_AVAILABLE_FLOOR = 14;

    public SummonTowerBlock(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE = Stream.of(
            Block.box(1.0D, 0.0D, 2.0D, 15.0D, 8.0D, 14.0D),
            Block.box(3.0D, 8.0D, 4.0D, 13.0D, 26.0D, 12.0D),
            Block.box(2.0D, 26.0D, 3.0D, 14.0D, 32.0D, 13.0D)
    ).reduce(Shapes::or).get();

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {

                int floor = attr.getCurrentTowerFloor();

                if ((floor + 1) > MAX_AVAILABLE_FLOOR) {
                    serverPlayer.sendSystemMessage(Component.literal("§6§l[昆仑大陆] §c无法进入，当前层数暂未开放（当前最高开放至第 " + MAX_AVAILABLE_FLOOR + " 层）！"));
                    return;
                }

                if (attr.isTowerChallenging()) {
                    serverPlayer.sendSystemMessage(Component.literal("§c[昆仑大陆] 历练已经开启，请专心迎敌！"));
                    return;
                }

                attr.setTowerLastActiveTick(serverPlayer.server.getTickCount());
                attr.setTowerChallenging(true);
                AchievementAPI.onTowerStart(serverPlayer);

                var floorData = TowerFloorRegistry.getFloorData(floor);
                NetworkHandler.sendToClient(new PacketSyncTowerTimer(floorData.timeLimitSeconds, true), serverPlayer);

                TowerSpawnerEngine.spawnFloorMonsters(serverPlayer, floor, pos);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}