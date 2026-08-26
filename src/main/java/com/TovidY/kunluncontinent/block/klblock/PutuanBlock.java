package com.TovidY.kunluncontinent.block.klblock;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.PacketOpenXiulianScreen;
import com.TovidY.kunluncontinent.screen.attribute.XiulianSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;

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
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            if (!player.isPassenger()) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    ArmorStand seat = new ArmorStand(level, pos.getX() + 0.5D, pos.getY() - 0.65D, pos.getZ() + 0.5D);
                    CompoundTag nbt = new CompoundTag();
                    nbt.putBoolean("Small", true);
                    seat.readAdditionalSaveData(nbt);

                    seat.setInvisible(true);
                    seat.setNoGravity(true);
                    seat.setInvulnerable(true);
                    seat.addTag("putuan_seat");

                    level.addFreshEntity(seat);
                    player.startRiding(seat);

                    int xiulianTime = cap.getXiulianTime();
                    if (xiulianTime > 0) {
                        player.sendSystemMessage(Component.literal("§e你盘膝而坐，开始感受天地灵气..."));

                        if (player instanceof ServerPlayer serverPlayer) {
                            NetworkHandler.INSTANCE.send(
                                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                                    new PacketOpenXiulianScreen(xiulianTime)
                            );
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§e修炼时间已耗尽，当前进入静心打坐模式（仅恢复精神力）..."));
                    }
                });
            }
        }
        return InteractionResult.SUCCESS;
    }
}