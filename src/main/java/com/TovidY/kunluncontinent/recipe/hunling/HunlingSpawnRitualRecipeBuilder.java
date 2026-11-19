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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 汇灵台「召唤生物」仪式的 datagen builder。
 *
 * <p>用法（3 腐肉摆基座、1 绿宝石当必需品、150 精神力、10 秒，召一只年限 1000 的僵尸）：</p>
 * <pre>
 * HunlingSpawnRitualRecipeBuilder.create(Items.EMERALD, "minecraft:zombie", Items.ZOMBIE_SPAWN_EGG)
 *         .ingredient(Items.ROTTEN_FLESH, 3)
 *         .spiritCost(150)
 *         .duration(200)          // 10 秒
 *         .nianxian(1000)
 *         .save(pWriter, KlMain.MOD_ID + ":hunling_spawn_zombie");
 * </pre>
 *
 * <p>带副产物、带召唤特效的写法：</p>
 * <pre>
 * HunlingSpawnRitualRecipeBuilder.create(Items.EMERALD, "minecraft:blaze", Items.BLAZE_SPAWN_EGG)
 *         .ingredient(Items.LAVA_BUCKET, 1, Items.BUCKET)   // 岩浆桶 → 空桶
 *         .ingredient(Items.BLAZE_POWDER, 2)
 *         .spiritCost(5000)
 *         .duration(600)
 *         .nianxian(50000)
 *         .spawnEffect("kunlun:boss_spawn")                 // 预留，后续接 BOSS 特效
 *         .save(pWriter, KlMain.MOD_ID + ":hunling_spawn_blaze");
 * </pre>
 *
 * <h3>参数速查</h3>
 * <table border="1">
 *   <tr><th>方法</th><th>含义</th><th>必填</th></tr>
 *   <tr><td>{@code create(必需品, 生物ID, 展示物)}</td>
 *       <td>必需品 + 字符串实体 id + <b>成品展示物</b>（JEI 产物槽用，一般写刷怪蛋）</td><td>✅</td></tr>
 *   <tr><td>{@code ingredient(物品, 数量)}</td><td>一条副物品（摆基座）</td><td>✅ 至少一条</td></tr>
 *   <tr><td>{@code ingredient(物品, 数量, 副产物)}</td><td>同上，消耗时返还副产物（水桶→空桶）</td><td>否</td></tr>
 *   <tr><td>{@code spiritCost(int)}</td><td>精神力总消耗</td><td>✅</td></tr>
 *   <tr><td>{@code duration(int)}</td><td>持续时间（tick，20 = 1 秒）</td><td>✅</td></tr>
 *   <tr><td>{@code nianxian(long)}</td><td>生物年限，属性由年限体系推导</td><td>✅</td></tr>
 *   <tr><td>{@code spawnEffect(String)}</td><td>召唤特效 id（预留，不写也能跑）</td><td>否</td></tr>
 *   <tr><td>{@code strictNbt()}</td><td>匹配时连 NBT 一起比</td><td>否</td></tr>
 * </table>
 */
public class HunlingSpawnRitualRecipeBuilder implements RecipeBuilder {

    private final ItemStack catalyst;
    private final ResourceLocation entityId;
    private final ItemStack displayItem;
    private final List<RitualIngredient> ingredients = new ArrayList<>();
    private int spiritCost = 0;
    private int durationTicks = 200;
    private long nianxian = 0L;
    private boolean strictNbt = false;
    @Nullable
    private String spawnEffect;
    private String group = "";

    private HunlingSpawnRitualRecipeBuilder(ItemStack catalyst, ResourceLocation entityId,
                                            ItemStack displayItem) {
        this.catalyst = catalyst;
        this.entityId = entityId;
        this.displayItem = displayItem;
    }

    /**
     * @param catalyst    必需品
     * @param entityId    真正的生成目标（字符串形式的实体 id）
     * @param displayItem 成品展示物 —— JEI 产物槽直接用它，<b>必填</b>。
     *                    一般写对应的刷怪蛋；没有刷怪蛋的生物（末影龙/凋灵）就写别的代表物。
     */
    public static HunlingSpawnRitualRecipeBuilder create(ItemLike catalyst, String entityId,
                                                         ItemLike displayItem) {
        ResourceLocation id = ResourceLocation.tryParse(entityId);
        if (id == null) {
            throw new IllegalArgumentException("无效的生物 ID: " + entityId);
        }
        return new HunlingSpawnRitualRecipeBuilder(new ItemStack(catalyst), id, new ItemStack(displayItem));
    }

    /** 用带 NBT 的 ItemStack（严格匹配时才有区别）。 */
    public static HunlingSpawnRitualRecipeBuilder create(ItemStack catalyst, String entityId,
                                                         ItemStack displayItem) {
        ResourceLocation id = ResourceLocation.tryParse(entityId);
        if (id == null) {
            throw new IllegalArgumentException("无效的生物 ID: " + entityId);
        }
        return new HunlingSpawnRitualRecipeBuilder(catalyst.copy(), id, displayItem.copy());
    }

    /** 一条副物品，无副产物。 */
    public HunlingSpawnRitualRecipeBuilder ingredient(ItemLike item, int count) {
        ingredients.add(new RitualIngredient(new ItemStack(item), count, ItemStack.EMPTY));
        return this;
    }

    /** 一条副物品 + 副产物（如「岩浆桶 → 空桶」）。 */
    public HunlingSpawnRitualRecipeBuilder ingredient(ItemLike item, int count, ItemLike remainder) {
        ingredients.add(new RitualIngredient(new ItemStack(item), count, new ItemStack(remainder)));
        return this;
    }

    public HunlingSpawnRitualRecipeBuilder spiritCost(int cost) {
        this.spiritCost = cost;
        return this;
    }

    /** 持续时间，单位 tick（200 = 10 秒）。 */
    public HunlingSpawnRitualRecipeBuilder duration(int ticks) {
        this.durationTicks = ticks;
        return this;
    }

    /** 生物年限。属性不写在配方里，交给年限体系推导。 */
    public HunlingSpawnRitualRecipeBuilder nianxian(long value) {
        this.nianxian = value;
        return this;
    }

    /** 召唤特效 id（预留）。 */
    public HunlingSpawnRitualRecipeBuilder spawnEffect(String id) {
        this.spawnEffect = id;
        return this;
    }

    public HunlingSpawnRitualRecipeBuilder strictNbt() {
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
                json.addProperty("entity", entityId.toString());
                json.addProperty("nianxian", nianxian);
                json.add("result_item", RitualIngredient.stackToJson(displayItem));
                if (spawnEffect != null && !spawnEffect.isEmpty()) {
                    json.addProperty("spawn_effect", spawnEffect);
                }
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
                return ModRecipes.HUNLING_SPAWN_SERIALIZER.get();
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
        return Items.AIR;
    }
}
