package com.TovidY.kunluncontinent.recipe.hunling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 汇灵台仪式配方的序列化。
 *
 * <p>JSON 长相：</p>
 * <pre>
 * {
 *   "type": "kunlun:hunling_ritual",
 *   "catalyst":  { "item": "minecraft:diamond" },
 *   "ingredients": [
 *     { "item": "minecraft:diamond", "count": 8 },
 *     { "item": "minecraft:water_bucket", "count": 2, "remainder": "minecraft:bucket" }
 *   ],
 *   "spirit_cost": 10000,
 *   "duration": 1200,
 *   "result": { "item": "minecraft:diamond_block" },
 *   "strict_nbt": false
 * }
 * </pre>
 *
 * <p>{@code catalyst} / {@code ingredients[].item} / {@code result} 都支持可选的 {@code "nbt"}
 * 字段（SNBT 字符串）；{@code remainder} 也可以带 {@code "remainder_nbt"}。
 * {@code strict_nbt} 不写默认 false = 只比物品 id。</p>
 */
public class HunlingRitualSerializer implements RecipeSerializer<HunlingRitualRecipe> {

    @Override
    public HunlingRitualRecipe fromJson(ResourceLocation id, JsonObject json) {
        JsonObject catalystObj = GsonHelper.getAsJsonObject(json, "catalyst");
        ItemStack catalyst = RitualIngredient.parseStack(catalystObj, "item", "nbt", 1);

        List<RitualIngredient> ingredients = new ArrayList<>();
        if (json.has("ingredients")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients")) {
                ingredients.add(RitualIngredient.fromJson(element.getAsJsonObject()));
            }
        }

        int spiritCost = GsonHelper.getAsInt(json, "spirit_cost", 0);
        int duration = GsonHelper.getAsInt(json, "duration", 200);
        boolean strictNbt = GsonHelper.getAsBoolean(json, "strict_nbt", false);

        JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result");
        int resultCount = Math.max(1, GsonHelper.getAsInt(resultObj, "count", 1));
        ItemStack result = RitualIngredient.parseStack(resultObj, "item", "nbt", resultCount);

        return new HunlingRitualRecipe(id, catalyst, ingredients, spiritCost, duration, result, strictNbt);
    }

    @Nullable
    @Override
    public HunlingRitualRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        ItemStack catalyst = buffer.readItem();

        int size = buffer.readVarInt();
        List<RitualIngredient> ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = buffer.readItem();
            int count = buffer.readVarInt();
            ItemStack remainder = buffer.readItem();
            ingredients.add(new RitualIngredient(stack, count, remainder));
        }

        int spiritCost = buffer.readVarInt();
        int duration = buffer.readVarInt();
        ItemStack result = buffer.readItem();
        boolean strictNbt = buffer.readBoolean();

        return new HunlingRitualRecipe(id, catalyst, ingredients, spiritCost, duration, result, strictNbt);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, HunlingRitualRecipe recipe) {
        buffer.writeItem(recipe.getCatalyst());

        List<RitualIngredient> ingredients = recipe.getRitualIngredients();
        buffer.writeVarInt(ingredients.size());
        for (RitualIngredient ing : ingredients) {
            buffer.writeItem(ing.stack());
            buffer.writeVarInt(ing.count());
            buffer.writeItem(ing.remainder());
        }

        buffer.writeVarInt(recipe.getSpiritCost());
        buffer.writeVarInt(recipe.getDurationTicks());
        buffer.writeItem(recipe.getResult());
        buffer.writeBoolean(recipe.isStrictNbt());
    }
}
