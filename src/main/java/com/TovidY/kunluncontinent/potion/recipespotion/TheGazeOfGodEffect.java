package com.TovidY.kunluncontinent.potion.recipespotion;

import com.google.common.collect.Lists;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TheGazeOfGodEffect extends MobEffect {

    public TheGazeOfGodEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFD700);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "A5B6C7D8-E9F0-11EA-ADC1-0242AC120002",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Lists.newArrayList();
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity.getDeltaMovement().y > 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}