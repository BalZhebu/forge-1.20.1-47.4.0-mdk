package com.TovidY.kunluncontinent;

import com.TovidY.kunluncontinent.Init.ModConfig;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import com.TovidY.kunluncontinent.Init.ModCreativeModelTab;
import com.TovidY.kunluncontinent.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(KlMain.MOD_ID)
public class KlMain {
    public static final String MOD_ID = "kunluncontinent";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Random random = new Random();

    public KlMain(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.CONFIG);

        ModMenuTypes.register(modEventBus);

        ModCreativeModelTab.register(modEventBus);

        EntityInit.register(modEventBus);

        changeAttributesIO();

        NetworkHandler.register();

        ModEffects.register(modEventBus);


        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
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

//    public void MyTraditionalChineseMedicine() {
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
//    }

}
