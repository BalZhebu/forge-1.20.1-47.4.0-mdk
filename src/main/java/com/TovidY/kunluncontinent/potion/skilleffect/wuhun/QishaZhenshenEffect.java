package com.TovidY.kunluncontinent.potion.skilleffect.wuhun;

import com.TovidY.kunluncontinent.potion.PotionAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

/**
 * 七杀真身（七杀剑第 7 魂技）—— <b>只把物攻翻倍</b>，不加任何其它属性。
 *
 * <p>{@code ModAttributeAPI} 里是 {@code value += attr.getWugong(...)}，
 * 所以这里返回 {@code value} 即"再加一份"，也就是 ×2。</p>
 */
public class QishaZhenshenEffect extends MobEffect implements PotionAttribute {

    public QishaZhenshenEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xB0182A);
    }

    @Override
    public float getWugong(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value;   // +100% → 物攻翻倍
    }

    @Override
    public float getWufang(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getBaojishanghai(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getBaojilv(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getZhenshang(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getWuchuan(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getKangbao(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getXixue(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getMinghzong(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getShanbi(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getShengminghuifu(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }

    @Override
    public float getMaxshengming(LivingEntity e, Map.Entry<MobEffect, MobEffectInstance> entry, float v) { return 0; }
}
