package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.item.klitem.DanYaoQuality;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.AlchemicalCalculator;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Optional;

public class LiandanluBlockEntity extends BlockEntity implements MenuProvider {

    /** 药渣槽在 ItemHandler 中的下标。 */
    public static final int DROSS_SLOT = AlchemicalCalculator.DROSS_SLOT;
    /** 聚魂瓶槽在 ItemHandler 中的下标。 */
    public static final int BOTTLE_SLOT = 18;
    /** 产出槽区间 [5, 16]。 */
    public static final int RESULT_SLOT_START = 5;
    public static final int RESULT_SLOT_END = 16;

    private final int furnaceLevel;
    private int progress = 0;
    private int maxProgress = 200;

    private final ItemStackHandler itemHandler = new ItemStackHandler(19) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LiandanluBlockEntity.this.progress;
                case 1 -> LiandanluBlockEntity.this.maxProgress;
                case 2 -> {
                    ItemStack stack = LiandanluBlockEntity.this.itemHandler.getStackInSlot(BOTTLE_SLOT);
                    yield stack.isEmpty() ? 0 : 1;
                }
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LiandanluBlockEntity.this.progress = value;
                case 1 -> LiandanluBlockEntity.this.maxProgress = value;
            }
        }
        @Override
        public int getCount() { return 3; }
    };

    public LiandanluBlockEntity(BlockPos pPos, BlockState pBlockState, int tier) {
        super(ModBlockEntities.LIANDANLU_BE.get(), pPos, pBlockState);
        this.furnaceLevel = tier;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LiandanluBlockEntity entity) {
        // 1. 获取配方
        SimpleContainer container = new SimpleContainer(AlchemicalCalculator.MATERIAL_SLOTS);
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            container.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<LiandanRecipe> recipeOptional = level.getRecipeManager()
                .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, level);

        if (recipeOptional.isEmpty()) {
            entity.progress = 0;
            return;
        }

        LiandanRecipe recipe = recipeOptional.get();
        ItemStack bottleStack = entity.itemHandler.getStackInSlot(BOTTLE_SLOT);

        if (!(bottleStack.getItem() instanceof SoulGatheringBottleItem bottle)
                || bottle.getNengliang(null, bottleStack) < recipe.getEnergyCost()
                || !canInsertResult(entity.itemHandler)) {
            // 材料/能量/输出任一不满足 → 进度清零，避免「攒了一半进度换材料」的白嫖
            entity.progress = 0;
            return;
        }

        entity.maxProgress = entity.getAdjustedCookingTime(recipe);
        entity.progress++;

        if (entity.progress >= entity.maxProgress) {
            executeCraft(entity, recipe, bottle, bottleStack);
            entity.progress = 0;
        }
    }

    // 在 LiandanluBlockEntity 类中添加
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            // 强制通知附近的所有观察者（包括打开界面的玩家）该方块数据已更新
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    private static void executeCraft(LiandanluBlockEntity entity, LiandanRecipe recipe, SoulGatheringBottleItem bottle, ItemStack bottleStack) {
        if (bottle.getNengliang(null, bottleStack) < recipe.getEnergyCost()) return;

        IItemHandlerModifiable handler = (IItemHandlerModifiable) entity.itemHandler;

        // 先算概率再扣能量：随机只消耗一次，且失败不会白扣
        double[] weights = AlchemicalCalculator.computeWeights(
                handler, recipe.isSpecial(), entity.furnaceLevel, recipe.getRecipeLevel());
        int finalQuality = AlchemicalCalculator.roll(weights, entity.level.getRandom());

        bottle.setNengliang(null, bottleStack, bottle.getNengliang(null, bottleStack) - recipe.getEnergyCost());

        ItemStack resultStack = recipe.getResultItem(entity.level.registryAccess()).copy();
        resultStack.getOrCreateTag().putInt("DanYaoQuality", finalQuality);

        // 扣材料：⭐ 内丹有 NEIDAN_KEEP_CHANCE 的概率「保住」，即不消耗这一颗。
        //    每格独立掷骰，5 格全是内丹时至少保住 1 颗 ≈ 67%，期望省下 20%。
        //    草药 / 药渣等非内丹材料照常消耗 —— 只有 NeidanItem 吃这个折扣。
        int keptNeidan = 0;
        for (int i = 0; i < AlchemicalCalculator.MATERIAL_SLOTS; i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (inSlot.getItem() instanceof NeidanItem
                    && entity.level.getRandom().nextFloat() < AlchemicalCalculator.NEIDAN_KEEP_CHANCE) {
                keptNeidan++;
                continue;
            }
            handler.extractItem(i, 1, false);
        }
        if (!handler.getStackInSlot(DROSS_SLOT).isEmpty()) handler.extractItem(DROSS_SLOT, 1, false);

        ItemStack remaining = resultStack;
        for (int i = RESULT_SLOT_START; i <= RESULT_SLOT_END; i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) break;
        }

        if (entity.level != null && !entity.level.isClientSide) {
            Player crafter = entity.level.getNearestPlayer(
                    entity.getBlockPos().getX() + 0.5D,
                    entity.getBlockPos().getY() + 0.5D,
                    entity.getBlockPos().getZ() + 0.5D,
                    8.0D, false);
            if (crafter != null) {
                AchievementAPI.onAlchemySuccess(crafter, finalQuality);
                // 保住了内丹就给个反馈，否则玩家看不出这个机制有没有生效
                if (keptNeidan > 0) {
                    crafter.displayClientMessage(Component.literal(
                            "§b炼丹余韵：§f" + keptNeidan + " §b颗内丹未被消耗"), true);
                }
            }
        }

        entity.setChanged();
    }

    /**
     * 炉温对耗时的影响：炉等级 ≥ 丹方等级 → 加速；低于 → 惩罚。
     *
     * <p>旧实现惩罚是 {@code 2^levelDiff}（最高 2^8=256 倍，配 72000 tick 上限 = 整整一小时）。
     * 这里改成温和的线性加速 + 对数惩罚，最高约 4 倍（400 → 1600 tick ≈ 80 秒）。</p>
     */
    private int getAdjustedCookingTime(LiandanRecipe recipe) {
        int baseTicks = Math.max(20, recipe.getCookTime());
        if (recipe.isSpecial()) {
            return baseTicks;
        }

        int danLevel = recipe.getRecipeLevel();
        int luLevel = this.furnaceLevel;

        if (luLevel >= danLevel) {
            float speedMultiplier = 1.0f + (luLevel - danLevel) * 0.35f;
            return (int) Math.max(baseTicks / speedMultiplier, 20);
        }
        int levelDiff = danLevel - luLevel;
        double penalty = Math.pow(1.35, Math.min(levelDiff, 8));
        return Math.min((int) (baseTicks * penalty), 3600);
    }

    private static boolean canInsertResult(IItemHandler handler) {
        for (int i = RESULT_SLOT_START; i <= RESULT_SLOT_END; i++) {
            if (handler.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }


    @Override public Component getDisplayName() { return Component.literal("炼丹炉"); }
    @Nullable @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new LiandanluMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("FurnaceLevel", this.furnaceLevel);
        pTag.putInt("Progress", this.progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("inventory")) {
            this.itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        }
        this.progress = pTag.getInt("Progress");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    // 在 LiandanluBlockEntity.java
    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    public int getFurnaceLevel() {
        return this.furnaceLevel;
    }


    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}