package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.DamageIndicatorRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

    /**
     * 塔词条【甲鳞】的减伤上限。
     *
     * <p>单条词条 1 级只减 6%，10 级约 11.4%（{@code 6% × 1.9}），远远够不到这个上限；
     * 留着是为了防止以后"多个减伤来源叠加"时把伤害压到 0（同 0.05 的伤害下限思路）。</p>
     */
    private static final float JIALIN_MAX_REDUCE = 0.60f;

    /**
     * 特殊特效的<b>基础</b>触发概率（0~1）。
     *
     * <p>⚠️ 实际触发概率 = 本值 ×玩家暴击率，见 {@link #getSpecialEffectChance}。
     * 之所以不写死，是因为设计意图是<b>「暴击率越高，特殊特效越容易出现」</b>，
     * 而不是「固定 10%」或「暴击后再掷一次」。</p>
     *
     * <p>原实现是 {@code RANDOM < 0.10f}（无条件固定 10%），
     * 且与暴击判定<b>互斥</b>（触发特效时根本不看暴击）——
     * 于是「暴击时才有机会出特效」的预期完全不成立。</p>
     */
    private static final float SPECIAL_EFFECT_BASE_CHANCE = 0.5f;

    /**
     * 根据玩家<b>实际暴击率</b>（已扣抗暴）算出特殊特效的触发概率。
     *
     * <p>线性映射：暴击率 0% → 0%；暴击率 50% → 25%；暴击率 100% → 50%。
     * 也就是说堆暴击到 100% 的玩家，每两次攻击能看到一次特效。</p>
     *
     * <p>刻意<b>不</b>要求先判定暴击 —— 特效与暴击是两条独立的线，
     * 只是<b>共用同一个「暴击率」作为强弱度量</b>。</p>
     *
     * @param finalCritRate 实际暴击率（{@code max(0, baojilv - kangbao)}，单位 %）
     * @return 触发概率（%）
     */
    private static float getSpecialEffectChance(float finalCritRate) {
        return Math.max(0f, Math.min(100f, finalCritRate)) * SPECIAL_EFFECT_BASE_CHANCE;
    }

    /**
     * 防御减伤的<b>下限系数</b>（0.05 = 最多只减 95% 伤害）。
     *
     * <p>⚠️ 为什么必须设下限：原公式 {@code 100 / (100 + fangyu)} 是典型的 <b>100/x 型衰减</b>，
     * 防御一高就断崖式暴跌，而且<b>没有下限</b>：</p>
     * <pre>
     *   防御    0 -> 系数 1.0000 (不减伤)
     *   防御  100 -> 系数 0.5000
     *   防御 1000 -> 系数 0.0909
     *   防御 5000 -> 系数 0.0196
     *   防御 20000 -> 系数 0.0050   <- 高等级怪普遍过万，伤害只剩 0.5%
     *   防御 50000 -> 系数 0.0020
     * </pre>
     * <p>于是「魂技看着很弱」：八阶魂技（4.3x × 百万年 15x ≈ 64.5 倍攻击力）
     * 打防御 2 万的怪时，从理论 1290 万掉到 6.4 万 —— <b>打不动上百万血量的 BOSS</b>。
     * 技能倍率再高也没用，因为衰减是无底的。</p>
     *
     * <p>加上 0.05 下限后：防御再高也至少保留 5% 伤害，
     * 高防怪仍有博弈空间（破甲、暴击、属性堆叠依然有效），但不会把伤害压到个位数。</p>
     */
    private static final float MIN_DAMAGE_FACTOR = 0.05f;

    /**
     * 穿透对防御的<b>半效系数</b>：穿透 = 防御 × 本值时，恰好削掉 50% 防御。
     *
     * <h3>为什么穿透不能再用减法</h3>
     * <p>旧实现是 {@code effectiveFangyu = max(0, fangyu - wuchuan)} —— 纯粹的<b>点数相减</b>。
     * 于是「穿透 ≥ 防御」时防御归零、减伤系数恒为 {@code 1.0}，
     * <b>所有防御低于玩家穿透的怪物，伤害数字一模一样</b>。
     * 实测：陆离剑 280 穿透（点满物穿 557）对上万年以内所有怪（防御上限 300 / 削弱后 105），
     * 全部被穿成 0，玩家看到的就是"十年、百年、千年、万年，打出来伤害完全相同"。</p>
     *
     * <h3>现在的写法</h3>
     * <p>削减比例取决于「穿透 / 防御」的<b>比值</b>而不是绝对点数：</p>
     * <pre>
     *   p = wuchuan / fangyu
     *   reduce = p / (p + 0.25)          // 0 → 0%，0.25 → 50%，1 → 80%，4 → 94%
     *   effectiveFangyu = fangyu × (1 - reduce)
     * </pre>
     * <p>削减比例永远到不了 100%，防御不会被"清零"，因此每条年限的伤害都不同且连续
     * （穿透从 {@code f-1} 涨到 {@code f} 时不会再出现断崖）。</p>
     *
     * <p>取 0.25 的含义：穿透堆到与目标防御<b>同量级</b>时，能削掉约 80% 防御；
     * 想进一步逼近"无视防御"需要指数级的溢出，避免一把剑通吃所有档位。</p>
     */
    private static final float PIERCE_HALF_RATIO = 0.25f;

    /**
     * <b>溢出穿透</b>的额外增伤上限（35%）。
     *
     * <p>当 {@code 穿透 > 防御}（即把对方防御整段盖过）时，超出的部分不再是"无效溢出"，
     * 而是转化为对最终伤害的加成 —— 这就是"穿透够高时打脆皮更疼"的手感来源。</p>
     */
    private static final float MAX_OVERFLOW_BONUS = 0.35f;

    /**
     * 溢出增伤的<b>半效点</b>（单位：穿透点数）：溢出该点数时拿到上限的一半。
     *
     * <p>用饱和曲线而不是线性，避免高穿透武器在低防目标上无限放大：
     * 溢出 200 → +11.7%，400 → +17.5%，1200 → +26.3%，永远不超过 +35%。</p>
     */
    private static final float OVERFLOW_SOFTNESS = 400f;

    private static final float EFFECT_TEAR = 0.225f;      // 撕裂
    private static final float EFFECT_ARMOR_PIERCE = 0.45f; // 破甲
    private static final float EFFECT_SCORCH = 0.675f;    // 燃烧
    private static final float EFFECT_DIZZY = 0.9f;       // 震荡
    // 剩余区间 = 湮灭

    /**
     * 计算防御减伤系数，<b>带下限</b>。
     *
     * @param effectiveFangyu 已扣除破甲的有效防御
     * @return 伤害保留系数，范围 [{@value #MIN_DAMAGE_FACTOR}, 1.0]
     */
    private static float getDamageFactor(float effectiveFangyu) {
        if (effectiveFangyu <= 0.0f) return 1.0f;
        return Math.max(MIN_DAMAGE_FACTOR, 100f / (100f + effectiveFangyu));
    }

    /**
     * 穿透削减后的<b>有效防御</b>。
     *
     * <p>按「穿透 / 防御」的比值削减（详见 {@link #PIERCE_HALF_RATIO}），
     * 结果恒 {@code > 0}（除非原本就是 0），因此不会出现"防御被清零 → 伤害完全相同"的饱和。</p>
     *
     * @param fangyu  目标原始防御
     * @param wuchuan 攻击者穿透点数
     */
    private static float applyPierce(float fangyu, float wuchuan) {
        if (fangyu <= 0.0f) return 0.0f;
        if (wuchuan <= 0.0f) return fangyu;
        float p = wuchuan / fangyu;
        float reduce = p / (p + PIERCE_HALF_RATIO);
        return fangyu * (1.0f - reduce);
    }

    /**
     * <b>溢出穿透</b>的伤害倍率（{@code >= 1.0}）。
     *
     * <p>只有 {@code 穿透 > 防御} 时才生效：溢出点数走饱和曲线映射到
     * {@code [1.0, 1 + MAX_OVERFLOW_BONUS]}。这是对"穿光防御"的补偿，
     * 让高穿透在面对低防目标时有明确收益，而不是只把防御削到 0 就结束。</p>
     *
     * @param fangyu  目标原始防御
     * @param wuchuan 攻击者穿透点数
     * @return 最终伤害的乘数
     */
    private static float getOverflowDamageMultiplier(float fangyu, float wuchuan) {
        float overflow = wuchuan - fangyu;
        if (overflow <= 0.0f) return 1.0f;
        return 1.0f + MAX_OVERFLOW_BONUS * overflow / (overflow + OVERFLOW_SOFTNESS);
    }

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

        float gongji = ModAttributeAPI.getGongji(attacker);
        float wuchuan = ModAttributeAPI.getWuchuan(attacker);
        float fangyu = ModAttributeAPI.getEffectiveFangyu(target);

        boolean isSkillDamage = com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem.isSkillDamage(target);

        // ==================== 防御减伤：穿透按比例削减 + 溢出增伤 ====================
        // 旧写法是 max(0, fangyu - wuchuan)：穿透一旦 ≥ 防御就把防御清零，
        // 所有低防怪的伤害数字完全相同。现在改为比值削减（永不清零）+ 溢出补偿。
        float effectiveFangyu = applyPierce(fangyu, wuchuan);
        float reductionFactor = getDamageFactor(effectiveFangyu);
        float overflowMultiplier = getOverflowDamageMultiplier(fangyu, wuchuan);
        float baseDamage = isSkillDamage
                ? event.getAmount() * reductionFactor
                : (gongji + event.getAmount()) * reductionFactor;
        baseDamage *= overflowMultiplier;

        if (attacker instanceof Player player) {
            baseDamage *= getFlyingDamageMultiplier(player);
        }

        float finalDamage = baseDamage;
        boolean isCrit = false;

        float baojilv = ModAttributeAPI.getBaojilv(attacker);
        float kangbao = ModAttributeAPI.getKangbao(target);
        float finalCritRate = Math.max(0, baojilv - kangbao);
        boolean critHit = (RANDOM.nextFloat() * 100) < finalCritRate;

        Float specialDamage = null;
        float effectChance = getSpecialEffectChance(finalCritRate);
        if (RANDOM.nextFloat() * 100.0f < effectChance) {
            specialDamage = triggerSpecialEffects(attacker, target, baseDamage);
        }
        boolean effectTriggered = specialDamage != null;

        if (effectTriggered) {
            finalDamage = specialDamage;
        } else if (critHit) {
            isCrit = true;
            float baojishanghai = ModAttributeAPI.getBaojishanghai(attacker);
            finalDamage = baseDamage * (baojishanghai / 100f);
        }

        finalDamage = Math.max(0.1f, finalDamage);

        // ==================== 塔词条【甲鳞】：受到伤害减免 ====================
        // 放在写回 event 之前：这是最后一道乘法，减伤对"已经算完的最终伤害"生效，
        // 与攻防公式、暴击、特殊特效都不冲突。
        if (target.getPersistentData().getBoolean("Skill_JiaLin_Available")) {
            float reduce = Math.min(JIALIN_MAX_REDUCE,
                    0.06f * towerSkillLevelMultiplier(target, "Skill_JiaLin_Level"));
            finalDamage *= (1.0f - reduce);
        }

        event.setAmount(finalDamage);

        CombatDamageBackupHandler.record(target, finalDamage);

        handleLifesteal(attacker, finalDamage);

        // ==================== 塔词条【噬血】：打中玩家时按"造成的伤害"回血 ====================
        // ⚠️ 不能按自身最大生命的百分比回：塔内高层怪物血量是百万级，
        // 每刀回 2% 就是几万血，等于物理免疫。按实际伤害的 20% × 等级系数（10 级 38%）才可控。
        if (attacker instanceof Mob mob && target instanceof Player
                && mob.getPersistentData().getBoolean("Skill_ShiXue_Available")) {
            float maxHp = mob.getMaxHealth();
            float heal = finalDamage * 0.20f * towerSkillLevelMultiplier(mob, "Skill_ShiXue_Level");
            if (mob.getHealth() < maxHp) {
                mob.heal(heal);
                // 保持 capability 里的生命镜像与真实血量一致（恢复/濒死等逻辑都依赖它）
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                        .ifPresent(cap -> cap.setShengming(mob.getHealth()));
                Vec3 healPos = new Vec3(mob.getX(), mob.getY() + mob.getBbHeight() + 0.2D, mob.getZ());
                DamageIndicatorRenderer.addIndicator("+" + fmt(heal), 0xFF00FF00, healPos);
            }
        }

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
     * 读取塔词条等级并换算成强度系数（1 级 = 1.0，10 级 = 1.9）。
     * 没写等级（0）按 1 级处理，避免老存档里的怪出现 0 强度词条。
     */
    private static float towerSkillLevelMultiplier(LivingEntity entity, String levelKey) {
        int level = entity.getPersistentData().getInt(levelKey);
        return com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.levelMultiplier(level <= 0 ? 1 : level);
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
            if (mode == 3) return;
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