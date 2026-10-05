package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import java.util.Random;

//定义怪物生成年限的类

public class MobAttributeLogic {
    private static final Random RANDOM = new Random();
    public static long calculateNianxian(Entity entity) {
        int specialYear = MonsterYearConfig.getSpecialLevel(entity, entity.level().getRandom());
        if (specialYear != -1) {
            return (long) specialYear;
        }
        ResourceKey<Level> dimension = entity.level().dimension();
        BlockPos pos = entity.blockPosition();
        double distance = Math.sqrt(pos.getX() * (double)pos.getX() + pos.getZ() * (double)pos.getZ());
        double factor = Math.min(1.0, distance / 10000.0);
        long minNianxian;
        long maxNianxian;
        if (dimension == Level.OVERWORLD) {
            minNianxian = 1;
            maxNianxian = 18000;
        } else if (dimension == Level.NETHER) {
            minNianxian = 100;
            maxNianxian = 150000;
        } else if (dimension == Level.END) {
            minNianxian = 1000;
            maxNianxian = 500000;
        } else if (dimension.equals(ModDimensions.POLAR_ICE_REALM_LEVEL_KEY)) {
            minNianxian = 15000;
            maxNianxian = 999999;
        }else if (dimension.equals(ModDimensions.CELESTIAL_REALM_LEVEL_KEY)){
            minNianxian = 100000;
            maxNianxian = 9990000;
        } else {
            minNianxian = 30000;
            maxNianxian = 990000;
        }
        long min = (long) (minNianxian + factor * ((maxNianxian - minNianxian) * 0.1));
        long max = (long) (minNianxian + factor * (maxNianxian - minNianxian));
        return nextLong(min, max);
    }

    private static long nextLong(long min, long max) {
        if (min >= max) return min;
        return min + (long) (RANDOM.nextDouble() * (max - min));
    }
}