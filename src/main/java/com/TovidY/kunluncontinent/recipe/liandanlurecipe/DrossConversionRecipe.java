package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.ModTags;
import com.TovidY.kunluncontinent.item.klitem.DanYaoQuality;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DrossConversionRecipe extends CustomRecipe {
    public DrossConversionRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int count = 0;
        ItemStack input = ItemStack.EMPTY;

        // 遍历合成表中的所有格位
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                count++;
                input = stack;
            }
        }

        // 只有当格位里只有一个物品，且该物品属于丹药标签，且品质为“破碎”时才匹配
        if (count == 1 && input.is(ModTags.Items.DANYAO_DROSS)) {
            // 这里检查 NBT，必须是 PO_SUI 的 ordinal (0)
            if (input.hasTag() && input.getTag().contains("DanYaoQuality")) {
                return input.getTag().getInt("DanYaoQuality") == DanYaoQuality.PO_SUI.ordinal();
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        // 匹配成功，返回丹渣物品
        return new ItemStack(ModItems.DROSS.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        // 你需要在 ModRecipes 里注册这个序列化器
        return ModRecipes.DROSS_CONVERSION_SERIALIZER.get();
    }
}
