package com.TovidY.kunluncontinent.potion.skilleffect;

import com.TovidY.kunluncontinent.potion.PotionAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class StoneSkinEffect extends MobEffect implements PotionAttribute {
    public StoneSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513); // 棕色
    }

    @Override
    public float getWugong(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getWufang(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseFangyu) {
        return baseFangyu * 0.3f;
    }

    @Override
    public float getBaojishanghai(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getBaojilv(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getZhenshang(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getWuchuan(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getKangbao(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getXixue(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getMinghzong(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getShanbi(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getShengminghuifu(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

    @Override
    public float getMaxshengming(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return 0;
    }

}
