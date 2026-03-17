package com.TovidY.kunluncontinent;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.Init.ModLootModifiers;
import com.TovidY.kunluncontinent.advancement.ModTriggers;
import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonEntity;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import com.TovidY.kunluncontinent.Init.ModCreativeModelTab;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Field;
import java.util.Random;

@Mod(KlMain.MOD_ID)
public class KlMain {
    public static final String MOD_ID = "kunluncontinent";
    public static final Random random = new Random();

    public KlMain(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModTriggers.register();

        ModLootModifiers.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, KLConfig.CONFIG);

        ModMenuTypes.register(modEventBus);

        ModCreativeModelTab.register(modEventBus);

        ModRecipes.register(modEventBus);

        EntityInit.register(modEventBus);

        changeAttributesIO();

        NetworkHandler.register();

        ModBlockEntities.register(modEventBus);

        ModEffects.register(modEventBus);

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    //解除生命值限制
    public static void changeAttributesIO()  {
        try {
            Field privateField = RangedAttribute.class.getDeclaredFields()[1];
            privateField.setAccessible(true);
            privateField.set(Attributes.MAX_HEALTH, Float.MAX_VALUE);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
