package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.entity.EntityInit;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

//用于自定义某些生物的年限
public class MonsterYearConfig {
    private static final Map<EntityType<?>, YearRange> SPECIAL_LEVELS = new HashMap<>();

    record YearRange(int min, int max) {}

    //min为最小年限
    //max为最大年限

    static {
        //末影龙
        register(EntityType.ENDER_DRAGON, 500000, 2000000);
        //凋零
        register(EntityType.WITHER, 800000, 1500000);
        //坚守者
        register(EntityType.WARDEN, 500000, 1300000);
        //冰晶
        register(EntityInit.ICE_CRYSTAL.get(), 500, 880000);
        //魔鲸
        register(EntityInit.DEMON_WHALE.get(), 1000000, 9000000);
    }

    private static void register(EntityType<?> type, int min, int max) {
        if (type != null) {
            SPECIAL_LEVELS.put(type, new YearRange(min, max));
        }
    }

    public static int getSpecialLevel(Entity entity, RandomSource random) {
        YearRange range = SPECIAL_LEVELS.get(entity.getType());
        if (range != null) {
            if (range.min >= range.max) return range.min;
            return range.min + random.nextInt(range.max - range.min + 1);
        }
        return -1;
    }

}