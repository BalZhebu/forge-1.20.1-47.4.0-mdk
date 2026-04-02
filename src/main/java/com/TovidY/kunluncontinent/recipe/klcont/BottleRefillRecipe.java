package com.TovidY.kunluncontinent.recipe.klcont;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class BottleRefillRecipe extends CustomRecipe {
    public BottleRefillRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private boolean isRepairable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return ModItems.COLDPROTECTIONLIST.stream()
                .anyMatch(reg -> stack.is(reg.get()));
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack bottle = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;
        int count = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            count++;

            if (stack.getItem() instanceof SoulGatheringBottleItem) {
                bottle = stack;
            } else if (isRepairable(stack)) {
                target = stack;
            }
        }

        return count == 2 && !bottle.isEmpty() && !target.isEmpty() &&
                bottle.getOrCreateTag().getInt("sh_nengliang") > 0 && target.isDamaged();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack bottle = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof SoulGatheringBottleItem) {
                bottle = stack;
            } else if (isRepairable(stack)) {
                target = stack.copy(); // 必须 copy，否则会修改原物品
            }
        }

        int energy = bottle.getOrCreateTag().getInt("sh_nengliang");
        int needed = target.getDamageValue();

        // 动态计算：有多少能量补多少耐久
        int refill = Math.min(energy, needed);
        target.setDamageValue(target.getDamageValue() - refill);

        return target;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remain = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.getItem() instanceof SoulGatheringBottleItem) {
                // 寻找对应的护身符来计算消耗
                ItemStack targetStack = ItemStack.EMPTY;
                for (int j = 0; j < container.getContainerSize(); j++) {
                    if (isRepairable(container.getItem(j))) {
                        targetStack = container.getItem(j);
                        break;
                    }
                }
                if (!targetStack.isEmpty()) {
                    ItemStack newBottle = stack.copy();
                    int currentEnergy = newBottle.getOrCreateTag().getInt("sh_nengliang");
                    int needed = targetStack.getDamageValue();
                    int consumed = Math.min(currentEnergy, needed);
                    newBottle.getOrCreateTag().putInt("sh_nengliang", currentEnergy - consumed);
                    remain.set(i, newBottle);
                }
            } else {
                remain.set(i, ForgeHooks.getCraftingRemainingItem(stack));
            }
        }
        return remain;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BOTTLE_REFILL_SERIALIZER.get();
    }
}