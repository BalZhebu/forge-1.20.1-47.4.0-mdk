package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 玩家升级事件监听器 - 监听各种事件并触发升级检查
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class PlayerUpgradeEventListener {

    /**
     * 监听玩家克隆事件（重生等），检查升级
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 玩家重生时检查升级
            PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
        }
    }

    /**
     * 监听玩家登录事件，检查升级
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 玩家登录时检查升级
            PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
        }
    }

    /**
     * 监听玩家加载完成事件，检查升级
     */
    @SubscribeEvent
    public static void onPlayerLoadComplete(PlayerEvent.LoadFromFile event) {
        // 这里暂时不直接处理，因为玩家对象可能尚未完全初始化
    }

    /**
     * 监听玩家 tick 事件，定期检查升级（可选，性能考虑）
     * 注意：这个事件非常频繁，如果性能要求高，可以考虑减少检查频率
     */
//    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && 
            !event.player.level().isClientSide && 
            event.player instanceof ServerPlayer serverPlayer) {
            // 每隔一定时间检查一次升级，例如每100 ticks（约5秒）检查一次
            if (serverPlayer.tickCount % 100 == 0) {
                PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
            }
        }
    }
}