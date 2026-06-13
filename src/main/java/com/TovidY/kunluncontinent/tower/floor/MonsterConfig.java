package com.TovidY.kunluncontinent.tower.floor;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MonsterConfig {
    public final ResourceLocation id;
    public final List<String> specifiedSkills; // 指定的技能名字列表
    public final int randomSkillCount;         // 额外随机的技能数量

    /**
     * 构造器一：全随机模式（兼容老关卡）
     * 用法：new MonsterConfig(僵尸ID, 2) -> 随机 2 个，指定 0 个
     */
    public MonsterConfig(ResourceLocation id, int randomSkillCount) {
        this.id = id;
        this.specifiedSkills = List.of();
        this.randomSkillCount = randomSkillCount;
    }

    /**
     * 构造器二：高级全定制模式（支持无限指定词条 + 尾随随机数）
     * 用法：new MonsterConfig(犼ID, 2, "不死", "反震", "嗜血") -> 指定这 3 个，再额外不重复随机 2 个
     * 用法：new MonsterConfig(犼ID, 0, "不死", "残暴") -> 【核心】：0随机！只带这两个纯手写定制词条！
     */
    public MonsterConfig(ResourceLocation id, int randomSkillCount, String... specifiedSkills) {
        this.id = id;
        this.specifiedSkills = List.of(specifiedSkills); // 自动将可变参数收纳为List，写多少个都行
        this.randomSkillCount = randomSkillCount;
    }
}