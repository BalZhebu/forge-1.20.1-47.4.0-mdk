package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 玩家升级事件监听器 - 监听各种事件并触发升级检查
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class PlayerUpgradeEventListener {

    /**
     * 玩家死亡重生 或 跨维度传送时，数据继承与同步
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        net.minecraft.world.entity.player.Player oldPlayer = event.getOriginal();
        net.minecraft.world.entity.player.Player newPlayer = event.getEntity();
        if (!newPlayer.level().isClientSide && newPlayer instanceof ServerPlayer serverPlayer) {
            oldPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(oldCap -> {
                newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());
                });
            });
            PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
            syncAllPlayerData(serverPlayer);
        }
    }

    /**
     * 玩家登录进服同步
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerUpgradeSystem.triggerUpgradeCheck(serverPlayer);
            syncAllPlayerData(serverPlayer);
        }
    }

    /**
     * 跨维度传送后同步（防止去下界/末地后 UI 变 0 级）
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncAllPlayerData(serverPlayer);
        }
    }

    /**
     * 统一发包同步工具方法
     */
    private static void syncAllPlayerData(ServerPlayer serverPlayer) {
        serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            NetworkHandler.sendToClient(new SPacketPlayerAttribute(serverPlayer.getId(), cap.serializeNBT()), serverPlayer);
            NetworkHandler.sendToClient(new PacketSyncGodData(cap), serverPlayer);
        });
    }
}