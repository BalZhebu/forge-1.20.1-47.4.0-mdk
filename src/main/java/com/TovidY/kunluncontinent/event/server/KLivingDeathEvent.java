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
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanDropHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.client.PacketSyncGodData;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//玩家击杀生物事件

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KLivingDeathEvent {
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void livingDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;
        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            Entity sourceEntity = event.getSource().getEntity();
            if (!(sourceEntity instanceof Player player)) return;

            ModDropHandler.tryExtraDrops(entity, player);

            handleGodGlimpse((ServerPlayer) player,entity);

            NeidanDropHandler.tryDropNeidan(entity, cap, player);
            handleExperience(player, cap);
            tryGenerateHunhuan(cap, entity.level(), entity.getOnPos());
            handleHunguDrop(entity, (int) cap.getNianxian(), player);
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
            // 1. 基础互斥逻辑：如果已经有神位了，直接跳过判定
            // 检查 godName 是否为空，确保玩家一次只能开启一个神考
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

                // --- C. 修罗神获取逻辑 (攻击力 > 15W，击杀任意生物) ---
                if (cap.getGongji() > 150000f) {
                    // 如果开启了 debugForceSuccess，概率为 100%，否则为 0.05%
                    float chance = cap.debugForceSuccess ? 1.0f : 0.0005f;
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
        MinecraftServer server = player.getServer();
        if (server != null) {
            Component msg = Component.literal("§l§f【神之遗迹】§6天降异象，神辉洒落！§f玩家 §e" + player.getName().getString() + " §f得到了 " + godName + " §f的认可，开启了神之试炼！");
            server.getPlayerList().broadcastSystemMessage(msg, false);
        }
        player.playNotifySound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 0.5f);
        com.TovidY.kunluncontinent.network.NetworkHandler.sendToClient(
                new PacketSyncGodData(cap),
                player
        );
        SynsAPI.synsPlayerAttribute(player);
        resetDebugStatus(cap);
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