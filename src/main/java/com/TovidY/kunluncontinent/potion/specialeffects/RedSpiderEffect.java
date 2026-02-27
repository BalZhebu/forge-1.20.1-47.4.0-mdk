package com.TovidY.kunluncontinent.potion.specialeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class RedSpiderEffect extends MobEffect {
    public RedSpiderEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // 正确，因为我们不需要每 tick 执行动作，只需要它挂在实体上
    }
}