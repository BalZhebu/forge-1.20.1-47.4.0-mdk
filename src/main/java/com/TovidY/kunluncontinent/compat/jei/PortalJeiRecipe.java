package com.TovidY.kunluncontinent.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class PortalJeiRecipe {
    private final Block frameBlock;
    private final Block portalBlock;
    private final ItemStack igniter;

    public PortalJeiRecipe(Block frameBlock, Block portalBlock, ItemStack igniter) {
        this.frameBlock = frameBlock;
        this.portalBlock = portalBlock;
        this.igniter = igniter;
    }

    public Block getFrameBlock() { return frameBlock; }
    public Block getPortalBlock() { return portalBlock; }
    public ItemStack getIgniter() { return igniter; }
}