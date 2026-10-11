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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpiritGatheringAltherBlockEntity extends BlockEntity implements MenuProvider {

    // ==================== 【常量：单一真相源】 ====================
    /** 聚魂瓶槽位数量。 */
    public static final int BOTTLE_SLOTS = 3;
    /** 瓶子槽位解锁所需的「大阵聚灵强度」门槛，下标 = 槽位号。 */
    public static final int[] SLOT_SCORE_REQUIREMENTS = {5, 140, 300};
    /** 水平扫描半径（格）。柱子在 radius 内即可被吸附。 */
    public static final int SCAN_RADIUS = 3;
    /** 垂直扫描半径（格）。 */
    public static final int SCAN_HEIGHT = 2;
    /** 最多可锁定的柱子数量。 */
    public static final int MAX_BOUND_STONES = 8;

    private int tickCount = 0;
    private int scanCooldown = 0;

    private int clientScore = 0;
    private int clientInterval = 200;
    private int clientRecover = 0;

    /** 当前大阵的总强度，由 {@link #serverTick} 维护，供 {@link #itemHandler} 的校验使用。 */
    private int totalScore = 0;

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

    private final ItemStackHandler itemHandler = new ItemStackHandler(BOTTLE_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // ⚠️ 之前这里恒返回 true，等于对全服开放了「任意物品塞进任意瓶子槽」。
            //    后果：漏斗/管道能把石头塞进槽位永久占死（石头既不能回能，
            //    又因为 mayPlace 过滤而无法用 shift+点击取出 —— 槽位彻底锁死）。
            //    聚灵台只认聚魂瓶，且三个槽位要按解锁强度分级校验。
            if (!AltarFilters.isValidItem(stack)) return false;
            return totalScore > SLOT_SCORE_REQUIREMENTS[slot];
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // 放取物品后立刻把解锁状态推给客户端，避免 GUI 迟 1 秒才亮
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public SpiritGatheringAltherBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SPIRIT_GATHERING_ALTAR_BE.get(), pPos, pBlockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpiritGatheringAltherBlockEntity blockEntity) {

        blockEntity.scanCooldown++;
        // 游戏刚启动、或者每隔 20 ticks（1秒），重新扫描与占有锁。
        //⚠️ boundStones 被清空（本台丢锁）或区块重新加载时，立刻重扫一次，
        //    否则要等满 20 tick 才会恢复显示。
        if (blockEntity.scanCooldown >= 20 || blockEntity.boundStones.isEmpty()) {
            if (blockEntity.scanCooldown >= 20) blockEntity.scanCooldown = 0;
            blockEntity.refreshAndLockStones(level, pos);
        }

        int newScore = 0;
        int newTimeReductionTicks = 0;
        int newRecoverAmount = 0;

        for (BlockPos stonePos : blockEntity.boundStones) {
            // ⚠️ 柱子可能已被玩家挖掉、或所在区块已卸载。
            //    之前直接 getBlockState 会**强制加载区块**，玩家在远处时白白吃内存。
            if (!level.hasChunkAt(stonePos)) continue;
            BlockState stoneState = level.getBlockState(stonePos);
            if (stoneState.getBlock() instanceof SpiritGatheringStoneBlock stoneBlock) {
                int tier = stoneBlock.getTier();
                if (tier == 0) {
                    newScore += 10; newTimeReductionTicks += 10; newRecoverAmount += 1;
                } else if (tier == 1) {
                    newScore += 25; newTimeReductionTicks += 16; newRecoverAmount += 2;
                } else if (tier == 2) {
                    newScore += 60; newTimeReductionTicks += 20; newRecoverAmount += 4;
                }
            }
        }

        blockEntity.totalScore = newScore;

        final int finalIntervalTicks = Math.max(10, 200 - newTimeReductionTicks);
        blockEntity.clientScore = newScore;
        blockEntity.clientInterval = finalIntervalTicks;
        blockEntity.clientRecover = newRecoverAmount;

        if (newRecoverAmount <= 0) {
            // 没有柱子可提供回能，直接清空计时并返回（省掉下面每 tick 的空转）
            blockEntity.tickCount = 0;
            return;
        }

        blockEntity.tickCount++;
        if (blockEntity.tickCount >= finalIntervalTicks) {
            blockEntity.tickCount = 0;
            boolean hasChanged = false;

            for (int i = 0; i < BOTTLE_SLOTS; i++) {
                ItemStack stack = blockEntity.itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof SoulGatheringBottleItem bottle) {
                    // 服务端权威复核解锁门槛：客户端 mayPlace 只是 UI 提示，
                    // 这里必须再挡一次，否则旧版本/自动化塞进来的瓶子会被白嫖回能。
                    if (newScore <= SLOT_SCORE_REQUIREMENTS[i]) continue;

                    int currentEnergy = bottle.getNengliang(null, stack);
                    int maxEnergy = bottle.getMaxnengliang(stack);
                    if (currentEnergy < maxEnergy) {
                        // ⚠️ 之前是 currentEnergy + totalRecoverAmount，不夹到 maxEnergy。
                        //    瓶子 1200 满、一次回 8 点时会写成 1208，虽然 setNengliang 内部
                        //    有Math.min 兜底，但这里补上钳制让逻辑自洽。
                        bottle.setNengliang(null, stack, Math.min(maxEnergy, currentEnergy + newRecoverAmount));
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
     * ✨【独占裁决 + 状态回写】
     *
     * <p><b>原实现的两个恶劣 BUG：</b></p>
     * <ol>
     *   <li><b>多台聚灵台重复吃同一根柱子</b>：原代码在遍历时对
     *       {@code OCCUPIED=false} 和 {@code true} 两个分支<b>添加了完全一样的内容</b>，
     *       等于 OCCUPIED 从未被用作判断依据。于是相邻两台聚灵台会同时把同一根柱子
     *       算进自己的 boundStones，强度与回能<b>成倍叠加</b>。</li>
     *   <li><b>每秒无条件 setBlock</b>：原代码先把范围内所有柱子强行解锁、再锁前 8 个，
     *       每个柱子每 20 tick 都要两次 {@code setBlock(..., 3)}，
     *       即使状态没变也写，触发无谓的方块更新与客户端同步。</li>
     * </ol>
     *
     * <p><b>现在的做法：</b>先算出「自己应该占哪些柱子」，再与「当前真实锁状态」求差集，
     * <b>只对状态真的发生变化的柱子调用 setBlock</b>，且flag 用 2（只同步客户端，
     * 不触发邻居更新）。独占判定仍以 OCCUPIED 为准，但补上了真正的抢占规则：
     * 已被<b>其它</b>聚灵台锁定的柱子不参与本次挑选，只有在本台自己的锁失效
     * （柱子被挖 / 区块卸载）时才重新洗牌。</p>
     */
    private void refreshAndLockStones(Level level, BlockPos centerPos) {
        // 1. 先把「本台上一轮的锁」记录下来，稍后用于求差集
        List<BlockPos> previouslyBound = new ArrayList<>(this.boundStones);
        this.boundStones.clear();

        // ⚠️ 清理上一轮里已经「不成立」的锁：柱子被挖、或所在区块未加载。
        //    不清的话，这些坐标会一直留在 previouslyBound 里，
        //    导致其它聚灵台刷新时把它们误判成「被本台占用」而永远抢不到。
        previouslyBound.removeIf(p -> !level.hasChunkAt(p)
                || !(level.getBlockState(p).getBlock() instanceof SpiritGatheringStoneBlock));

        List<StoneWrapper> availableStones = new ArrayList<>();

        // 2. 遍历辐射范围，收集「当前未被任何聚灵台锁定」的柱子
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_HEIGHT; y <= SCAN_HEIGHT; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos targetPos = centerPos.offset(x, y, z);
                    // ⚠️ 必须先判区块是否加载，否则 getBlockState 会强制加载区块
                    if (!level.hasChunkAt(targetPos)) continue;
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.getBlock() instanceof SpiritGatheringStoneBlock stoneBlock) {
                        // 只有「未被锁定」或「被本台自己锁定过」的柱子才可被本台认领。
                        // 注意：不能用"被锁定就一律抢"，否则两台会来回抖动互相抢柱。
                        boolean occupiedByOther = targetState.getValue(SpiritGatheringStoneBlock.OCCUPIED)
                                && !previouslyBound.contains(targetPos);
                        if (!occupiedByOther) {
                            availableStones.add(new StoneWrapper(targetPos.immutable(), stoneBlock.getTier()));
                        }
                    }
                }
            }
        }

        // 3. 倾序挑选：从高等级到低等级排序（等阶相同时按位置排序，保证刷新结果稳定不抖动）
        availableStones.sort((a, b) -> {
            int byTier = Integer.compare(b.tier, a.tier);
            if (byTier != 0) return byTier;
            return Long.compare(a.pos.asLong(), b.pos.asLong());
        });

        int activeCount = Math.min(MAX_BOUND_STONES, availableStones.size());
        Set<BlockPos> wanted = new HashSet<>();
        for (int i = 0; i < activeCount; i++) {
            wanted.add(availableStones.get(i).pos);
        }

        // 4. 求差集，只对状态变化的柱子写方块
        Set<BlockPos> toUnlock = new HashSet<>(previouslyBound);
        toUnlock.removeAll(wanted);
        for (BlockPos pos : toUnlock) {
            setStoneOccupied(level, pos, false);
        }
        for (BlockPos pos : wanted) {
            setStoneOccupied(level, pos, true);
            this.boundStones.add(pos);
        }
    }

    /**
     * 只在状态<b>真的变化</b>时才写方块，避免每秒无谓的方块更新 / 客户端同步。
     */
    private static void setStoneOccupied(Level level, BlockPos pos, boolean occupied) {
        if (!level.hasChunkAt(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SpiritGatheringStoneBlock)) return;
        if (state.getValue(SpiritGatheringStoneBlock.OCCUPIED) == occupied) return;
        // flag 2 = 只更新客户端。锁标记不影响碰撞/红石，没必要触发邻居更新。
        level.setBlock(pos, state.setValue(SpiritGatheringStoneBlock.OCCUPIED, occupied), 2);
    }

    /**
     * 当聚灵台被玩家挖掉 / 被活塞推动时，释放辐射区所有基石。
     * 之前这是<b>死代码</b>（全项目无人调用），导致挖掉聚灵台后柱子永远残留 OCCUPIED=true。
     */
    public void releaseAllStones(Level level) {
        for (BlockPos stonePos : new ArrayList<>(boundStones)) {
            setStoneOccupied(level, stonePos, false);
        }
        boundStones.clear();
        totalScore = 0;
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
        //⚠️ boundStones 必须持久化。之前不存，导致关服重启后：
        //    内存里 boundStones 空 → 但世界里的 OCCUPIED=true 还在 →
        //    新逻辑会认为「这些锁属于别人」而永不认领，玩家周围柱子全部失灵。
        long[] packed = new long[this.boundStones.size()];
        for (int i = 0; i < packed.length; i++) {
            packed[i] = this.boundStones.get(i).asLong();
        }
        pTag.putLongArray("BoundStones", packed);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("Inventory"));
        this.tickCount = pTag.getInt("TickCount");
        this.boundStones.clear();
        long[] packed = pTag.getLongArray("BoundStones");
        for (long v : packed) {
            this.boundStones.add(BlockPos.of(v));
        }
        // 读档后立刻安排一次重扫（下一 tick 就会走scanCooldown>=20 的分支）
        this.scanCooldown = 19;
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