package com.TovidY.kunluncontinent.entity;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.entity.snowdemon.SnowDemonEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//生物生成事件总线
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntitySpawnEvents {

    @SubscribeEvent
    public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        event.register(
                EntityInit.ICE_CRYSTAL.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                IceCrystalEntity::checkIceCrystalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        event.register(
                EntityInit.SNOW_DEMON.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SnowDemonEntity::checkSnowDemonSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}
