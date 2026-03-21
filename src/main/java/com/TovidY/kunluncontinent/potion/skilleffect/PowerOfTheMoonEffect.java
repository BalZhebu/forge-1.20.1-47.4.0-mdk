package com.TovidY.kunluncontinent.potion.skilleffect;

import com.TovidY.kunluncontinent.potion.PotionAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class PowerOfTheMoonEffect extends MobEffect implements PotionAttribute {

    public PowerOfTheMoonEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x5A9513);
    }

    @Override
    public float getWugong(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseGongji) {
        return baseGongji * 0.5f;
    }

    @Override
    public float getWufang(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseGongji) {
        return baseGongji * 0.5f;
    }

    @Override
    public float getBaojishanghai(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseDamage) {
        return baseDamage * 0.5f;
    }

    @Override
    public float getBaojilv(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseDamage) {
        return 0;
    }

    @Override
    public float getZhenshang(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseDamage) {
        return 0;
    }

    @Override
    public float getWuchuan(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseDamage) {
        return 0;
    }

    @Override
    public float getKangbao(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseDamage) {
        return 0;
    }

    @Override
    public float getXixue(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseHealth) {
        return baseHealth + 20;
    }

    @Override
    public float getMinghzong(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> effectEntry, float baseHealth) {
        return baseHealth + 20;
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
