package com.TovidY.kunluncontinent.capability.mobattributes;

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