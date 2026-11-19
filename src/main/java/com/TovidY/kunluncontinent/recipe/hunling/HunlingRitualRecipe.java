package com.TovidY.kunluncontinent.recipe.hunling;

import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 汇灵台仪式 · <b>产出物品</b>版：必需品放上台，仪式结束后变成成品。
 *
 * <h3>参数</h3>
 * <ul>
 *   <li><b>必需品</b>（catalyst）：最后摆到汇灵台上的那件，制作时会被成品替换掉；</li>
 *   <li><b>副物品</b>（ingredients）：摆基座上的，逐件消耗。每条可带
 *       <b>副产物</b>（remainder，如「水桶 → 空桶」），消耗时从基座位置弹出；</li>
 *   <li><b>精神力消耗</b>（spiritCost）：最终数值，按 {@code 总量 / 时长} 逐 tick 扣；</li>
 *   <li><b>制作时长</b>（duration）：tick 数；</li>
 *   <li><b>严格 NBT</b>（strictNbt）：默认 false = 只看物品 id。</li>
 * </ul>
 *
 * <p>匹配逻辑在 {@link HunlingRitual} 接口里，这里只负责「产物是什么」。</p>
 */
public class HunlingRitualRecipe implements Recipe<Container>, HunlingRitual {

    private final ResourceLocation id;
    private final ItemStack catalyst;
    private final List<RitualIngredient> ingredients;
    private final int spiritCost;
    private final int durationTicks;
    private final ItemStack result;
    private final boolean strictNbt;

    public HunlingRitualRecipe(ResourceLocation id, ItemStack catalyst, List<RitualIngredient> ingredients,
                               int spiritCost, int durationTicks, ItemStack result, boolean strictNbt) {
        this.id = id;
        this.catalyst = catalyst;
        this.ingredients = List.copyOf(ingredients);
        this.spiritCost = Math.max(0, spiritCost);
        this.durationTicks = Math.max(1, durationTicks);
        this.result = result;
        this.strictNbt = strictNbt;
    }

    // ==================== 仪式参数 ====================

    public ItemStack getResult() {
        return result;
    }

    @Override
    public ItemStack getCatalyst() {
        return catalyst;
    }

    @Override
    public List<RitualIngredient> getRitualIngredients() {
        return ingredients;
    }

    @Override
    public int getSpiritCost() {
        return spiritCost;
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public boolean isStrictNbt() {
        return strictNbt;
    }

    // ==================== Recipe 接口（不被容器使用） ====================

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(catalyst));
        for (RitualIngredient ing : ingredients) {
            list.add(Ingredient.of(ing.stack()));
        }
        return list;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.HUNLING_RITUAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<HunlingRitualRecipe> getType() {
        return ModRecipes.HUNLING_RITUAL_TYPE.get();
    }
}
