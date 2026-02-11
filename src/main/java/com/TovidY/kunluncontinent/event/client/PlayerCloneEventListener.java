package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家克隆事件监听器 - 处理玩家死亡重生后的属性继承
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCloneEventListener {
    // 使用静态 Map 来临时存储玩家死亡前的属性数据
    private static final Map<UUID, CompoundTag> playerAttributeDataCache = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerCloneEvent(PlayerEvent.Clone event)
    {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(oriState->{
            event.getEntity().getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newState ->{
                newState.deserializeNBT(oriState.serializeNBT());
            });
        });
        event.getOriginal().invalidateCaps();
    }

    /**
     * 监听玩家克隆事件，确保死亡重生后保留原有属性
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 仅在服务端处理
        if (event.getEntity().level().isClientSide) {
            return;
        }
        // 只处理死亡重生的情况
        if (event.isWasDeath()) {
            var originalPlayer = event.getOriginal();
            var newPlayer = event.getEntity();
            // 获取原始玩家的 NBT 数据
            var originalCapabilityOptional = originalPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).resolve();
            if (originalCapabilityOptional.isPresent()) {
                var originalAttrs = originalCapabilityOptional.get();
                // 记录原始属性
                // 获取新玩家的属性Capability
                newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newAttrs -> {
                    // 将原玩家的属性复制到新玩家
                    newAttrs.setShengming(originalAttrs.getShengming());
                    newAttrs.setMaxshengming(originalAttrs.getMaxshengming());
                    newAttrs.setJingshenli(originalAttrs.getJingshenli());
                    newAttrs.setMaxjingshenli(originalAttrs.getMaxjingshenli());
                    newAttrs.setMingzhong(originalAttrs.getMingzhong());
                    newAttrs.setFangyu(originalAttrs.getFangyu());
                    newAttrs.setGongji(originalAttrs.getGongji());
                    newAttrs.setBaojilv(originalAttrs.getBaojilv());
                    newAttrs.setBaojishanghai(originalAttrs.getBaojishanghai());
                    newAttrs.setXixue(originalAttrs.getXixue());
                    newAttrs.setShanbi(originalAttrs.getShanbi());
                    newAttrs.setKangbao(originalAttrs.getKangbao());
                    newAttrs.setJingyan(originalAttrs.getJingyan());
                    newAttrs.setDengji(originalAttrs.getDengji());
                    newAttrs.setMaxjingyan(originalAttrs.getMaxjingyan());
                    newAttrs.setInitialized(originalAttrs.isInitialized()); // 保持初始化状态
                    // 如果是服务器玩家，同步最大生命值到原生属性
                    if (newPlayer instanceof ServerPlayer serverPlayer) {
                        PlayerAttributeInit.syncMaxHealthToPlayer(serverPlayer, newAttrs.getMaxshengming());
                    }
                });
            } else {
                KlMain.LOGGER.warn("[KunlunContinent] 原始播放器功能不存在，正在尝试从缓存恢复");
                // 尝试从缓存中恢复数据
                UUID playerUUID = originalPlayer.getUUID();
                CompoundTag cachedData = playerAttributeDataCache.remove(playerUUID);
                
                if (cachedData != null) {
                    // 从缓存的 NBT 数据恢复到新玩家
                    newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newAttrs -> {
                        ((PlayerAttributeCapability) newAttrs).deserializeNBT(cachedData);
                        // 如果是服务器玩家，同步最大生命值到原生属性
                        if (newPlayer instanceof ServerPlayer serverPlayer) {
                            PlayerAttributeInit.syncMaxHealthToPlayer(serverPlayer, newAttrs.getMaxshengming());
                        }
                    });
                } else {
                    KlMain.LOGGER.warn("[KunlunContinent] 未找到缓存数据，依赖 Forge 的自动 NBT 复制");
                    newPlayer.reviveCaps();
                }
            }
        }
    }
}