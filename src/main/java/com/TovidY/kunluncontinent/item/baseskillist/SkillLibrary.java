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
        register(Wuhunname.pohunqiang, 6, List.of(
                ModItems.SKILL_POHUN_6
        ));
        register(Wuhunname.pohunqiang, 7, List.of(
                ModItems.SKILL_POHUN_7
        ));
        register(Wuhunname.pohunqiang, 8, List.of(
                ModItems.SKILL_POHUN_8
        ));
        register(Wuhunname.pohunqiang, 9, List.of(
                ModItems.SKILL_POHUN_9
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
        register(Wuhunname.bahuangji, 6, List.of(
                ModItems.SKILL_BAHUANG_6
        ));
        register(Wuhunname.bahuangji, 7, List.of(
                ModItems.SKILL_BAHUANG_7
        ));
        register(Wuhunname.bahuangji, 8, List.of(
                ModItems.SKILL_BAHUANG_8
        ));
        register(Wuhunname.bahuangji, 9, List.of(
                ModItems.SKILL_BAHUANG_9
        ));

        //裂金虎（第一魂技池：横爪 + 4 个变体，获得时随机五选一）
        register(Wuhunname.liejinhu, 1, List.of(
                ModItems.SKILL_LEIJINHU_1,
                ModItems.SKILL_LEIJINHU_1B,
                ModItems.SKILL_LEIJINHU_1C,
                ModItems.SKILL_LEIJINHU_1D,
                ModItems.SKILL_LEIJINHU_1E
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
        register(Wuhunname.liejinhu, 6, List.of(
                ModItems.SKILL_LEIJINHU_6
        ));
        register(Wuhunname.liejinhu, 7, List.of(
                ModItems.SKILL_LEIJINHU_7
        ));
        register(Wuhunname.liejinhu, 8, List.of(
                ModItems.SKILL_LEIJINHU_8
        ));
        register(Wuhunname.liejinhu, 9, List.of(
                ModItems.SKILL_LEIJINHU_9
        ));

        //磐石巨猿（第一魂技池：重击 + 4 个变体，获得时随机五选一）
        register(Wuhunname.panshijuyuan, 1, List.of(
                ModItems.SKILL_PANSHIJUYUAN_1,
                ModItems.SKILL_PANSHIJUYUAN_1B,
                ModItems.SKILL_PANSHIJUYUAN_1C,
                ModItems.SKILL_PANSHIJUYUAN_1D,
                ModItems.SKILL_PANSHIJUYUAN_1E
        ));
        register(Wuhunname.panshijuyuan, 2, List.of(
                ModItems.SKILL_PANSHIJUYUAN_2));
        register(Wuhunname.panshijuyuan, 3, List.of(
                ModItems.SKILL_PANSHIJUYUAN_3));
        register(Wuhunname.panshijuyuan, 4, List.of(
                ModItems.SKILL_PANSHIJUYUAN_4));
        register(Wuhunname.panshijuyuan, 5, List.of(
                ModItems.SKILL_PANSHIJUYUAN_5));
        register(Wuhunname.panshijuyuan, 6, List.of(
                ModItems.SKILL_PANSHIJUYUAN_6));
        register(Wuhunname.panshijuyuan, 7, List.of(
                ModItems.SKILL_PANSHIJUYUAN_7));
        register(Wuhunname.panshijuyuan, 8, List.of(
                ModItems.SKILL_PANSHIJUYUAN_8));
        register(Wuhunname.panshijuyuan, 9, List.of(
                ModItems.SKILL_PANSHIJUYUAN_9
        ));
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