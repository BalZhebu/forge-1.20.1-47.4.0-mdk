package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.Init.KLConfig;
import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanDropHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

            //内丹掉落机制
            if (sourceEntity instanceof Player player) {
                NeidanDropHandler.tryDropNeidan(entity, cap,player);
            }

            if (cap.getNianxian() > 1 && sourceEntity instanceof Player player) {
                int wugongValue = (int) cap.getGongji();
                if (wugongValue > 0) {
                    float expReward;
                    float playerLevel = ModAttributeAPI.getDengji(player); // 获取玩家等级

                    float baseMultiplier;      // 基础倍率
                    float levelBonus = 0.0f;  // 等级加成
                    float maxMultiplier;       // 最大倍率

                    // 20级以内的新手保护期
                    if (playerLevel <= 20) {
                        baseMultiplier = 0.05f; // 前期基础倍率提高10倍
                        maxMultiplier = 0.15f;   // 最大倍率保持不变
                        // 20级以内不享受等级加成
                    }

                    // 20级以上的正常逻辑
                    else if (cap.getNianxian() >= 100000 && cap.getNianxian() <= 1500000) {
                        baseMultiplier = 0.008f;
                        maxMultiplier = 0.10f;
                        if (playerLevel >= 100) {
                            levelBonus = Math.min(
                                    maxMultiplier - baseMultiplier,
                                    (playerLevel - 100) * 0.0005f
                            );
                        }
                    }
                    else {
                        baseMultiplier = 0.008f;
                        maxMultiplier = 0.15f;
                        if (playerLevel >= 100) {
                            levelBonus = Math.min(
                                    maxMultiplier - baseMultiplier,
                                    (playerLevel - 100) * 0.0005f
                            );
                        }
                    }

                    expReward = wugongValue * (baseMultiplier + levelBonus);

                    if (player instanceof ServerPlayer serverPlayer) {
                        PlayerHunhuanAPI.addJingyan(serverPlayer, expReward);
                    }

                }
            }
            if (sourceEntity instanceof Player player) {
                tryGenerateHunhuan(cap, entity.level(), entity.getOnPos());
            }
        });
    }

    private static void tryGenerateHunhuan(MobAttributeCapability cap, Level level, BlockPos pos) {
        if (!KLConfig.ENABLE_HUNHUAN_PROBABILITY.get()) {
            addHunhuanEntity(cap, level, pos);
            return;
        }
        long nianxian = cap.getNianxian();
        double probability = getHunhuanProbability(nianxian);
        if (RANDOM.nextDouble() <= probability) {
            addHunhuanEntity(cap, level, pos);
        }
    }

    private static double getHunhuanProbability(long nianxian) {
        if (nianxian >= 10000000) {
            return KLConfig.TIER7_PROB.get();
        } else if (nianxian >= 100000) {
            return KLConfig.TIER5_PROB.get();
        } else if (nianxian >= 10000) {
            return KLConfig.TIER4_PROB.get();
        } else if (nianxian >= 1000) {
            return KLConfig.TIER3_PROB.get();
        } else if (nianxian >= 100) {
            return KLConfig.TIER2_PROB.get();
        } else if (nianxian >= 10) {
            return KLConfig.TIER1_PROB.get();
        } else {
            return KLConfig.TIER1_PROB.get();
        }
    }

    public static void addHunhuanEntity(MobAttributeCapability sourceCap, Level level, BlockPos onPos) {
        if (level.isClientSide) return;
        HunhuanEntity hunhuanEntity = new HunhuanEntity(EntityInit.HUNHUAN.get(), level);
        hunhuanEntity.setPos(onPos.getX() + 0.5, onPos.getY() + 1.2, onPos.getZ() + 0.5);
        hunhuanEntity.setNoGravity(true);
        CompoundTag tag = sourceCap.serializeNBT();
        hunhuanEntity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(targetCap -> {
            targetCap.deserializeNBT(tag);
            hunhuanEntity.setNianxian(targetCap.getNianxian());
        });
        level.addFreshEntity(hunhuanEntity);
    }

}
