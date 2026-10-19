package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.advancement.AchievementAPI;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.command.tovid.HunguAdminStatus;
import com.TovidY.kunluncontinent.drop.DropExecutor;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.event.client.CoinDropHandler;
import com.TovidY.kunluncontinent.godclass.GodExamFeedback;
import com.TovidY.kunluncontinent.godclass.interfac.GodTaskType;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanDropHandler;
import com.TovidY.kunluncontinent.special.SpecialSoulRing;
import com.TovidY.kunluncontinent.special.SpecialSoulRingRegistry;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
import com.TovidY.kunluncontinent.network.server.PacketPlayGodRitualEffect;
import com.TovidY.kunluncontinent.network.server.PacketSyncTowerTimer;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.tower.TowerSpawnerEngine;
import com.TovidY.kunluncontinent.tower.TowerStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.TovidY.kunluncontinent.tower.TowerRestrictionHandler.returnPlayerToSpawn;

//玩家击杀生物事件

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KLivingDeathEvent {
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;
        if (entity.getPersistentData().getBoolean("TowerSpawned")) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void livingDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        // 塔词条【殉爆】必须先于「层数推进」结算：死亡瞬间的余波属于这一层
        detonateOnDeath(entity);

        towerSpawed(entity);

        playerDeach(entity);

        if (entity.getPersistentData().getBoolean("TowerSpawned")) {
            // 塔怪默认不掉落（onMobDrops 已清空原版战利品）；只有"有神考"的玩家击杀才发奖励
            grantTowerReward(event, entity);
            return;
        }

        if (entity instanceof PlayerNpcEntity) {
            return;
        }

        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            Entity sourceEntity = event.getSource().getEntity();
            if (!(sourceEntity instanceof Player player)) return;

            handleGodGlimpse((ServerPlayer) player,entity);

            CoinDropHandler.tryDropCoins(entity, cap.getNianxian(),player);

            NeidanDropHandler.tryDropNeidan(entity, cap, player);
            handleExperience(player, cap);

            // ⭐ 特殊生物（娱乐 / 剧情魂环）：统一查 special 包注册表。
            //    没注册过的普通生物 special == null，走原来的年限概率逻辑，行为完全不变。
            SpecialSoulRing special = SpecialSoulRingRegistry.get(entity.getType());

            // 魂环：特殊生物可声明「必定掉落」，跳过年限概率表直接生成
            if (special != null && special.alwaysDropSoulRing()) {
                addHunhuanEntity(cap, entity.level(), entity.getOnPos(), entity);
            } else {
                tryGenerateHunhuan(cap, entity.level(), entity.getOnPos(), entity);
            }

            // 魂骨：特殊生物可声明「必定不掉」，连骰子都不掷
            if (special == null || !special.neverDropSoulBone()) {
                handleHunguDrop(entity, cap.getNianxian(), player);
            }

            // ⭐ 声明式掉落表（返气草种子 / 深海精华 / 你以后加的一切）
            // 新增掉落只需要改 drop/DropRegistry.java，不用碰这个文件
            DropExecutor.apply(entity);

            handleGodKillTask(player, entity);
        });
    }

    /**
     * 挑战塔「神考奖励」：塔怪唯一的合法掉落通道。
     *
     * <p>规则见 {@link com.TovidY.kunluncontinent.tower.TowerDropReward}：</p>
     * <ul>
     *   <li>资格 = 玩家有神考任务（进行中或已封神）<b>且</b>正在爬塔挑战
     *       （调试指令刷的测试怪除外）；</li>
     *   <li>魂环：每次击杀最多 1 个，严格走 {@code KLConfig} 的年限概率，不吃层数加成；</li>
     *   <li>魂骨 / 魂币 / 内丹：概率与品质按层数线性提升。</li>
     * </ul>
     */
    private static void grantTowerReward(LivingDeathEvent event, LivingEntity entity) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!com.TovidY.kunluncontinent.tower.TowerDropReward.rewardEligible(player, entity)) return;

        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            int floor = com.TovidY.kunluncontinent.tower.TowerDropReward.resolveFloor(player, entity);

            // 1. 魂环：只会掉 1 个，配置概率，不随层数加成
            tryGenerateHunhuan(cap, entity.level(), entity.getOnPos(), entity);

            // 2. 魂骨：配置概率 + 层数带来的额外绝对概率
            handleHunguDrop(entity, cap.getNianxian(), player,
                    com.TovidY.kunluncontinent.tower.TowerDropReward.hunguBonus(floor));

            // 3. 魂币：触发率 / 钱袋率 / 数量 / 品质全部按层数提升
            com.TovidY.kunluncontinent.tower.TowerDropReward.dropCoins(entity, cap.getNianxian(), player, floor);

            // 4. 内丹：掉落率与品质按层数提升（内丹等级仍看年限）
            com.TovidY.kunluncontinent.tower.TowerDropReward.dropNeidan(entity, cap, player, floor);
        });
    }

    /**
     * 塔词条【殉爆】：死亡瞬间以自身为圆心炸开，对 5 格内的玩家造成
     * 「自身最大生命 8% × 等级系数」的魔法伤害。
     *
     * <p><b>为什么不生成真正的 {@code Explosion}：</b>原版 {@code Explosion}
     * 会破坏方块（城堡/塔内地形被炸出坑是灾难性的），也会点燃实体。
     * 这里只用粒子 + 音效还原"炸开"的观感，伤害走 {@code magic()} 伤害源
     * （不带攻击者实体，因此也不会被 {@code CombatEventHandler} 的攻防公式重算掉）。</p>
     *
     * <p><b>为什么不按自身最大生命算：</b>塔内高层怪物的血量是百万级的，
     * 按"自身最大生命 ×%"结算会直接把玩家秒掉（第 90 层的怪 8% 血量就是十几万）。
     * 所以这里和【血祭】一样，用<b>玩家自身最大生命的百分比</b>做基数，
     * 既随层数涨价（等级系数），又不会被怪物的血量通胀带飞。</p>
     */
    private static void detonateOnDeath(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return;

        CompoundTag nbt = mob.getPersistentData();
        if (!nbt.getBoolean("Skill_XunBao_Available")) return;

        int level = Math.max(1, Math.min(com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.MAX_LEVEL,
                nbt.getInt("Skill_XunBao_Level")));
        float multiplier = com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.levelMultiplier(level);
        // 4% × 等级系数（10 级 7.6%），魔法伤害：不吃护甲，但也不会因为怪物通胀而失控
        float ratio = 0.04f * multiplier;

        net.minecraft.world.phys.AABB area = mob.getBoundingBox().inflate(5.0D, 4.0D, 5.0D);
        List<Player> players = mob.level().getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
            if (player.distanceToSqr(mob) > 25.0D) continue;   // 球形判定，半径 5
            player.hurt(mob.level().damageSources().magic(),
                    Math.max(1.0F, player.getMaxHealth() * ratio));
        }

        if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.4D, mob.getZ(),
                    8, 1.8D, 0.6D, 1.8D, 0.0D);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.4D, mob.getZ(),
                    20, 1.5D, 0.5D, 1.5D, 0.0D);
        }
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5F, 0.7F);
    }

    private static void playerDeach(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                if (attr.isTowerChallenging()) {
                    attr.setTowerChallenging(false);
                    attr.setTowerLastActiveTick(0);
                    int currentFloor = attr.getCurrentTowerFloor();
                    attr.setCurrentTowerFloor(Math.max(0, currentFloor - 1));
                    player.sendSystemMessage(Component.literal("§4[幻境法则] 不幸身陨，历练就此终结！攻略失败！"));
                    com.TovidY.kunluncontinent.tower.TowerSpawnerEngine.clearTowerMonstersForPlayer(player);
                    com.TovidY.kunluncontinent.tower.TowerStateManager.releaseTower(player);
                    com.TovidY.kunluncontinent.network.NetworkHandler.sendToClient(
                            new PacketSyncTowerTimer(0, false), player
                    );
                    SynsAPI.synsPlayerAttribute(player);
                }
            });
            return;
        }
    }

    @SubscribeEvent
    public static void onMobTakeDamage(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Mob mob)) return;
        var nbt = mob.getPersistentData();
        if (nbt.getBoolean("Skill_BuSi_Available")) {
            float currentHealth = mob.getHealth();
            float incomingDamage = event.getAmount();
            float nextHealth = currentHealth - incomingDamage;
            float maxHealth = mob.getMaxHealth();
            float threshold = maxHealth * 0.05f;
            if (nextHealth <= threshold || nextHealth <= 0) {
                event.setCanceled(true);
                mob.setHealth(maxHealth);
                var modifier = com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.getSkillByName("不死");
                if (modifier != null) {
                    com.TovidY.kunluncontinent.tower.skill.TowerSkillNotifier.popSkillText(mob, modifier.getTriggerText());
                }
                nbt.putBoolean("Skill_BuSi_Available", false);
                nbt.putBoolean("Skill_BuSi_Triggered", true);
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                        net.minecraft.sounds.SoundEvents.TOTEM_USE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    private static void towerSpawed(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            CompoundTag tag = mob.getPersistentData();
            if (tag.contains("TowerSpawned") && tag.getBoolean("TowerSpawned") && tag.contains("TowerOwner")) {
                String ownerUuid = tag.getString("TowerOwner");
                ServerPlayer towerPlayer = mob.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(ownerUuid));
                if (towerPlayer != null) {
                    mob.getServer().execute(() -> {
                        boolean hasRemaining = false;
                        for (Entity e : towerPlayer.serverLevel().getAllEntities()) {
                            if (e instanceof Mob remainingMob && remainingMob.isAlive()) {
                                CompoundTag rTag = remainingMob.getPersistentData();
                                if (rTag.contains("TowerSpawned") && rTag.getString("TowerOwner").equals(ownerUuid)) {
                                    hasRemaining = true;
                                    break;
                                }
                            }
                        }
                        if (!hasRemaining) {
                            towerPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                                if (attr.isTowerChallenging()) {
                                    int oldFloor = attr.getCurrentTowerFloor();
                                    attr.setCurrentTowerFloor(oldFloor + 1);
                                    attr.setTowerChallenging(false);
                                    attr.setTowerLastActiveTick(0);
                                    towerPlayer.sendSystemMessage(Component.literal("§a§l[昆仑大陆] 历练成功！恭喜通关第 " + (oldFloor + 1) + " 层！已解锁下一层。"));
                                    AchievementAPI.onTowerCleared(towerPlayer, oldFloor + 1);
                                    NetworkHandler.sendToClient(new PacketSyncTowerTimer(0, false), towerPlayer);
                                    if (attr.getGodName() == null || attr.getGodName().isEmpty()) {
                                        // 神位触发概率：基础 = 0.5% × 通关层数（第100层基础恰好 50%）。
                                        // 关口眷顾：整五层（5/15/25...）×1.5，整十层（10/20/30...100）×2。
                                        // 曲线设计：只有第 100 层会自然到达 100%（0.5%×100×2 = 100%）——
                                        // 第 90 层 90%、第 80 层 80%……越接近百层越接近必出，但只有百层是必出。
                                        int floorNumber = oldFloor + 1;
                                        double successChance = attr.debugForceSuccess ? 1.0 : (0.005 * floorNumber);
                                        if (floorNumber % 10 == 0) {
                                            successChance *= 2.0;
                                        } else if (floorNumber % 5 == 0) {
                                            successChance *= 1.5;
                                        }
                                        if (floorNumber >= 100) {
                                            successChance = 1.0; // 百层登顶：必出神位
                                        }
                                        successChance = Math.min(successChance, 1.0);
                                        if (RANDOM.nextDouble() < successChance) {
                                            String[][] godPool = {
                                                    {"sea_god", "§b海神"},
                                                    {"angel_god", "§e天使神"},
                                                    {"asura_god", "§c修罗神"},
                                            };
                                            int randomIndex = RANDOM.nextInt(godPool.length);
                                            String selectedGodId = godPool[randomIndex][0];
                                            String selectedGodName = godPool[randomIndex][1];
                                            triggerGodExam(towerPlayer, attr, selectedGodId, selectedGodName);
                                            resetDebugStatus(attr);
                                        }
                                    }
                                    SynsAPI.synsPlayerAttribute(towerPlayer);
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    private static void handleGodKillTask(Player player, LivingEntity killedEntity) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(godCap -> {
            if (godCap.getGodName().isEmpty() || godCap.isGod() || godCap.getAssignedTargets() == null) return;
            int stage = godCap.getCurrentStage();
            if (stage < 1 || stage > 9) return;
            String typeStr = godCap.getAssignedTypes()[stage];
            if (!GodTaskType.KILL.name().equals(typeStr)) return;
            String target = godCap.getAssignedTargets()[stage];
            if (target == null) return;
            String killedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(killedEntity.getType()).toString();
            if (killedEntityId.equals(target)) {
                godCap.addGodTaskProgress(1);
                int required = godCap.getAssignedCounts()[stage];
                if (godCap.getGodTaskProgress() >= required) {
                    // ⭐ 击杀达成考核时必须主动播「考核通过」演出。
                    //    之前这里只调 checkTaskCompletion，而那方法只发奖励 + 推进阶段、
                    //    不发任何提示，于是玩家杀掉最后一只怪后屏幕毫无反应，
                    //    必须自己点面板的「进度检查」才看得到（C2SCheckTaskPacket 里才有演出）。
                    //    completedStage 必须在推进阶段之前取，否则会显示成下一考的序号。
                    int completedStage = stage;
                    GodExamFeedback.announcePass(serverPlayer, completedStage);
                    godCap.checkTaskCompletion(serverPlayer);
                }
                NetworkHandler.sendToClient(new PacketSyncGodData(godCap), serverPlayer);
                SynsAPI.synsPlayerAttribute(serverPlayer);
            }
        });
    }

    // ==================================================================================
    //  前期经验倍率（击杀生物获得的经验）
    // ==================================================================================

    /**
     * 30 级以内的击杀经验倍率。
     *
     * <p>定位：前期"打不动百年怪"的本质是等级带来的战力太少，
     * 而 21 级处经验系数又从 5% 掉到 0.8%（{@code handleExperience} 里的分档），
     * 于是新手会在 20 级前后彻底卡住。这里给 30 级以内一条阶梯式加速，
     * 让玩家用更少的击杀把等级拉出"打不动"的区间，之后回归 1 倍。</p>
     *
     * <table>
     *   <tr><th>等级</th><th>倍率</th></tr>
     *   <tr><td>0 ~ 10</td><td>×2.5</td></tr>
     *   <tr><td>11 ~ 20</td><td>×2.0</td></tr>
     *   <tr><td>21 ~ 30</td><td>×1.5</td></tr>
     *   <tr><td>31 及以上</td><td>×1.0</td></tr>
     * </table>
     */
    private static final float EXP_BOOST_UP_TO_10 = 2.5f;
    private static final float EXP_BOOST_UP_TO_20 = 2.0f;
    private static final float EXP_BOOST_UP_TO_30 = 1.5f;

    /** 按玩家当前等级取击杀经验倍率（0 级 = 未觉醒也算前期）。 */
    private static float earlyGameExpBoost(int level) {
        if (level <= 10) return EXP_BOOST_UP_TO_10;
        if (level <= 20) return EXP_BOOST_UP_TO_20;
        if (level <= 30) return EXP_BOOST_UP_TO_30;
        return 1.0f;
    }

    private static void handleExperience(Player player, MobAttributeCapability cap) {
        if (cap.getNianxian() <= 1) return;
        int wugongValue = (int) cap.getGongji();
        if (wugongValue <= 0) return;

        float playerLevel = ModAttributeAPI.getDengji(player);
        float baseMultiplier;
        float levelBonus = 0.0f;
        float maxMultiplier;

        if (playerLevel <= 20) {
            baseMultiplier = 0.05f;
            maxMultiplier = 0.15f;
        } else {
            baseMultiplier = 0.008f;
            maxMultiplier = (cap.getNianxian() >= 100000 && cap.getNianxian() <= 1500000) ? 0.10f : 0.15f;
            if (playerLevel >= 100) {
                levelBonus = Math.min(maxMultiplier - baseMultiplier, (playerLevel - 100) * 0.0005f);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            // 前期倍率乘在最后，不影响上面两档基础系数的相对关系
            float expBoost = earlyGameExpBoost((int) playerLevel);
            PlayerHunhuanAPI.addJingyan(serverPlayer, wugongValue * (baseMultiplier + levelBonus) * expBoost);
        }
    }

    private static void handleHunguDrop(LivingEntity entity, long nianxian, Player player) {
        handleHunguDrop(entity, nianxian, player, 0.0D);
    }

    /**
     * @param bonusChance 层数带来的额外绝对概率（塔内奖励才有；普通击杀传 0）
     */
    private static void handleHunguDrop(LivingEntity entity, long nianxian, Player player, double bonusChance) {
        // 只在「真正的魂骨」里抽：HUNGULIST 还混着分解八卦与魂导器，直接在里面等概率取会让
        // 实际掉出魂骨的概率只剩配置值的 7/17（详见 boneOnlyPool 的注释）
        List<RegistryObject<Item>> pool = boneOnlyPool();
        if (pool.isEmpty()) return;

        double baseChance = HunguAdminStatus.isAlwaysDrop(player) ? 1.0 :
                (KLConfig.hungupingheng.get() ? calculateHunguChance(nianxian) : KLConfig.baseDropChance.get());
        // 塔内加成是"加法"而不是乘系数：低年限档位的基础概率只有 0.1%，乘系数几乎看不出成长
        double dropChance = baseChance + Math.max(0.0D, bonusChance);
        if (bonusChance > 0.0D) {
            // 塔内奖励留 15% 的"脸黑"余量；本来就是 100% 的档位（千年万年级）不往下压
            dropChance = Math.min(Math.max(baseChance, com.TovidY.kunluncontinent.tower.TowerDropReward.HUNGU_CHANCE_CAP), dropChance);
        }
        if (RANDOM.nextDouble() >= Math.min(1.0D, dropChance)) return;
        Item item = pool.get(RANDOM.nextInt(pool.size())).get();
        ItemStack stack = new ItemStack(item);
        stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            // toUpdateNianxian 的签名是 int（现有上限 1880 万年，远在 int 范围内）
            attr.toUpdateNianxian((int) nianxian);
            String fullName = entity.getDisplayName().getString();
            String cleanName = fullName;
            if (fullName.contains("-----")) {
                cleanName = fullName.substring(0, fullName.indexOf("-----"));
            }
            attr.setSourceName(cleanName);
            int count = rollAttributeCount();
            List<String> pool2 = new ArrayList<>(List.of(
                    "gongji", "fangyu", "baojilv", "baojishanghai",
                    "wuchuan", "shanbi", "mingzhong", "kangbao", "shengminghuifu", "xixue"
            ));
            attr.getActiveAttributes().clear();
            attr.getActiveAttributes().add("maxshengming");
            Collections.shuffle(pool2);
            for (int i = 0; i < count - 1 && !pool2.isEmpty(); i++) {
                attr.getActiveAttributes().add(pool2.get(i));
            }
            stack.getOrCreateTag().put("shanhaiitematuble", attr.serializeNBT());
            ItemEntity itemEntity = entity.spawnAtLocation(stack);
            if (itemEntity != null) itemEntity.setExtendedLifetime();
        });
    }

    /**
     * 真正的魂骨物品池（7 件：头骨 / 胸骨 / 左右手骨 / 左右腿骨 / 外附魂骨）。
     *
     * <p><b>为什么必须单独抽一个池：</b>{@link ModItems#HUNGULIST} 里除了 7 件魂骨，
     * 还塞了 5 种「分解八卦」和 5 种「魂导器」（共 17 项）。
     * 如果魂骨概率判定通过后在 17 项里等概率取，实际掉出魂骨的概率只有配置值的
     * {@code 7/17 ≈ 41%} —— 这就是「配置写的概率和游戏里实际感觉不一样」的根源。</p>
     *
     * <p>惰性构建：静态初始化阶段 {@code RegistryObject.get()} 还取不到物品实例，
     * 第一次掉落时再过滤并缓存。</p>
     */
    private static List<RegistryObject<Item>> BONE_ONLY_POOL;

    private static List<RegistryObject<Item>> boneOnlyPool() {
        if (BONE_ONLY_POOL == null) {
            BONE_ONLY_POOL = ModItems.HUNGULIST.stream()
                    .filter(item -> item.get() instanceof com.TovidY.kunluncontinent.item.hungu.BoneItem)
                    .toList();
        }
        return BONE_ONLY_POOL;
    }

    //魂骨词条
    /**
     * 魂骨词条数量权重（下标 0 → 1 条，下标 9 → 10 条）。
     *
     * <h3>回文（对称）分布</h3>
     * <p>权重串 {@code 1,2,4,7,9,9,7,4,2,1} 本身是回文，峰值落在中间的 <b>5~6 条</b>：
     * 数量越靠近中段越常见，两端（1 条 / 10 条）都很稀少。总权重 46。</p>
     *
     * <table border="1">
     *   <caption>条数 → 概率</caption>
     *   <tr><th>条数</th><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td></tr>
     *   <tr><th>概率</th><td>2.2%</td><td>4.3%</td><td>8.7%</td><td>15.2%</td><td><b>19.6%</b></td><td><b>19.6%</b></td><td>15.2%</td><td>8.7%</td><td>4.3%</td><td>2.2%</td></tr>
     * </table>
     *
     * <p>平均 <b>5.5 条</b>。旧实现是 {@code 5/10/20/30/20/8/4/1.5/1/0.5}（峰值在 4 条、右半边断崖式下跌），
     * 平均只有 4.07 条，所以体感"词条很少"。新分布把中段整体抬起，同时保留两端稀有。</p>
     *
     * <p>注：{@code applyAttributes} 是给<b>每一项</b>都赋满值的，不按条数分摊 ——
     * 所以条数变多就是纯增强，不存在"条目多则单项变弱"的补偿。</p>
     */
    private static final int[] ATTRIBUTE_COUNT_WEIGHTS = {1, 2, 4, 7, 9, 9, 7, 4, 2, 1};

    /**
     * 摇魂骨词条数量（1~10）。按 {@link #ATTRIBUTE_COUNT_WEIGHTS} 的权重抽取。
     */
    private static int rollAttributeCount() {
        int total = 0;
        for (int weight : ATTRIBUTE_COUNT_WEIGHTS) {
            total += weight;
        }
        int roll = RANDOM.nextInt(total);
        for (int i = 0; i < ATTRIBUTE_COUNT_WEIGHTS.length; i++) {
            roll -= ATTRIBUTE_COUNT_WEIGHTS[i];
            if (roll < 0) {
                return i + 1;
            }
        }
        return 1;   // 理论上到不了这里
    }

    /**
     * 年限 → 概率档位（0~6 对应配置里的 tier1~tier7；-1 = 不足 10 年）。
     *
     * <p><b>档位边界是全项目唯一定义</b>：魂环与魂骨都走这里。
     * 原来两个方法各写了一遍 if 链，任何一处改边界就会让"魂环按 A 档、魂骨按 B 档"地悄悄错开，
     * 这类错位在游戏里只表现为"概率怪怪的"，极难排查。</p>
     */
    private static int nianxianTier(long nianxian) {
        if (nianxian >= 10_000_000L) return 6;   // 1000 万年以上
        if (nianxian >= 1_000_000L) return 5;    // 100 万 ~ 999 万
        if (nianxian >= 100_000L) return 4;      // 10 万 ~ 99 万
        if (nianxian >= 10_000L) return 3;       // 1 万 ~ 9.9 万
        if (nianxian >= 1_000L) return 2;        // 1000 ~ 9999
        if (nianxian >= 100L) return 1;          // 100 ~ 999
        if (nianxian >= 10L) return 0;           // 10 ~ 99
        return -1;                               // 不足 10 年
    }

    /** 魂骨爆率：按档位读配置；不足 10 年不掉魂骨（配置的 tier1 从 10 年起算）。 */
    private static double calculateHunguChance(long nianxian) {
        return switch (nianxianTier(nianxian)) {
            case 0 -> KLConfig.dropChanceTier1.get();
            case 1 -> KLConfig.dropChanceTier2.get();
            case 2 -> KLConfig.dropChanceTier3.get();
            case 3 -> KLConfig.dropChanceTier4.get();
            case 4 -> KLConfig.dropChanceTier5.get();
            case 5 -> KLConfig.dropChanceTier6.get();
            case 6 -> KLConfig.dropChanceTier7.get();
            default -> 0.0D;
        };
    }

    private static void tryGenerateHunhuan(MobAttributeCapability cap, Level level, BlockPos pos, Entity source) {
        double prob = KLConfig.ENABLE_HUNHUAN_PROBABILITY.get() ? getHunhuanProb(cap.getNianxian()) : 1.0;
        if (RANDOM.nextDouble() <= prob) {
            addHunhuanEntity(cap, level, pos, source);
        }
    }

    private static void handleGodGlimpse(ServerPlayer player, LivingEntity victim) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {

            if (cap.getGodName() != null && !cap.getGodName().isEmpty()) {
                return;
            }

            boolean isQualified = cap.debugIgnoreTianfu || (cap.getDengji() > 75 && cap.getXiantianTalent() >= 7);

            if (isQualified) {
                boolean isLevel99 = cap.getDengji() >= 99;

                if (victim instanceof Drowned || victim instanceof Guardian || victim instanceof ElderGuardian) {
                    float chance = cap.debugForceSuccess ? 1.0f : (isLevel99 ? 0.01f : 0.005f);
                    if (RANDOM.nextFloat() < chance) {
                        triggerGodExam(player, cap, "sea_god", "§b海神");
                        resetDebugStatus(cap);
                        return;
                    }
                }

                if (victim.getMobType() == MobType.UNDEAD) {
                    float chance = cap.debugForceSuccess ? 1.0f : (isLevel99 ? 0.01f : 0.005f);
                    if (RANDOM.nextFloat() < chance) {
                        triggerGodExam(player, cap, "angel_god", "§e天使神");
                        resetDebugStatus(cap);
                        return;
                    }
                }

                if (cap.getGongji() > 50000f) {
                    float chance = cap.debugForceSuccess ? 1.0f : (isLevel99 ? 0.01f : 0.0055f);
                    if (RANDOM.nextFloat() < chance) {
                        triggerGodExam(player, cap, "asura_god", "§c修罗神");
                        resetDebugStatus(cap);
                        return;
                    }
                }
            }
        });
    }

    private static void resetDebugStatus(PlayerAttributeCapability cap) {
        cap.debugIgnoreTianfu = false;
        cap.debugForceSuccess = false;
    }

    private static void triggerGodExam(ServerPlayer player, PlayerAttributeCapability cap, String godId, String godName) {
        cap.initializeGodExam(player, godId);
        AchievementAPI.onGodExamStart(player);
        MinecraftServer server = player.getServer();
        if (server != null) {
            Component msg = Component.literal("§l§f【神之遗迹】§6天降异象，神辉洒落！§f玩家 §e" + player.getName().getString() + " §f得到了 " + godName + " §f的认可，开启了神之试炼！");
            server.getPlayerList().broadcastSystemMessage(msg, false);
        }
        player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 0.5f);
        com.TovidY.kunluncontinent.network.NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
        SynsAPI.synsPlayerAttribute(player);
        resetDebugStatus(cap);

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ModEffects.THE_GAZE_OF_GOD.get(), 310, 0, false, false
        ));

        com.TovidY.kunluncontinent.network.NetworkHandler.sendToClient(
                new PacketPlayGodRitualEffect(300, godName),
                player
        );

        if (server != null) {
            java.util.Timer timer = new java.util.Timer();
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    server.execute(() -> {
                        if (player.isAlive()) {
                            player.sendSystemMessage(Component.literal("§6§l[昆仑大陆] 传承完成！神位种子已彻底融入你的灵魂，退出幻境！"));
                            returnPlayerToSpawn(player);
                            TowerStateManager.releaseTower(player);
                            TowerSpawnerEngine.clearTowerMonstersForPlayer(player);
                        }
                    });
                }
            }, 15000);
        }
    }

    /** 魂环生成概率：按档位读配置；不足 10 年沿用 tier1（配置注释写的就是"10 年内"）。 */
    private static double getHunhuanProb(long nianxian) {
        return switch (Math.max(0, nianxianTier(nianxian))) {
            case 0 -> KLConfig.TIER1_PROB.get();
            case 1 -> KLConfig.TIER2_PROB.get();
            case 2 -> KLConfig.TIER3_PROB.get();
            case 3 -> KLConfig.TIER4_PROB.get();
            case 4 -> KLConfig.TIER5_PROB.get();
            case 5 -> KLConfig.TIER6_PROB.get();
            default -> KLConfig.TIER7_PROB.get();
        };
    }

    /**
     * 生成一个魂环实体。
     *
     * @param source 掉落这个魂环的生物。<b>必须传</b> —— 魂环的能力是从它身上整份
     *               复制过来的，顺手把来源实体类型 ID 也写进能力里，
     *               吸收时才能判断「这个魂环该不该给特殊魂技」（见 {@code special} 包）。
     */
    public static void addHunhuanEntity(MobAttributeCapability sourceCap, Level level, BlockPos pos, Entity source) {
        if (level.isClientSide) return;
        HunhuanEntity hunhuan = new HunhuanEntity(EntityInit.HUNHUAN.get(), level);
        hunhuan.setPos(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
        hunhuan.setNoGravity(true);
        hunhuan.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(targetCap -> {
            targetCap.deserializeNBT(sourceCap.serializeNBT());
            hunhuan.setNianxian(targetCap.getNianxian());
            // 记下来源生物类型（魂环是独立实体，拿不到原生物，只能靠这个带过去）
            if (source != null) {
                targetCap.setSourceType(String.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(source.getType())));
            }
        });
        level.addFreshEntity(hunhuan);
    }

}