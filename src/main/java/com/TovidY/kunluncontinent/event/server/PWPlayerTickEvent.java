package com.TovidY.kunluncontinent.event.server;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PWPlayerTickEvent {

    @SubscribeEvent
    public static void onStartTracking(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END) {
            if (!player.level().isClientSide) {
                long gameTime = player.level().getGameTime();
                if (gameTime % 100 == 0) {
                    SynsAPI.synsPlayerAttribute(player);
                }
                if (gameTime % 20 == 0) {
                    float maxshengming = ModAttributeAPI.getMaxshengming(player);
                    if (Math.abs(maxshengming - player.getMaxHealth()) > 0.1f) {
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxshengming);
                    }
                }
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                    playerUpdateServere(player, capability);
                    if (gameTime % 100 == 0) {
                        updatePlayerHealthRegen(player, capability);
                    }
                    if (gameTime % 120 == 0) {
                        updateJingshenliRegen(player, capability);
                    }
                    updatePlayerFly(player, capability);
                });
            }
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
            // 检查是否被击落（可以添加一个临时状态标记）
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
                capability.setJingshenli(capability.getJingshenli() - 25 - (float) capability.getDengji() / 5);
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