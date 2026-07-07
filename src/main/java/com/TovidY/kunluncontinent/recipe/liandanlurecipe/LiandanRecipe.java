package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.TovidY.kunluncontinent.item.klitem.DanYaoItem;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class LiandanRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient ingredient; // 要求的内丹种类
    private final ItemStack result;      // 产出的丹药基础物品
    private final int cookTime;          // 炼制耗时
    private final int energyCost;

    private final boolean isSpecial;

    private final int recipeLevel;

    public LiandanRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result,int cookTime,int recipeLevel,boolean isSpecial, int energyCost) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.cookTime = cookTime;
        this.energyCost = energyCost;
        this.recipeLevel = recipeLevel;

        this.isSpecial = isSpecial;
    }

    public int getRecipeLevel() {
        if (this.result.getItem() instanceof DanYaoItem danyao) {
            return danyao.getTier();
        }
        return 1;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level level) {
        for (int i = 0; i < 5; i++) {
            if (!ingredient.test(inv.getItem(i))) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return result.copy();
    }

    @Override public boolean canCraftInDimensions(int pWidth, int pHeight) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess pRegistryAccess) { return result; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.LIANDAN_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.LIANDAN_TYPE.get(); }

    public Ingredient getIngredient() {
        return ingredient;
    }
    public int getCookTime() { return cookTime; }
}