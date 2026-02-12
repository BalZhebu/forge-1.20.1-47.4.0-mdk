package com.TovidY.kunluncontinent.entity;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.hunhe.HunheEntity;
import com.TovidY.kunluncontinent.entity.hunhe.HunheRender;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanRender;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
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

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(EntityInit.HUNHUAN.get(), HunhuanRender::new);
            EntityRenderers.register(EntityInit.HUNHE.get(), HunheRender::new);
        }
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
