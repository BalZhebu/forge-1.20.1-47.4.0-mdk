package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.blockentity.LiandanluBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiandanluBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private final int tier;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 5, 16),         // 底部脚座
            Block.box(-2, 5, -1, 18, 16, 17),       // 中间炉体 (包含侧边扩展)
            Block.box(-7, 10, 7, 23, 16, 9),        // 左右突出最长的炉耳/手柄 (-7 ~ 23)
            Block.box(1, 16, 1, 15, 21, 15),        // 盖子下层
            Block.box(4, 21, 4, 12, 31, 12)         // 盖子顶纽
    );

    // 旋转计算生成其他 3 个方向
    private static final VoxelShape SHAPE_EAST = calculateRotation(SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = calculateRotation(SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = calculateRotation(SHAPE_NORTH, Direction.WEST);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.getValue(FACING);
        return switch (facing) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return this.getShape(pState, pLevel, pPos, pContext);
    }


    /**
     * 辅助工具：将 VoxelShape 绕 Y 轴旋转到指定方向
     */
    private static VoxelShape calculateRotation(VoxelShape src, Direction to) {
        VoxelShape[] buffer = new VoxelShape[]{src, Shapes.empty()};
        int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;

        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                buffer[1] = Shapes.joinUnoptimized(
                        buffer[1],
                        Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX),
                        BooleanOp.OR
                );
            });
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0].optimize();
    }

    public LiandanluBlock(Properties pProperties, int tier) {
        super(pProperties.noCollission().noOcclusion());
        this.tier = tier;
    }

    public int getTier() {
        return this.tier;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new LiandanluBlockEntity(pPos, pState, this.tier);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof LiandanluBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, (MenuProvider) entity, pPos);
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.LIANDANLU_BE.get(), LiandanluBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof LiandanluBlockEntity be) {
                be.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), handler.getStackInSlot(i));
                    }
                });
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(Component.translatable("tooltip.kunluncontinent.liandanlu").withStyle(ChatFormatting.DARK_GRAY));
    }
}
