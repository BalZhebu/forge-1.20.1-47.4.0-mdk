package com.TovidY.kunluncontinent.tower.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class TowerSkillPool {

    public interface ITowerModifier {
        String getName();
        boolean isActive();
        void apply(Mob mob);

        default String getTriggerText() {
            return isActive() ? "§6§l⚔ " + getName() + "！ ⚔" : "§4§l✨ " + getName() + "！ ✨";
        }
    }

    public static void ShieldActiveSkillNotify(Mob mob, String skillName) {
        ITowerModifier modifier = getSkillByName(skillName);
        if (modifier != null) {
            com.TovidY.kunluncontinent.tower.skill.TowerSkillNotifier.popSkillText(mob, modifier.getTriggerText());
        }
    }

    private static final List<ITowerModifier> SKILL_POOL = new ArrayList<>();

    static {
        // 1. 被动：不死
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "不死"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_BuSi_Available", true);
            }
        });
        // 2. 被动：迅速
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "迅速"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                // 永久给予速度 II (等级1代表 2 级)，不显示粒子效果，常驻身上
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                        Integer.MAX_VALUE, 1, false, false
                ));
            }
        });

//        SKILL_POOL.add(new ITowerModifier() {
//            @Override public String getName() { return "九叶剑草"; }
//            @Override public boolean isActive() { return true; }
//            @Override public void apply(Mob mob) { /* 技能逻辑 */ }
//
//            // 如果不满意默认的样式，可以随时在这里单独全手工定制！
//            @Override
//            public String getTriggerText() {
//                return "§2§l叶动乾坤 ⚔ 九叶剑草！";
//            }
//        });

// ==================== 【追加在主动技能区】 ====================
// 1. 主动：狂暴
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "狂暴"; }
            @Override public boolean isActive() { return true; }
            @Override public void apply(Mob mob) {
                // 1. 注入狂暴主动标记
                mob.getPersistentData().putBoolean("Skill_KuangBao_Active", true);

                // 2. 核心：通过 AI 目标系统让怪物学会什么时候释放这个技能！
                // 优先级设为 1（极高），让它在战斗时优先考虑释放
                mob.goalSelector.addGoal(1, new MobKuangBaoGoal(mob));
            }
        });
    }

    /**
     * 根据技能名称获取对应的修改器
     */
    public static ITowerModifier getSkillByName(String name) {
        return SKILL_POOL.stream()
                .filter(modifier -> modifier.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 核心优化方法：支持指定词条 + 补全不重复的随机词条
     * @param mob 目标怪物
     * @param specifiedSkillNames 强制指定的技能名字列表
     * @param randomCount 需要额外随机抽取的数量
     */
    public static void applySkills(Mob mob, List<String> specifiedSkillNames, int randomCount) {
        List<ITowerModifier> finalAppliedSkills = new ArrayList<>();

        // 1. 先把强制指定的词条加载进去
        for (String skillName : specifiedSkillNames) {
            ITowerModifier modifier = getSkillByName(skillName);
            if (modifier != null) {
                finalAppliedSkills.add(modifier);
            }
        }

        // 2. 计算还需要随机抽取多少个
        if (randomCount > 0 && !SKILL_POOL.isEmpty()) {
            List<ITowerModifier> availablePool = new ArrayList<>(SKILL_POOL);
            // 【核心去重】：从随机池子里把已经指定的词条无情剔除，绝不重复！
            availablePool.removeIf(modifier -> specifiedSkillNames.contains(modifier.getName()));

            // 随机洗牌
            Collections.shuffle(availablePool, new Random());

            // 补充不足的词条
            int added = 0;
            for (ITowerModifier modifier : availablePool) {
                if (added >= randomCount) break;
                finalAppliedSkills.add(modifier);
                added++;
            }
        }

        // 3. 统一对怪物实施技能注入，并格式化名字挂件
        if (finalAppliedSkills.isEmpty()) return;

        StringBuilder sb = new StringBuilder(" §7[词条: ");
        for (ITowerModifier modifier : finalAppliedSkills) {
            modifier.apply(mob); // 物理注入NBT或属性
            String color = modifier.isActive() ? "§6" : "§e";
            sb.append(color).append(modifier.getName()).append(" ");
        }
        sb.append("§7]");

        if (mob.getCustomName() != null) {
            mob.setCustomName(Component.literal(mob.getCustomName().getString() + sb.toString()));
        } else {
            mob.setCustomName(Component.literal(sb.toString().trim()));
        }
        mob.setCustomNameVisible(true);
    }
}