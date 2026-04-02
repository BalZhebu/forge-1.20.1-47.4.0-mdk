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

                // 2. 判定是否为击杀任务
                if (GodTaskType.KILL.name().equals(typeStr) && target != null) {
                    // 获取被击杀生物的注册名 (例如 "minecraft:zombie")
                    String killedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString();

                    if (killedEntityId.equals(target)) {
                        // 进度自增
                        cap.addGodTaskProgress(1);

                        // 3. 检查是否达到完成条件
                        int required = cap.getAssignedCounts()[stage];
                        if (cap.getGodTaskProgress() >= required) {
                            // 达成目标：发放奖励，进入下一考
                            cap.checkTaskCompletion(player);
                        }

                        // 4. 核心修正：调用你 NetworkHandler 里的 sendToClient 方法同步数据
                        NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
                    }
                }
            });
        }
    }
}