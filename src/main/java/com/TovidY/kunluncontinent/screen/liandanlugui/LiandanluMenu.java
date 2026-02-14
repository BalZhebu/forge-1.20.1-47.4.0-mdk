package com.TovidY.kunluncontinent.screen.liandanlugui;

import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
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
    private final ContainerData data;

    public LiandanluMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public LiandanluMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.LIANDANLU_MENU.get(), id);

        checkContainerDataCount(data, 2);
        this.blockEntity = entity;
        this.data = data;
        this.access = ContainerLevelAccess.create(inv.player.level(), entity.getBlockPos());

        // 获取 BlockEntity 的物品处理器
        this.internal = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .orElse(new ItemStackHandler(19));

        // 1. 注册内丹槽 (0-4)
        this.addSlot(new SlotItemHandler(internal, 0, 8, 8));
        this.addSlot(new SlotItemHandler(internal, 1, 25, 8));
        this.addSlot(new SlotItemHandler(internal, 2, 42, 8));
        this.addSlot(new SlotItemHandler(internal, 3, 59, 8));
        this.addSlot(new SlotItemHandler(internal, 4, 77, 8));

        // 2. 注册药渣槽 (17)
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
                    @Override
                    public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }

        this.addDataSlots(data);
        addPlayerInventory(inv);
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

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
                    if (sourceStack.is(ModBlocks.DROSS_BLOCK.get().asItem())) {
                        if (!this.moveItemStackTo(sourceStack, 17, 18, false)) return ItemStack.EMPTY;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (sourceStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    private void addPlayerInventory(Inventory inv) {
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