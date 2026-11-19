package com.TovidY.kunluncontinent.recipe.hunling;

import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 给汇灵台仪式写 datagen JSON 的 builder。
 *
 * <p>用法（测试配方：8 钻石摆在基座、1 钻石当必需品、1 万精神力、60 秒，产出钻石块）：</p>
 * <pre>
 * HunlingRitualRecipeBuilder.create(Items.DIAMOND, Items.DIAMOND_BLOCK)
 *         .ingredient(Items.DIAMOND, 8)
 *         .spiritCost(10000)
 *         .duration(1200)              // tick
 *         .save(pWriter, KlMain.MOD_ID + ":hunling_ritual_diamond");
 * </pre>
 *
 * <p>带副产物（消耗水桶返还空桶）：</p>
 * <pre>
 * .ingredient(Items.WATER_BUCKET, 2, Items.BUCKET)
 * </pre>
 *
 * <p>需要连 NBT 一起匹配时加 {@code .strictNbt()}。</p>
 */
public class HunlingRitualRecipeBuilder implements RecipeBuilder {

    private final ItemStack catalyst;
    private final ItemStack result;
    private final List<RitualIngredient> ingredients = new ArrayList<>();
    private int spiritCost = 0;
    private int durationTicks = 200;
    private boolean strictNbt = false;
    private String group = "";

    private HunlingRitualRecipeBuilder(ItemStack catalyst, ItemStack result) {
        this.catalyst = catalyst;
        this.result = result;
    }

    public static HunlingRitualRecipeBuilder create(ItemLike catalyst, ItemLike result) {
        return new HunlingRitualRecipeBuilder(new ItemStack(catalyst), new ItemStack(result));
    }

    /** 用带 NBT 的 ItemStack 当必需品（严格匹配时才有区别）。 */
    public static HunlingRitualRecipeBuilder create(ItemStack catalyst, ItemStack result) {
        return new HunlingRitualRecipeBuilder(catalyst.copy(), result.copy());
    }

    /**
     * 必需品用普通物品、成品用带数量的 ItemStack —— <b>产出多个</b>时用这个。
     *
     * <pre>
     * HunlingRitualRecipeBuilder.create(Items.GOLD_INGOT, new ItemStack(Blocks.GOLD_BLOCK, 3))
     * </pre>
     */
    public static HunlingRitualRecipeBuilder create(ItemLike catalyst, ItemStack result) {
        return new HunlingRitualRecipeBuilder(new ItemStack(catalyst), result.copy());
    }

    /** 一条副物品，无副产物。 */
    public HunlingRitualRecipeBuilder ingredient(ItemLike item, int count) {
        ingredients.add(new RitualIngredient(
                new ItemStack(item), count, ItemStack.EMPTY));
        return this;
    }

    /** 一条副物品 + 副产物（如「水桶 → 空桶」）。 */
    public HunlingRitualRecipeBuilder ingredient(ItemLike item, int count, ItemLike remainder) {
        ingredients.add(new RitualIngredient(
                new ItemStack(item), count, new ItemStack(remainder)));
        return this;
    }

    /** 精神力总消耗（会按持续时长平均扣完）。 */
    public HunlingRitualRecipeBuilder spiritCost(int cost) {
        this.spiritCost = cost;
        return this;
    }

    /** 仪式时长，单位 tick（1200 = 60 秒）。 */
    public HunlingRitualRecipeBuilder duration(int ticks) {
        this.durationTicks = ticks;
        return this;
    }

    /** 匹配时连 NBT 一起比（默认只看物品 id）。 */
    public HunlingRitualRecipeBuilder strictNbt() {
        this.strictNbt = true;
        return this;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.add("catalyst", RitualIngredient.stackToJson(catalyst));

                JsonArray array = new JsonArray();
                for (RitualIngredient ing : ingredients) {
                    array.add(RitualIngredient.toJson(ing));
                }
                json.add("ingredients", array);

                json.addProperty("spirit_cost", spiritCost);
                json.addProperty("duration", durationTicks);
                json.add("result", RitualIngredient.stackToJson(result));
                if (strictNbt) {
                    json.addProperty("strict_nbt", true);
                }
            }

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ModRecipes.HUNLING_RITUAL_SERIALIZER.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }

    @Override
    public RecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        this.group = groupName == null ? "" : groupName;
        return this;
    }

    @Override
    public Item getResult() {
        return result.getItem();
    }
}
