package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

//伤害源判定
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class CombatEventHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity target = event.getEntity();

        float shanbi = ModAttributeAPI.getShanbi(target);
        float mingzhong = ModAttributeAPI.getMingzhong(attacker);
        float dodgeChance = Math.max(0, shanbi - mingzhong) / (Math.max(0, shanbi - mingzhong) + 100f);
        if (RANDOM.nextFloat() <= dodgeChance) {
            event.setCanceled(true);
            return;
        }

        float gongji = ModAttributeAPI.getGongji(attacker);
        float wuchuan = ModAttributeAPI.getWuchuan(attacker);
        float fangyu = ModAttributeAPI.getEffectiveFangyu(target);

        float effectiveFangyu = Math.max(0, fangyu - wuchuan);
        float reductionFactor = 100f / (100f + effectiveFangyu);
        float baseDamage = (gongji + event.getAmount()) * reductionFactor;

        float baojilv = ModAttributeAPI.getBaojilv(attacker);
        float kangbao = ModAttributeAPI.getKangbao(target);
        float finalCritRate = Math.max(0, baojilv - kangbao);
        boolean isCrit = (RANDOM.nextFloat() * 100) < finalCritRate;

        float finalDamage = baseDamage;
        boolean effectTriggered = false;

        if (RANDOM.nextFloat() < 0.1f) { // 10% 概率触发特殊效果
            effectTriggered = handleSpecialEffects(attacker, target, baseDamage);
            if (effectTriggered) {
                finalDamage = calculateSpecialDamage(attacker, target, baseDamage);
            }
        }

        if (!effectTriggered && isCrit) {
            float baojishanghai = ModAttributeAPI.getBaojishanghai(attacker);
            finalDamage = baseDamage * (baojishanghai / 100f);
        }

        finalDamage = Math.max(0.1f, finalDamage);
        event.setAmount(finalDamage);

        handleLifesteal(attacker, finalDamage);

        if (attacker instanceof Player player && !effectTriggered) {
            sendDamageMessage(player, target, finalDamage, isCrit);
        }
    }

    private static void handleLifesteal(LivingEntity attacker, float damage) {
        float xixueValue = ModAttributeAPI.getXixue(attacker);
        if (xixueValue > 0 && attacker.getHealth() > 0) {
            float healAmount = (xixueValue / (xixueValue + 100f)) * damage;
            attacker.heal(healAmount);
        }
    }

    private static boolean handleSpecialEffects(LivingEntity attacker, LivingEntity target, float baseDamage) {
        float roll = RANDOM.nextFloat();
        String name = target.getDisplayName().getString();

        if (roll <= 0.225f) { // 撕裂
            showEffectMsg(attacker, "§7§l撕裂！", name, baseDamage * 1.5f);
            return true;
        } else if (roll <= 0.45f) { // 破甲
            target.addEffect(new MobEffectInstance(ModEffects.ARMOR_PIERCING.get(), 60, 1));
            showEffectMsg(attacker, "§9§l破甲！", name, baseDamage * 1.4f);
            return true;
        } else if (roll <= 0.675f) { // 燃烧
            target.addEffect(new MobEffectInstance(ModEffects.SCORCHING.get(), 200, 1));
            showEffectMsg(attacker, "§4§l燃烧！", name, baseDamage * 1.2f);
            return true;
        } else if (roll <= 0.9f) { // 震荡
            target.addEffect(new MobEffectInstance(ModEffects.DIZZINESS.get(), 60, 5));
            showEffectMsg(attacker, "§6§l震荡！", name, baseDamage * 1.2f);
            return true;
        } else { // 湮灭
            float trueDamage = target.getMaxHealth() * 0.2f;
            target.hurt(target.damageSources().magic(), trueDamage);
            showEffectMsg(attacker, "§5§l湮灭！", name, trueDamage);
            return true;
        }
    }

    private static float calculateSpecialDamage(LivingEntity attacker, LivingEntity target, float base) {
        return base * 1.3f;
    }

    private static void showEffectMsg(LivingEntity attacker, String prefix, String targetName, float dmg) {
        if (attacker instanceof Player player) {
            player.displayClientMessage(Component.literal(prefix + " §f对 §e" + targetName + " §f造成 §6§l" + String.format("%.1f", dmg) + " 点伤害"), true);
        }
    }

    private static void sendDamageMessage(Player player, LivingEntity target, float damage, boolean isCrit) {
        String name = target.getDisplayName().getString();
        String dmgStr = String.format("%.1f", damage);
        Component msg = isCrit
                ? Component.literal("§c§l暴击！ §f对 §e" + name + " §f造成 §6§l" + dmgStr+ " 点伤害")
                : Component.literal("§7对 §e" + name + " §f造成 §f" + dmgStr+ " 点伤害");
        player.displayClientMessage(msg, true);
    }
}