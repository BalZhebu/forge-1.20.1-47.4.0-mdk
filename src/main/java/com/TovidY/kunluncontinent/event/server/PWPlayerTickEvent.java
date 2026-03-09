package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// 玩家每Tick触发

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PWPlayerTickEvent {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            long gameTime = player.level().getGameTime();
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                playerUpdateServere(player, capability);

                handleSkillCasting(serverPlayer, capability, gameTime);
                
                if (gameTime % 20 == 0) {
                    float maxshengming = ModAttributeAPI.getMaxshengming(player);
                    if (Math.abs(maxshengming - player.getMaxHealth()) > 0.1f) {
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxshengming);
                    }
                }
                if (gameTime % 100 == 0) {
                    updatePlayerHealthRegen(player, capability);
                    SynsAPI.synsPlayerAttribute(player);
                }
                if (gameTime % 120 == 0) {
                    updateJingshenliRegen(player, capability);
                }

                updatePlayerFly(player, capability);

                handleMeditationLogic(serverPlayer, capability, gameTime);
            });
        }
    }

    private static void handleSkillCasting(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        BaseSkillItem castingSkill = cap.getCurrentCastingSkill();
        if (castingSkill != null) {
            int currentTick = cap.getCastingTick();
            cap.setCastingTick(currentTick + 1);
            if (gameTime % 5 == 0) {
                player.serverLevel().sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 2.2, player.getZ(),
                        3, 0.2, 0.2, 0.2, 0.0
                );
            }
            if (cap.getCastingTick() >= cap.getRequiredCastTick()) {
                String currentWuhun = cap.getWuhunName();
                int selectedSlot = cap.getSelectedSkillSlot();
                int nianxian = 10;
                List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(currentWuhun);
                if (rings != null && selectedSlot >= 0 && selectedSlot < rings.size()) {
                    nianxian = (int) rings.get(selectedSlot).getNianxian();
                }
                float powerMultiplier = castingSkill.getPowerMultiplier(nianxian);
                float costMultiplier = castingSkill.getCostMultiplier(nianxian);
                castingSkill.executeEffect(player.level(), player, powerMultiplier);
                castingSkill.applyPenalty(player, costMultiplier);
                cap.setSkillLastUsedTime(currentWuhun, selectedSlot, gameTime);
                player.displayClientMessage(Component.literal("§a§l魂技释放成功！"), true);
                cap.stopCasting();
                SynsAPI.synsPlayerAttribute(player);
            }
        }
    }

    private static void handleMeditationLogic(ServerPlayer player, PlayerAttributeCapability cap, long gameTime) {
        boolean isMeditating = player.getVehicle() != null && player.getVehicle().getTags().contains("putuan_seat");
        if (isMeditating) {
            if (gameTime % 20 == 0) {
                float currentJs = cap.getJingshenli();
                float maxJs = cap.getMaxjingshenli();
                if (currentJs < maxJs) {
                    float recoveryAmount = (maxJs * 0.02f) + 5.0f;
                    cap.setJingshenli(Math.min(maxJs, currentJs + recoveryAmount));
                    if (cap.getXiulianTime() <= 0) {
                        SynsAPI.synsPlayerAttribute(player);
                    }
                }
            }

            if (gameTime % 20 == 0) {
                int currentTime = cap.getXiulianTime();
                if (currentTime > 0) {
                    cap.setXiulianTime(currentTime - 1);
                    int level = cap.getDengji();
                    float minutesToLevel = (level <= 30) ? 5f : (level <= 89) ? 7f : 10f;
                    float gain = cap.getMaxjingyan() / (minutesToLevel * 60f);
                    cap.setJingyan(cap.getJingyan() + gain);
                    PlayerUpgradeSystem.checkAndProcessUpgrade(player, cap);

                    if (gameTime % 40 == 0) SynsAPI.synsPlayerAttribute(player);

                    if (cap.getXiulianTime() <= 0) {
                        cap.setUsingAll(true);
                        player.sendSystemMessage(Component.translatable("putuan.xiulian.finish"));
                        player.stopRiding();
                    }
                }
            }
        } else {
            int recoverTickRate = cap.isUsingAll() ? 87 : 100;
            if (gameTime % recoverTickRate == 0) {
                if (cap.getXiulianTime() < 600) {
                    cap.setXiulianTime(cap.getXiulianTime() + 1);
                    if (cap.getXiulianTime() >= 600) cap.setUsingAll(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ArmorStand armorStand && armorStand.getTags().contains("putuan_seat")) {
            if (armorStand.getPassengers().isEmpty()) {
                armorStand.discard();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (player.getVehicle() != null && player.getVehicle().getTags().contains("putuan_seat")) {
                    player.stopRiding();
                    player.sendSystemMessage(Component.translatable("心神受损").withStyle(ChatFormatting.RED));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                }
                    if (cap.getCurrentCastingSkill() != null) {
                    cap.stopCasting();
                    player.displayClientMessage(Component.literal("§c魂力紊乱，吟唱中断！"), true);
                }
            });
        }
    }

    private static void updatePlayerHealthRegen(Player player, PlayerAttributeCapability capability) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float regenAttr = ModAttributeAPI.getShengminghuifu(player);

        if (currentHealth < maxHealth && regenAttr > 0) {
            float regenPercent = Math.min(regenAttr * 0.002f, 0.06f); // 上限 5%
            float healAmount = maxHealth * regenPercent;

            if (healAmount > 0) {
                player.heal(healAmount);
                SynsAPI.synsPlayerAttribute(player);
            }
        }
    }

    private static void updateJingshenliRegen(Player player, PlayerAttributeCapability capability) {
        float currentJingshenli = capability.getJingshenli();
        float maxJingshenli = ModAttributeAPI.getMaxjingshenli(player);

        if (currentJingshenli < maxJingshenli) {
            float regenAmount = maxJingshenli * 0.01f;
            float nextJingshenli = Math.min(currentJingshenli + regenAmount, maxJingshenli);
            capability.setJingshenli(nextJingshenli);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    private static void updatePlayerFly(Player player, @NotNull PlayerAttributeCapability capability) {
        if (!player.isCreative() && !player.isSpectator()) {
            if (player.getPersistentData().contains("knocked_down")) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            else if (ModAttributeAPI.getMaxjingshenli(player) > 5000) {
                player.getAbilities().mayfly = true;
            } else {
                player.getAbilities().mayfly = false;
            }

            if (player.getAbilities().flying && player.level().getGameTime() % 10 == 0) {
                capability.setJingshenli(capability.getJingshenli() - 30 + (float) capability.getDengji() / 5);
            }

            if (capability.getWuhunName() != null && player.level().getGameTime() % 20 == 0) {
                int level = capability.getDengji();
                float cost;
                if (level <= 20) {
                    cost = 1.0f + (level * 0.01f);
                }else if (level <= 30) {
                    cost = 3.0f + (level * 0.1f);
                } else if (level <= 89) {
                    cost = 10.0f + (level * 0.2f);
                } else {
                    cost = 20.0f + (level * 0.3f);
                }
                capability.setJingshenli(capability.getJingshenli() - cost);
            }
        }
    }

    private static void playerUpdateServere(Player player, @NotNull PlayerAttributeCapability capability) {
        float jingshenli = ModAttributeAPI.getJingshenli(player);
        if(jingshenli<0){
            player.sendSystemMessage(Component.translatable("精神力不足").withStyle(ChatFormatting.GRAY));
            if(player.getVehicle() instanceof HunhuanEntity){
                player.removeVehicle();
            }
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 2));
            capability.setJingshenli(0);
            capability.setHunhuankuaiguan(-1);
            SynsAPI.synsPlayerAttribute(player);
        }
    }
}