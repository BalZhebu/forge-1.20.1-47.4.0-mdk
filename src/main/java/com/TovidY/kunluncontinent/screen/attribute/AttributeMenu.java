package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

//属性菜单'
public class AttributeMenu extends AbstractContainerMenu implements MenuProvider {

    public AttributeMenu(int pContainerId, Inventory inv, net.minecraft.network.FriendlyByteBuf extraData) {
        this(pContainerId, inv);
    }

    public AttributeMenu(int pContainerId, Inventory inv) {
        super(ModMenuTypes.ATTRUBUTE_MENU.get(), pContainerId);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("属性面板");
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("属性面板");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new AttributeMenu(id, inv);
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return null;
    }
}