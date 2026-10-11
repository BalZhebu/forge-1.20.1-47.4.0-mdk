package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.AttributePoints;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.capability.playerattributes.WuhunPermanent;
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
            // ⚠️ 同一次 Clone 上还有 {@code PlayerCloneEventListener}，它在末尾会
            //    oldPlayer.invalidateCaps()。若我们排在它之后，就读不到旧数据了
            //    （getCapability 静默返回空 → 神考数据没复制过去 → 面板消失）。
            //    这里主动 reviveCaps 一次，读取完由下面再 invalidate 交还。
            boolean revived = false;
            if (!oldPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).isPresent()) {
                oldPlayer.reviveCaps();
                revived = true;
            }
            oldPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(oldCap -> {
                newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());
                });
            });
            if (revived) oldPlayer.invalidateCaps();
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
            serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> {
                        AttributePoints.grantOnLogin(serverPlayer, cap);
                        // 清掉"本 tick 已结算"标记（重登后要能重新抽成）
                        WuhunPermanent.clearSettledFlag(cap);
                    });
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
     * ⭐ <b>死亡重生后同步</b> —— 修"玩家死后神考面板消失，必须重启游戏才恢复"。
     *
     * <p><b>为什么 {@code PlayerEvent.Clone} 不够</b>：Clone 触发时新玩家的 capability
     * 可能还没从旧玩家复制完（项目里有<b>两个</b> Clone 监听器都在做数据搬运，
     * 谁先跑不确定），此时发包会把"空/半空"的神考数据同步到客户端。
     * 客户端 {@code GodClientData} 是静态字段，收过一次错的就错了，
     * 而客户端不会主动纠正 → 页签的 visible 为 false → 面板消失。</p>
     *
     * <p>{@code PlayerRespawnEvent} 在<b>重生完全落地之后</b>触发，
     * 此时新玩家实体和 capability 都已就绪，是发包的正确时机。</p>
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
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