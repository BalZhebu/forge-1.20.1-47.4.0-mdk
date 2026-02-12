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

//玩家死亡复活事件
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCloneEventListener {
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
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (event.isWasDeath()) {
            var originalPlayer = event.getOriginal();
            var newPlayer = event.getEntity();
            var originalCapabilityOptional = originalPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).resolve();
            if (originalCapabilityOptional.isPresent()) {
                var originalAttrs = originalCapabilityOptional.get();
                newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newAttrs -> {
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
                    newAttrs.setWuchuan(originalAttrs.getWuchuan());
                    newAttrs.setShengmingHuifu(originalAttrs.getShengmingHuifu());
                    newAttrs.setInitialized(originalAttrs.isInitialized());
                    if (newPlayer instanceof ServerPlayer serverPlayer) {
                        PlayerAttributeInit.syncMaxHealthToPlayer(serverPlayer, newAttrs.getMaxshengming());
                    }
                });
            } else {
                UUID playerUUID = originalPlayer.getUUID();
                CompoundTag cachedData = playerAttributeDataCache.remove(playerUUID);
                
                if (cachedData != null) {
                    newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newAttrs -> {
                        ((PlayerAttributeCapability) newAttrs).deserializeNBT(cachedData);
                        if (newPlayer instanceof ServerPlayer serverPlayer) {
                            PlayerAttributeInit.syncMaxHealthToPlayer(serverPlayer, newAttrs.getMaxshengming());
                        }
                    });
                } else {
                    newPlayer.reviveCaps();
                }
            }
        }
    }
}