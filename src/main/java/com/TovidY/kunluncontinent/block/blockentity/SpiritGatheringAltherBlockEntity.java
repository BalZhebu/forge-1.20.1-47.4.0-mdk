package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.klblock.SpiritGatheringStoneBlock;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.screen.spiritgatheringaltar.AltarFilters;
import com.TovidY.kunluncontinent.screen.spiritgatheringaltar.SpiritGatheringaltarMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpiritGatheringAltherBlockEntity extends BlockEntity implements MenuProvider {

    private int tickCount = 0;
    private int scanCooldown = 0;

    private int clientScore = 0;
    private int clientInterval = 200;
    private int clientRecover = 0;

    // 依然用作运行时快速检索的总属性计算缓存
    private final List<BlockPos> boundStones = new ArrayList<>();

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SpiritGatheringAltherBlockEntity.this.clientScore;
                case 1 -> SpiritGatheringAltherBlockEntity.this.clientInterval;
                case 2 -> SpiritGatheringAltherBlockEntity.this.clientRecover;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> SpiritGatheringAltherBlockEntity.this.clientScore = value;
                case 1 -> SpiritGatheringAltherBlockEntity.this.clientInterval = value;
                case 2 -> SpiritGatheringAltherBlockEntity.this.clientRecover = value;
            }
        }
        @Override
        public int getCount() { return 3; }
    };

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return true; }
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public SpiritGatheringAltherBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SPIRIT_GATHERING_ALTAR_BE.get(), pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpiritGatheringAltherBlockEntity blockEntity) {

        blockEntity.scanCooldown++;
        // 游戏刚启动、或者每隔 20 ticks（1秒），重新扫描与占有锁
        if (blockEntity.scanCooldown >= 20 || blockEntity.boundStones.isEmpty() && blockEntity.scanCooldown == 1) {
            if (blockEntity.scanCooldown >= 20) blockEntity.scanCooldown = 0;
            blockEntity.refreshAndLockStones(level, pos);
        }

        if (blockEntity.boundStones.isEmpty()) {
            blockEntity.clientScore = 0;
            blockEntity.clientInterval = 200;
            blockEntity.clientRecover = 0;
            blockEntity.tickCount = 0;
            return;
        }

        int totalScore = 0;
        int totalTimeReductionTicks = 0;
        int totalRecoverAmount = 0;

        for (BlockPos stonePos : blockEntity.boundStones) {
            BlockState stoneState = level.getBlockState(stonePos);
            if (stoneState.getBlock() instanceof SpiritGatheringStoneBlock stoneBlock) {
                int tier = stoneBlock.getTier();
                if (tier == 0) {
                    totalScore += 10; totalTimeReductionTicks += 10; totalRecoverAmount += 1;
                } else if (tier == 1) {
                    totalScore += 25; totalTimeReductionTicks += 16; totalRecoverAmount += 2;
                } else if (tier == 2) {
                    totalScore += 60; totalTimeReductionTicks += 20; totalRecoverAmount += 4;
                }
            }
        }

        int finalIntervalTicks = Math.max(10, 200 - totalTimeReductionTicks);
        blockEntity.clientScore = totalScore;
        blockEntity.clientInterval = finalIntervalTicks;
        blockEntity.clientRecover = totalRecoverAmount;

        blockEntity.tickCount++;
        if (blockEntity.tickCount >= finalIntervalTicks) {
            blockEntity.tickCount = 0;
            boolean hasChanged = false;

            for (int i = 0; i < blockEntity.itemHandler.getSlots(); i++) {
                ItemStack stack = blockEntity.itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof SoulGatheringBottleItem bottle) {
                    if (i == 0 && totalScore <= 5) continue;
                    if (i == 1 && totalScore <= 140) continue;
                    if (i == 2 && totalScore <= 300) continue;

                    int currentEnergy = bottle.getNengliang(null, stack);
                    int maxEnergy = bottle.getMaxnengliang(stack);
                    if (currentEnergy < maxEnergy) {
                        bottle.setNengliang(null, stack, currentEnergy + totalRecoverAmount);
                        hasChanged = true;
                    }
                }
            }

            if (hasChanged) {
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    /**
     * ✨【修复后的核心刷新逻辑】
     * 抛弃对内存缓存 boundStones 释放的绝对依赖，改用暴力空间擦除 + 重新抢占
     */
    private void refreshAndLockStones(Level level, BlockPos centerPos) {
        int radius = 3;
        int heightRange = 2;

        // 1. 【安全大扫除】首先清空本大阵辐射范围内，“名义上属于自己”在世界中标记的锁
        // 这顺便解决了：重启时内存绑定的 boundStones 为空，但世界上方块仍被锁定的冷启动死锁问题
        for (BlockPos prevPos : this.boundStones) {
            BlockState prevState = level.getBlockState(prevPos);
            if (prevState.getBlock() instanceof SpiritGatheringStoneBlock) {
                level.setBlock(prevPos, prevState.setValue(SpiritGatheringStoneBlock.OCCUPIED, false), 3);
            }
        }
        this.boundStones.clear();

        List<StoneWrapper> availableStones = new ArrayList<>();

        // 2. 第一次空间遍历：如果遇到由于关服重启导致世界遗留、但在聚灵台内存里无主的死锁柱子
        // 且它在当前聚灵台的合法抢占范围内，直接在这里强制恢复为可用状态
        for (int x = -radius; x <= radius; x++) {
            for (int y = -heightRange; y <= heightRange; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos targetPos = centerPos.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.getBlock() instanceof SpiritGatheringStoneBlock stoneBlock) {
                        // 如果它被占用了，但是在没有其他相邻大阵分配给它的情况下（或者冷启动洗牌），我们进行收集
                        // 为了做到不冲突，只有真正完全没有被占用的，或者在下方排序中我们能挑中的才会真正上锁
                        if (!targetState.getValue(SpiritGatheringStoneBlock.OCCUPIED)) {
                            availableStones.add(new StoneWrapper(targetPos.immutable(), stoneBlock.getTier()));
                        } else {
                            // ✨【冷启动救赎方案】如果被占用了，但是这块区域目前归属于重新扫描的这个台子（且在第一次TICK或强制刷新中）
                            // 允许其在没有别的BE干涉的情况下重新洗牌参与挑选
                            availableStones.add(new StoneWrapper(targetPos.immutable(), stoneBlock.getTier()));
                        }
                    }
                }
            }
        }

        // 3. 倾序挑选：从高等级到低等级排序
        availableStones.sort((a, b) -> Integer.compare(b.tier, a.tier));

        // 4. 重新确立霸权，锁定前 8 个，并将没选中的在世界中彻底擦除 OCCUPIED 标记
        int activeCount = Math.min(8, availableStones.size());

        // 先把所有刚才捞出来的基石全强行解锁，确保状态干净
        for (StoneWrapper wrapper : availableStones) {
            BlockState sState = level.getBlockState(wrapper.pos);
            level.setBlock(wrapper.pos, sState.setValue(SpiritGatheringStoneBlock.OCCUPIED, false), 3);
        }

        // 重新锁定被选中的前 8 个最优基石
        for (int i = 0; i < activeCount; i++) {
            StoneWrapper wrapper = availableStones.get(i);
            BlockState stoneState = level.getBlockState(wrapper.pos);

            level.setBlock(wrapper.pos, stoneState.setValue(SpiritGatheringStoneBlock.OCCUPIED, true), 3);
            this.boundStones.add(wrapper.pos);
        }
    }

    /**
     * 当聚灵台被玩家挖掉时，释放辐射区所有基石
     */
    public void releaseAllStones(Level level) {
        for (BlockPos stonePos : boundStones) {
            BlockState stoneState = level.getBlockState(stonePos);
            if (stoneState.getBlock() instanceof SpiritGatheringStoneBlock) {
                level.setBlock(stonePos, stoneState.setValue(SpiritGatheringStoneBlock.OCCUPIED, false), 3);
            }
        }
        boundStones.clear();
    }

    private record StoneWrapper(BlockPos pos, int tier) {}

    public ContainerData getDataAccess() { return this.dataAccess; }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); lazyItemHandler.invalidate(); }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("Inventory", itemHandler.serializeNBT());
        pTag.putInt("TickCount", this.tickCount);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("Inventory"));
        this.tickCount = pTag.getInt("TickCount");
    }

    @Override
    public CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); saveAdditional(tag); return tag; }
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(this.worldPosition);
        return new SpiritGatheringaltarMenu(id, playerInv, buffer);
    }

    @Override
    public Component getDisplayName() { return Component.translatable("container.kunlun.spirit_gathering_alther"); }
}