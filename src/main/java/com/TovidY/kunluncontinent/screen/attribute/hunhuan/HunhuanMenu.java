package com.TovidY.kunluncontinent.screen.attribute.hunhuan;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class HunhuanMenu extends AbstractContainerMenu {

    public HunhuanMenu(int id, Inventory inv) {
        this(id, inv, FriendlyByteBuf.class.cast(null));
    }

    public HunhuanMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.HUNHUAN_MENU.get(), id);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("魂环面板");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new HunhuanMenu(id, inv, null);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}