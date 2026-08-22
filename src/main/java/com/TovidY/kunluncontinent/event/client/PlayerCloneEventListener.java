package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//玩家死亡复活事件
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCloneEventListener {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide) { return; }
        var oldPlayer = event.getOriginal();
        var newPlayer = event.getEntity();
        oldPlayer.reviveCaps();
        oldPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(oldAttrs -> {
            newPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(newAttrs -> {
                newAttrs.deserializeNBT(oldAttrs.serializeNBT());
                if (event.isWasDeath()) {
                    float maxHp = newAttrs.getMaxshengming();
                    //重生后恢复50%的血量
                    float spawnHp = maxHp * 0.5f;
                    newAttrs.setShengming(spawnHp);
                    if (newPlayer instanceof ServerPlayer serverPlayer) {
                        PlayerAttributeInit.syncMaxHealthToPlayer(serverPlayer, maxHp);
                        serverPlayer.setHealth(spawnHp);
                        CompoundTag nbtData = newAttrs.serializeNBT();
                        var packet = new com.TovidY.kunluncontinent.network.server.SPacketPlayerAttribute(serverPlayer.getId(), nbtData);
                        com.TovidY.kunluncontinent.network.NetworkHandler.INSTANCE.send(
                                PacketDistributor.PLAYER.with(() -> serverPlayer),
                                packet
                        );
                    }
                }
            });
        });
        oldPlayer.invalidateCaps();
    }
}