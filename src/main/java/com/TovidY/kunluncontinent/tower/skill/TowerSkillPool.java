package com.TovidY.kunluncontinent.tower.skill;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.tower.skill.jineng.*;
import net.minecraft.world.entity.Mob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 挑战塔怪物词条池（1~10 级）。
 *
 * <p><b>等级写法：</b>在词条名后接罗马数字即可（可带空格）：{@code "狂暴 III"}、{@code "壁垒X"}。
 * 不带后缀 = 1 级（完全兼容老配置）。</p>
 *
 * <p><b>等级强度曲线（统一）：</b>1 级 = 原始数值，每高 1 级强度 +10%，
 * 即强度 = 原始值 × (1 + (等级-1) × 0.1)，10 级 = 190%。
 * 见 {@link #levelMultiplier(int)}。离散型效果（药水等级）按各自映射处理并设上限。</p>
 *
 * <p><b>注入方式：</b>被动词条写 NBT 标记 / 永久属性，等级同步写入 {@code Skill_XXX_Level} 备用；
 * 主动词条给 goalSelector 挂 AI，等级作为构造参数传入 Goal。</p>
 */
public class TowerSkillPool {

    /** 单个词条等级上限。 */
    public static final int MAX_LEVEL = 10;

    /** 等级强度步长：每高 1 级 +10%。 */
    private static final float LEVEL_STEP = 0.10f;

    /** 合法的罗马数字等级（下标 + 1 = 等级值）。 */
    private static final List<String> ROMAN_LEVELS =
            List.of("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X");

    /** 解析 "名字 + 可选罗马数字" 的正则（允许中间有空格）。 */
    private static final Pattern SPEC_PATTERN = Pattern.compile("^(.*?)\\s*([IVX]{1,4})$");

    public interface ITowerModifier {
        String getName();
        boolean isActive();

        /** 1 级注入（兼容老写法）。 */
        void apply(Mob mob);

        /**
         * 带等级的注入入口。默认退回 {@link #apply(Mob)}（等级不敏感的词条零改动）；
         * 强度随等级变化的词条重写这个方法。
         */
        default void apply(Mob mob, int level) { apply(mob); }

        default String getTriggerText() {
            return isActive() ? "§6§l⚔ " + getName() + "！ ⚔" : "§4§l✨ " + getName() + "！ ✨";
        }

        /** 触发飘字文案（把词条名替换成"名字+罗马数字"）。 */
        default String getTriggerText(int level) {
            return getTriggerText().replace(getName(), displayName(getName(), level));
        }
    }

    /** 一条已解析的词条引用：词条本体 + 等级 + 显示名（含罗马数字）。 */
    private record SkillRef(ITowerModifier modifier, int level, String display) {}

    /** 解析结果：词条基础名 + 等级 + 是否显式写了罗马后缀。 */
    public record ParsedSpec(String name, int level, boolean explicit) {}

    // ==================================================================================
    //  等级与罗马数字工具
    // ==================================================================================

    /**
     * 等级强度系数：1 级 = 1.0，每级 +10%，10 级 = 1.9。
     * 所有随等级变强的词条统一乘这个系数。
     */
    public static float levelMultiplier(int level) {
        int lv = Math.max(1, Math.min(MAX_LEVEL, level));
        return 1f + (lv - 1) * LEVEL_STEP;
    }

    /** 等级 → 罗马数字（1→I ... 10→X），越界 clamp。 */
    public static String toRoman(int level) {
        int lv = Math.max(1, Math.min(MAX_LEVEL, level));
        return ROMAN_LEVELS.get(lv - 1);
    }

    /** 词条显示名：名字 + 罗马数字（如 "狂暴III"）。 */
    public static String displayName(String name, int level) {
        return name + toRoman(level);
    }

    /**
     * 解析一条词条规格：{@code "狂暴 III"} 或 {@code "狂暴III"} → (名字="狂暴", 等级=3, 显式=true)。
     * 没有合法罗马后缀 → 整串当词条名、等级 1、显式=false（老写法完全兼容）。
     */
    public static ParsedSpec parseSpec(String raw) {
        String s = raw == null ? "" : raw.trim();
        Matcher m = SPEC_PATTERN.matcher(s);
        if (m.matches()) {
            String roman = m.group(2);
            int idx = ROMAN_LEVELS.indexOf(roman);
            if (idx >= 0 && !m.group(1).isBlank()) {
                return new ParsedSpec(m.group(1).trim(), idx + 1, true);
            }
        }
        return new ParsedSpec(s, 1, false);
    }

    // ==================================================================================
    //  主动词条触发飘字
    // ==================================================================================

    public static void ShieldActiveSkillNotify(Mob mob, String skillName) {
        ShieldActiveSkillNotify(mob, skillName, 1);
    }

    /** 主动词条触发飘字（带等级显示）。 */
    public static void ShieldActiveSkillNotify(Mob mob, String skillName, int level) {
        ITowerModifier modifier = getSkillByName(skillName);
        if (modifier != null) {
            com.TovidY.kunluncontinent.tower.skill.TowerSkillNotifier.popSkillText(mob, modifier.getTriggerText(level));
        }
    }

    // ==================================================================================
    //  词条池
    // ==================================================================================

    private static final List<ITowerModifier> SKILL_POOL = new ArrayList<>();

    static {
        // ==================== 被动词条 ====================

        // 被动：不死（效果恒为"复活一次"，等级写入 NBT 备用）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "不死"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_BuSi_Available", true);
            }
            @Override public void apply(Mob mob, int level) {
                apply(mob);
                mob.getPersistentData().putInt("Skill_BuSi_Level", level);
            }
        });

        // 被动：迅速（离散映射：等级=药水等级，1级=速度II，最高速度V）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "迅速"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                        Integer.MAX_VALUE, Math.min(level, 4), false, false
                ));
            }
        });

        // 被动：荆棘（等级写入 NBT 备用，消费端在 CombatEventHandler）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "荆棘"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_JingJi_Available", true);
            }
            @Override public void apply(Mob mob, int level) {
                apply(mob);
                mob.getPersistentData().putInt("Skill_JingJi_Level", level);
            }
        });

        // 被动：禁空（等级写入 NBT 备用，消费端在 PWPlayerTickEvent）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "禁空"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_JinKong_Available", true);
            }
            @Override public void apply(Mob mob, int level) {
                apply(mob);
                mob.getPersistentData().putInt("Skill_JinKong_Level", level);
            }
        });

        // 被动：免伤（等级写入 NBT 备用，消费端在 CombatEventHandler）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "免伤"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_MianShang_Available", true);
            }
            @Override public void apply(Mob mob, int level) {
                apply(mob);
                mob.getPersistentData().putInt("Skill_MianShang_Level", level);
            }
        });

        // 被动：壁垒（等级 → 额外物防：基础 +60% × 等级系数，10级 +114%）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "壁垒"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    float bonusWufang = cap.getFangyu() * 0.6f * levelMultiplier(level);
                    MobTempAttributeManager.applyPermanentAttribute(mob, MobAttributeType.WUFANG, bonusWufang);
                });
            }
        });

        // 被动：浑厚（等级 → 额外血量：基础 +65% × 等级系数，10级 +124%）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "浑厚"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    float bonusMaxHp = cap.getMaxshengming() * 0.65f * levelMultiplier(level);
                    MobTempAttributeManager.applyPermanentAttribute(mob, MobAttributeType.MAX_SHENGMING, bonusMaxHp);
                });
            }
        });

        // 被动：濒死（等级写入 NBT 备用，血线监控在 MobTempAttributeManager 统一 Tick）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "濒死"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_BinSi_Available", true);
            }
            @Override public void apply(Mob mob, int level) {
                apply(mob);
                mob.getPersistentData().putInt("Skill_BinSi_Level", level);
            }
        });

        // ==================== 主动词条（等级作为构造参数传给 AI Goal） ====================

        // 主动：狂暴（等级 → 物攻加成：基础 +50% × 等级系数，10级 +95%）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "狂暴"; }
            @Override public boolean isActive() { return true; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getPersistentData().putBoolean("Skill_KuangBao_Active", true);
                mob.getPersistentData().putInt("Skill_KuangBao_Level", level);
                mob.goalSelector.addGoal(1, new MobKuangBaoGoal(mob, level));
            }
        });

        // 主动：泥沼（等级 → 减速强度/时长，见 MobNiZhaoGoal）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "泥沼"; }
            @Override public boolean isActive() { return true; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getPersistentData().putBoolean("Skill_NiZhao_Active", true);
                mob.getPersistentData().putInt("Skill_NiZhao_Level", level);
                mob.goalSelector.addGoal(1, new MobNiZhaoGoal(mob, level));
            }
        });

        // 主动：瞬移（等级 → 冷却缩短：基础 10 秒 ÷ 等级系数，10级约 5.3 秒）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "瞬移"; }
            @Override public boolean isActive() { return true; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getPersistentData().putBoolean("Skill_ShunYi_Active", true);
                mob.getPersistentData().putInt("Skill_ShunYi_Level", level);
                mob.goalSelector.addGoal(4, new MobShunYiGoal(mob, level));
            }
        });

        // 主动：恢复（等级 → 治疗比例：基础 20% × 等级系数，10级 38%）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "恢复"; }
            @Override public boolean isActive() { return true; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getPersistentData().putBoolean("Skill_HuiFu_Active", true);
                mob.getPersistentData().putInt("Skill_HuiFu_Level", level);
                mob.goalSelector.addGoal(5, new MobHuiFuGoal(mob, level));
            }
        });

        // 主动：恐惧（等级 → 负面时长，见 MobKongJuGoal）
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "恐惧"; }
            @Override public boolean isActive() { return true; }
            @Override public void apply(Mob mob) { apply(mob, 1); }
            @Override public void apply(Mob mob, int level) {
                mob.getPersistentData().putBoolean("Skill_KongJu_Active", true);
                mob.getPersistentData().putInt("Skill_KongJu_Level", level);
                mob.goalSelector.addGoal(3, new MobKongJuGoal(mob, level));
            }
        });
    }

    /** 根据词条基础名（不含等级）获取对应的修改器。 */
    public static ITowerModifier getSkillByName(String name) {
        return SKILL_POOL.stream()
                .filter(modifier -> modifier.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 词条注入入口（老写法兼容：默认等级 1）。
     *
     * @param mob                 目标怪物
     * @param specifiedSkillNames 强制指定的词条规格列表（纯名字，或带罗马数字后缀单点覆盖）
     * @param randomCount         需要额外随机抽取的数量
     */
    public static void applySkills(Mob mob, List<String> specifiedSkillNames, int randomCount) {
        applySkills(mob, specifiedSkillNames, randomCount, 1);
    }

    /**
     * 词条注入入口（懒人版）。
     *
     * <p><b>等级规则：</b>词条名不带后缀时自动使用 {@code defaultLevel}（整层一个数字，
     * 改一处全层生效）；带罗马数字后缀（如 {@code "狂暴 III"}）则以显式后缀为准，
     * 用于个别词条单独调级。显示名永远自动拼成"名字+罗马数字"。</p>
     *
     * @param mob                 目标怪物
     * @param specifiedSkillNames 强制指定的词条名列表（如 {@code ["狂暴", "壁垒", "泥沼"]}）
     * @param randomCount         需要额外随机抽取的数量（随机词条恒为 1 级）
     * @param defaultLevel        本层默认词条等级（1~10）
     */
    public static void applySkills(Mob mob, List<String> specifiedSkillNames, int randomCount, int defaultLevel) {
        List<SkillRef> finalAppliedSkills = new ArrayList<>();

        // 1. 先把强制指定的词条解析并加载进去（无后缀 → 用 defaultLevel；有罗马后缀 → 单点覆盖）
        for (String rawSpec : specifiedSkillNames) {
            ParsedSpec spec = parseSpec(rawSpec);
            ITowerModifier modifier = getSkillByName(spec.name());
            if (modifier != null) {
                int level = spec.explicit() ? spec.level()
                        : Math.max(1, Math.min(MAX_LEVEL, defaultLevel));
                finalAppliedSkills.add(new SkillRef(modifier, level, displayName(modifier.getName(), level)));
            }
        }

        // 2. 计算还需要随机抽取多少个（随机词条恒为 1 级）
        if (randomCount > 0 && !SKILL_POOL.isEmpty()) {
            List<ITowerModifier> availablePool = new ArrayList<>(SKILL_POOL);
            // 【核心去重】：从随机池子里把已经指定的词条无情剔除，绝不重复！
            availablePool.removeIf(modifier -> specifiedSkillNames.stream()
                    .map(raw -> parseSpec(raw).name())
                    .anyMatch(name -> name.equals(modifier.getName())));

            // 随机洗牌
            Collections.shuffle(availablePool, new Random());

            // 补充不足的词条
            int added = 0;
            for (ITowerModifier modifier : availablePool) {
                if (added >= randomCount) break;
                finalAppliedSkills.add(new SkillRef(modifier, 1, displayName(modifier.getName(), 1)));
                added++;
            }
        }

        // 3. 统一对怪物实施技能注入，并把词条标签写入 capability
        //    （随 SPacketEntityAttribute 同步给客户端，客户端在名牌第二行绘制词条）
        if (finalAppliedSkills.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (SkillRef ref : finalAppliedSkills) {
            ref.modifier().apply(mob, ref.level()); // 带等级注入
            String color = ref.modifier().isActive() ? "§6" : "§e";
            sb.append(color).append(ref.display()).append(" ");
        }
        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY)
                .ifPresent(cap -> cap.setTowerSkillsTag(sb.toString().trim()));
    }
}
