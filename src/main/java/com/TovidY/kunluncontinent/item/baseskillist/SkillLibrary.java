package com.TovidY.kunluncontinent.item.baseskillist;

import com.TovidY.kunluncontinent.capability.playerattributes.Wuhunname;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//技能随机分配

public class SkillLibrary {
    private static final Map<String, Map<Integer, List<RegistryObject<? extends BaseSkillItem>>>> REGISTRY = new HashMap<>();
    static {
        //破魂枪 / 八荒戟 / 裂金虎 / 磐石巨猿 / 天使：
        //  全部已迁移到声明式注册（ModItems.WUHUN_* / WuhunRegistry），这里不再手写。
        //  查表见 getRandomSkill()：先查 WuhunRegistry，查不到才回落到本类的手工表。
        //天使：已迁移到声明式注册（ModItems.WUHUN_TIANSHI / WuhunRegistry），这里不再手写。
        //      —— 查表见 getRandomSkill()：先查 WuhunRegistry，查不到才回落到本类的手工表。
    }
    private static void register(String wuhun, int ringIndex, List<RegistryObject<? extends BaseSkillItem>> skills) {
        REGISTRY.computeIfAbsent(wuhun, k -> new HashMap<>()).put(ringIndex, skills);
    }
    public static BaseSkillItem getRandomSkill(String wuhun, int ringIndex, RandomSource random) {
        // ① 先查声明式注册表（新式武魂都在这儿登记）
        BaseSkillItem fromRegistry = com.TovidY.kunluncontinent.item.wuhun.WuhunRegistry
                .randomSkill(wuhun, ringIndex, random);
        if (fromRegistry != null) {
            return fromRegistry;
        }
        // ② 回落到本类的手工表（尚未迁移的老武魂）
        Map<Integer, List<RegistryObject<? extends BaseSkillItem>>> wuhunSkills = REGISTRY.get(wuhun);
        if (wuhunSkills != null && wuhunSkills.containsKey(ringIndex)) {
            List<RegistryObject<? extends BaseSkillItem>> pool = wuhunSkills.get(ringIndex);
            return pool.get(random.nextInt(pool.size())).get();
        }
        return null;
    }
}