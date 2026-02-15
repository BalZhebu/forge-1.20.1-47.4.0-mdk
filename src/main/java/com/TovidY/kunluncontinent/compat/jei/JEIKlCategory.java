package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JEIKlCategory implements IRecipeCategory<LiandanRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kl_polishing");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/liandanlu_jei.png");

    private final ResourceLocation PROGRESS_EMPTY = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/huoyan.png");
    private final ResourceLocation PROGRESS_FULL = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/huoyanmax.png");

    public static final RecipeType<LiandanRecipe> LIANDANLU_TYPE = new RecipeType<>(UID, LiandanRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    private final IDrawable staticFlame;
    private final IDrawableAnimated animatedFlame;

    public JEIKlCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 188, 80)
                .setTextureSize(188, 80).build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.LIANDANLU1.get()));
        this.staticFlame = helper.createDrawable(PROGRESS_EMPTY, 0, 0, 32, 32);
        this.animatedFlame = helper.drawableBuilder(PROGRESS_FULL, 0, 0, 32, 32)
                .setTextureSize(32, 32)
                .buildAnimated(120, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public RecipeType<LiandanRecipe> getRecipeType() {
        return LIANDANLU_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("liandalu");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(LiandanRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int flameX = 67;
        int flameY = 30;
        this.staticFlame.draw(guiGraphics, flameX, flameY);
        this.animatedFlame.draw(guiGraphics, flameX, flameY);
        guiGraphics.drawString(Minecraft.getInstance().font,Component.translatable("丹渣块降低丹药出现破碎的概率"),5,63,0xFFFFFF,true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiandanRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < 5; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + (i * 18), 8)
                    .addIngredients(recipe.getIngredient());
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 9, 42)
                .addItemStack(new ItemStack(ModBlocks.DROSS_BLOCK.get().asItem()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 109, 9)
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }
}