package com.TovidY.kunluncontinent.block.portal.polarice;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * 极冰界传送门。
 *
 * <p>查找 / 新建 / 防重复生成的逻辑全部在 {@link RealmTeleporter}，
 * 本类只提供「用哪种方块、去哪个维度」三个参数。</p>
 */
public class PolarIceTeleporter extends RealmTeleporter {

    public PolarIceTeleporter(ServerLevel level) {
        super(level);
    }

    @Override
    protected Block portalBlock() {
        return ModBlocks.POLAR_ICE_PORTAL.get();
    }

    @Override
    protected Block frameBlock() {
        return ModBlocks.POLAR_ICE_PORTAL_BLOCK.get();
    }

    @Override
    protected ResourceKey<Level> targetDimension() {
        return ModDimensions.POLAR_ICE_REALM_LEVEL_KEY;
    }
}
