package com.TovidY.kunluncontinent.recipe.liandanlurecipe;

import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class LiandanRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private final Ingredient ingredient;
    private final int cookTime;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    public LiandanRecipeBuilder(ItemLike ingredient, ItemLike result, int cookTime) {
        this.ingredient = Ingredient.of(ingredient);
        this.result = result.asItem();
        this.cookTime = cookTime;
    }

    public static LiandanRecipeBuilder create(ItemLike ingredient, ItemLike result, int cookTime) {
        return new LiandanRecipeBuilder(ingredient, result, cookTime);
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.add("ingredient", ingredient.toJson());
                JsonObject resultObj = new JsonObject();
                resultObj.addProperty("item", ForgeRegistries.ITEMS.getKey(result).toString());
                json.add("result", resultObj);
                json.addProperty("cookTime", cookTime);
            }
            @Override public ResourceLocation getId() { return id; }
            @Override public RecipeSerializer<?> getType() { return ModRecipes.LIANDAN_SERIALIZER.get(); }
            @Nullable @Override public JsonObject serializeAdvancement() { return null; }
            @Nullable @Override public ResourceLocation getAdvancementId() { return null; }
        });
    }

    @Override public RecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) { return this; }
    @Override public RecipeBuilder group(@Nullable String group) { return this; }
    @Override public Item getResult() { return result; }
}
