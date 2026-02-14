package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class LiandanluMenu extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final ContainerLevelAccess access;
    private final BlockEntity blockEntity;

    public LiandanluMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.LIANDANLU_MENU.get(), id);

        BlockPos pos = extraData.readBlockPos();
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.blockEntity = inv.player.level().getBlockEntity(pos);
        this.internal = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .orElse(new ItemStackHandler(19));
        this.addSlot(new SlotItemHandler(internal, 0, 8, 8));
        this.addSlot(new SlotItemHandler(internal, 1, 25, 8));
        this.addSlot(new SlotItemHandler(internal, 2, 42, 8));
        this.addSlot(new SlotItemHandler(internal, 3, 59, 8));
        this.addSlot(new SlotItemHandler(internal, 4, 77, 8));
        this.addSlot(new SlotItemHandler(internal, 17, 8, 41) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModBlocks.DROSS_BLOCK.get().asItem());
            }
        });

        this.addSlot(new SlotItemHandler(internal, 18, 34, 41));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int index = 5 + (row * 4) + col;
                this.addSlot(new SlotItemHandler(internal, index, 135 + col * 17, 8 + row * 17) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }

        addPlayerInventory(inv);
    }

    // 在 LiandanluMenu.java 中
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack sourceStack = slot.getItem();
            itemstack = sourceStack.copy();

            if (index < 19) {
                if (!this.moveItemStackTo(sourceStack, 19, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(sourceStack, 0, 5, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (sourceStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    private void addPlayerInventory(Inventory inv) {
        // 使用你指定的坐标偏置
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                this.addSlot(new Slot(inv, j + (i + 1) * 9, 26 + j * 18, 86 + i * 18));
        for (int i = 0; i < 9; ++i)
            this.addSlot(new Slot(inv, i, 26 + i * 18, 144));
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, blockEntity.getBlockState().getBlock());
    }
}