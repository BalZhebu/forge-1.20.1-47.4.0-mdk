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
        float multiplier = floorData.attributeMultiplier;
        int skillLevel = floorData.skillLevel;

        // 汇总本层怪物总数（每条配置自带数量）
        int totalMobs = 0;
        for (MonsterConfig config : floorData.monsters) {
            totalMobs += config.count;
        }

        StringBuilder spawnMsg = new StringBuilder("§d[幻境法则] 开启第 ")
                .append(floor + 1).append(" 层历练！本关限时: ").append(floorData.timeLimitSeconds)
                .append(" 秒！共 ").append(totalMobs).append(" 名敌人！");
        if (multiplier > 1.0f) {
            spawnMsg.append(" §c(全属性×").append(trimMultiplier(multiplier)).append(")");
        }
        if (skillLevel > 1) {
            spawnMsg.append(" §6(词条等级: ").append(com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.toRoman(skillLevel)).append(")");
        }
        player.sendSystemMessage(Component.literal(spawnMsg.toString()));

        for (MonsterConfig config : floorData.monsters) {
            EntityType.byString(config.id.toString()).ifPresent(entityType -> {
                for (int i = 0; i < config.count; i++) {
                    spawnSingle(player, level, entityType, config, nianxian, multiplier, skillLevel, spawnCenterPos);
                }
            });
        }
    }

    /** 生成一只怪物：注入年限 → 层倍率放大 → 词条（按层等级）→ 归属标记。 */
    private static void spawnSingle(ServerPlayer player, ServerLevel level, EntityType<? extends Entity> entityType,
                                    MonsterConfig config, long nianxian, float multiplier, int skillLevel, BlockPos spawnCenterPos) {
        Entity entity = entityType.create(level);

        if (entity instanceof Mob mob) {
            // 位置微调（每只独立随机散布）
            double offsetX = (level.random.nextDouble() - 0.5) * 4.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 4.0;
            mob.moveTo(spawnCenterPos.getX() + 0.5 + offsetX, spawnCenterPos.getY() + 1.0, spawnCenterPos.getZ() + 0.5 + offsetZ, level.random.nextFloat() * 360F, 0.0F);

            mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                cap.initNianxian(nianxian);
                // 本层全属性倍率（1.0 = 不变），必须在词条加成之前
                cap.applyAttributeMultiplier(multiplier);
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
            // 词条按本层默认等级注入（个别词条可用罗马后缀单独覆盖）
            TowerSkillPool.applySkills(mob, config.specifiedSkills, config.randomSkillCount, skillLevel);
            level.addFreshEntity(mob);
            SynsAPI.synsEntityAttribute(mob);
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

    /** 倍率显示：整数不带小数点（3.0 → 3），非整数保留一位（2.5 → 2.5）。 */
    private static String trimMultiplier(float multiplier) {
        if (multiplier == (long) multiplier) {
            return String.valueOf((long) multiplier);
        }
        return String.format("%.1f", multiplier);
    }
}
