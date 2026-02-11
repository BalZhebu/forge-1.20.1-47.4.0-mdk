package com.TovidY.kunluncontinent.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

//该类为工具等级类，用于写工具等级
public enum ModToolTiers implements Tier {
    //前面为材料名，后面数值分别为：采集等级，耐久度，攻击速度，攻击伤害，附魔属性，{}里面的修复的材料
    GRAY_IRON(3,2100,8.0F,5.0F,30,
            ()->Ingredient.of(ModItems.GRAY_IRON_INGOT.get())),

    CLOUD_PATTERNED_BRONZE(4, 3000, 13.0F, 10.0F, 30,
                                   () -> Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())),

    TEST_ITEM(1,100,5.0F,3.0F,15,
            ()->Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()));


    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    ModToolTiers(int level, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    public int getUses() {
        return this.uses;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getAttackDamageBonus() {
        return this.damage;
    }

    public int getLevel() {
        return this.level;
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

}
