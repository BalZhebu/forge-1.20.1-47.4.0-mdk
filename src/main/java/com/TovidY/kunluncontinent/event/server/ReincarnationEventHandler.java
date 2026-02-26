package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.klitem.ZhuanShengTestItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReincarnationEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 关键点：只在 Phase.END 且是服务端逻辑时执行
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            ZhuanShengTestItem.checkReincarnationProgress(event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // 同样建议只在服务端处理逻辑
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player) {
            ZhuanShengTestItem.cancelReincarnation(player);
        }
    }
}
