package com.TovidY.kunluncontinent.potion.specialeffects.coldeffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ExtremeColdEffect extends MobEffect {
    public ExtremeColdEffect() {
        super(MobEffectCategory.HARMFUL, 0xADD8E6);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
