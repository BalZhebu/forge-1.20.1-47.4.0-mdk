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

import java.util.List;

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
            spawnMsg.append(" §c(生命/防御/攻击×").append(trimMultiplier(multiplier)).append(")");
        }
        if (skillLevel > 1) {
            spawnMsg.append(" §6(词条等级: ").append(com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.toRoman(skillLevel)).append(")");
        }
        player.sendSystemMessage(Component.literal(spawnMsg.toString()));

        for (MonsterConfig config : floorData.monsters) {
            EntityType.byString(config.id.toString()).ifPresent(entityType -> {
                for (int i = 0; i < config.count; i++) {
                    spawnSingle(player, level, entityType, config, nianxian, multiplier, skillLevel, spawnCenterPos, floor);
                }
            });
        }
    }

    /** 生成一只怪物：注入年限 → 层倍率放大 → 词条（按层等级）→ 归属标记。 */
    private static Mob spawnSingle(ServerPlayer player, ServerLevel level, EntityType<? extends Entity> entityType,
                                   MonsterConfig config, long nianxian, float multiplier, int skillLevel,
                                   BlockPos spawnCenterPos, int floor) {
        return spawnOne(player, level, entityType, nianxian, multiplier, skillLevel, spawnCenterPos, floor,
                config.specifiedSkills, config.randomSkillCount);
    }

    /**
     * 生成一只塔怪的唯一实现（正式挑战与 {@code /kunluncontinent tower spawn} 测试共用）。
     *
     * <p>把它从 {@link #spawnSingle} 里抽出来，是为了让测试指令能自由指定词条列表
     * （{@code /tower spawnWith}），又不至于把"建实体 → 注属性 → 打标签 → 注入词条"
     * 这套流程复制一份 —— 复制出来的那套迟早会跟正式流程走偏。</p>
     *
     * @param specified   指定的词条（可为空列表）
     * @param randomCount 额外随机抽取的词条数量
     * @return 生成成功的怪物；实体类型创建失败时返回 null
     */
    private static Mob spawnOne(ServerPlayer player, ServerLevel level, EntityType<? extends Entity> entityType,
                                long nianxian, float multiplier, int skillLevel, BlockPos spawnCenterPos,
                                int floor, List<String> specified, int randomCount) {
        Entity entity = entityType.create(level);
        if (!(entity instanceof Mob mob)) return null;

        // 位置微调（每只独立随机散布）
        double offsetX = (level.random.nextDouble() - 0.5) * 4.0;
        double offsetZ = (level.random.nextDouble() - 0.5) * 4.0;
        mob.moveTo(spawnCenterPos.getX() + 0.5 + offsetX, spawnCenterPos.getY() + 1.0, spawnCenterPos.getZ() + 0.5 + offsetZ, level.random.nextFloat() * 360F, 0.0F);

        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            cap.initNianxian(nianxian);
            // 本层核心三围倍率（生命/防御/攻击；1.0 = 不变），必须在词条加成之前
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
        // 记录所属层索引：词条的「随层数解锁」与后续调试/统计都要用
        customTag.putInt("TowerFloor", floor);
        // 词条按本层默认等级注入（个别词条可用罗马后缀单独覆盖）；
        // floor 只用来过滤随机池（新词条未到登场层不会随机到）
        TowerSkillPool.applySkills(mob, specified, randomCount, skillLevel, floor);
        level.addFreshEntity(mob);
        SynsAPI.synsEntityAttribute(mob);
        return mob;
    }

    /**
     * 【测试指令】把指定层（0 基层索引）的怪物整套刷到指定位置。
     *
     * <p>与 {@link #spawnFloorMonsters} 的区别：<b>不发"开启第 N 层历练"的消息、
     * 不改玩家的挑战状态</b>，纯刷怪；怪物依旧带 {@code TowerSpawned} 标签，
     * 所以击杀不掉战利品、也能被 {@link #clearTowerMonstersForPlayer} 一并清掉。</p>
     *
     * @param overrideSkillLevel 词条等级覆盖；<=0 表示用本层配表的默认等级
     * @return 实际生成的怪物数量
     */
    public static int spawnTestFloor(ServerPlayer player, int floor, BlockPos center, int overrideSkillLevel) {
        ServerLevel level = player.serverLevel();
        TowerFloorRegistry.FloorData data = TowerFloorRegistry.getFloorData(floor);
        int skillLevel = overrideSkillLevel > 0
                ? Math.max(1, Math.min(TowerSkillPool.MAX_LEVEL, overrideSkillLevel))
                : data.skillLevel;

        int spawned = 0;
        for (MonsterConfig config : data.monsters) {
            var maybeType = EntityType.byString(config.id.toString());
            if (maybeType.isEmpty()) continue;
            for (int i = 0; i < config.count; i++) {
                Mob mob = spawnOne(player, level, maybeType.get(), data.baseNianxian, data.attributeMultiplier,
                        skillLevel, center, floor, config.specifiedSkills, config.randomSkillCount);
                if (mob != null) {
                    // 标记"调试刷出来的怪"：神考玩家击杀它也能拿到塔内奖励（方便验证掉落曲线）
                    mob.getPersistentData().putBoolean("TowerTestSpawn", true);
                    spawned++;
                }
            }
        }
        return spawned;
    }

    /**
     * 【测试指令】只刷 1 只怪，并且只给它挂指定的一条词条。
     *
     * <p>用本层第一条刷怪配置的怪物类型（这一层"长什么样"由它代表），
     * 年限与核心三围倍率依旧按该层来 —— 这样测出来的词条强度才是真实环境下的强度。
     * 指定词条不受 {@code getMinFloor()} 限制，方便在低层直接测高层词条。</p>
     *
     * @return 生成成功的怪物；该层第一条配置的实体类型无效时返回 null
     */
    public static Mob spawnTestMonster(ServerPlayer player, int floor, String skillName, BlockPos center) {
        ServerLevel level = player.serverLevel();
        TowerFloorRegistry.FloorData data = TowerFloorRegistry.getFloorData(floor);
        if (data.monsters.isEmpty()) return null;

        var maybeType = EntityType.byString(data.monsters.get(0).id.toString());
        if (maybeType.isEmpty()) return null;

        Mob mob = spawnOne(player, level, maybeType.get(), data.baseNianxian, data.attributeMultiplier,
                data.skillLevel, center, floor, List.of(skillName), 0);
        if (mob != null) {
            mob.getPersistentData().putBoolean("TowerTestSpawn", true);
        }
        return mob;
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
