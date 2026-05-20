package com.TovidY.kunluncontinent.item.armor;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.function.Supplier;

//装备属性接口
public enum ModArmorMaterials implements ArmorMaterial {

    // 构造参数：名字, 耐久乘数, 防御值数组[鞋, 腿, 胸, 头], 附魔能力, 音效, 韧性, 击退抗性, 修复材料Suppier

    GRAY_IRON("gray_iron", 33, new int[]{4, 7, 10, 4}, 30, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.0F, 0.1F,
            () -> Ingredient.of(ModItems.GRAY_IRON_INGOT.get())),

    CLOUD_PATTERNED_BRONZE("cloud_patterned_bronze", 40, new int[]{6, 9, 12, 6}, 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.2F,
            () -> Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())),

    RED_FIRE("red_fire", 55, new int[]{8, 13, 15, 10}, 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 7.0F, 0.3F,
            () -> Ingredient.of(ModItems.RED_FIRE_INGOT.get())),

    SUNKEN_SILVER("sunken_silver", 70, new int[]{10, 15, 20, 13}, 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 10.0F, 0.5F,
            () -> Ingredient.of(ModItems.SUNKEN_SILVER_INGOT.get())),

    COLD_HEARTED_STEEL("cold_hearted_steel", 88, new int[]{15, 20, 25, 15}, 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 15.0F, 0.8F,
            () -> Ingredient.of(ModItems.COLD_HEARTED_STEEL_INGOT.get())),

    RINSEI("rinsei", 100, new int[]{17, 22, 27, 18}, 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 18.0F, 0.8F,
            () -> Ingredient.of(ModItems.RINSEI_INGOT.get()));

    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, 13);
        map.put(ArmorItem.Type.LEGGINGS, 15);
        map.put(ArmorItem.Type.CHESTPLATE, 16);
        map.put(ArmorItem.Type.HELMET, 11);
    });

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int[] protectionArray, int enchantmentValue, SoundEvent sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;

        // 内部自动映射
        this.protectionFunctionForType = new EnumMap<>(ArmorItem.Type.class);
        this.protectionFunctionForType.put(ArmorItem.Type.BOOTS, protectionArray[0]);
        this.protectionFunctionForType.put(ArmorItem.Type.LEGGINGS, protectionArray[1]);
        this.protectionFunctionForType.put(ArmorItem.Type.CHESTPLATE, protectionArray[2]);
        this.protectionFunctionForType.put(ArmorItem.Type.HELMET, protectionArray[3]);
    }

    public int getDurabilityForType(ArmorItem.Type pType) {
        return HEALTH_FUNCTION_FOR_TYPE.get(pType) * this.durabilityMultiplier;
    }

    public int getDefenseForType(ArmorItem.Type pType) {
        return this.protectionFunctionForType.get(pType);
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return KlMain.MOD_ID + ":" + this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}