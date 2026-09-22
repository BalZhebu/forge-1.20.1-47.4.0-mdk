package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.block.ModBlockEntities;
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
                    ItemStack stack = LiandanluBlockEntity.this.itemHandler.getStackInSlot(18);
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
        SimpleContainer container = new SimpleContainer(5);
        for (int i = 0; i < 5; i++) container.setItem(i, entity.itemHandler.getStackInSlot(i));

        Optional<LiandanRecipe> recipeOptional = level.getRecipeManager()
                .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, level);

        if (recipeOptional.isPresent()) {
            LiandanRecipe recipe = recipeOptional.get();
            ItemStack bottleStack = entity.itemHandler.getStackInSlot(18);

            if (bottleStack.getItem() instanceof SoulGatheringBottleItem bottle &&
                    bottle.getNengliang(null, bottleStack) >= recipe.getEnergyCost() &&
                    canInsertResult(entity.itemHandler)) {

                entity.maxProgress = entity.getAdjustedCookingTime(recipe);
                entity.progress++;

                if (entity.progress >= entity.maxProgress) {
                    executeCraft(entity, recipe, bottle, bottleStack);
                    entity.progress = 0;
                }
            }
        } else {
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

        bottle.setNengliang(null, bottleStack, bottle.getNengliang(null, bottleStack) - recipe.getEnergyCost());

        IItemHandlerModifiable handler = (IItemHandlerModifiable) entity.itemHandler;
        int finalQuality = AlchemicalCalculator.calculateResultQuality(handler);
        ItemStack resultStack = recipe.getResultItem(entity.level.registryAccess()).copy();
        resultStack.getOrCreateTag().putInt("DanYaoQuality", finalQuality);

        for (int i = 0; i < 5; i++) handler.extractItem(i, 1, false);
        if (!handler.getStackInSlot(17).isEmpty()) handler.extractItem(17, 1, false);

        ItemStack remaining = resultStack;
        for (int i = 5; i <= 16; i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) break;
        }

        // 成就：把功劳记给炼丹炉旁边最近的玩家
        if (entity.level != null && !entity.level.isClientSide) {
            Player crafter = entity.level.getNearestPlayer(
                    entity.getBlockPos().getX() + 0.5D,
                    entity.getBlockPos().getY() + 0.5D,
                    entity.getBlockPos().getZ() + 0.5D,
                    8.0D, false);
            AchievementAPI.onAlchemySuccess(crafter, finalQuality);
        }

        entity.setChanged(); // 确保数据持久化
    }

    private int getAdjustedCookingTime(LiandanRecipe recipe) {
        int danLevel = recipe.getRecipeLevel();
        int luLevel = this.furnaceLevel;
        int baseTicks = recipe.getCookTime();
        if (recipe.isSpecial()) {
            return baseTicks;
        }

        float speedMultiplier;
        if (luLevel >= danLevel) {
            speedMultiplier = 1.0f + (luLevel - danLevel) * 0.5f;
            return (int) Math.max(baseTicks / speedMultiplier, 20);
        } else {
            int levelDiff = danLevel - luLevel;
            long penaltyTicks = (long) baseTicks * (long) Math.pow(2, levelDiff);
            return (int) Math.min(penaltyTicks, 72000);
        }
    }

    private static boolean canInsertResult(IItemHandler handler) {
        for (int i = 5; i <= 16; i++) {
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


    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}