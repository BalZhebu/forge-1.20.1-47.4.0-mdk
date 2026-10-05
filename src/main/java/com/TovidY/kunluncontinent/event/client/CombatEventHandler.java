package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.DamageIndicatorRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

    // ---- 概率/系数常量集中管理，改起来不用满文件找魔法数字 ----
    private static final float MIANSHANG_PROC_CHANCE = 0.10f;
    private static final float THORNS_PROC_CHANCE = 0.10f;
    private static final float THORNS_REFLECT_RATIO = 0.50f;
    private static final float SPECIAL_EFFECT_CHANCE = 0.10f;

    private static final float EFFECT_TEAR = 0.225f;      // 撕裂
    private static final float EFFECT_ARMOR_PIERCE = 0.45f; // 破甲
    private static final float EFFECT_SCORCH = 0.675f;    // 燃烧
    private static final float EFFECT_DIZZY = 0.9f;       // 震荡
    // 剩余区间 = 湮灭

    private static float getFlyingDamageMultiplier(Player player) {
        if (!player.getAbilities().flying) return 1.0f;
        // 用 mod 自己的等级（dengji），不是原版 experienceLevel
        int level = ModAttributeAPI.getDengji(player);
        if (level < 70) return 0.6f;
        if (level >= 90) return 0.9f;
        return 0.6f + (level - 70) * 0.015f;
    }

    private static String fmt(float v) {
        return String.format("%.1f", v);
    }

    /**
     * 优先级 <b>HIGHEST</b>（在整个事件总线上<b>最早</b>执行）。
     *
     * <p>为什么必须最早：要拿到原版 {@code event.getAmount()} 当「武器基础伤害」，
     * 此时它还没被护甲/抗性/其他 mod 改过。若用 LOW/HIGH 起步，别的 mod 可能已经把
     * amount 覆写成 1（手打伤害）→ 我们拿 1 当基础，算出来自然也是 1 —— 这就是
     * "装了某些 mod 后伤害只剩一滴血"的成因之一。</p>
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity target = event.getEntity();
        if (target == null || !target.isAlive()) return;

        // ==================== 免伤判定 ====================
        if (attacker instanceof Player player
                && target.getPersistentData().getBoolean("Skill_MianShang_Available")
                && RANDOM.nextFloat() < MIANSHANG_PROC_CHANCE) {
            event.setCanceled(true);
            processDisplay(player, target, null, "免疫", 0xFFFF0000);
            return;
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

        // ==================== 基础伤害（飞行系数只在这里乘一次） ====================
        float gongji = ModAttributeAPI.getGongji(attacker);
        float wuchuan = ModAttributeAPI.getWuchuan(attacker);
        float fangyu = ModAttributeAPI.getEffectiveFangyu(target);

        float effectiveFangyu = Math.max(0, fangyu - wuchuan);
        float reductionFactor = 100f / (100f + effectiveFangyu);
        float baseDamage = (gongji + event.getAmount()) * reductionFactor;

        if (attacker instanceof Player player) {
            baseDamage *= getFlyingDamageMultiplier(player);
        }

        float finalDamage = baseDamage;
        boolean isCrit = false;

        // ==================== 特殊特效判定（返回真实伤害，不再二次计算） ====================
        Float specialDamage = null;
        if (RANDOM.nextFloat() < SPECIAL_EFFECT_CHANCE) {
            specialDamage = triggerSpecialEffects(attacker, target, baseDamage);
        }
        boolean effectTriggered = specialDamage != null;
        if (effectTriggered) {
            finalDamage = specialDamage;
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

        finalDamage = Math.max(0.1f, finalDamage);
        event.setAmount(finalDamage);

        // ⭐ 登记给 LivingDamageEvent 兜底用。
        //   其他 mod 若在 Hurt 阶段 setCanceled(true)（无敌/护盾类最常见），
        //   或者把 amount 覆写成 1，我们在这里 set 的值就废了 → 怪物只掉一滴血。
        //   CombatDamageBackupHandler 会在 Damage 阶段把被腰斩的数值补回来。
        CombatDamageBackupHandler.record(target, finalDamage);

        handleLifesteal(attacker, finalDamage);

        // ==================== 荆棘反伤 + 伤害飘字（合并为一处，不再重复） ====================
        if (attacker instanceof Player player) {

            if (target.getPersistentData().getBoolean("Skill_JingJi_Available")
                    && RANDOM.nextFloat() < THORNS_PROC_CHANCE) {
                float reflectDamage = finalDamage * THORNS_REFLECT_RATIO;
                player.hurt(target.damageSources().mobAttack(target), reflectDamage);
                Vec3 playerPos = new Vec3(player.getX(), player.getY() + player.getBbHeight() + 0.2D, player.getZ());
                DamageIndicatorRenderer.addIndicator("反伤 " + fmt(reflectDamage), 0xFFFF2222, playerPos);
            }

            if (!effectTriggered) {
                String dmgStr = fmt(finalDamage);
                String targetName = target.getDisplayName().getString();
                if (isCrit) {
                    Component msg = Component.literal("§c§l暴击！ §f对 §e" + targetName + " §f造成 §6§l" + dmgStr + " 点伤害");
                    processDisplay(player, target, msg, "暴击 " + dmgStr, 0xFFFF2222);
                } else {
                    Component msg = Component.literal("§7对 §e" + targetName + " §f造成 §f" + dmgStr + " 点伤害");
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
     * 返回本次特效造成的“真实最终伤害”；未触发任何特效则返回 null。
     * 注意：不再在此处直接调用 target.hurt()，避免在当前事件处理未结束时
     * 对同一目标触发新的 LivingHurtEvent（有递归/双重扣血风险）。
     */
    private static Float triggerSpecialEffects(LivingEntity attacker, LivingEntity target, float baseDamage) {
        float roll = RANDOM.nextFloat();
        String name = target.getDisplayName().getString();

        if (roll <= EFFECT_TEAR) {
            float dmg = baseDamage * 1.5f;
            showEffectMsg(attacker, target, "§7§l撕裂！", name, dmg, "撕裂 " + fmt(dmg), 0xFF999999);
            return dmg;
        } else if (roll <= EFFECT_ARMOR_PIERCE) {
            target.addEffect(new MobEffectInstance(ModEffects.ARMOR_PIERCING.get(), 60, 1));
            float dmg = baseDamage * 1.4f;
            showEffectMsg(attacker, target, "§9§l破甲！", name, dmg, "破甲 " + fmt(dmg), 0xFF5555FF);
            return dmg;
        } else if (roll <= EFFECT_SCORCH) {
            target.addEffect(new MobEffectInstance(ModEffects.SCORCHING.get(), 200, 1));
            float dmg = baseDamage * 1.2f;
            showEffectMsg(attacker, target, "§4§l燃烧！", name, dmg, "燃烧 " + fmt(dmg), 0xFFFF5555);
            return dmg;
        } else if (roll <= EFFECT_DIZZY) {
            target.addEffect(new MobEffectInstance(ModEffects.DIZZINESS.get(), 60, 5));
            float dmg = baseDamage * 1.2f;
            showEffectMsg(attacker, target, "§6§l震荡！", name, dmg, "震荡 " + fmt(dmg), 0xFFFFAA00);
            return dmg;
        } else {
            // 湮灭：不再手动 hurt()，真实伤害交给外层 event.setAmount 统一生效
            float trueDamage = target.getMaxHealth() * 0.2f;
            showEffectMsg(attacker, target, "§5§l湮灭！", name, trueDamage, "湮灭 " + fmt(trueDamage), 0xFFAA00AA);
            return trueDamage;
        }
    }

    private static void showEffectMsg(LivingEntity attacker, LivingEntity target, String prefix,
                                      String targetName, float dmg, String indicatorText, int color) {
        if (attacker instanceof Player player) {
            Component msg = Component.literal(prefix + " §f对 §e" + targetName + " §f造成 §6§l" + fmt(dmg) + " 点伤害");
            processDisplay(player, target, msg, indicatorText, color);
        }
    }

    private static void processDisplay(Player player, LivingEntity target, Component msg, String indicatorText, int color) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            int mode = cap.getDamageDisplayMode();
            if (mode == 3) return; // 快速返回：配置为隐藏时不构造任何对象
            switch (mode) {
                case 0 -> player.displayClientMessage(msg, true);
                case 1 -> player.displayClientMessage(msg, false);
                case 2 -> {
                    Vec3 spawnPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() + 0.2D, target.getZ());
                    DamageIndicatorRenderer.addIndicator(indicatorText, color, spawnPos);
                }
            }
        });
    }
}