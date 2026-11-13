package com.TovidY.kunluncontinent.block.portal.polarice;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class PolarIcePortalBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public PolarIcePortalBlock() {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .strength(-1.0F)
                .lightLevel((state) -> 12)
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
     * 框架被拆 → 门跟着消散（与地狱门完全相同的机制）。
     *
     * <p>{@code updateShape} 在<b>任何邻居变化</b>时被调用（拆方块、爆炸、活塞…都算），
     * 所以这里不需要额外去监听 {@code BlockEvent.BreakEvent} —— 原版地狱门就是这么做的：
     * 一旦门形不再完整就返回空气，被拆的那一格会连锁通知其余门方块，整扇门一起消失。</p>
     *
     * <p>三种情况直接放行不做校验：</p>
     * <ul>
     *   <li><b>垂直于门平面的前后两面</b>（门轴是 X 时的南北邻居）：那一面本来就没有框架，
     *       玩家在旁边放/拆方块不该把门炸掉；</li>
     *   <li><b>邻居也是门方块</b>：建门时门方块是一格一格放的，先放的那几格会在
     *       后放的格子到来时收到通知，此时校验必然失败 → 必须跳过，否则门刚建好就自毁；</li>
     *   <li>其余情况才真的去校验 2×3 内腔 + 一圈框架。</li>
     * </ul>
     */
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        Direction.Axis axis = state.getValue(AXIS);

        // ① 前后两面（垂直于门平面）：不参与框架，放行
        if (facing.getAxis().isHorizontal() && facing.getAxis() != axis) {
            return super.updateShape(state, facing, facingState, level, pos, facingPos);
        }
        // ② 邻居也是门方块：交给它自己判，避免建门过程中互相判空
        if (facingState.is(this)) {
            return super.updateShape(state, facing, facingState, level, pos, facingPos);
        }
        // ③ 框架残缺 → 消散
        boolean complete = PortalFrameValidator.isComplete(level, pos, axis, this,
                ModBlocks.POLAR_ICE_PORTAL_BLOCK.get());
        return complete
                ? super.updateShape(state, facing, facingState, level, pos, facingPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F,
                    random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (entity.getPortalCooldown() > 0) {
                entity.setPortalCooldown();
                return;
            }
            ResourceKey<Level> destination = level.dimension() == ModDimensions.POLAR_ICE_REALM_LEVEL_KEY
                    ? Level.OVERWORLD : ModDimensions.POLAR_ICE_REALM_LEVEL_KEY;

            ServerLevel destWorld = serverLevel.getServer().getLevel(destination);
            if (destWorld != null) {
                entity.setPortalCooldown();
                entity.changeDimension(destWorld, new PolarIceTeleporter(destWorld));
            }
        }
    }

}