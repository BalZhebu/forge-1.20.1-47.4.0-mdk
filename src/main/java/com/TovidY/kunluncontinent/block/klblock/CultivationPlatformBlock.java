package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.shapes.VoxelShape;

public class CultivationPlatformBlock extends Block {
    public CultivationPlatformBlock(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag data = serverPlayer.getPersistentData();
            if (!serverPlayer.hasEffect(ModEffects.RED_SPIDER_LILY_POTION.get())) {
                serverPlayer.sendSystemMessage(Component.literal("你没有彼岸花的效果，无法使用重修台！").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            if (data.getBoolean("IsPreparingReincarnation")) {
                data.remove("IsPreparingReincarnation");
                data.remove("ReincarnationTimer");
                serverPlayer.sendSystemMessage(Component.literal("重修准备已取消。").withStyle(ChatFormatting.YELLOW));
            } else {
                data.putBoolean("IsPreparingReincarnation", true);
                data.putInt("ReincarnationTimer", 200);
                serverPlayer.sendSystemMessage(Component.literal("重修准备开始，10秒后引动雷劫，再次右键可取消。").withStyle(ChatFormatting.GOLD));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
