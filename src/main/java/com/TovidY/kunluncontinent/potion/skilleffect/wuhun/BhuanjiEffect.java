package com.TovidY.kunluncontinent.potion.skilleffect.wuhun;

import com.TovidY.kunluncontinent.potion.PotionAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class BhuanjiEffect  extends MobEffect implements PotionAttribute {

    public BhuanjiEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x55D513);
    }

    @Override
    public float getWugong(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return value * 0.7f;
    }

    @Override
    public float getWufang(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return value * 0.5f;
    }

    @Override
    public float getBaojishanghai(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return value * 0.3f;
    }

    @Override
    public float getBaojilv(LivingEntity livingEntity, Map.Entry<MobEffect, MobEffectInstance> mobEffectMobEffectInstanceEntry, float value) {
        return value * 0.1f;
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
        return value * 0.1f;
    }
}
