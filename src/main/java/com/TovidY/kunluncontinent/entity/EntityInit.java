package com.TovidY.kunluncontinent.entity;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.Icecrysta.CustomModel;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalRenderer;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceShardEntity;
import com.TovidY.kunluncontinent.entity.demon.DemonWhaleEntity;
import com.TovidY.kunluncontinent.entity.demon.DemonWhaleModel;
import com.TovidY.kunluncontinent.entity.demon.DemonWhaleRenderer;
import com.TovidY.kunluncontinent.entity.eyetrans.EyeTransformationEntity;
import com.TovidY.kunluncontinent.entity.hunhe.HunheEntity;
import com.TovidY.kunluncontinent.entity.hunhe.HunheRender;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntity;
import com.TovidY.kunluncontinent.entity.icempress.IceEmpressRenderer;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanRender;
import com.TovidY.kunluncontinent.entity.lonebamboo.LoneBambooEntity;
import com.TovidY.kunluncontinent.entity.lonebamboo.LoneBambooModel;
import com.TovidY.kunluncontinent.entity.lonebamboo.LoneBambooRenderer;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcRenderer;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonEntity;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonNewModel;
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

    public static final RegistryObject<EntityType<PlayerNpcEntity>> PLAYER_NPC =
            ENTITY_TYPES.register("player_npc", () -> EntityType.Builder.of(PlayerNpcEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("player_npc"));

    public static final RegistryObject<EntityType<EyeTransformationEntity>> EYE_TRANSFORMATION_ENTITY =
            ENTITY_TYPES.register("eye_transformation_entity",
                    () -> EntityType.Builder.<EyeTransformationEntity>of(EyeTransformationEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .build("eye_transformation_entity"));

    public static final RegistryObject<EntityType<SnowDemonEntity>> SNOW_DEMON = ENTITY_TYPES.register("snow_demon",
            () -> EntityType.Builder.of(SnowDemonEntity::new, MobCategory.MONSTER)
                    .sized(4.4F, 8.5F)
                    .build("snow_demon"));

    public static final RegistryObject<EntityType<DemonWhaleEntity>> DEMON_WHALE =
            ENTITY_TYPES.register("demon_whale", () -> EntityType.Builder.of(DemonWhaleEntity::new, MobCategory.MONSTER)
                    .sized(8.0F, 6.5F)
                    .build("demon_whale"));

    public static final RegistryObject<EntityType<LoneBambooEntity>> LONE_BAMBOO =
            ENTITY_TYPES.register("lone_bamboo", () -> EntityType.Builder.of(LoneBambooEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.9F)
                    .clientTrackingRange(10)
                    .build("lone_bamboo"));

    /** 雪帝：敌对生物，不自然生成（只靠刷怪蛋 / 指令）。 */
    public static final RegistryObject<EntityType<IceEmpressEntity>> ICE_EMPRESS =
            ENTITY_TYPES.register("ice_empress", () -> EntityType.Builder.of(IceEmpressEntity::new, MobCategory.MONSTER)
                    // 模型在 IceEmpressRenderer 里放大 2 倍（原模型 1.0×2.6），
                    // 命中箱必须同步放大，否则会出现「看着 5 格高、打不中上半个身子」。
                    .sized(2.0F, 5.2F)
                    .clientTrackingRange(10)
                    .build("ice_empress"));

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(CustomModel.LAYER_LOCATION, CustomModel::createBodyLayer);
            event.registerLayerDefinition(SnowDemonNewModel.LAYER_LOCATION, SnowDemonNewModel::createBodyLayer);
            event.registerLayerDefinition(DemonWhaleModel.LAYER_LOCATION, DemonWhaleModel::createBodyLayer);
            event.registerLayerDefinition(LoneBambooModel.LAYER_LOCATION, LoneBambooModel::createBodyLayer);
            event.registerLayerDefinition(com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntityModel.LAYER_LOCATION,
                    com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntityModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(EntityInit.HUNHUAN.get(), HunhuanRender::new);
            EntityRenderers.register(EntityInit.HUNHE.get(), HunheRender::new);
            EntityRenderers.register(EntityInit.ICE_CRYSTAL.get(), IceCrystalRenderer::new);
            EntityRenderers.register(EntityInit.ICE_SHARD.get(), ThrownItemRenderer::new);
            EntityRenderers.register(EntityInit.SNOW_DEMON.get(), SnowDemonRenderer::new);

            EntityRenderers.register(EntityInit.DEMON_WHALE.get(), DemonWhaleRenderer::new);

            EntityRenderers.register(EntityInit.LONE_BAMBOO.get(), LoneBambooRenderer::new);

            EntityRenderers.register(EntityInit.ICE_EMPRESS.get(),
                    com.TovidY.kunluncontinent.entity.icempress.IceEmpressRenderer::new);

            EntityRenderers.register(EntityInit.PLAYER_NPC.get(), PlayerNpcRenderer::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(EntityInit.EYE_TRANSFORMATION_ENTITY.get(),
                    context -> new ThrownItemRenderer<>(context, 1.0F, true));
        }
    }

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(EntityInit.ICE_CRYSTAL.get(), IceCrystalEntity.createAttributes().build());
            event.put(EntityInit.SNOW_DEMON.get(), SnowDemonEntity.createAttributes().build());
            event.put(EntityInit.DEMON_WHALE.get(), DemonWhaleEntity.createAttributes().build());

            event.put(EntityInit.LONE_BAMBOO.get(), LoneBambooEntity.createAttributes().build());

            event.put(EntityInit.ICE_EMPRESS.get(),
                    com.TovidY.kunluncontinent.entity.icempress.IceEmpressEntity.createAttributes().build());

            event.put(EntityInit.PLAYER_NPC.get(), PlayerNpcEntity.createAttributes().build());
        }
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
