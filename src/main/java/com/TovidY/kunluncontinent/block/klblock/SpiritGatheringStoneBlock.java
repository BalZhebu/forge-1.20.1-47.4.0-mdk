package com.TovidY.kunluncontinent.block.klblock;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SpiritGatheringStoneBlock extends Block {
    public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");
    private final int tier;

    public SpiritGatheringStoneBlock(Properties pProperties, int tier) {
        super(pProperties);
        this.tier = tier;

        this.registerDefaultState(this.stateDefinition.any().setValue(OCCUPIED, false));
    }

    public int getTier() {
        return this.tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }

    protected static final VoxelShape SHAPE = Stream.of(
            // 底座 14×2×14
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D),
            // 柱身 6×12×6
            Block.box(5.0D, 2.0D, 5.0D, 11.0D, 14.0D, 11.0D),
            // 顶面边框 北
            Block.box(4.0D, 14.0D, 2.0D, 12.0D, 15.0D, 4.0D),
            // 顶面边框 南
            Block.box(4.0D, 14.0D, 12.0D, 12.0D, 15.0D, 14.0D),
            // 顶面边框 西
            Block.box(2.0D, 14.0D, 4.0D, 4.0D, 15.0D, 12.0D),
            // 顶面边框 东
            Block.box(12.0D, 14.0D, 4.0D, 14.0D, 15.0D, 12.0D),
            // 紫色水晶 4×4×4
            Block.box(6.0D, 15.0D, 6.0D, 10.0D, 19.0D, 10.0D)
    ).reduce(Shapes::or).get();

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        pTooltip.add(Component.translatable("spirit_gathering_stone_0_mees").withStyle(ChatFormatting.GRAY));
        pTooltip.add(Component.translatable("spirit_gathering_stone_1_mees").withStyle(ChatFormatting.GRAY));
        pTooltip.add(Component.translatable("spirit_gathering_stone_2_mees").withStyle(ChatFormatting.GRAY));
    }
}
