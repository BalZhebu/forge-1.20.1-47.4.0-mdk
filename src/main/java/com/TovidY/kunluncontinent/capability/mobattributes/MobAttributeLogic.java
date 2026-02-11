package com.TovidY.kunluncontinent.capability.mobattributes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Random;

//定义怪物生成年限的类
public class MobAttributeLogic {
    private static final Random RANDOM = new Random();

    /**
     * 根据维度和坐标计算随机年限
     * 所有维度都受距离 (0,0) 的影响：越近年限越低，越远年限越高
     * 10000格为最大距离加成点，超过则按10000格计算
     * 各维度最低年限：主世界1年，地狱100年，末地1000年
     */
    public static long calculateNianxian(ResourceKey<Level> dimension, BlockPos pos) {
        // 计算距离 (0,0) 的水平距离
        double distance = Math.sqrt(pos.getX() * pos.getX() + pos.getZ() * pos.getZ());
        
        // 计算距离因子（0.0 ~ 1.0），超过10000格按1.0计算
        double factor = Math.min(1.0, distance / 10000.0);
        
        // 根据维度确定年限范围
        long minNianxian;
        long maxNianxian;
        
        if (dimension == Level.OVERWORLD) {
            minNianxian = 1;
            maxNianxian = 12000;
        } else if (dimension == Level.NETHER) {
            minNianxian = 100;
            maxNianxian = 120000;
        } else if (dimension == Level.END) {
            minNianxian = 1000;
            maxNianxian = 1200000;
        } else {
            // 其他维度默认
            minNianxian = 1;
            maxNianxian = 1000;
        }
        
        // 年限范围：根据距离因子在 [minNianxian, maxNianxian] 之间插值
        // factor=0 时接近 minNianxian，factor=1 时接近 maxNianxian
        long min = (long) (minNianxian + factor * ((maxNianxian - minNianxian) * 0.1));
        long max = (long) (minNianxian + factor * (maxNianxian - minNianxian));
        
        return nextLong(min, max);
    }

    private static long nextLong(long min, long max) {
        if (min >= max) return min;
        return min + (long) (RANDOM.nextDouble() * (max - min));
    }
}
