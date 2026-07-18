package com.TovidY.kunluncontinent.screen.spiritgatheringaltar;

import com.TovidY.kunluncontinent.block.blockentity.SpiritGatheringAltherBlockEntity;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Supplier;

public class SpiritGatheringaltarMenu extends AbstractContainerMenu {
    public final Level world;
    public final Player entity;
    private IItemHandler internal;
    private boolean bound = false;
    private Supplier<Boolean> boundItemMatcher = null;
    private Entity boundEntity = null;
    private BlockEntity boundBlockEntity = null;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;

    // 【新增】ContainerData 变量，默认用长度为 3 的简易数据插槽保
    private ContainerData containerData;

    // 定义一个静态内部类，用来作为“虚拟/临时背包”的保底容器
    private static class VirtualItemStackHandler extends ItemStackHandler {
        public VirtualItemStackHandler(int size) {
            super(size);
        }
    }

    public SpiritGatheringaltarMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(ModMenuTypes.SPIRITGATHERING_MENU.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();

        // 默认初始化数据通道
        this.containerData = new SimpleContainerData(3);

        // 默认初始化为虚拟容器保底，如果绑定成功，会被方块实体的容器覆盖
        this.internal = new VirtualItemStackHandler(3);

        if (extraData != null) {
            BlockPos pos = extraData.readBlockPos();
            this.access = ContainerLevelAccess.create(world, pos);

            // ✨ 【核心修复】不依赖未知的字节数残留，直接通过获取到的方块实体安全转正
            this.boundBlockEntity = this.world.getBlockEntity(pos);

            if (this.boundBlockEntity instanceof SpiritGatheringAltherBlockEntity altarBe) {
                // 1. 成功绑定方块实体
                altarBe.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
                    this.internal = cap;
                    this.bound = true;
                });
                this.containerData = altarBe.getDataAccess();
            } else {
                // 2. 保底兼容：如果不是方块实体，再看看是不是手持物品或实体（对应你以前的逻辑）
                int readableBytes = extraData.readableBytes();
                if (readableBytes == 1) {
                    byte hand = extraData.readByte();
                    ItemStack itemstack = hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem();
                    this.boundItemMatcher = () -> itemstack == (hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem());
                    itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
                        this.internal = cap;
                        this.bound = true;
                    });
                } else if (readableBytes > 1) {
                    extraData.readByte(); // 丢弃 padding
                    this.boundEntity = world.getEntity(extraData.readVarInt());
                    if (this.boundEntity != null) {
                        this.boundEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(cap -> {
                            this.internal = cap;
                            this.bound = true;
                        });
                    }
                }
            }
        }

        // 【核心代码】向 Menu 注册数据同步插槽
        this.addDataSlots(this.containerData);

// 【核心修改】添加本面板的 3 个自定义槽位 (0, 1, 2)，并附加强度锁限制
        this.addSlot(new SlotItemHandler(internal, 0, 27, 15) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return getStoneScore() > 5 && AltarFilters.isValidItem(stack);
            }

            @Override
            public boolean isActive() {
                return true;
            }
        });

        this.addSlot(new SlotItemHandler(internal, 1, 82, 15) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return getStoneScore() > 140 && AltarFilters.isValidItem(stack);
            }
        });

        this.addSlot(new SlotItemHandler(internal, 2, 135, 15) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // 槽位 2：强度大于 300 解开
                return getStoneScore() > 300 && AltarFilters.isValidItem(stack);
            }
        });

        // 添加玩家背包槽位 (9 - 35)
        for (int si = 0; si < 3; ++si) {
            for (int sj = 0; sj < 9; ++sj) {
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 10 + sj * 18, 82 + si * 18));
            }
        }
        // 添加玩家快捷栏槽位 (0 - 8)
        for (int si = 0; si < 9; ++si) {
            this.addSlot(new Slot(inv, si, 10 + si * 18, 140));
        }
    }

    // ==================== 【新增：暴露给 Screen 的数据查询口】 ====================
    public int getStoneScore() {
        return this.containerData.get(0);
    }

    public int getIntervalTicks() {
        return this.containerData.get(1);
    }

    public int getRecoverAmount() {
        return this.containerData.get(2);
    }
    // =========================================================================

    @Override
    public boolean stillValid(Player player) {
        if (this.bound) {
            if (this.boundItemMatcher != null) return this.boundItemMatcher.get();
            if (this.boundBlockEntity != null) return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
            if (this.boundEntity != null) return this.boundEntity.isAlive();
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 3) { // 从自定义槽位快捷传回背包
                if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else { // 从背包快捷传到自定义槽位
                // ✨【核心修复】快捷传送时，必须动态判断槽位是否满足解锁强度，不能无脑塞
                int currentScore = getStoneScore();
                boolean p0Open = currentScore > 5;
                boolean p1Open = currentScore > 140;
                boolean p2Open = currentScore > 300;

                // 尝试逐个塞入已解锁的槽位
                boolean moved = false;
                if (p0Open && AltarFilters.isValidItem(itemstack1)) {
                    moved = this.moveItemStackTo(itemstack1, 0, 1, false);
                }
                if (!moved && p1Open && AltarFilters.isValidItem(itemstack1)) {
                    moved = this.moveItemStackTo(itemstack1, 1, 2, false);
                }
                if (!moved && p2Open && AltarFilters.isValidItem(itemstack1)) {
                    moved = this.moveItemStackTo(itemstack1, 2, 3, false);
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }


    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if ((internal instanceof VirtualItemStackHandler) && playerIn instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    playerIn.drop(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    playerIn.getInventory().placeItemBackInInventory(internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
                }
            }
        }
    }
}