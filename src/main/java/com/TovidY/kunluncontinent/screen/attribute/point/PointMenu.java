package com.TovidY.kunluncontinent.screen.attribute.point;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PointMenu extends AbstractContainerMenu {

    public PointMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.POINT_MENU.get(), id);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("属性点面板");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new PointMenu(id, inv, null);
        }
    }
}
