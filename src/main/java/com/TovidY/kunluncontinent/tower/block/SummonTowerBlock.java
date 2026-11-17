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

    /**
     * 开放层数锁定值。
     *
     * <p><b>0 = 不锁定</b>，跟随 {@link TowerFloorRegistry#getMaxAvailableFloor()}
     * （第 101 层起由公式无限延伸，所以默认不设终点）；
     * 想把进度锁在某一层（比如先只放到第 20 层）就把这里改成具体数字。</p>
     */
    private static final int LOCKED_MAX_FLOOR = 0;

    private static int maxAvailableFloor() {
        return LOCKED_MAX_FLOOR > 0 ? LOCKED_MAX_FLOOR : TowerFloorRegistry.getMaxAvailableFloor();
    }

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
                int maxFloor = maxAvailableFloor();

                if ((floor + 1) > maxFloor) {
                    serverPlayer.sendSystemMessage(Component.literal("§6§l[昆仑大陆] §c无法进入，当前层数暂未开放（当前最高开放至第 " + maxFloor + " 层）！"));
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