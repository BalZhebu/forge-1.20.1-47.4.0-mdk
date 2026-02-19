package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class HunguMenu extends AbstractContainerMenu {
    private final Player entity;
    private IItemHandler internal;

    public HunguMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.HUNGU_MENU.get(), id);
        this.entity = inv.player;

        this.internal = entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(cap -> cap.getHunguInventory()).orElse(new ItemStackHandler(7));

        this.addSlot(new SlotItemHandler(internal, 0, 153, 8)); // 头部
        this.addSlot(new SlotItemHandler(internal, 1, 117, 16)); // 胸部
        this.addSlot(new SlotItemHandler(internal, 2, 117, 43)); // 左手
        this.addSlot(new SlotItemHandler(internal, 3, 190, 43)); // 右手
        this.addSlot(new SlotItemHandler(internal, 4, 117, 69)); // 左脚
        this.addSlot(new SlotItemHandler(internal, 5, 190, 69)); // 右脚
        this.addSlot(new SlotItemHandler(internal, 6, 190, 16)); // 外附
        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 81 + sj * 18, 97 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 81 + si * 18, 155));
    }

    public static class Provider implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.literal("魂骨面板");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new HunguMenu(id, inv, null);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 7) { // 魂骨槽索引
                if (!this.moveItemStackTo(itemstack1, 7, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 7, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
}
