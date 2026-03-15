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
        register(Wuhunname.pohunqiang, 1, List.of(
                ModItems.SKILL_POHUN_1
        ));
        register(Wuhunname.pohunqiang, 2, List.of(
                ModItems.SKILL_POHUN_2
        ));

        register(Wuhunname.bahuangji, 1, List.of(
                ModItems.SKILL_BAHUANG_1
        ));
        register(Wuhunname.bahuangji, 2, List.of(
                ModItems.SKILL_BAHUANG_2
        ));

        register(Wuhunname.liejinhu, 1, List.of(
                ModItems.SKILL_LEIJINHU_1
        ));
        register(Wuhunname.liejinhu, 2, List.of(
                ModItems.SKILL_LEIJINHU_2
        ));

        register(Wuhunname.panshijuyuan, 1, List.of(
                ModItems.SKILL_PANSHIJUYUAN_1));
        register(Wuhunname.panshijuyuan, 2, List.of(
                ModItems.SKILL_PANSHIJUYUAN_2));
    }
    private static void register(String wuhun, int ringIndex, List<RegistryObject<? extends BaseSkillItem>> skills) {
        REGISTRY.computeIfAbsent(wuhun, k -> new HashMap<>()).put(ringIndex, skills);
    }
    public static BaseSkillItem getRandomSkill(String wuhun, int ringIndex, RandomSource random) {
        Map<Integer, List<RegistryObject<? extends BaseSkillItem>>> wuhunSkills = REGISTRY.get(wuhun);
        if (wuhunSkills != null && wuhunSkills.containsKey(ringIndex)) {
            List<RegistryObject<? extends BaseSkillItem>> pool = wuhunSkills.get(ringIndex);
            return pool.get(random.nextInt(pool.size())).get();
        }
        return null;
    }
}