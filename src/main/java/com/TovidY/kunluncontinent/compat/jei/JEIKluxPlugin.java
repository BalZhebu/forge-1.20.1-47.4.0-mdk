package com.TovidY.kunluncontinent.compat.jei;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.ModTags;
import com.TovidY.kunluncontinent.item.klitem.DanYaoItem;
import com.TovidY.kunluncontinent.item.klitem.DanYaoQuality;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.DrossConversionRecipe;
import com.TovidY.kunluncontinent.recipe.liandanlurecipe.LiandanRecipe;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class JEIKluxPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(KlMain.MOD_ID, "jei_klux");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JEIKlCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        // 这里的逻辑是正确的
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<LiandanRecipe> liandanRecipes = recipeManager.getAllRecipesFor(ModRecipes.LIANDAN_TYPE.get());
        registration.addRecipes(JEIKlCategory.LIANDANLU_TYPE, liandanRecipes);
        //忘川药水配方
        registration.addRecipes(RecipeTypes.BREWING, List.of(
                factory.createBrewingRecipe(
                        List.of(new ItemStack(ModItems.RED_SPIDER_LILY_ITEM.get())),
                        List.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                        new ItemStack(ModItems.RED_SPIDER_LILY_POTION.get())
                )
        ));

        List<ItemStack> repairables = new ArrayList<>();
        ModItems.COLDPROTECTIONLIST.forEach(reg -> repairables.add(new ItemStack(reg.get())));
        ModItems.THUNDER_PROTECTION_LIST.forEach(reg -> repairables.add(new ItemStack(reg.get())));
        List<CraftingRecipe> displayRecipes = new ArrayList<>();
        for (ItemStack target : repairables) {
            ItemStack fullBottle = new ItemStack(ModItems.SOUL_GATHERING_BOTTLE_4.get());
            fullBottle.getOrCreateTag().putInt("sh_nengliang", 1314520);
            ItemStack damagedTarget = target.copy();
            damagedTarget.setDamageValue(damagedTarget.getMaxDamage() / 2);
            ItemStack result = target.copy();
            result.setDamageValue(0);
            ShapelessRecipe dummyRecipe = new ShapelessRecipe(
                    new ResourceLocation(KlMain.MOD_ID, "jei_repair_" + target.getItem().toString()),
                    "repair",
                    CraftingBookCategory.EQUIPMENT,
                    result,
                    NonNullList.of(Ingredient.EMPTY,
                            Ingredient.of(fullBottle),
                            Ingredient.of(damagedTarget))
            );
            displayRecipes.add(dummyRecipe);
        }
        registration.addRecipes(RecipeTypes.CRAFTING, displayRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // 这里的点击区域可以根据你的火焰位置微调
        registration.addRecipeClickArea(LiandanluScreen.class, 105, 30, 32, 32,
                JEIKlCategory.LIANDANLU_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LIANDANLU1.get()), JEIKlCategory.LIANDANLU_TYPE);
    }
}