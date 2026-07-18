package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.command.HunguAdminStatus;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.godclass.interfac.GodTaskType;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanDropHandler;
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
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

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

        towerSpawed(entity);

        playerDeach(entity);

        if (entity.getPersistentData().getBoolean("TowerSpawned")) {
            return;
        }

        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            Entity sourceEntity = event.getSource().getEntity();
            if (!(sourceEntity instanceof Player player)) return;

            ModDropHandler.tryExtraDrops(entity, player);

            handleGodGlimpse((ServerPlayer) player,entity);

            NeidanDropHandler.tryDropNeidan(entity, cap, player);
            handleExperience(player, cap);
            tryGenerateHunhuan(cap, entity.level(), entity.getOnPos());
            handleHunguDrop(entity, (int) cap.getNianxian(), player);

            handleGodKillTask(player, entity);
        });
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
                                    NetworkHandler.sendToClient(new PacketSyncTowerTimer(0, false), towerPlayer);
                                    if (attr.getGodName() == null || attr.getGodName().isEmpty()) {
                                        double successChance = attr.debugForceSuccess ? 1.0 : (0.005 * (oldFloor + 1));
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
                    godCap.checkTaskCompletion(serverPlayer);
                }
                NetworkHandler.sendToClient(new PacketSyncGodData(godCap), serverPlayer);
            }
        });
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
            PlayerHunhuanAPI.addJingyan(serverPlayer, wugongValue * (baseMultiplier + levelBonus));
        }
    }

    private static void handleHunguDrop(LivingEntity entity, int nianxian, Player player) {
        if (ModItems.HUNGULIST == null || ModItems.HUNGULIST.isEmpty()) return;
        double dropChance = HunguAdminStatus.isAlwaysDrop(player) ? 1.0 :
                (KLConfig.hungupingheng.get() ? calculateHunguChance(nianxian) : KLConfig.baseDropChance.get());
        if (RANDOM.nextDouble() > dropChance) return;
        Item item = ModItems.HUNGULIST.get(RANDOM.nextInt(ModItems.HUNGULIST.size())).get();
        ItemStack stack = new ItemStack(item);
        stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            attr.toUpdateNianxian(nianxian);
            String fullName = entity.getDisplayName().getString();
            String cleanName = fullName;
            if (fullName.contains("-----")) {
                cleanName = fullName.substring(0, fullName.indexOf("-----"));
            }
            attr.setSourceName(cleanName);
            int count = rollAttributeCount();
            List<String> pool = new ArrayList<>(List.of(
                    "gongji", "fangyu", "baojilv", "baojishanghai",
                    "wuchuan", "shanbi", "mingzhong", "kangbao", "shengminghuifu", "xixue"
            ));
            attr.getActiveAttributes().clear();
            attr.getActiveAttributes().add("maxshengming");
            Collections.shuffle(pool);
            for (int i = 0; i < count - 1 && !pool.isEmpty(); i++) {
                attr.getActiveAttributes().add(pool.get(i));
            }
            stack.getOrCreateTag().put("shanhaiitematuble", attr.serializeNBT());
            ItemEntity itemEntity = entity.spawnAtLocation(stack);
            if (itemEntity != null) itemEntity.setExtendedLifetime();
        });
    }

    private static int rollAttributeCount() {
        double r = RANDOM.nextDouble();
        if (r < 0.05) return 1;
        if (r < 0.15) return 2;
        if (r < 0.35) return 3;
        if (r < 0.65) return 4;
        if (r < 0.85) return 5;
        if (r < 0.93) return 6;
        if (r < 0.97) return 7;
        if (r < 0.985) return 8;
        if (r < 0.995) return 9;
        return 10;
    }

    private static double calculateHunguChance(int nianxian) {
        if (nianxian >= 100000000) return KLConfig.dropChanceTier7.get();
        if (nianxian >= 10000000) return KLConfig.dropChanceTier7.get();
        if (nianxian >= 1000000)  return KLConfig.dropChanceTier6.get();
        if (nianxian >= 100000)   return KLConfig.dropChanceTier5.get();
        if (nianxian >= 10000)    return KLConfig.dropChanceTier4.get();
        if (nianxian >= 1000)     return KLConfig.dropChanceTier3.get();
        if (nianxian >= 100)      return KLConfig.dropChanceTier2.get();
        if (nianxian >= 10)       return KLConfig.dropChanceTier1.get();
        return 0;
    }

    private static void tryGenerateHunhuan(MobAttributeCapability cap, Level level, BlockPos pos) {
        double prob = KLConfig.ENABLE_HUNHUAN_PROBABILITY.get() ? getHunhuanProb(cap.getNianxian()) : 1.0;
        if (RANDOM.nextDouble() <= prob) {
            addHunhuanEntity(cap, level, pos);
        }
    }


    private static void handleGodGlimpse(ServerPlayer player, LivingEntity victim) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getGodName() != null && !cap.getGodName().isEmpty()) {
                return;
            }
            // 2. 门槛判定逻辑
            // 如果开启了 debugIgnoreTianfu (由指令控制)，则跳过等级和天赋检查
            // 否则，必须满足：等级 > 75 且 先天天赋 >= 7
            boolean isQualified = cap.debugIgnoreTianfu || (cap.getDengji() > 75 && cap.getXiantianTalent() >= 7);

            if (isQualified) {
                // --- A. 海神获取逻辑 (溺尸、守卫者、远古守卫者) ---
                if (victim instanceof Drowned || victim instanceof Guardian || victim instanceof ElderGuardian) {
                    // 如果开启了 debugForceSuccess，概率为 100%，否则为 0.5%
                    float chance = cap.debugForceSuccess ? 1.0f : 0.005f;
                    if (RANDOM.nextFloat() < chance) {
                        triggerGodExam(player, cap, "sea_god", "§b海神");
                        resetDebugStatus(cap);
                        return;
                    }
                }

                // --- B. 天使神获取逻辑 (击杀亡灵生物) ---
                if (victim.getMobType() == MobType.UNDEAD) {
                    // 如果开启了 debugForceSuccess，概率为 100%，否则为 0.2%
                    float chance = cap.debugForceSuccess ? 1.0f : 0.002f;
                    if (RANDOM.nextFloat() < chance) {
                        triggerGodExam(player, cap, "angel_god", "§e天使神");
                        resetDebugStatus(cap);
                        return;
                    }
                }

                // --- C. 修罗神获取逻辑 (攻击力 > 5W，击杀任意生物) ---
                if (cap.getGongji() > 50000f) {
                    // 如果开启了 debugForceSuccess，概率为 100%，否则为 0.05%
                    float chance = cap.debugForceSuccess ? 1.0f : 0.001f;
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
        // [前面你原有的初始化、广播、音效代码全部保持不变...]
        cap.initializeGodExam(player, godId);
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

    private static double getHunhuanProb(long nianxian) {
        if (nianxian >= 10000000) return KLConfig.TIER7_PROB.get();
        if (nianxian >= 1000000)  return KLConfig.TIER6_PROB.get();
        if (nianxian >= 100000)  return KLConfig.TIER5_PROB.get();
        if (nianxian >= 10000)   return KLConfig.TIER4_PROB.get();
        if (nianxian >= 1000)    return KLConfig.TIER3_PROB.get();
        if (nianxian >= 100)     return KLConfig.TIER2_PROB.get();
        return KLConfig.TIER1_PROB.get();
    }

    public static void addHunhuanEntity(MobAttributeCapability sourceCap, Level level, BlockPos pos) {
        if (level.isClientSide) return;
        HunhuanEntity hunhuan = new HunhuanEntity(EntityInit.HUNHUAN.get(), level);
        hunhuan.setPos(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
        hunhuan.setNoGravity(true);
        hunhuan.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(targetCap -> {
            targetCap.deserializeNBT(sourceCap.serializeNBT());
            hunhuan.setNianxian(targetCap.getNianxian());
        });
        level.addFreshEntity(hunhuan);
    }

}