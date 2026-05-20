package com.TovidY.kunluncontinent.item.tool;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

//该类为工具等级类，用于写工具等级
public enum ModToolTiers implements Tier, ICustomWeaponAttributes {

    // 基础参数含义：采集等级, 耐久, 挖掘速度, 基础伤害, 附魔力, 修复材料Supplier
    // 附加RPG参数：附加攻击, 物理穿透, 吸血率, 暴击率, 暴击伤害
    GRAY_IRON(3, 2100, 20.0F, 5.0F, 30,
            () -> Ingredient.of(ModItems.GRAY_IRON_INGOT.get()),
            30.0f, 0.0f, 0.0f, 0.0f, 0.0f),

    CLOUD_PATTERNED_BRONZE(4, 3200, 40.0F, 10.0F, 30,
            () -> Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()),
            50.0f, 0.35f, 0.0f, 0.0f, 0.0f),

    RED_FIRE(5, 4500, 80.0F, 20.0F, 30,
            () -> Ingredient.of(ModItems.RED_FIRE_INGOT.get()),
            75.0f, 0.5f, 0.2f, 0.0f, 0.0f),

    SUNKEN_SILVER(6, 6600, 142.0F, 50.0F, 30,
            () -> Ingredient.of(ModItems.SUNKEN_SILVER_INGOT.get()),
            100.0f, 0.75f, 0.40f, 0.25f, 0.0f),

    COLD_HEARTED_STEEL(7, 9200, 180.0F, 70.0F, 30,
            () -> Ingredient.of(ModItems.COLD_HEARTED_STEEL_INGOT.get()),
            220.0f, 1.0f, 0.5f, 0.5f, 1.2f),

    RINSEI(8, 15000, 220.0F, 90.0F, 30,
            () -> Ingredient.of(ModItems.RINSEI_INGOT.get()),
            360.0f, 1.5f, 1.0f, 1.0f, 1.5f),

    TEST_ITEM(1, 100, 5.0F, 3.0F, 15,
            () -> Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()),
            999999.0f, 100.0f, 100.0f, 100.0f, 300.0f);


    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    private final float customGongji;
    private final float customWuchuan;
    private final float customXixue;
    private final float customBaoji;
    private final float customBaojiShanghai;

    ModToolTiers(int level, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient,
                 float customGongji, float customWuchuan, float customXixue, float customBaoji, float customBaojiShanghai) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;

        this.customGongji = customGongji;
        this.customWuchuan = customWuchuan;
        this.customXixue = customXixue;
        this.customBaoji = customBaoji;
        this.customBaojiShanghai = customBaojiShanghai;
    }

    @Override public int getUses() { return this.uses; }
    @Override public float getSpeed() { return this.speed; }
    @Override public float getAttackDamageBonus() { return this.damage; }
    @Override public int getLevel() { return this.level; }
    @Override public int getEnchantmentValue() { return this.enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return this.repairIngredient.get(); }

    @Override public float getCustomGongji() { return this.customGongji; }
    @Override public float getCustomWuchuan() { return this.customWuchuan; }
    @Override public float getCustomXixue() { return this.customXixue; }
    @Override public float getCustomBaoji() { return this.customBaoji; }
    @Override public float getCustomBaojiShanghai() { return this.customBaojiShanghai; }
}