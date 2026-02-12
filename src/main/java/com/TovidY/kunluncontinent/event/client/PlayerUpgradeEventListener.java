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
    }
}