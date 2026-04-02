package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
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

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        net.minecraft.world.entity.player.Player oldPlayer = event.getOriginal();
        net.minecraft.world.entity.player.Player newPlayer = event.getEntity();
        if (!newPlayer.level().isClientSide) {
            oldPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(oldCap -> {
                newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());
                });
            });
            if (newPlayer instanceof ServerPlayer serverPlayer) {
                PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
                newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    NetworkHandler.sendToClient(new PacketSyncGodData(cap), serverPlayer);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                NetworkHandler.sendToClient(new PacketSyncGodData(cap), serverPlayer);
            });

            PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
        }
    }
}