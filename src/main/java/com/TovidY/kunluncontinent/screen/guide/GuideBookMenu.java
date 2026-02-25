package com.TovidY.kunluncontinent.screen.guide;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class GuideBookMenu extends AbstractContainerMenu {
    public GuideBookMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.GUIDE_BOOK_MENU.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}