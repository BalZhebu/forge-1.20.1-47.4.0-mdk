package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.AlchemicalCalculator;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    private int maxProgress = 200; // 默认值

    // 19个槽位：0-4内丹, 5-16输出, 17药渣块, 18可选
    private final ItemStackHandler itemHandler = new ItemStackHandler(19) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    // 同步给 Menu 和 Screen 的数据
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LiandanluBlockEntity.this.progress;
                case 1 -> LiandanluBlockEntity.this.maxProgress;
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
        public int getCount() {
            return 2;
        }
    };

    public LiandanluBlockEntity(BlockPos pPos, BlockState pBlockState, int tier) {
        super(ModBlockEntities.LIANDANLU_BE.get(), pPos, pBlockState);
        this.furnaceLevel = tier;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LiandanluBlockEntity entity) {
        // 构造临时容器匹配配方
        SimpleContainer container = new SimpleContainer(5);
        for (int i = 0; i < 5; i++) {
            container.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        Optional<LiandanRecipe> recipeOptional = level.getRecipeManager()
                .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, level);

        if (recipeOptional.isPresent()) {
            LiandanRecipe recipe = recipeOptional.get();
            if (canInsertResult(entity.itemHandler)) {
                // 核心：根据炉子和丹药等阶动态设定时长
                entity.maxProgress = entity.getAdjustedCookingTime(recipe);
                entity.progress++;

                if (entity.progress >= entity.maxProgress) {
                    executeCraft(entity, recipe);
                    entity.progress = 0;
                }
            }
        } else {
            entity.progress = 0;
        }
    }

    private int getAdjustedCookingTime(LiandanRecipe recipe) {
        int danLevel = recipe.getRecipeLevel(); // 需在Recipe类中实现此方法
        int luLevel = this.furnaceLevel;
        int finalTimeInSeconds;

        if (luLevel >= danLevel) {
            // 炉阶 >= 丹阶：基础10秒，每高出一阶减1秒
            finalTimeInSeconds = 10 - (luLevel - danLevel);
        } else {
            // 炉阶 < 丹阶：10秒 + (差值 * 10秒)
            finalTimeInSeconds = 10 + (danLevel - luLevel) * 30;
        }
        // 保底1秒，转为Tick
        return Math.max(finalTimeInSeconds, 1) * 20;
    }

    private static boolean canInsertResult(IItemHandler handler) {
        for (int i = 5; i <= 16; i++) {
            if (handler.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private static void executeCraft(LiandanluBlockEntity entity, LiandanRecipe recipe) {
        IItemHandlerModifiable handler = (IItemHandlerModifiable) entity.itemHandler;

        // 计算品质
        int finalQuality = AlchemicalCalculator.calculateResultQuality(handler);
        ItemStack resultStack = recipe.getResultItem(entity.level.registryAccess()).copy();
        resultStack.getOrCreateTag().putInt("DanYaoQuality", finalQuality);

        // 消耗材料
        for (int i = 0; i < 5; i++) {
            handler.extractItem(i, 1, false);
        }
        if (!handler.getStackInSlot(17).isEmpty()) {
            handler.extractItem(17, 1, false);
        }

        // 放入结果
        ItemStack remaining = resultStack;
        for (int i = 5; i <= 16; i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) break;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("炼丹炉");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        // 关键：将 this.data 传入 Menu
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
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        this.progress = pTag.getInt("Progress");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}