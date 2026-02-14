package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIKluxPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "jei_klux");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JEIKlCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 这里的逻辑是正确的
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<LiandanRecipe> liandanRecipes = recipeManager.getAllRecipesFor(ModRecipes.LIANDAN_TYPE.get());

        registration.addRecipes(JEIKlCategory.LIANDANLU_TYPE, liandanRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // 这里的点击区域可以根据你的火焰位置微调
        registration.addRecipeClickArea(LiandanluScreen.class, 105, 30, 14, 14,
                JEIKlCategory.LIANDANLU_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 建议加上：这样点击炼丹炉方块也能看到配方
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LIANDANLU1.get()), JEIKlCategory.LIANDANLU_TYPE);
    }
}