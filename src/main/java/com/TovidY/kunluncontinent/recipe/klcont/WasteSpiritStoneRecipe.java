package com.TovidY.kunluncontinent.recipe.klcont;

import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.spiritstone.SpiritStoneItem;
import com.TovidY.kunluncontinent.item.spiritstone.SpiritStoneQuality;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * 废品蕴灵石 → 废弃蕴灵石（{@code feiqi_spirit_stone}）。
 *
 * <p>品质为「废品」的蕴灵石榨不出任何修为，也别占背包：单独放在合成台里直接拆成粉末。
 * 和 {@code DrossConversionRecipe}（破碎丹药 → 丹渣）是同一套路子 —— 单格、看 NBT、出固定产物。</p>
 */
public class WasteSpiritStoneRecipe extends CustomRecipe {

    public WasteSpiritStoneRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int count = 0;
        ItemStack input = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                count++;
                input = stack;
            }
        }
        if (count != 1 || !(input.getItem() instanceof SpiritStoneItem)) {
            return false;
        }
        return SpiritStoneQuality.get(input) == SpiritStoneQuality.WASTE;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        return new ItemStack(ModItems.FEIQI_SPIRIT_STONE.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WASTE_SPIRIT_STONE_SERIALIZER.get();
    }

    // ==================== JEI 展示 ====================

    /** JEI 会跳过 {@code isSpecial() == true} 的配方，关掉它才能在 JEI 里看到这条。 */
    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        // ⚠️ 不能直接 Ingredient.of(Item...)：那样每个 ItemStack 都没有品质 NBT，
        //    SpiritStoneItem#getName 会退化成默认的「上品」，JEI 里就显示成【上品】九阶蕴灵石，
        //    和「只有废品才能分解」这条规则自相矛盾。这里逐颗写入 WASTE 再包成 Ingredient。
        Item[] stones = {
                ModItems.FIRST_SPIRIT_STONE.get(), ModItems.TWO_SPIRIT_STONE.get(),
                ModItems.THREE_SPIRIT_STONE.get(), ModItems.FOUR_SPIRIT_STONE.get(),
                ModItems.FIVE_SPIRIT_STONE.get(), ModItems.SIX_SPIRIT_STONE.get(),
                ModItems.SEVEN_SPIRIT_STONE.get(), ModItems.EIGHT_SPIRIT_STONE.get(),
                ModItems.NINE_SPIRIT_STONE.get()
        };
        ItemStack[] stacks = new ItemStack[stones.length];
        for (int i = 0; i < stones.length; i++) {
            ItemStack stack = new ItemStack(stones[i]);
            SpiritStoneQuality.set(stack, SpiritStoneQuality.WASTE);
            stacks[i] = stack;
        }
        list.add(Ingredient.of(stacks));
        return list;
    }

    @Override
    public ItemStack getResultItem(@Nullable RegistryAccess access) {
        return new ItemStack(ModItems.FEIQI_SPIRIT_STONE.get());
    }
}
