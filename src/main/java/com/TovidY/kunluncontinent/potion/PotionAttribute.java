package com.TovidY.kunluncontinent.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

//药水属性接口
public interface PotionAttribute {

    float getWugong(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getWufang(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getBaojishanghai(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getBaojilv(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getZhenshang(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getWuchuan(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getKangbao(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getXixue(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getMinghzong(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getShanbi(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getShengminghuifu(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

    float getMaxshengming(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value);

}
