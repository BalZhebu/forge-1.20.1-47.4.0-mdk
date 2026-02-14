package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class LiandanRecipeSerializer implements RecipeSerializer<LiandanRecipe> {
    @Override
    public LiandanRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
        Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "ingredient"));
        JsonObject resultObj = GsonHelper.getAsJsonObject(pSerializedRecipe, "result");
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(resultObj, "item"));
        ItemStack result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemId));
        int cookTime = GsonHelper.getAsInt(pSerializedRecipe, "cookTime", 200);
        int recipeLevel = GsonHelper.getAsInt(pSerializedRecipe, "recipeLevel", 1);
        return new LiandanRecipe(pRecipeId, ingredient, result, cookTime, recipeLevel);
    }

    @Nullable
    @Override
    public LiandanRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
        ItemStack result = pBuffer.readItem();
        int cookTime = pBuffer.readInt();
        int recipeLevel = pBuffer.readInt();

        return new LiandanRecipe(pRecipeId, ingredient, result, cookTime, recipeLevel);
    }

    @Override
    public void toNetwork(FriendlyByteBuf pBuffer, LiandanRecipe pRecipe) {
        pRecipe.getIngredient().toNetwork(pBuffer);
        pBuffer.writeItem(pRecipe.getResultItem(null));
        pBuffer.writeInt(pRecipe.getCookTime());
        // 新增：向网络写入 level
        pBuffer.writeInt(pRecipe.getRecipeLevel());
    }
}