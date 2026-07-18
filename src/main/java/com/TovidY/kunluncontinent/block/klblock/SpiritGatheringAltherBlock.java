package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.blockentity.SpiritGatheringAltherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class SpiritGatheringAltherBlock extends BaseEntityBlock {

    protected static final VoxelShape SHAPE = Stream.of(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D),
            Block.box(3.0D, 3.0D, 3.0D, 13.0D, 5.0D, 13.0D),
            Block.box(4.0D, 5.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(4.0D, 6.0D, 4.0D, 12.0D, 7.0D, 12.0D),
            Block.box(4.0D, 7.0D, 4.0D, 5.0D, 11.0D, 5.0D),
            Block.box(11.0D, 7.0D, 4.0D, 12.0D, 11.0D, 5.0D),
            Block.box(11.0D, 7.0D, 11.0D, 12.0D, 11.0D, 12.0D),
            Block.box(4.0D, 7.0D, 11.0D, 5.0D, 11.0D, 12.0D),
            Block.box(3.0D, 11.0D, 11.0D, 13.0D, 12.0D, 12.0D),
            Block.box(3.0D, 11.0D, 4.0D, 13.0D, 12.0D, 5.0D),
            Block.box(4.0D, 11.0D, 12.0D, 5.0D, 12.0D, 13.0D),
            Block.box(11.0D, 11.0D, 12.0D, 12.0D, 12.0D, 13.0D),
            Block.box(11.0D, 11.0D, 3.0D, 12.0D, 12.0D, 4.0D),
            Block.box(4.0D, 11.0D, 3.0D, 5.0D, 12.0D, 4.0D),
            Block.box(4.0D, 11.0D, 5.0D, 5.0D, 12.0D, 11.0D),
            Block.box(11.0D, 11.0D, 5.0D, 12.0D, 12.0D, 11.0D),
            Block.box(4.0D, 12.0D, 4.0D, 5.0D, 13.0D, 5.0D),
            Block.box(4.0D, 12.0D, 11.0D, 5.0D, 13.0D, 12.0D),
            Block.box(11.0D, 12.0D, 11.0D, 12.0D, 13.0D, 12.0D),
            Block.box(11.0D, 12.0D, 4.0D, 12.0D, 13.0D, 5.0D)
    ).reduce(Shapes::or).get();

    public SpiritGatheringAltherBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 确保这里的 BlockEntity 类名和你注册的对应
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SPIRIT_GATHERING_ALTAR_BE.get(),
                SpiritGatheringAltherBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        int radius = 3;

        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                if (x != 0 || z != 0) {
                    if (random.nextInt(16) == 0) {
                        for (int y = 0; y <= 3; ++y) {
                            BlockPos stonePos = pos.offset(x, y, z);
                            BlockState stoneState = level.getBlockState(stonePos);

                            if (stoneState.getBlock() instanceof SpiritGatheringStoneBlock stoneBlock) {
                                // 这里正确调用了你强调的 stoneBlock.getTier()
                                int stoneLevel = stoneBlock.getTier();

                                double targetX = pos.getX() + 0.5D;
                                double targetY = pos.getY() + 0.6D;
                                double targetZ = pos.getZ() + 0.5D;

                                double offsetX = (stonePos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D) - targetX;
                                double offsetY = (stonePos.getY() + 0.8D) - targetY;
                                double offsetZ = (stonePos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D) - targetZ;

                                double startX = stonePos.getX() + 0.5D;
                                double startY = stonePos.getY() + 1.2D;
                                double startZ = stonePos.getZ() + 0.5D;

                                if (stoneLevel == 2) {
                                    level.addParticle(ParticleTypes.GLOW, startX, startY, startZ, -offsetX * 0.15D, -offsetY * 0.15D, -offsetZ * 0.15D);
                                } else if (stoneLevel == 1) {
                                    level.addParticle(ParticleTypes.FLAME, startX, startY, startZ, -offsetX * 0.12D, -offsetY * 0.12D, -offsetZ * 0.12D);
                                } else {
                                    level.addParticle(ParticleTypes.PORTAL, targetX, targetY, targetZ, offsetX, offsetY, offsetZ);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpiritGatheringAltherBlockEntity altarEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, altarEntity, pos);
            } else {
                throw new IllegalStateException("聚灵台方块实体缺失，请检查注册！");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritGatheringAltherBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SpiritGatheringAltherBlockEntity altarEntity) {
                altarEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                    }
                });
                level.updateNeighbourForOutputSignal(pos, this);
            }
            // 3. 关键修复：如果你用了 BaseEntityBlock，必须先在上面处理完掉落，再调用 super.onRemove
            // 否则父类会提前把 BlockEntity 删掉，导致 getCapability 拿不到数据，吞掉里面的物品！
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
}