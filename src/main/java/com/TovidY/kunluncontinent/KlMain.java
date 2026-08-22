package com.TovidY.kunluncontinent;

import com.TovidY.kunluncontinent.Init.AddSeedsLootModifier;
import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.Init.ModLootModifiers;
import com.TovidY.kunluncontinent.advancement.ModTriggers;
import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.block.blockentity.SpiritGatheringAltarRenderer;
import com.TovidY.kunluncontinent.block.blockentity.UnderwaterAltarRenderer;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.item.tool.DecompositionItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import com.TovidY.kunluncontinent.Init.ModCreativeModelTab;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Random;

@Mod(KlMain.MOD_ID)
public class KlMain {
    public static final String MOD_ID = "kunluncontinent";
    public static final Random random = new Random();

    public static final Logger LOGGER = LogManager.getLogger();

    public KlMain()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        GodRegistry.init();

        AddSeedsLootModifier.LOOT_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModTriggers.register();

        ModLootModifiers.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KLConfig.CONFIG);

        ModMenuTypes.register(modEventBus);

        ModCreativeModelTab.register(modEventBus);

        changeAttributesIO();

        ModRecipes.register(modEventBus);

        EntityInit.register(modEventBus);

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
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.UNDERWATER_ALTAR_TILE.get(),
                    UnderwaterAltarRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.SPIRIT_GATHERING_ALTAR_BE.get(),
                    SpiritGatheringAltarRenderer::new);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventHandler {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            DecompositionItem.onPlayerTick(event);
        }
    }

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
