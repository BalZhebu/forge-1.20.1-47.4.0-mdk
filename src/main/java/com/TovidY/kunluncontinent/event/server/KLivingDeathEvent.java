package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.command.HunguAdminStatus;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanDropHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

            // E. 属性分配逻辑
            attr.getActiveAttributes().clear();
            // 保底必得：生命加成
            attr.getActiveAttributes().add("maxshengming");

            // 随机打乱池子，抽取剩余词条
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
        if (r < 0.05) return 1;   // 5% 概率 1 词条
        if (r < 0.15) return 2;   // 10% 概率 2 词条
        if (r < 0.35) return 3;   // 20% 概率 3 词条
        if (r < 0.65) return 4;   // 30% 概率 4 词条 (峰值)
        if (r < 0.85) return 5;   // 20% 概率 5 词条 (峰值)
        if (r < 0.93) return 6;   // 8% 概率 6 词条
        if (r < 0.97) return 7;   // 4% 概率 7 词条
        if (r < 0.985) return 8;  // 1.5% 概率 8 词条
        if (r < 0.995) return 9;  // 1% 概率 9 词条
        return 10;                // 0.5% 概率 10 词条 (极品全满)
    }

    private static double calculateHunguChance(int nianxian) {
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

    private static double getHunhuanProb(long nianxian) {
        if (nianxian >= 10000000) return KLConfig.TIER7_PROB.get();
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