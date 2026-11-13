package com.TovidY.kunluncontinent.block.portal.polarice;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ThunderRealmPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public ThunderRealmPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .strength(-1.0F)
                .lightLevel((state) -> 15)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    /**
     * 框架被拆 → 门跟着消散（与地狱门、极冰门同一套机制）。
     *
     * <p>说明见 {@link PolarIcePortalBlock#updateShape}：邻居变化时校验 2×3 内腔 + 一圈框架，
     * 不再完整就返回空气，被拆掉的那一格会连锁把整扇门带走。</p>
     */
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        Direction.Axis axis = state.getValue(AXIS);

        // ① 垂直于门平面的前后两面：没有框架，放行
        if (facing.getAxis().isHorizontal() && facing.getAxis() != axis) {
            return super.updateShape(state, facing, facingState, level, pos, facingPos);
        }
        // ② 邻居也是门方块：交给它自己判（建门过程中不能互相判空）
        if (facingState.is(this)) {
            return super.updateShape(state, facing, facingState, level, pos, facingPos);
        }
        // ③ 框架残缺 → 消散
        boolean complete = PortalFrameValidator.isComplete(level, pos, axis, this,
                ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get());
        return complete
                ? super.updateShape(state, facing, facingState, level, pos, facingPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(80) == 0) { // 稍微响一点
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.2F,
                    random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 2; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            // 改为电火花粒子
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (entity.getPortalCooldown() > 0) {
                entity.setPortalCooldown();
                return;
            }
            ResourceKey<Level> destination = level.dimension() == ModDimensions.THUNDER_REALM_LEVEL_KEY
                    ? Level.OVERWORLD : ModDimensions.THUNDER_REALM_LEVEL_KEY;
            ServerLevel destWorld = serverLevel.getServer().getLevel(destination);
            if (destWorld != null) {
                entity.setPortalCooldown();
                entity.changeDimension(destWorld, new ThunderRealmTeleporter(destWorld));
            }
        }
    }
}
