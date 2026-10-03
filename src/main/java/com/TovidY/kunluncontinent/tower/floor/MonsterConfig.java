package com.TovidY.kunluncontinent.tower.floor;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 一条刷怪配置：用什么怪、刷几只、带什么词条。
 *
 * <p>四种写法（后两种是老关卡的兼容写法，语义 = 数量 1）：</p>
 * <pre>
 * new MonsterConfig(僵尸, 数量2, 随机3)                  → 刷 2 只，各随机 3 词条
 * new MonsterConfig(僵尸, 数量2, 随机0, "壁垒", "浑厚")   → 刷 2 只，只带这两个手写词条
 * new MonsterConfig(僵尸, 随机3)                        → 刷 1 只，随机 3 词条
 * new MonsterConfig(僵尸, 随机3, "狂暴", "恐惧")         → 刷 1 只，指定 2 个再随机 3 个
 * </pre>
 */
public class MonsterConfig {
    public final ResourceLocation id;
    /** 该配置生成的怪物数量（最小 1）。 */
    public final int count;
    public final List<String> specifiedSkills; // 指定的技能名字列表
    public final int randomSkillCount;         // 额外随机的技能数量

    /** 老写法兼容：1 只 + 全随机词条。 */
    public MonsterConfig(ResourceLocation id, int randomSkillCount) {
        this(id, 1, randomSkillCount);
    }

    /** 老写法兼容：1 只 + 指定词条 + 尾随随机。 */
    public MonsterConfig(ResourceLocation id, int randomSkillCount, String... specifiedSkills) {
        this(id, 1, randomSkillCount, specifiedSkills);
    }

    /** 指定数量 + 全随机词条。 */
    public MonsterConfig(ResourceLocation id, int count, int randomSkillCount) {
        this.id = id;
        this.count = Math.max(1, count);
        this.specifiedSkills = List.of();
        this.randomSkillCount = randomSkillCount;
    }

    /** 指定数量 + 指定词条 + 尾随随机（randomSkillCount 填 0 = 纯手写词条，不随机）。 */
    public MonsterConfig(ResourceLocation id, int count, int randomSkillCount, String... specifiedSkills) {
        this.id = id;
        this.count = Math.max(1, count);
        this.specifiedSkills = List.of(specifiedSkills);
        this.randomSkillCount = randomSkillCount;
    }
}
