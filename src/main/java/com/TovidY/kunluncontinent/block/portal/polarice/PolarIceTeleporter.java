package com.TovidY.kunluncontinent.block.portal.polarice;

import com.TovidY.kunluncontinent.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Consumer;
import java.util.function.Function;

public class PolarIceTeleporter implements ITeleporter {
    protected final ServerLevel level;

    public PolarIceTeleporter(ServerLevel level) {
        this.level = level;
    }

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos destinationPos = findPortal(destWorld, entity.blockPosition());
        if (destinationPos == null) {
            destinationPos = createPortal(destWorld, entity.blockPosition());
        }
        return new PortalInfo(
                new Vec3(destinationPos.getX() + 0.5D, destinationPos.getY(), destinationPos.getZ() + 0.5D),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    private BlockPos findPortal(ServerLevel destWorld, BlockPos startPos) {
        for (BlockPos pos : BlockPos.betweenClosed(startPos.offset(-16, -16, -16), startPos.offset(16, 16, 16))) {
            if (destWorld.getBlockState(pos).is(ModBlocks.POLAR_ICE_PORTAL.get())) {
                return pos.immutable();
            }
        }
        return null;
    }

    private BlockPos createPortal(ServerLevel destWorld, BlockPos pos) {
        int y = destWorld.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        if (y < 1) y = 70;
        BlockPos spawnPos = new BlockPos(pos.getX(), y, pos.getZ());
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                BlockPos target = spawnPos.offset(dx, dy, 0);
                if (dx == -1 || dx == 2 || dy == 0 || dy == 4) {
                    destWorld.setBlockAndUpdate(target, ModBlocks.POLAR_ICE_PORTAL_BLOCK.get().defaultBlockState());
                } else {
                    destWorld.setBlockAndUpdate(target, ModBlocks.POLAR_ICE_PORTAL.get().defaultBlockState()
                            .setValue(PolarIcePortalBlock.AXIS, Direction.Axis.X));
                }
            }
        }
        return spawnPos.offset(0, 1, 0);
    }
}