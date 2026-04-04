package com.TovidY.kunluncontinent.block.portal.polarice;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ModPortalEvents {

    @SubscribeEvent
    public static void onPortalActivation(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        BlockState clickedState = level.getBlockState(pos);
        if (stack.is(ModItems.EXTREME_COLD_SNOWFLAKE.get()) && clickedState.is(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get())) {
            activatePortal(event, stack, level, pos, face, ModBlocks.POLAR_ICE_PORTAL.get(), ModBlocks.POLAR_ICE_PORTAL_BLOCK.get());
        }
        else if (stack.is(ModItems.THUNDERREALM_SNOWFLAKE.get()) && clickedState.is(ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get())) {
            activatePortal(event, stack, level, pos, face, ModBlocks.THUNDER_REALM_PORTAL.get(), ModBlocks.THUNDER_REALM_PORTAL_BLOCK.get());
        }
    }

    private static void activatePortal(PlayerInteractEvent.RightClickBlock event, ItemStack stack, Level level, BlockPos pos, Direction face, Block portal, Block frame) {
        BlockPos attemptPos = pos.relative(face);
        if (trySpawnPortal(level, attemptPos, portal, frame)) {
            if (!event.getEntity().isCreative()) stack.shrink(1);
            level.playSound(null, attemptPos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 1.0F, 1.0F);
            event.setCanceled(true);
        }
    }

    private static boolean trySpawnPortal(Level level, BlockPos pos, Block portal, Block frame) {
        if (checkFrame(level, pos, Direction.Axis.X, frame)) return fillPortal(level, pos, Direction.Axis.X, portal);
        if (checkFrame(level, pos, Direction.Axis.Z, frame)) return fillPortal(level, pos, Direction.Axis.Z, portal);
        return false;
    }


    private static boolean checkFrame(Level level, BlockPos pos, Direction.Axis axis, Block frame) {
        for (int d = 0; d < 2; d++) {
            for (int h = 0; h < 3; h++) {
                BlockPos p = (axis == Direction.Axis.X) ? pos.east(d).above(h) : pos.south(d).above(h);
                if (!level.isEmptyBlock(p)) return false;
            }
        }
        for (int d = 0; d < 2; d++) {
            BlockPos bottom = (axis == Direction.Axis.X) ? pos.east(d).below() : pos.south(d).below();
            BlockPos top = (axis == Direction.Axis.X) ? pos.east(d).above(3) : pos.south(d).above(3);
            if (!level.getBlockState(bottom).is(frame) || !level.getBlockState(top).is(frame)) return false;
        }
        for (int h = 0; h < 3; h++) {
            BlockPos left = (axis == Direction.Axis.X) ? pos.west().above(h) : pos.north().above(h);
            BlockPos right = (axis == Direction.Axis.X) ? pos.east(2).above(h) : pos.south(2).above(h);

            if (!level.getBlockState(left).is(frame) || !level.getBlockState(right).is(frame)) return false;
        }
        return true;
    }


    private static boolean fillPortal(Level level, BlockPos pos, Direction.Axis axis, Block portal) {
        BlockState state = portal.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
        for (int d = 0; d < 2; d++) {
            for (int h = 0; h < 3; h++) {
                BlockPos p = (axis == Direction.Axis.X) ? pos.east(d).above(h) : pos.south(d).above(h);
                level.setBlock(p, state, 3);
            }
        }
        return true;
    }
}
