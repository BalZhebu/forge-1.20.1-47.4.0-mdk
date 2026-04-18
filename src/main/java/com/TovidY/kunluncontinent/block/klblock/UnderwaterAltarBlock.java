package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.blockentity.UnderwaterAltarTile;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.demon.DemonWhaleEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class UnderwaterAltarBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public UnderwaterAltarBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UnderwaterAltarTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.UNDERWATER_ALTAR_TILE.get(),
                UnderwaterAltarTile::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(ModItems.DEEP_SEA_OFFERINGS.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof UnderwaterAltarTile tile && !tile.isSummoning()) {
                if (!level.isClientSide) {
                    tile.startSummon();
                    clearSummonArea(level, pos);
                    if (!player.isCreative()) itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private void clearSummonArea(Level level, BlockPos center) {
        int radius = 12;
        BlockPos minPos = center.offset(-radius, -radius, -radius);
        BlockPos maxPos = center.offset(radius, radius, radius);
        for (BlockPos targetPos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (targetPos.equals(center)) continue;
            BlockState targetState = level.getBlockState(targetPos);
            if (!targetState.isAir() && targetState.getFluidState().isEmpty()) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}