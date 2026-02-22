package com.TovidY.kunluncontinent;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.advancement.ModTriggers;
import com.TovidY.kunluncontinent.block.ModBlockEntities;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.recipe.ModRecipes;
import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import com.TovidY.kunluncontinent.Init.ModCreativeModelTab;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
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

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModTriggers.register();

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
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 设置冰晶的生成规则：怪物类型、高度(地表)、生成规则(怪物通用规则)
            SpawnPlacements.register(EntityInit.ICE_CRYSTAL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IceCrystalEntity::checkIceCrystalSpawnRules);
        });
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
