package com.TovidY.kunluncontinent.tower.block;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SummonTowerBlock extends Block {
    public SummonTowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                if (attr.isTowerChallenging()) {
                    serverPlayer.sendSystemMessage(Component.literal("§c[昆仑大陆] 历练已经开启，请专心迎敌！"));
                    return;
                }
                attr.setTowerLastActiveTick(serverPlayer.server.getTickCount());
                attr.setTowerChallenging(true);

                int floor = attr.getCurrentTowerFloor();
                var floorData = TowerFloorRegistry.getFloorData(floor);
                NetworkHandler.sendToClient(new PacketSyncTowerTimer(floorData.timeLimitSeconds, true), serverPlayer);

                TowerSpawnerEngine.spawnFloorMonsters(serverPlayer, floor, pos);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}