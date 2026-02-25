package com.TovidY.kunluncontinent.block.klblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PutuanBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);

    public PutuanBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (!player.isPassenger()) {

                ArmorStand seat = new ArmorStand(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                byte flags = seat.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS);
                flags = (byte)(flags | 1);
                flags = (byte)(flags | 16);
                seat.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, flags);
                seat.setInvisible(true);
                seat.setNoGravity(true);
                seat.setInvulnerable(true);
                seat.addTag("putuan_seat");
                level.addFreshEntity(seat);
                player.startRiding(seat);
                player.sendSystemMessage(Component.literal("§e你盘膝而坐，开始感受天地灵气..."));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}