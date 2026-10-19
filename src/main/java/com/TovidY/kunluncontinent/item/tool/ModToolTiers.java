package com.TovidY.kunluncontinent.item.tool;

import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * 工具 / 武器等级表。
 *
 * <h3>⚠️ 附加 RPG 参数的单位</h3>
 * <p>这五个值会被 {@code ModAttributeAPI} 直接喂进战斗公式，<b>必须用战斗公式的单位</b>，
 * 写成 {@code 0.25f} 这种"比例"会被当成 0.25 点，等于没有（历史版本就是这么写的，
 * 于是武器的穿透/吸血/暴击几乎完全不生效，暴击伤害还会因为低于 100 而变成负收益）。</p>
 * <table>
 *   <tr><th>字段</th><th>单位</th><th>战斗里的用法</th></tr>
 *   <tr><td>附加攻击</td><td>点数</td><td>并入攻击力；<b>会计入「强攻」加点的乘算基数</b></td></tr>
 *   <tr><td>物理穿透</td><td>点数</td><td>{@code 目标防御 − 穿透} 的减防，<b>不是百分比</b></td></tr>
 *   <tr><td>吸血</td><td>点数</td><td>实际吸血率 = {@code x / (x + 100)}（40 点 ≈ 28.6%）</td></tr>
 *   <tr><td>暴击率</td><td>百分点</td><td>4 = 4%</td></tr>
 *   <tr><td>暴击伤害</td><td>百分点</td><td>100 = 1.0 倍；<b>低于 100 会让暴击比普通攻击还低</b></td></tr>
 * </table>
 *
 * <p>强度参考：主世界百年怪 ≈ 450~940 血 / 15~31 防，万年怪 ≈ 5000~22500 血 / 67~300 防。
 * 灰铁剑 60 攻 ≈ 4~9 刀一只百年怪；陆离剑 1100 攻 + 280 穿透对应万年以上的怪。</p>
 */
//该类为工具等级类，用于写工具等级
public enum ModToolTiers implements Tier, ICustomWeaponAttributes {

    // 基础参数含义：采集等级, 耐久, 挖掘速度, 基础伤害, 附魔力, 修复材料Supplier
    // 附加RPG参数：附加攻击, 物理穿透, 吸血, 暴击率(%), 暴击伤害(%)
    GRAY_IRON(3, 2100, 20.0F, 5.0F, 30,
            () -> Ingredient.of(ModItems.GRAY_IRON_INGOT.get()),
            60.0f, 12.0f, 8.0f, 4.0f, 110.0f),

    CLOUD_PATTERNED_BRONZE(4, 3200, 40.0F, 10.0F, 30,
            () -> Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()),
            120.0f, 30.0f, 15.0f, 6.0f, 125.0f),

    RED_FIRE(5, 4500, 80.0F, 20.0F, 30,
            () -> Ingredient.of(ModItems.RED_FIRE_INGOT.get()),
            210.0f, 60.0f, 25.0f, 8.0f, 145.0f),

    SUNKEN_SILVER(6, 6600, 142.0F, 50.0F, 30,
            () -> Ingredient.of(ModItems.SUNKEN_SILVER_INGOT.get()),
            380.0f, 110.0f, 35.0f, 10.0f, 165.0f),

    COLD_HEARTED_STEEL(7, 9200, 180.0F, 70.0F, 30,
            () -> Ingredient.of(ModItems.COLD_HEARTED_STEEL_INGOT.get()),
            650.0f, 180.0f, 45.0f, 12.0f, 190.0f),

    RINSEI(8, 15000, 220.0F, 90.0F, 30,
            () -> Ingredient.of(ModItems.RINSEI_INGOT.get()),
            1100.0f, 280.0f, 60.0f, 14.0f, 220.0f),

    TEST_ITEM(1, 100, 5.0F, 3.0F, 15,
            () -> Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get()),
            999999.0f, 10000.0f, 5000.0f, 100.0f, 1000.0f);


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