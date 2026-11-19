package com.TovidY.kunluncontinent.recipe;

import com.TovidY.kunluncontinent.recipe.klcont.BottleRefillRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitualRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingRitualSerializer;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingSpawnRitualRecipe;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingSpawnRitualSerializer;
import com.TovidY.kunluncontinent.recipe.klcont.SpiritStoneInfuseRecipe;
import com.TovidY.kunluncontinent.recipe.klcont.WasteSpiritStoneRecipe;
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

    // ==================== 汇灵台仪式 ====================

    /** 汇灵台仪式配方序列化器（JSON 结构较复杂，手写）。 */
    public static final RegistryObject<RecipeSerializer<HunlingRitualRecipe>> HUNLING_RITUAL_SERIALIZER =
            SERIALIZERS.register("hunling_ritual", HunlingRitualSerializer::new);

    public static final RegistryObject<RecipeType<HunlingRitualRecipe>> HUNLING_RITUAL_TYPE =
            TYPES.register("hunling_ritual", () -> new RecipeType<>() {
                @Override public String toString() { return "hunling_ritual"; }
            });

    // ---- 汇灵台仪式 · 召唤生物版 ----

    public static final RegistryObject<RecipeSerializer<HunlingSpawnRitualRecipe>> HUNLING_SPAWN_SERIALIZER =
            SERIALIZERS.register("hunling_spawn_ritual", HunlingSpawnRitualSerializer::new);

    public static final RegistryObject<RecipeType<HunlingSpawnRitualRecipe>> HUNLING_SPAWN_TYPE =
            TYPES.register("hunling_spawn_ritual", () -> new RecipeType<>() {
                @Override public String toString() { return "hunling_spawn_ritual"; }
            });

    public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> BOTTLE_REFILL_SERIALIZER =
            SERIALIZERS.register("bottle_refill", () -> new SimpleCraftingRecipeSerializer<>(BottleRefillRecipe::new));

    /** 蕴灵石 → 聚魂瓶（能量 1:1 转移，保留瓶子等级与已有能量）。 */
    public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> SPIRIT_STONE_INFUSE_SERIALIZER =
            SERIALIZERS.register("spirit_stone_infuse",
                    () -> new SimpleCraftingRecipeSerializer<>(SpiritStoneInfuseRecipe::new));

    /** 废品蕴灵石 → 废弃蕴灵石。 */
    public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> WASTE_SPIRIT_STONE_SERIALIZER =
            SERIALIZERS.register("waste_spirit_stone",
                    () -> new SimpleCraftingRecipeSerializer<>(WasteSpiritStoneRecipe::new));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);

    }
}
