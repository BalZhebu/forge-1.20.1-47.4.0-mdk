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

//属性菜单
public class AttributeMenu extends AbstractContainerMenu implements MenuProvider {

    public AttributeMenu(int pContainerId, Inventory inv, net.minecraft.network.FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player, new SimpleContainerData(7));
    }

    public AttributeMenu(int pContainerId, Inventory inv) {
        this(pContainerId, inv, inv.player, new SimpleContainerData(7));
    }

    public AttributeMenu(int pContainerId, Inventory inv, Player player, SimpleContainerData simpleContainerData) {
        super(ModMenuTypes.ATTRUBUTE_MENU.get(), pContainerId);
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }
    private void addPlayerInventory(Inventory playerInventory) {
        for (int si = 0; si < 3; ++si) {
            for (int sj = 0; sj < 9; ++sj) {
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 73 + 8 + sj * 18, 13 + 84 + si * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int si = 0; si < 9; ++si) {
            this.addSlot(new Slot(playerInventory, si, 73 + 8 + si * 18, 13 + 142));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("属性面板");
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AttributeMenu(pContainerId, pPlayerInventory);
    }
}