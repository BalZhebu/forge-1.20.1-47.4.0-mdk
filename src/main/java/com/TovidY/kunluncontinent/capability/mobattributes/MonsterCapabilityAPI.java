package com.TovidY.kunluncontinent.capability.mobattributes;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
public interface MonsterCapabilityAPI {

    public static MobAttributeCapability genMonsterCapability(Entity entity) {
        RandomSource random = entity.level().random;
        int level = genLevel(entity, random);

        MobAttributeCapability capability = new MobAttributeCapability();

        capability.initNianxian(level);

        return capability;
    }

    public static int genLevel(Entity entity, RandomSource random) {
        int specialLevel = MonsterYearConfig.getSpecialLevel(entity, random);
        if (specialLevel != -1) {
            return specialLevel;
        }
        int index = 0;
        if (entity instanceof Mob && entity instanceof Enemy) {
            ResourceKey<Level> dimension = entity.level().dimension();
            if (dimension == Level.OVERWORLD) {
                int i = random.nextInt(3) + 1;
                index = random.nextInt((int) Math.pow(16, i));
            } else if (dimension == Level.NETHER) {
                int i = random.nextInt(4) + 1;
                index = random.nextInt((int) Math.pow(20, i)) + 100;
            } else if (dimension == Level.END) {
                int i = random.nextInt(6) + 1;
                index = Math.min(random.nextInt((int) Math.pow(9, i)), 1000000) + 100;
            } else {
                int i = random.nextInt(6) + 1;
                index = Math.min(random.nextInt((int) Math.pow(9, i)), 1000000) + 100;
            }
            index += genjvli(entity, random);
            if (((Mob) entity).getMaxHealth() > 60) {
                int healthBoost = (int) (1 + 1000000 * Math.log10(((Mob) entity).getMaxHealth()));
                index = Math.max(index, healthBoost);
            }
        } else {
            index = random.nextInt(11);
        }
        return Math.min(index, 9990000);
    }

    public static int genjvli(Entity entity, RandomSource random) {
        double x = entity.getX();
        double z = entity.getZ();
        double sqrt = Math.sqrt(x * x + z * z);

        int boost;
        if (sqrt > 30000) boost = random.nextInt((int) Math.max(1, 1536000 + sqrt * 10));
        else if (sqrt > 26000) boost = random.nextInt(768000);
        else if (sqrt > 22000) boost = random.nextInt(384000);
        else if (sqrt > 18000) boost = random.nextInt(192000);
        else if (sqrt > 15000) boost = random.nextInt(96000);
        else if (sqrt > 12000) boost = random.nextInt(48000);
        else if (sqrt > 10000) boost = random.nextInt(24000);
        else if (sqrt > 8000)  boost = random.nextInt(12000);
        else if (sqrt > 6000)  boost = random.nextInt(6000);
        else if (sqrt > 4000)  boost = random.nextInt(3000);
        else if (sqrt > 3000)  boost = random.nextInt(1000);
        else if (sqrt > 2000)  boost = random.nextInt(500);
        else if (sqrt > 1000)  boost = random.nextInt(200);
        else boost = random.nextInt(120);

        return boost;
    }
}