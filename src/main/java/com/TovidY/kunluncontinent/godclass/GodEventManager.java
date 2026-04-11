package com.TovidY.kunluncontinent.godclass;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodTaskType;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class GodEventManager {

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.getGodName().isEmpty() || cap.isGod() || cap.getAssignedTargets() == null) return;
                int stage = cap.getCurrentStage();
                if (stage < 1 || stage > 9) return;
                String target = cap.getAssignedTargets()[stage];
                String typeStr = cap.getAssignedTypes()[stage];
                if (GodTaskType.KILL.name().equals(typeStr) && target != null) {
                    String killedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString();
                    if (killedEntityId.equals(target)) {
                        cap.addGodTaskProgress(1);
                        int required = cap.getAssignedCounts()[stage];
                        if (cap.getGodTaskProgress() >= required) {
                            cap.checkTaskCompletion(player);
                        }
                        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
                    }
                }
            });
        }
    }
}