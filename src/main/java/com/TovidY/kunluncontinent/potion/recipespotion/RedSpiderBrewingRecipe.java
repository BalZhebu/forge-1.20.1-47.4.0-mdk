package com.TovidY.kunluncontinent.potion.recipespotion;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RedSpiderBrewingRecipe implements IBrewingRecipe {

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(new RedSpiderBrewingRecipe()));
    }

    @Override
    public boolean isInput(ItemStack input) {
        return input.is(Items.POTION) && PotionUtils.getPotion(input) == Potions.AWKWARD;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(ModItems.RED_SPIDER_LILY_ITEM.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (isInput(input) && isIngredient(ingredient)) {
            return new ItemStack(ModItems.RED_SPIDER_LILY_POTION.get());
        }
        return ItemStack.EMPTY;
    }
}
