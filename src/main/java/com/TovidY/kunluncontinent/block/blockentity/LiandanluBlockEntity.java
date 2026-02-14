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

    private int progress = 0;
    private int maxProgress = 400;

    // 定义 19 个槽位
    private final ItemStackHandler itemHandler = new ItemStackHandler(19) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged(); // 物品变化时标记保存
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public LiandanluBlockEntity(BlockPos pPos, BlockState pBlockState) {
        // 假设你已经注册了 BLOCK_ENTITY_TYPE
        super(ModBlockEntities.LIANDANLU_BE.get(), pPos, pBlockState);
    }

    // 这是每 tick 运行一次的服务器端逻辑
    public static void serverTick(Level level, BlockPos pos, BlockState state, LiandanluBlockEntity entity) {
        // 1. 获取当前槽位里的所有物品（封装成 SimpleContainer 方便配方匹配）
        SimpleContainer container = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i = 0; i < entity.itemHandler.getSlots(); i++) {
            container.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        // 2. 尝试从世界中寻找匹配的自定义配方 (kunlun:liandan)
        Optional<LiandanRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.LIANDAN_TYPE.get(), container, level);

        if (recipe.isPresent()) {
            // 3. 检查输出槽是否已满（5-16 槽位是否还有空间）
            if (canInsertResult(entity.itemHandler)) {
                entity.progress++;
                entity.maxProgress = recipe.get().getCookTime();

                // 4. 进度条跑满，执行炼制
                if (entity.progress >= entity.maxProgress) {
                    executeCraft(entity, recipe.get());
                    entity.progress = 0; // 重置进度
                }
            }
        } else {
            // 如果配方不匹配或材料被中途拿走，重置进度
            entity.progress = 0;
        }
    }

    private static boolean canInsertResult(IItemHandler handler) {
        // 检查 5-16 槽位是否有任意一个空格子
        for (int i = 5; i <= 16; i++) {
            if (handler.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private static void executeCraft(LiandanluBlockEntity entity, LiandanRecipe recipe) {
        IItemHandlerModifiable handler = (IItemHandlerModifiable) entity.itemHandler;

        int finalQuality = AlchemicalCalculator.calculateResultQuality(handler);
        ItemStack resultStack = recipe.getResultItem(entity.level.registryAccess()).copy();
        resultStack.getOrCreateTag().putInt("DanYaoQuality", finalQuality);

        for (int i = 0; i < 5; i++) {
            handler.extractItem(i, 1, false);
        }

        if (!handler.getStackInSlot(17).isEmpty()) {
            handler.extractItem(17, 1, false);
        }

        ItemStack remaining = resultStack;
        for (int i = 5; i <= 16; i++) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) break; // 全部放进去了，退出循环
        }
    }

    public float getEfficiencyMultiplier() {
        Block block = this.getBlockState().getBlock();
        if (block == ModBlocks.LIANDANLU1.get()) return 1.0f;
        if (block == ModBlocks.LIANDANLU2.get()) return 1.5f;
        if (block == ModBlocks.LIANDANLU3.get()) return 2.0f;
        return 1.0f;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("炼丹炉");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new LiandanluMenu(pContainerId, pPlayerInventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    // 保存数据
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(pTag);
    }

    // 读取数据
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
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
