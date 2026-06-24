package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.DamageIndicatorRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

//伤害源判定

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public class CombatEventHandler {
    private static final Random RANDOM = new Random();

    /**
     * 【全新核心方法】：动态计算玩家在飞行状态下的伤害乘数（也就是剩余伤害百分比）
     */
    private static float getFlyingDamageMultiplier(Player player) {
        if (!player.getAbilities().flying) {
            return 1.0f;
        }
        int level = player.experienceLevel;

        if (level < 70) {
            return 0.6f;
        } else if (level >= 90) {
            return 0.9f;
        } else {
            return 0.6f + (level - 70) * 0.015f;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity target = event.getEntity();
        if (target == null || !target.isAlive()) return;

        if (attacker instanceof Player player && target.getPersistentData().getBoolean("Skill_MianShang_Available")) {
            if (RANDOM.nextFloat() < 0.10f) {
                event.setCanceled(true);
                processDisplay(player, target, null, "免疫", 0xFFFF0000);
                return;
            }
        }

        // ==================== 闪避判定 ====================
        float shanbi = ModAttributeAPI.getShanbi(target);
        float mingzhong = ModAttributeAPI.getMingzhong(attacker);
        float diff = Math.max(0, shanbi - mingzhong);
        float dodgeChance = diff / (diff + 100f);
        if (RANDOM.nextFloat() < dodgeChance) {
            event.setCanceled(true);
            if (attacker instanceof Player player) {
                Component dodgeMsg = Component.literal("§e" + target.getDisplayName().getString() + " §7§l闪避了这次攻击！");
                processDisplay(player, target, dodgeMsg, "闪避", 0xFFFFFF00);
            }
            return;
        }

        // ==================== 伤害计算 ====================
        float gongji = ModAttributeAPI.getGongji(attacker);
        float wuchuan = ModAttributeAPI.getWuchuan(attacker);
        float fangyu = ModAttributeAPI.getEffectiveFangyu(target);

        float effectiveFangyu = Math.max(0, fangyu - wuchuan);
        float reductionFactor = 100f / (100f + effectiveFangyu);
        float baseDamage = (gongji + event.getAmount()) * reductionFactor;
        float finalDamage = baseDamage;

        boolean isCrit = false;
        boolean effectTriggered = false;

        // ==================== 5大特殊特效判定 ====================
        if (RANDOM.nextFloat() < 0.1f) {
            effectTriggered = triggerSpecialEffects(attacker, target, baseDamage);
            if (effectTriggered) {
                finalDamage = calculateSpecialDamage(attacker, target, baseDamage);
            }
        }

        // ==================== 暴击判定 ====================
        if (!effectTriggered) {
            float baojilv = ModAttributeAPI.getBaojilv(attacker);
            float kangbao = ModAttributeAPI.getKangbao(target);
            float finalCritRate = Math.max(0, baojilv - kangbao);
            if ((RANDOM.nextFloat() * 100) < finalCritRate) {
                isCrit = true;
                float baojishanghai = ModAttributeAPI.getBaojishanghai(attacker);
                finalDamage = baseDamage * (baojishanghai / 100f);
            }
        }

        // ==================== 空战御空法则动态拦截 ====================
        if (attacker instanceof Player player) {
            // 乘以我们全新设计的动态等级过渡系数
            finalDamage = finalDamage * getFlyingDamageMultiplier(player);
        }

        finalDamage = Math.max(0.1f, finalDamage);
        event.setAmount(finalDamage);
        handleLifesteal(attacker, finalDamage);

        // ==================== 普通/暴击伤害消息发送 ====================
        if (attacker instanceof Player player) {
            if (!effectTriggered) {
                String dmgStr = String.format("%.1f", finalDamage);
                if (isCrit) {
                    Component msg = Component.literal("§c§l暴击！ §f对 §e" + target.getDisplayName().getString() + " §f造成 §6§l" + dmgStr + " 点伤害");
                    processDisplay(player, target, msg, "暴击 " + dmgStr, 0xFFFF2222);
                } else {
                    Component msg = Component.literal("§7对 §e" + target.getDisplayName().getString() + " §f造成 §f" + dmgStr + " 点伤害");
                    processDisplay(player, target, msg, dmgStr, 0xFFFFFFFF);
                }
            }
        }

        // ==================== 普通/暴击伤害消息发送 与 【荆棘之体】反伤判定 ====================
        if (attacker instanceof Player player) {

            if (target.isAlive() && target.getPersistentData().getBoolean("Skill_JingJi_Available")) {
                if (RANDOM.nextFloat() < 0.10f) {
                    float reflectDamage = finalDamage * 0.50f;
                    player.hurt(target.damageSources().mobAttack(target), reflectDamage);
                    String reflectStr = String.format("%.1f", reflectDamage);
                    net.minecraft.world.phys.Vec3 playerPos = new net.minecraft.world.phys.Vec3(player.getX(), player.getY() + player.getBbHeight() + 0.2D, player.getZ());
                    DamageIndicatorRenderer.addIndicator("反伤 " + reflectStr, 0xFFFF2222, playerPos);
                }
            }

            if (!effectTriggered) {
                String dmgStr = String.format("%.1f", finalDamage);
                if (isCrit) {
                    Component msg = Component.literal("§c§l暴击！ §f对 §e" + target.getDisplayName().getString() + " §f造成 §6§l" + dmgStr + " 点伤害");
                    processDisplay(player, target, msg, "暴击 " + dmgStr, 0xFFFF2222);
                } else {
                    Component msg = Component.literal("§7对 §e" + target.getDisplayName().getString() + " §f造成 §f" + dmgStr + " 点伤害");
                    processDisplay(player, target, msg, dmgStr, 0xFFFFFFFF);
                }
            }
        }
    }

    private static void handleLifesteal(LivingEntity attacker, float damage) {
        float xixueValue = ModAttributeAPI.getXixue(attacker);
        if (xixueValue > 0 && attacker.getHealth() > 0) {
            float healAmount = (xixueValue / (xixueValue + 100f)) * damage;
            attacker.heal(healAmount);
        }
    }

    /**
     * 特效触发逻辑（同步适配了全新的等级动态飞行削弱）
     */
    private static boolean triggerSpecialEffects(LivingEntity attacker, LivingEntity target, float baseDamage) {
        float roll = RANDOM.nextFloat();
        String name = target.getDisplayName().getString();

        // 获取飞行削弱乘数（如果攻击者不是玩家，则返回 1.0f 不影响正常怪物伤害）
        float flyMultiplier = attacker instanceof Player p ? getFlyingDamageMultiplier(p) : 1.0f;

        if (roll <= 0.225f) { // 1. 撕裂
            float dmg = baseDamage * 1.5f * flyMultiplier;
            showEffectMsg(attacker, target, "§7§l撕裂！", name, dmg, "撕裂 " + String.format("%.1f", dmg), 0xFF999999);
            return true;
        } else if (roll <= 0.45f) { // 2. 破甲
            target.addEffect(new MobEffectInstance(ModEffects.ARMOR_PIERCING.get(), 60, 1));
            float dmg = baseDamage * 1.4f * flyMultiplier;
            showEffectMsg(attacker, target, "§9§l破甲！", name, dmg, "破甲 " + String.format("%.1f", dmg), 0xFF5555FF);
            return true;
        } else if (roll <= 0.675f) { // 3. 燃烧
            target.addEffect(new MobEffectInstance(ModEffects.SCORCHING.get(), 200, 1));
            float dmg = baseDamage * 1.2f * flyMultiplier;
            showEffectMsg(attacker, target, "§4§l燃烧！", name, dmg, "燃烧 " + String.format("%.1f", dmg), 0xFFFF5555);
            return true;
        } else if (roll <= 0.9f) { // 4. 震荡
            target.addEffect(new MobEffectInstance(ModEffects.DIZZINESS.get(), 60, 5));
            float dmg = baseDamage * 1.2f * flyMultiplier;
            showEffectMsg(attacker, target, "§6§l震荡！", name, dmg, "震荡 " + String.format("%.1f", dmg), 0xFFFFAA00);
            return true;
        } else { // 5. 湮灭
            float trueDamage = target.getMaxHealth() * 0.2f;
            DamageSource source = attacker instanceof Player p ? target.damageSources().playerAttack(p) : target.damageSources().mobAttack(attacker);
            target.hurt(source, trueDamage);
            showEffectMsg(attacker, target, "§5§l湮灭！", name, trueDamage, "湮灭 " + String.format("%.1f", trueDamage), 0xFFAA00AA);
            return true;
        }
    }

    private static float calculateSpecialDamage(LivingEntity attacker, LivingEntity target, float base) {
        return base * 1.3f;
    }

    private static void showEffectMsg(LivingEntity attacker, LivingEntity target, String prefix, String targetName, float dmg, String indicatorText, int color) {
        if (attacker instanceof Player player) {
            Component msg = Component.literal(prefix + " §f对 §e" + targetName + " §f造成 §6§l" + String.format("%.1f", dmg) + " 点伤害");
            processDisplay(player, target, msg, indicatorText, color);
        }
    }

    private static void processDisplay(Player player, LivingEntity target, Component msg, String indicatorText, int color) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            int mode = cap.getDamageDisplayMode();
            switch (mode) {
                case 0:
                    player.displayClientMessage(msg, true);
                    break;
                case 1:
                    player.displayClientMessage(msg, false);
                    break;
                case 2:
                    Vec3 spawnPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() + 0.2D, target.getZ());
                    DamageIndicatorRenderer.addIndicator(indicatorText, color, spawnPos);
                    break;
                case 3:
                    break;
            }
        });
    }
}