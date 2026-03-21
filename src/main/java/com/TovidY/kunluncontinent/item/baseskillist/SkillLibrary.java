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
        //破魂枪
        register(Wuhunname.pohunqiang, 1, List.of(
                ModItems.SKILL_POHUN_1
        ));
        register(Wuhunname.pohunqiang, 2, List.of(
                ModItems.SKILL_POHUN_2
        ));
        register(Wuhunname.pohunqiang, 3, List.of(
                ModItems.SKILL_POHUN_3
        ));
        register(Wuhunname.pohunqiang, 4, List.of(
                ModItems.SKILL_POHUN_4
        ));
        register(Wuhunname.pohunqiang, 5, List.of(
                ModItems.SKILL_POHUN_5
        ));

        //八荒戟
        register(Wuhunname.bahuangji, 1, List.of(
                ModItems.SKILL_BAHUANG_1
        ));
        register(Wuhunname.bahuangji, 2, List.of(
                ModItems.SKILL_BAHUANG_2
        ));
        register(Wuhunname.bahuangji, 3, List.of(
                ModItems.SKILL_BAHUANG_3
        ));
        register(Wuhunname.bahuangji, 4, List.of(
                ModItems.SKILL_BAHUANG_4
        ));
        register(Wuhunname.bahuangji, 5, List.of(
                ModItems.SKILL_BAHUANG_5
        ));

        //裂金虎
        register(Wuhunname.liejinhu, 1, List.of(
                ModItems.SKILL_LEIJINHU_1
        ));
        register(Wuhunname.liejinhu, 2, List.of(
                ModItems.SKILL_LEIJINHU_2
        ));
        register(Wuhunname.liejinhu, 3, List.of(
                ModItems.SKILL_LEIJINHU_3
        ));
        register(Wuhunname.liejinhu, 4, List.of(
                ModItems.SKILL_LEIJINHU_4
        ));
        register(Wuhunname.liejinhu, 5, List.of(
                ModItems.SKILL_LEIJINHU_5
        ));

        //磐石巨猿
        register(Wuhunname.panshijuyuan, 1, List.of(
                ModItems.SKILL_PANSHIJUYUAN_1));
        register(Wuhunname.panshijuyuan, 2, List.of(
                ModItems.SKILL_PANSHIJUYUAN_2));
        register(Wuhunname.panshijuyuan, 3, List.of(
                ModItems.SKILL_PANSHIJUYUAN_3));
        register(Wuhunname.panshijuyuan, 4, List.of(
                ModItems.SKILL_PANSHIJUYUAN_4));
        register(Wuhunname.panshijuyuan, 5, List.of(
                ModItems.SKILL_PANSHIJUYUAN_5));
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