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
    //基础耐久乘数例钻石=33，铁=15，皮革=5
    //构造参数，33 = 基础耐久度乘数，4/7/10/4、都是各部位护甲值，30 = 附魔能力，ARMOR_EQUIP_DIAMOND穿戴音效，3.0F = 盔甲韧性，0.1F = 击退抗性，Ingredient方法 = 修复材料

    GRAY_IRON("gray_iron", 33, Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, 4);
        map.put(ArmorItem.Type.LEGGINGS, 7);
        map.put(ArmorItem.Type.CHESTPLATE, 10);
        map.put(ArmorItem.Type.HELMET, 4);
    }), 30, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.0F, 0.1F, () ->
            Ingredient.of(ModItems.GRAY_IRON_INGOT.get())),

    CLOUD_PATTERNED_BRONZE("cloud_patterned_bronze",40,Util.make(new EnumMap<>(ArmorItem.Type.class),(map) -> {
        map.put(ArmorItem.Type.HELMET,6);
        map.put(ArmorItem.Type.CHESTPLATE,12);
        map.put(ArmorItem.Type.LEGGINGS,9);
        map.put(ArmorItem.Type.BOOTS,6);
    }),30,SoundEvents.ARMOR_EQUIP_NETHERITE,5.0F,0.2F,()->Ingredient.of(ModItems.CLOUD_PATTERNED_BRONZE_INGOT.get())),

    RED_FIRE("red_fire", 55, Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.HELMET, 10);
        map.put(ArmorItem.Type.CHESTPLATE, 15);
        map.put(ArmorItem.Type.LEGGINGS, 13);
        map.put(ArmorItem.Type.BOOTS, 8);
    }), 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 7.0F, 0.3F, () -> Ingredient.of(ModItems.RED_FIRE_INGOT.get())),

    SUNKEN_SILVER("sunken_silver", 30, Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.HELMET, 13);
        map.put(ArmorItem.Type.CHESTPLATE, 20);
        map.put(ArmorItem.Type.LEGGINGS, 15);
        map.put(ArmorItem.Type.BOOTS, 10);
    }), 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 10.0F, 0.5F, () -> Ingredient.of(ModItems.SUNKEN_SILVER_INGOT.get())),

    COLD_HEARTED_STEEL("cold_heated_steel", 45, Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.HELMET, 15);
        map.put(ArmorItem.Type.CHESTPLATE, 25);
        map.put(ArmorItem.Type.LEGGINGS, 20);
        map.put(ArmorItem.Type.BOOTS, 15);
    }), 30, SoundEvents.ARMOR_EQUIP_NETHERITE, 15.0F, 0.8F, () -> Ingredient.of(ModItems.COLD_HEARTED_STEEL_INGOT.get())),








    ;


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

    ModArmorMaterials(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionFunctionForType, int enchantmentValue, SoundEvent sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionFunctionForType = protectionFunctionForType;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
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
