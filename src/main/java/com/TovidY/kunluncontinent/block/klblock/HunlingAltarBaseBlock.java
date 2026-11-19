package com.TovidY.kunluncontinent.block.klblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

/**
 * 汇灵台底座 —— 也能摆供品的矮柱子。
 *
 * <p>除了模型和碰撞箱，行为与 {@link HunlingAltarBlock} 完全一致（都继承
 * {@link AbstractHunlingAltarBlock}）。顶面同样在 {@code y = 9/16}，
 * 所以共用同一个渲染高度。</p>
 */
public class HunlingAltarBaseBlock extends AbstractHunlingAltarBlock {

    /** 与模型 3 个元素一一对应（方座 / 柱身 / 顶面），柱身四周是真镂空。 */
    protected static final VoxelShape SHAPE = Stream.of(
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 1.0D, 12.0D),   // 方座
            Block.box(6.0D, 1.0D, 6.0D, 10.0D, 8.0D, 10.0D),   // 柱身
            Block.box(5.0D, 8.0D, 5.0D, 11.0D, 9.0D, 11.0D)    // 顶面
    ).reduce(Shapes::or).get();

    public HunlingAltarBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** 底座只负责「摆一件 / 取回」，仪式由汇灵台那边统一启动。 */
    @Override
    protected net.minecraft.world.InteractionResult handleUse(
            net.minecraft.world.level.Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand,
            com.TovidY.kunluncontinent.block.blockentity.HunlingAltarBlockEntity altar) {
        return handleSimplePlace(level, pos, player, hand, altar);
    }
}
