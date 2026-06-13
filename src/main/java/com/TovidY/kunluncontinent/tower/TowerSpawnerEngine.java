package com.TovidY.kunluncontinent.tower;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.tower.floor.MonsterConfig;
import com.TovidY.kunluncontinent.tower.floor.TowerFloorRegistry;
import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TowerSpawnerEngine {

    public static void spawnFloorMonsters(ServerPlayer player, int floor, BlockPos spawnCenterPos) {
        ServerLevel level = player.serverLevel();

        TowerFloorRegistry.FloorData floorData = TowerFloorRegistry.getFloorData(floor);
        long nianxian = floorData.baseNianxian;

        player.sendSystemMessage(Component.literal("§d[幻境法则] 开启第 " + (floor + 1) + " 层历练！本关限时: " + floorData.timeLimitSeconds + " 秒！"));

        for (MonsterConfig config : floorData.monsters) {
            EntityType.byString(config.id.toString()).ifPresent(entityType -> {
                Entity entity = entityType.create(level);

                if (entity instanceof Mob mob) {
                    // 位置微调
                    double offsetX = (level.random.nextDouble() - 0.5) * 4.0;
                    double offsetZ = (level.random.nextDouble() - 0.5) * 4.0;
                    mob.moveTo(spawnCenterPos.getX() + 0.5 + offsetX, spawnCenterPos.getY() + 1.0, spawnCenterPos.getZ() + 0.5 + offsetZ, level.random.nextFloat() * 360F, 0.0F);

                    mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.initNianxian(nianxian);
                        float maxHealth = cap.getMaxshengming();
                        var attr = mob.getAttribute(Attributes.MAX_HEALTH);
                        if (attr != null) {
                            attr.setBaseValue(maxHealth);
                            mob.setHealth(maxHealth);
                        }
                    });

                    CompoundTag customTag = mob.getPersistentData();
                    customTag.putBoolean("TowerSpawned", true);
                    customTag.putString("TowerOwner", player.getUUID().toString());
                    TowerSkillPool.applySkills(mob, config.specifiedSkills, config.randomSkillCount);
                    level.addFreshEntity(mob);
                    SynsAPI.synsEntityAttribute(mob);
                }
            });
        }
    }

    public static void clearTowerMonstersForPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob mob) {
                CompoundTag customTag = mob.getPersistentData();
                if (customTag.contains("TowerSpawned") && customTag.getBoolean("TowerSpawned")) {
                    if (customTag.contains("TowerOwner") && customTag.getString("TowerOwner").equals(player.getUUID().toString())) {
                        mob.discard();
                    }
                }
            }
        }
    }
}