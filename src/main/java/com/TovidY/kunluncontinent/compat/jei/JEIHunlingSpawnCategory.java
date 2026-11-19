package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.recipe.hunling.HunlingSpawnRitualRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * JEI 里的汇灵台仪式页（<b>召唤生物</b>版）。
 *
 * <p>布局与物品版完全共用（{@link JEIHunlingCategory} 里那套静态常量与方法），
 * 唯一区别是产物槽的内容。</p>
 *
 * <h3>产物槽为什么直接用配方里的 {@code result_item}</h3>
 * <p>最初这里是从实体 id 反推 {@code SpawnEggItem.byId(...)}，但有三个坑：
 * 末影龙/凋灵这类没有刷怪蛋、自定义刷怪蛋未必继承 {@code SpawnEggItem}、
 * 以及有时根本不想展示成蛋。所以改成配方必填一个「成品展示物」，这里只负责照画。</p>
 */
public class JEIHunlingSpawnCategory implements IRecipeCategory<HunlingSpawnRitualRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(KlMain.MOD_ID, "hunling_spawn_ritual");
    public static final RecipeType<HunlingSpawnRitualRecipe> TYPE =
            new RecipeType<>(UID, HunlingSpawnRitualRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    private final ItemStack baseIcon;
    private final ItemStack altarIcon;

    public JEIHunlingSpawnCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(JEIHunlingCategory.WIDTH, JEIHunlingCategory.HEIGHT);
        this.baseIcon = new ItemStack(ModBlocks.HUNLING_ALTAR_BASE.get());
        this.altarIcon = new ItemStack(ModBlocks.HUNLING_ALTAR.get());
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.altarIcon.copy());
    }

    @Override
    public RecipeType<HunlingSpawnRitualRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("汇灵台仪式 · 召唤");
    }

    @Override
    public int getWidth() {
        return JEIHunlingCategory.WIDTH;
    }

    @Override
    public int getHeight() {
        return JEIHunlingCategory.HEIGHT;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(HunlingSpawnRitualRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        this.background.draw(graphics, 0, 0);
        JEIHunlingCategory.drawCommonFrame(graphics);
        JEIHunlingCategory.drawFooter(graphics, recipe.getSpiritCost(), recipe.getDurationTicks());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HunlingSpawnRitualRecipe recipe, IFocusGroup focuses) {
        JEIHunlingCategory.fillBaseSlots(builder, recipe.getRitualIngredients(), this.baseIcon);
        JEIHunlingCategory.fillCenterSlots(builder, recipe.getCatalyst(), this.altarIcon);

        builder.addSlot(RecipeIngredientRole.OUTPUT,
                        JEIHunlingCategory.RESULT_X, JEIHunlingCategory.RESULT_Y)
                .addItemStack(recipe.getDisplayItem());
    }
}
