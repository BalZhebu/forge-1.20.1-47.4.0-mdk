package com.TovidY.kunluncontinent.recipe;

import com.TovidY.kunluncontinent.recipe.klcont.BottleRefillRecipe;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.DrossConversionRecipe;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "kunlun");
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "kunlun");

    public static final RegistryObject<RecipeSerializer<LiandanRecipe>> LIANDAN_SERIALIZER =
            SERIALIZERS.register("liandan", () -> new LiandanRecipeSerializer());

    public static final RegistryObject<RecipeSerializer<DrossConversionRecipe>> DROSS_CONVERSION_SERIALIZER =
            SERIALIZERS.register("dross_conversion", () -> new SimpleCraftingRecipeSerializer<>(DrossConversionRecipe::new));

    public static final RegistryObject<RecipeType<LiandanRecipe>> LIANDAN_TYPE =
            TYPES.register("liandan", () -> new RecipeType<>() {
                @Override public String toString() { return "liandan"; }
            });

    public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> BOTTLE_REFILL_SERIALIZER =
            SERIALIZERS.register("bottle_refill", () -> new SimpleCraftingRecipeSerializer<>(BottleRefillRecipe::new));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);

    }
}
