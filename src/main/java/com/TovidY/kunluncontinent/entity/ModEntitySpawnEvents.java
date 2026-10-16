package com.TovidY.kunluncontinent.entity;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.entity.Icecrysta.IceCrystalEntity;
import com.TovidY.kunluncontinent.entity.lonebamboo.LoneBambooEntity;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
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

        // ── 孤竹：主世界地表 ──
        // 高度图用 MOTION_BLOCKING_NO_LEAVES，会跳过树叶直接给地表，
        // 所以竹子不会插在树冠上。昼夜判定刻意不做，见 LoneBambooEntity#checkLoneBambooSpawnRules
        event.register(
                EntityInit.LONE_BAMBOO.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                LoneBambooEntity::checkLoneBambooSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // ── PlayerNPC 自然生成规则 ──
        // 主世界/下界/末地/自定义维度：ON_GROUND + 运动阻挡线 + 亮度≥8 在 checkSpawnRules 中处理
        event.register(
                EntityInit.PLAYER_NPC.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PlayerNpcEntity::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}

