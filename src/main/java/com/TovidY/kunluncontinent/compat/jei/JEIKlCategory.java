package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JEIKlCategory implements IRecipeCategory<LiandanRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "kl_polishing");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/liandanlu_jei.png");

    public static final RecipeType<LiandanRecipe> LIANDANLU_TYPE =
            new RecipeType<>(UID,LiandanRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public JEIKlCategory(IGuiHelper helper) {

        IDrawableBuilder iDrawableBuilder = helper.drawableBuilder(TEXTURE, 0, 0, 188, 80);

        this.background = iDrawableBuilder.setTextureSize(188,80).build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModBlocks.LIANDANLU1.get()));
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
    public @Nullable IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiandanRecipe recipe, IFocusGroup focuses) {
        // 输入槽 (5个内丹槽)
        for (int i = 0; i < 5; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + (i * 18), 8)
                    .addIngredients(recipe.getIngredient());
        }
        // 药渣 (可选)
        builder.addSlot(RecipeIngredientRole.CATALYST, 9, 42)
                .addItemStack(new ItemStack(ModBlocks.DROSS_BLOCK.get().asItem()));
        // 输出 (使用正确的 Access 传入)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 109, 9)
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

//            for (int i = 0; i < 5; i++) {
//        builder.addSlot(RecipeIngredientRole.INPUT, 10 + (i * 18), 6)
//                .addIngredients(recipe.getIngredient());
//    }
//    // 药渣 (可选)
//        builder.addSlot(RecipeIngredientRole.CATALYST, 8, 43)
//            .addItemStack(new ItemStack(ModBlocks.DROSS_BLOCK.get().asItem()));
//    // 输出 (使用正确的 Access 传入)
//        builder.addSlot(RecipeIngredientRole.OUTPUT, 109, 8)
//            .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));

}
