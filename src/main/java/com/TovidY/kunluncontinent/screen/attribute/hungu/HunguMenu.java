package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.nbt.CompoundTag;
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
    private IItemHandler internal;

    public HunguMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.HUNGU_MENU.get(), id);
        this.entity = inv.player;


        this.internal = entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(cap -> cap.getHunguInventory()).orElse(new ItemStackHandler(7));
        this.addSlot(new RestrictedHunguSlot(internal, 0, 153, 8, 0)); // 头部
        this.addSlot(new RestrictedHunguSlot(internal, 1, 117, 16, 1)); // 躯干
        this.addSlot(new RestrictedHunguSlot(internal, 2, 117, 43, 2)); // 左手
        this.addSlot(new RestrictedHunguSlot(internal, 3, 190, 43, 3)); // 右手
        this.addSlot(new RestrictedHunguSlot(internal, 4, 117, 69, 4)); // 左腿
        this.addSlot(new RestrictedHunguSlot(internal, 5, 190, 69, 5)); // 右腿
        this.addSlot(new RestrictedHunguSlot(internal, 6, 190, 16, 6));

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
            if (index < 7) {
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
        if (!player.level().isClientSide) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                cap.refreshBoneAttributes(player);
            });
        }
    }

    public class RestrictedHunguSlot extends SlotItemHandler {
        private final int slotType; // 0-6

        public RestrictedHunguSlot(IItemHandler itemHandler, int index, int x, int y, int type) {
            super(itemHandler, index, x, y);
            this.slotType = type;
        }
        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            Item item = stack.getItem();
            // 根据你的 ModItems 里的具体注册名进行匹配
            // 假设槽位顺序是：0头, 1胸, 2左手, 3右手, 4左腿, 5右腿, 6外附
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
    }

}
