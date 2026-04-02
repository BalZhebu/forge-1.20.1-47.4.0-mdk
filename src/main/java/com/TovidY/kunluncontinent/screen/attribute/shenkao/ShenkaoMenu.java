package com.TovidY.kunluncontinent.screen.attribute.shenkao;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShenkaoMenu extends AbstractContainerMenu {

    public ShenkaoMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv);
    }

    public ShenkaoMenu(int id, Inventory inv) {
        super(ModMenuTypes.SHENKAO_MENU.get(), id);

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 143 + 8 + sj * 18, 12 + 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 143 + 8 + si * 18, 12 + 142));
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("神祇试炼");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new ShenkaoMenu(id, inv);
        }
    }
}