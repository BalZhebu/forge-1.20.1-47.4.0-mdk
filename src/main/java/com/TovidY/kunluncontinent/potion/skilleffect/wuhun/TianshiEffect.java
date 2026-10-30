package com.TovidY.kunluncontinent.potion.skilleffect.wuhun;

import com.TovidY.kunluncontinent.potion.PotionAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

/**
 * 天使真身（第七魂技）的属性增幅。
 *
 * <h3>为什么是这几项</h3>
 * <p>天使是「光 + 神圣」定位：<b>攻高、耐打、爆得狠</b>，但不给闪避/吸血的邪道手感。</p>
 * <table border="1">
 *   <tr><th>属性</th><th>增幅</th><th>理由</th></tr>
 *   <tr><td>攻击</td><td>+80%</td><td>四真身里最高 —— 天使的立身之本</td></tr>
 *   <tr><td>最大生命</td><td>+50%</td><td>开真身不至于被一波带走</td></tr>
 *   <tr><td>防御</td><td>+25%</td><td>比重装（裂金虎 +10%）高一档，但仍低于纯坦</td></tr>
 *   <tr><td>暴击率</td><td>+15%</td><td>配合审判之矛/终焉救赎的爆发</td></tr>
 *   <tr><td>暴击伤害</td><td>+35%</td><td>同上</td></tr>
 *   <tr><td>生命回复</td><td>+30%</td><td>「圣光自愈」的体现，和第五魂技呼应</td></tr>
 * </table>
 *
 * <p>注意 {@link PotionAttribute} 的返回值语义是「<b>在当前值之上再加</b>」，
 * 所以 {@code value * 0.8f} = 在当前攻击力基础上再 +80%。</p>
 */
public class TianshiEffect extends MobEffect implements PotionAttribute {

    public TianshiEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFE9A3);
    }

    @Override
    public float getWugong(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value * 0.80f;
    }

    @Override
    public float getMaxshengming(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value * 0.50f;
    }

    @Override
    public float getWufang(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value * 0.25f;
    }

    @Override
    public float getBaojilv(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value * 0.15f;
    }

    @Override
    public float getBaojishanghai(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value * 0.35f;
    }

    @Override
    public float getShengminghuifu(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return value * 0.30f;
    }

    // ---- 以下不给增幅（天使不靠这些吃饭） ----

    @Override
    public float getZhenshang(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return 0;
    }

    @Override
    public float getWuchuan(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return 0;
    }

    @Override
    public float getKangbao(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return 0;
    }

    @Override
    public float getXixue(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return 0;
    }

    @Override
    public float getMinghzong(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return 0;
    }

    @Override
    public float getShanbi(LivingEntity entity, Map.Entry<MobEffect, MobEffectInstance> entry, float value) {
        return 0;
    }
}
