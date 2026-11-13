package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.blockentity.UnderwaterAltarTile;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
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

    /** 清场半径（水平，格）。 */
    private static final int CLEAR_RADIUS = 12;
    /** 清场高度（只往上，格）。 */
    private static final int CLEAR_HEIGHT = 12;

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
        // ⚠️ 只清「祭坛这一格及以上」，绝不动下面：
        //    原来是 ±12 的立方体，连祭坛脚下的海床 / 基座一起挖掉 ——
        //    ① BOSS 生成在 y+3，脚下先被挖空，容易掉进自己挖的坑里卡死；
        //    ② 会把海底地形整片掏塌。现在只在祭坛上方开一个"穹顶"给它活动。
        BlockPos minPos = center.offset(-CLEAR_RADIUS, 0, -CLEAR_RADIUS);
        BlockPos maxPos = center.offset(CLEAR_RADIUS, CLEAR_HEIGHT, CLEAR_RADIUS);
        for (BlockPos targetPos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (targetPos.equals(center)) continue;
            BlockState targetState = level.getBlockState(targetPos);
            if (!targetState.isAir() && targetState.getFluidState().isEmpty()) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}