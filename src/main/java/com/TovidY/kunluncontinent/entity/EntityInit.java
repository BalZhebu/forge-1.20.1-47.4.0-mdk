package com.TovidY.kunluncontinent.entity;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.Icecrysta.CustomModel;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalRenderer;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceShardEntity;
import com.TovidY.kunluncontinent.entity.hunhe.HunheEntity;
import com.TovidY.kunluncontinent.entity.hunhe.HunheRender;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanRender;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonEntity;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonModel;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

//实体注册代码

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KlMain.MOD_ID);

    public static final RegistryObject<EntityType<HunhuanEntity>> HUNHUAN =
            ENTITY_TYPES.register("hunhuan", () -> EntityType.Builder.of(HunhuanEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("hunhuan"));

    public static final RegistryObject<EntityType<HunheEntity>> HUNHE =
            ENTITY_TYPES.register("hunhe", () -> EntityType.Builder.of(HunheEntity::new, MobCategory.MISC)
                    .updateInterval(Integer.MAX_VALUE)
                    .sized(1.0f, 1.0f).build("hunhe"));

    public static final RegistryObject<EntityType<IceCrystalEntity>> ICE_CRYSTAL = ENTITY_TYPES.register("ice_crystal",
            () -> EntityType.Builder.of(IceCrystalEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.2F)
                    .build("ice_crystal"));
    public static final RegistryObject<EntityType<IceShardEntity>> ICE_SHARD = ENTITY_TYPES.register("ice_shard",
            () -> EntityType.Builder.<IceShardEntity>of(IceShardEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("ice_shard"));

    public static final RegistryObject<EntityType<SnowDemonEntity>> SNOW_DEMON = ENTITY_TYPES.register("snow_demon",
            () -> EntityType.Builder.of(SnowDemonEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.2F)
                    .build("snow_demon"));

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(CustomModel.LAYER_LOCATION, CustomModel::createBodyLayer);
            event.registerLayerDefinition(SnowDemonModel.LAYER_LOCATION, SnowDemonModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(EntityInit.HUNHUAN.get(), HunhuanRender::new);
            EntityRenderers.register(EntityInit.HUNHE.get(), HunheRender::new);
            EntityRenderers.register(EntityInit.ICE_CRYSTAL.get(), IceCrystalRenderer::new);
            EntityRenderers.register(EntityInit.ICE_SHARD.get(), ThrownItemRenderer::new);
            EntityRenderers.register(EntityInit.SNOW_DEMON.get(), SnowDemonRenderer::new);
        }
    }

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(EntityInit.ICE_CRYSTAL.get(), IceCrystalEntity.createAttributes().build());
            event.put(EntityInit.SNOW_DEMON.get(), SnowDemonEntity.createAttributes().build());
        }
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
