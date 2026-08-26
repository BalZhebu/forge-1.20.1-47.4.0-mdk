package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import org.jetbrains.annotations.NotNull;

public class HunguMenu extends AbstractContainerMenu {
    private final Player entity;
    private final IItemHandler internal;

    public HunguMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.HUNGU_MENU.get(), id);
        this.entity = inv.player;

        this.internal = entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getHunguInventory())
                .orElse(new ItemStackHandler(7));

        this.addSlot(new RestrictedHunguSlot(internal, 0, 237, 26, 0));

        this.addSlot(new RestrictedHunguSlot(internal, 2, 211, 48, 2));  // 2: 左臂
        this.addSlot(new RestrictedHunguSlot(internal, 1, 237, 48, 1));  // 1: 躯干 (胸骨)
        this.addSlot(new RestrictedHunguSlot(internal, 3, 263, 48, 3));  // 3: 右臂

        // 下层：外附魂骨居中（胸骨下方），左右腿骨分列两侧（臂骨下方）
        this.addSlot(new RestrictedHunguSlot(internal, 4, 211, 74, 4));  // 4: 左腿
        this.addSlot(new RestrictedHunguSlot(internal, 6, 237, 74, 6));  // 6: 外附魂骨
        this.addSlot(new RestrictedHunguSlot(internal, 5, 263, 74, 5));  // 5: 右腿

        // =========================================================================
        // 2. 玩家物品栏（整体右移至靠右边界，Y=108 留出充足呼吸感）
        // =========================================================================
        int invStartX = 156;
        int invStartY = 108;

        // 3x9 背包
        for (int si = 0; si < 3; ++si) {
            for (int sj = 0; sj < 9; ++sj) {
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, invStartX + sj * 18, invStartY + si * 18));
            }
        }

        // 1x9 快捷栏
        for (int si = 0; si < 9; ++si) {
            this.addSlot(new Slot(inv, si, invStartX + si * 18, invStartY + 56));
        }
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        refreshStatsOnServer();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            if (index < 7) {
                if (!this.moveItemStackTo(stackInSlot, 7, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onTake(player, stackInSlot);
            } else {
                boolean moved = false;
                for (int i = 0; i < 7; i++) {
                    Slot targetSlot = this.slots.get(i);
                    if (targetSlot.mayPlace(stackInSlot) && !targetSlot.hasItem()) {
                        if (this.moveItemStackTo(stackInSlot, i, i + 1, false)) {
                            moved = true;
                            break;
                        }
                    }
                }
                if (!moved) {
                    if (index < 34) {
                        if (!this.moveItemStackTo(stackInSlot, 34, 43, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(stackInSlot, 7, 34, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
            refreshStatsOnServer();
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        refreshStatsOnServer();
    }

    private void refreshStatsOnServer() {
        if (!this.entity.level().isClientSide()) {
            this.entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                cap.refreshBoneAttributes(this.entity);
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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

    public class RestrictedHunguSlot extends SlotItemHandler {
        private final int slotType;

        public RestrictedHunguSlot(IItemHandler itemHandler, int index, int x, int y, int type) {
            super(itemHandler, index, x, y);
            this.slotType = type;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            Item item = stack.getItem();
            return switch (this.slotType) {
                case 0 -> item == ModItems.SOUL_BEAST_SKULL.get();
                case 1 -> item == ModItems.SOUL_BEAST_BREASTBONE.get();
                case 2 -> item == ModItems.SOUL_BEAST_LEFT_HAND_BONE.get();
                case 3 -> item == ModItems.SOUL_BEAST_RIGHT_HAND_BONE.get();
                case 4 -> item == ModItems.SOUL_BEAST_LEFT_LEG_BONE.get();
                case 5 -> item == ModItems.SOUL_BEAST_RIGHT_LEG_BONE.get();
                case 6 -> item == ModItems.SOUL_BEAST_EXTERNAL_APPENDAGES.get();
                default -> false;
            };
        }

        @Override
        public int getMaxStackSize() { return 1; }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) { return 1; }

        @Override
        public void setChanged() {
            super.setChanged();
            refreshStatsOnServer();
        }
    }
}