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
 * 召唤生物版仪式的序列化。
 *
 * <p>JSON 长相：</p>
 * <pre>
 * {
 *   "type": "kunlun:hunling_spawn_ritual",
 *   "catalyst":  { "item": "minecraft:emerald" },
 *   "ingredients": [
 *     { "item": "minecraft:rotten_flesh", "count": 3, "remainder": "minecraft:bucket" }
 *   ],
 *   "spirit_cost": 150,
 *   "duration": 200,
 *   "entity": "minecraft:zombie",
 *   "nianxian": 1000,
 *   "result_item": { "item": "minecraft:zombie_spawn_egg" },
 *   "spawn_effect": "kunlun:boss_spawn",
 *   "strict_nbt": false
 * }
 * </pre>
 *
 * <p>{@code result_item} 是<b>必填</b>的成品展示物（一般写对应的刷怪蛋），JEI 产物槽直接用它，
 * 不再从实体 id 反推。{@code spawn_effect} 是<b>可选</b>的预留字段（后续接 BOSS 召唤特效）。
 * {@code nianxian} 不写默认 0（= 生物的基础形态，属性按年限 0 推导）。</p>
 */
public class HunlingSpawnRitualSerializer implements RecipeSerializer<HunlingSpawnRitualRecipe> {

    @Override
    public HunlingSpawnRitualRecipe fromJson(ResourceLocation id, JsonObject json) {
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

        String entityRaw = GsonHelper.getAsString(json, "entity");
        ResourceLocation entityId = ResourceLocation.tryParse(entityRaw);
        if (entityId == null) {
            throw new IllegalArgumentException("无效的生物 ID: " + entityRaw);
        }

        long nianxian = GsonHelper.getAsLong(json, "nianxian", 0L);

        // 成品展示物：必填。缺了就给一条能看懂的错，而不是默默显示空气
        if (!json.has("result_item")) {
            throw new IllegalArgumentException("仪式配方 " + id + " 缺少必填字段 result_item（成品展示物）");
        }
        JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result_item");
        ItemStack displayItem = RitualIngredient.parseStack(resultObj, "item", "nbt",
                Math.max(1, GsonHelper.getAsInt(resultObj, "count", 1)));

        String spawnEffect = json.has("spawn_effect")
                ? GsonHelper.getAsString(json, "spawn_effect")
                : null;

        return new HunlingSpawnRitualRecipe(id, catalyst, ingredients, spiritCost, duration,
                strictNbt, entityId, nianxian, displayItem, spawnEffect);
    }

    @Nullable
    @Override
    public HunlingSpawnRitualRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
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
        boolean strictNbt = buffer.readBoolean();

        ResourceLocation entityId = buffer.readResourceLocation();
        long nianxian = buffer.readVarLong();
        ItemStack displayItem = buffer.readItem();
        String spawnEffect = buffer.readBoolean() ? buffer.readUtf() : null;

        return new HunlingSpawnRitualRecipe(id, catalyst, ingredients, spiritCost, duration,
                strictNbt, entityId, nianxian, displayItem, spawnEffect);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, HunlingSpawnRitualRecipe recipe) {
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
        buffer.writeBoolean(recipe.isStrictNbt());

        buffer.writeResourceLocation(recipe.getEntityId());
        buffer.writeVarLong(recipe.getNianxian());
        buffer.writeItem(recipe.getDisplayItem());
        String effect = recipe.getSpawnEffect();
        buffer.writeBoolean(effect != null);
        if (effect != null) {
            buffer.writeUtf(effect);
        }
    }
}
