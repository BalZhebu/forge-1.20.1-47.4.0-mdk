package com.TovidY.kunluncontinent.block.portal.polarice;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class ModPortalEvents {

    @SubscribeEvent
    public static void onPortalActivation(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.EXTREME_COLD_SNOWFLAKE.get())) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        if (level.getBlockState(pos).is(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get())) {
            BlockPos attemptPos = pos.relative(face);
            if (trySpawnPortal(level, attemptPos)) {
                if (!event.getEntity().isCreative()) stack.shrink(1);
                level.playSound(null, attemptPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                event.setCanceled(true);
            }
        }
    }

    private static boolean trySpawnPortal(Level level, BlockPos pos) {
        if (checkFrame(level, pos, Direction.Axis.X)) return fillPortal(level, pos, Direction.Axis.X);
        if (checkFrame(level, pos, Direction.Axis.Z)) return fillPortal(level, pos, Direction.Axis.Z);
        return false;
    }

    private static boolean checkFrame(Level level, BlockPos pos, Direction.Axis axis) {
        for (int d = 0; d < 2; d++) {
            for (int h = 0; h < 3; h++) {
                BlockPos p = (axis == Direction.Axis.X) ? pos.east(d).above(h) : pos.south(d).above(h);
                if (!level.isEmptyBlock(p)) return false;
            }
        }

        for (int d = 0; d < 2; d++) {
            BlockPos bottom = (axis == Direction.Axis.X) ? pos.east(d).below() : pos.south(d).below();
            BlockPos top = (axis == Direction.Axis.X) ? pos.east(d).above(3) : pos.south(d).above(3);
            if (!isFrame(level, bottom) || !isFrame(level, top)) return false;
        }
        for (int h = 0; h < 3; h++) {
            BlockPos left = (axis == Direction.Axis.X) ? pos.west().above(h) : pos.north().above(h);
            BlockPos right = (axis == Direction.Axis.X) ? pos.east(2).above(h) : pos.south(2).above(h);
            if (!isFrame(level, left) || !isFrame(level, right)) return false;
        }
        return true;
    }


    private static boolean isFrame(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.POLAR_ICE_PORTAL_BLOCK.get());
    }

    private static boolean fillPortal(Level level, BlockPos pos, Direction.Axis axis) {
        BlockState state = ModBlocks.POLAR_ICE_PORTAL.get().defaultBlockState().setValue(PolarIcePortalBlock.AXIS, axis);
        for (int d = 0; d < 2; d++) {
            for (int h = 0; h < 3; h++) {
                BlockPos p = (axis == Direction.Axis.X) ? pos.east(d).above(h) : pos.south(d).above(h);
                level.setBlock(p, state, 3);
            }
        }
        return true;
    }
}
