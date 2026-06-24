package com.TovidY.kunluncontinent.tower.skill;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.tower.skill.jineng.*;
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

        // 3. 被动：荆棘
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "荆棘"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_JingJi_Available", true);
            }
        });

        // 4.被动：禁空
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "禁空"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                mob.getPersistentData().putBoolean("Skill_JinKong_Available", true);
            }
        });

        // 被动：免伤
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "免伤"; }
            @Override public boolean isActive() { return false; }
            @Override public void apply(Mob mob) {
                // 注入被动免伤 NBT 标记
                mob.getPersistentData().putBoolean("Skill_MianShang_Available", true);
            }
        });

        // 被动：壁垒
        SKILL_POOL.add(new ITowerModifier() {
            @Override
            public String getName() { return "壁垒"; }
            @Override
            public boolean isActive() { return false; }
            @Override
            public void apply(Mob mob) {
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    float baseWufang = cap.getFangyu();
                    float bonusWufang = baseWufang * 0.6f;
                    MobTempAttributeManager.applyPermanentAttribute(mob, MobAttributeType.WUFANG, bonusWufang);
                });
            }
        });

        // 被动：浑厚
        SKILL_POOL.add(new ITowerModifier() {
            @Override
            public String getName() { return "浑厚"; }
            @Override
            public boolean isActive() { return false; }
            @Override
            public void apply(Mob mob) {
                mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    float baseMaxHp = cap.getMaxshengming();
                    float bonusMaxHp = baseMaxHp * 0.65f;
                    MobTempAttributeManager.applyPermanentAttribute(mob, MobAttributeType.MAX_SHENGMING, bonusMaxHp);
                });
            }
        });

        // 被动：濒死
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "濒死"; }
            @Override public boolean isActive() { return false; } // 被动技能
            @Override public void apply(Mob mob) {
                // 仅注入一个被动标记 NBT，具体的血线监控交由管理器统一 Tick 处理
                mob.getPersistentData().putBoolean("Skill_BinSi_Available", true);
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

        // 2. 主动：泥沼
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "泥沼"; }
            @Override public boolean isActive() { return true; } // 主动技能
            @Override public void apply(Mob mob) {
                // 1. 注入泥沼主动标记
                mob.getPersistentData().putBoolean("Skill_NiZhao_Active", true);
                // 2. 注入专属 AI 目标，优先级和狂暴相同，让它在战斗中智能判断释放
                mob.goalSelector.addGoal(1, new MobNiZhaoGoal(mob));
            }
        });

        // 3. 主动：瞬移
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "瞬移"; }
            @Override public boolean isActive() { return true; } // 主动技能
            @Override public void apply(Mob mob) {
                // 1. 注入瞬移主动标记
                mob.getPersistentData().putBoolean("Skill_ShunYi_Active", true);
                // 2. 核心：注入专属 AI 目标。优先级设为 4（较低优先级），作为保命或突进手段
                mob.goalSelector.addGoal(4, new MobShunYiGoal(mob));
            }
        });

        // 4. 主动：恢复
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "恢复"; }
            @Override public boolean isActive() { return true; } // 主动技能
            @Override public void apply(Mob mob) {
                // 1. 注入恢复主动标记
                mob.getPersistentData().putBoolean("Skill_HuiFu_Active", true);
                // 2. 核心：注入专属 AI 目标。优先级设为 5（较低优先级），属于低危或保命时使用的身法/功法
                mob.goalSelector.addGoal(5, new MobHuiFuGoal(mob));
            }
        });

        // 5. 主动：恐惧
        SKILL_POOL.add(new ITowerModifier() {
            @Override public String getName() { return "恐惧"; }
            @Override public boolean isActive() { return true; } // 主动技能
            @Override public void apply(Mob mob) {
                // 1. 注入恐惧主动标记
                mob.getPersistentData().putBoolean("Skill_KongJu_Active", true);
                // 2. 注入专属 AI 目标。优先级设为 3（高优先级），进战后会非常果断地释放
                mob.goalSelector.addGoal(3, new MobKongJuGoal(mob));
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