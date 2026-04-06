package com.TovidY.kunluncontinent.item;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.capability.playerattributes.Wuhunname;
import com.TovidY.kunluncontinent.entity.EntityInit;
import com.TovidY.kunluncontinent.item.armor.ModArmorBaseItem;
import com.TovidY.kunluncontinent.item.armor.ModArmorMaterials;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.nine.SkillBahuang9;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.nine.SkillPanshijuyuan9;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.nine.SkillLeijinhu9;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.eight.SkillPohun8;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.five.SkillBahuang5;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.six.SkillBahuang6;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.eight.SkillBahuang8;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.five.SkillPanshijuyuan5;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.six.SkillPanshijuyuan6;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.eight.SkillLeijinhu8;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.five.SkillLeijinhu5;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.six.SkillLeijinhu6;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.eight.SkillPanshijuyuan8;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.five.SkillPohun5;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.four.SkillBahuang4;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.one.BahuangjiItem;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.one.SkillBahuang1;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.treen.SkillBahuang3;
import com.TovidY.kunluncontinent.item.baseskillist.bahuangji.two.SkillBahuang2;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.four.SkillPanshijuyuan4;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.one.SkillPanshijuyuan1;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.treen.SkillPanshijuyuan3;
import com.TovidY.kunluncontinent.item.baseskillist.juyuan.two.SkillPanshijuyuan2;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.four.SkillLeijinhu4;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.one.SkillLeijinhu1;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.treen.SkillLeijinhu3;
import com.TovidY.kunluncontinent.item.baseskillist.liejinhu.two.SkillLeijinhu2;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.four.SkillPohun4;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.nine.SkillPohun9;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one.PohunqiangItem;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one.SkillPohun1;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.six.SkillPohun6;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.treen.SkillPohun3;
import com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.two.SkillPohun2;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.SkillBahuang7;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.SkillLeijinhu7;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.SkillPanshijuyuan7;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.SkillPohun7;
import com.TovidY.kunluncontinent.item.hungu.BoneItem;
import com.TovidY.kunluncontinent.item.klitem.*;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.testitemblock.TestLevelUp;
import com.TovidY.kunluncontinent.item.tool.DecompositionItem;
import com.TovidY.kunluncontinent.item.tool.ModSwordBaseItem;
import com.TovidY.kunluncontinent.item.tool.ModToolTiers;
import com.TovidY.kunluncontinent.item.tool.SoulGatheringBottleItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.List;

//物品注册
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, KlMain.MOD_ID);
    //测试物品
    public static final RegistryObject<Item> DANYAO_TEST = ITEMS.register("danyao_test",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(999999).setMinLevel(999));
    public static final RegistryObject<Item> DANYAO_JINGSHENLI = ITEMS.register("danyao_jingshenli",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingshenlibaifenbi(100).setMinLevel(999));
    public static final RegistryObject<Item> TEST_SWORD = ITEMS.register("test_sword",()->new ModSwordBaseItem(ModToolTiers.TEST_ITEM,3,-1.8F,new Item.Properties()));
    public static final RegistryObject<Item> DANYAO_DENGJI_JIA = ITEMS.register("danyao_dengji_jia", () -> new TestLevelUp(new Item.Properties().stacksTo(1), 1));
    public static final RegistryObject<Item> DANYAO_DENGJI_JIAN = ITEMS.register("danyao_dengji_jian", () -> new TestLevelUp(new Item.Properties().stacksTo(1), -1));
    public static final RegistryObject<Item> TEST_ZHUANSHENG = ITEMS.register("test_zhuansheng", () -> new ZhuanShengTestItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> INSTANT_KILL_SWORD =
            ITEMS.register("instant_kill_sword", InstantKillSwordItem::new);

    //升级模版
    public static final RegistryObject<Item> RINSEI_FORGING_TEMPLATE = ITEMS.register("rinsei_forging_template",()->new Item(new Item.Properties().stacksTo(16)));


    //药草种子
    public static final RegistryObject<Item> RED_SPIDER_SEEDS = ITEMS.register("red_spider_seeds",
            () -> new ItemNameBlockItem(ModBlocks.RED_SPIDER_LILY_BLOCK.get(), new Item.Properties()){
                @Override
                public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                    pTooltip.add(Component.translatable("item.red_spider_seeds.tooltip"));
                }
            });

    //固元草种子
    public static final RegistryObject<Item> GUYUANCAO_SEEDS = ITEMS.register("guyuancao_seeds",
            () -> new ItemNameBlockItem(ModBlocks.GUYUANCAO_BLOCK.get(), new Item.Properties()){
                @Override
                public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                    pTooltip.add(Component.translatable("item.guyuancao_seeds.tooltip").withStyle(ChatFormatting.GRAY));
                    pTooltip.add(Component.translatable("item.guyuancao_seeds.tooltip2").withStyle(ChatFormatting.GRAY));
                }
            });

    //返气草
    public static final RegistryObject<Item> FANQICAO_SEEDS = ITEMS.register("fanqicao_seeds",
            () -> new ItemNameBlockItem(ModBlocks.FANQICAO_BLOCK.get(), new Item.Properties()){
                @Override
                public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
                    pTooltip.add(Component.translatable("item.guyuancao_seeds.tooltip").withStyle(ChatFormatting.GRAY));
                    pTooltip.add(Component.translatable("item.guyuancao_seeds.tooltip2").withStyle(ChatFormatting.GRAY));
                }
            });

    //草药物品
    public static final RegistryObject<Item> RED_SPIDER_LILY_ITEM = ITEMS.register("red_spider_lily_item",
            () -> new Item(new Item.Properties()));

    //固元草
    public static final RegistryObject<Item> GUYUANCAO_ITEM = ITEMS.register("guyuancao_item",
            () -> new Item(new Item.Properties()));

    //返气草
    public static final RegistryObject<Item> FANQICAO_ITEM = ITEMS.register("fanqicao_item",
            () -> new Item(new Item.Properties()));

    //药水
    public static final RegistryObject<Item> RED_SPIDER_LILY_POTION = ITEMS.register("red_spider_lily_potion",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .food(new FoodProperties.Builder()
                            .alwaysEat()
                            .nutrition(0)
                            .saturationMod(0)
                            .effect(() -> new MobEffectInstance(ModEffects.RED_SPIDER_LILY_POTION.get(), 600, 0), 1.0F)
                            .build())));

    //传送门
    public static final RegistryObject<Item> EXTREME_COLD_SNOWFLAKE = ITEMS.register("extreme_cold_snowflake",()->new EngravingKnifeItem(new Item.Properties()){
        @Override
        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
            pTooltipComponents.add(Component.translatable("tooltip.kunluncontinent.extreme_cold_snowflake1").withStyle(ChatFormatting.DARK_GRAY));
            pTooltipComponents.add(Component.translatable("tooltip.kunluncontinent.extreme_cold_snowflake2").withStyle(ChatFormatting.DARK_GRAY));
        }
    });

    public static final RegistryObject<Item> THUNDERREALM_SNOWFLAKE = ITEMS.register("thunderrealm_snowflake",()->new EngravingKnifeItem(new Item.Properties()){
        @Override
        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
            pTooltipComponents.add(Component.translatable("tooltip.kunluncontinent.extreme_cold_snowflake1").withStyle(ChatFormatting.DARK_GRAY));
            pTooltipComponents.add(Component.translatable("tooltip.kunluncontinent.extreme_cold_snowflake2").withStyle(ChatFormatting.DARK_GRAY));
        }
    });

    //武魂果实
    public static final RegistryObject<Item> GUOSHI_POHUNQIANG = ITEMS.register("guoshi_pohunqiang",()->new WuhunguoshiItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setWuhunname(Wuhunname.pohunqiang));
    public static final RegistryObject<Item> GUOSHI_BAHUANGJI = ITEMS.register("guoshi_bahuangji",()->new WuhunguoshiItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setWuhunname(Wuhunname.bahuangji));
    public static final RegistryObject<Item> GUOSHI_LEIJINHU = ITEMS.register("guoshi_leijinhu",()->new WuhunguoshiItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setWuhunname(Wuhunname.liejinhu));
    public static final RegistryObject<Item> GUOSHI_PANSHIJUYUAN = ITEMS.register("guoshi_panshijuyuan",()->new WuhunguoshiItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setWuhunname(Wuhunname.panshijuyuan));

    //材料物品（普通物品）
    public static final RegistryObject<Item> EXTREME_COLD_SNOWFLAKE_FRAGMENT = ITEMS.register("extreme_cold_snowflake_fragment",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUIDE_BOOK = ITEMS.register("guide_book",()->new GuideBookItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EXTREME_COLD = ITEMS.register("extreme_cold",()->new Item(new Item.Properties()));

    public static final RegistryObject<Item> EYE_TRANSFORMATION  = ITEMS.register("eye_transformation",()->new EyeTransformationItem(new Item.Properties().stacksTo(1)));


    //御寒魂导器
    public static final RegistryObject<Item> LOW_COLD_PROTECTION = ITEMS.register("low_cold_protection",()->new ThermalAmuletItem(new Item.Properties().stacksTo(1).durability(3000),false));
    public static final RegistryObject<Item> MID_COLD_PROTECTION = ITEMS.register("mid_cold_protection",()->new ThermalAmuletItem(new Item.Properties().stacksTo(1).durability(8999),false));
    public static final RegistryObject<Item> HIGH_COLD_PROTECTION = ITEMS.register("high_cold_protection",()->new ThermalAmuletItem(new Item.Properties().stacksTo(1).durability(35888).fireResistant(),false));
    public static final RegistryObject<Item> TOP_COLD_PROTECTION = ITEMS.register("top_cold_protection",()->new ThermalAmuletItem(new Item.Properties().stacksTo(1).fireResistant(),true));

    //魂骨
    public static final RegistryObject<Item> SOUL_BEAST_SKULL = ITEMS.register("soul_beast_skull",()->new BoneItem(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BEAST_BREASTBONE = ITEMS.register("soul_beast_breastbone",()->new BoneItem(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BEAST_LEFT_HAND_BONE = ITEMS.register("soul_beast_left_hand_bone",()->new BoneItem(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BEAST_RIGHT_HAND_BONE = ITEMS.register("soul_beast_right_hand_bone",()->new BoneItem(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BEAST_LEFT_LEG_BONE = ITEMS.register("soul_beast_left_leg_bone",()->new BoneItem(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BEAST_RIGHT_LEG_BONE = ITEMS.register("soul_beast_right_leg_bone",()->new BoneItem(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BEAST_EXTERNAL_APPENDAGES = ITEMS.register("soul_beast_external_appendages",()->new BoneItem(new Item.Properties()));

    //内丹
    public static final RegistryObject<Item> NEIDAN1 = ITEMS.register("neidan1",()->new NeidanItem(new Item.Properties(),1));
    public static final RegistryObject<Item> NEIDAN2 = ITEMS.register("neidan2",()->new NeidanItem(new Item.Properties(),2));
    public static final RegistryObject<Item> NEIDAN3 = ITEMS.register("neidan3",()->new NeidanItem(new Item.Properties(),3));
    public static final RegistryObject<Item> NEIDAN4 = ITEMS.register("neidan4",()->new NeidanItem(new Item.Properties(),4));
    public static final RegistryObject<Item> NEIDAN5 = ITEMS.register("neidan5",()->new NeidanItem(new Item.Properties(),5));
    public static final RegistryObject<Item> NEIDAN6 = ITEMS.register("neidan6",()->new NeidanItem(new Item.Properties(),6));
    public static final RegistryObject<Item> NEIDAN7 = ITEMS.register("neidan7",()->new NeidanItem(new Item.Properties(),7));
    public static final RegistryObject<Item> NEIDAN8 = ITEMS.register("neidan8",()->new NeidanItem(new Item.Properties(),8));
    public static final RegistryObject<Item> NEIDAN9 = ITEMS.register("neidan9",()->new NeidanItem(new Item.Properties(),9));

    //生物蛋
    public static final RegistryObject<Item> ICE_CRYSTAL_SPAWN_EGG = ITEMS.register("ice_crystal_spawn_egg", () -> new ForgeSpawnEggItem(EntityInit.ICE_CRYSTAL, 0xD9F3FF, 0x32A4FF, new Item.Properties()));
    public static final RegistryObject<Item> SNOW_DEMON_SPAWN_EGG = ITEMS.register("snow_demon_spawn_egg", () -> new ForgeSpawnEggItem(EntityInit.SNOW_DEMON, 0x000000, 0xFFFFFF, new Item.Properties()));

    //矿石宝石
    public static final RegistryObject<Item> GRAY_IRON_INGOT = ITEMS.register("gray_iron_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_INGOT = ITEMS.register("cloud_patterned_bronze_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_INGOT = ITEMS.register("red_fire_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_INGOT = ITEMS.register("sunken_silver_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_INGOT = ITEMS.register("cold_hearted_steel_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_INGOT = ITEMS.register("rinsei_ingot",()->new Item(new Item.Properties()){
        @Override
        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
            pTooltipComponents.add(Component.translatable("item.rinsei_ingot.tooltip").withStyle(ChatFormatting.GRAY));
        }
    });

    public static final RegistryObject<Item> RUBY = ITEMS.register("ruby",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> AMETHYST = ITEMS.register("amethyst",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> SAPPHIRE = ITEMS.register("sapphire",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> STARLIGHT_STONE = ITEMS.register("starlight_stone",()->new Item(new Item.Properties()));

    //装备
    public static final RegistryObject<Item> GRAY_IRON_HELMET = ITEMS.register("gray_iron_helmet",()->new ModArmorBaseItem(ModArmorMaterials.GRAY_IRON,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_CHESTPLATE = ITEMS.register("gray_iron_chestplate",()->new ModArmorBaseItem(ModArmorMaterials.GRAY_IRON,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_LEGGINGS = ITEMS.register("gray_iron_leggings",()->new ModArmorBaseItem(ModArmorMaterials.GRAY_IRON,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_BOOTS = ITEMS.register("gray_iron_boots",()->new ModArmorBaseItem(ModArmorMaterials.GRAY_IRON,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_HELMET = ITEMS.register("cloud_patterned_bronze_helmet",()->new ModArmorBaseItem(ModArmorMaterials.CLOUD_PATTERNED_BRONZE,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_CHESTPLATE = ITEMS.register("cloud_patterned_bronze_chestplate",()->new ModArmorBaseItem(ModArmorMaterials.CLOUD_PATTERNED_BRONZE,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_LEGGINGS = ITEMS.register("cloud_patterned_bronze_leggings",()->new ModArmorBaseItem(ModArmorMaterials.CLOUD_PATTERNED_BRONZE,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_BOOTS = ITEMS.register("cloud_patterned_bronze_boots",()->new ModArmorBaseItem(ModArmorMaterials.CLOUD_PATTERNED_BRONZE,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> RED_FIRE_HELMET = ITEMS.register("red_fire_helmet",()->new ModArmorBaseItem(ModArmorMaterials.RED_FIRE,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_CHESTPLATE = ITEMS.register("red_fire_chestplate",()->new ModArmorBaseItem(ModArmorMaterials.RED_FIRE,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_LEGGINGS = ITEMS.register("red_fire_leggings",()->new ModArmorBaseItem(ModArmorMaterials.RED_FIRE,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_BOOTS = ITEMS.register("red_fire_boots",()->new ModArmorBaseItem(ModArmorMaterials.RED_FIRE,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> SUNKEN_SILVER_HELMET = ITEMS.register("sunken_silver_helmet",()->new ModArmorBaseItem(ModArmorMaterials.SUNKEN_SILVER,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_CHESTPLATE = ITEMS.register("sunken_silver_chestplate",()->new ModArmorBaseItem(ModArmorMaterials.SUNKEN_SILVER,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_LEGGINGS = ITEMS.register("sunken_silver_leggings",()->new ModArmorBaseItem(ModArmorMaterials.SUNKEN_SILVER,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_BOOTS = ITEMS.register("sunken_silver_boots",()->new ModArmorBaseItem(ModArmorMaterials.SUNKEN_SILVER,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> COLD_HEARTED_STEEL_HELMET = ITEMS.register("cold_hearted_steel_helmet",()->new ModArmorBaseItem(ModArmorMaterials.COLD_HEARTED_STEEL,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_CHESTPLATE = ITEMS.register("cold_hearted_steel_chestplate",()->new ModArmorBaseItem(ModArmorMaterials.COLD_HEARTED_STEEL,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_LEGGINGS = ITEMS.register("cold_hearted_steel_leggings",()->new ModArmorBaseItem(ModArmorMaterials.COLD_HEARTED_STEEL,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_BOOTS = ITEMS.register("cold_hearted_steel_boots",()->new ModArmorBaseItem(ModArmorMaterials.COLD_HEARTED_STEEL,ArmorItem.Type.BOOTS,new Item.Properties()));

    public static final RegistryObject<Item> RINSEI_HELMET = ITEMS.register("rinsei_helmet",()->new ModArmorBaseItem(ModArmorMaterials.RINSEI,ArmorItem.Type.HELMET,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_CHESTPLATE = ITEMS.register("rinsei_chestplate",()->new ModArmorBaseItem(ModArmorMaterials.RINSEI,ArmorItem.Type.CHESTPLATE,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_LEGGINGS = ITEMS.register("rinsei_leggings",()->new ModArmorBaseItem(ModArmorMaterials.RINSEI,ArmorItem.Type.LEGGINGS,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_BOOTS = ITEMS.register("rinsei_boots",()->new ModArmorBaseItem(ModArmorMaterials.RINSEI,ArmorItem.Type.BOOTS,new Item.Properties()));

    //工具
    //武魂武器
    public static final RegistryObject<Item> POHUNQIANG = ITEMS.register("pohunqiang", PohunqiangItem::new);
    public static final RegistryObject<Item> BAHUANGJI = ITEMS.register("bahuangji", BahuangjiItem::new);

    public static final RegistryObject<Item> GRAY_IRON_SWORD = ITEMS.register("gray_iron_sword",()->new ModSwordBaseItem(ModToolTiers.GRAY_IRON,3,-1.8F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_PICKAXE = ITEMS.register("gray_iron_pickaxe",()->new PickaxeItem(ModToolTiers.GRAY_IRON,1, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_AXE = ITEMS.register("gray_iron_axe",()->new AxeItem(ModToolTiers.GRAY_IRON,8.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_SHOVEL = ITEMS.register("gray_iron_shovel",()->new ShovelItem(ModToolTiers.GRAY_IRON,1.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_HOE = ITEMS.register("gray_iron_hoe",()->new HoeItem(ModToolTiers.GRAY_IRON,-2, 0.5F,new Item.Properties()));

    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_SWORD = ITEMS.register("cloud_patterned_bronze_sword",()->new ModSwordBaseItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,5,-1.3F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_PICKAXE = ITEMS.register("cloud_patterned_bronze_pickaxe",()->new PickaxeItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,4, -1.5F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_AXE = ITEMS.register("cloud_patterned_bronze_axe",()->new AxeItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,10.0F, -1.9F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_SHOVEL = ITEMS.register("cloud_patterned_bronze_shovel",()->new ShovelItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,2.8F, -0.8F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_HOE = ITEMS.register("cloud_patterned_bronze_hoe",()->new HoeItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,-1, 1.0F,new Item.Properties()));

    public static final RegistryObject<Item> RED_FIRE_SWORD = ITEMS.register("red_fire_sword",()->new ModSwordBaseItem(ModToolTiers.RED_FIRE,6,-1.0F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_PICKAXE = ITEMS.register("red_fire_pickaxe",()->new PickaxeItem(ModToolTiers.RED_FIRE,4, -1.1F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_AXE = ITEMS.register("red_fire_axe",()->new AxeItem(ModToolTiers.RED_FIRE,12.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_SHOVEL = ITEMS.register("red_fire_shovel",()->new ShovelItem(ModToolTiers.RED_FIRE,2.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_HOE = ITEMS.register("red_fire_hoe",()->new HoeItem(ModToolTiers.RED_FIRE,-1, 1.0F,new Item.Properties()));

    public static final RegistryObject<Item> SUNKEN_SILVER_SWORD = ITEMS.register("sunken_silver_sword",()->new ModSwordBaseItem(ModToolTiers.SUNKEN_SILVER,4,-1.5F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_PICKAXE = ITEMS.register("sunken_silver_pickaxe",()->new PickaxeItem(ModToolTiers.SUNKEN_SILVER,4, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_AXE = ITEMS.register("sunken_silver_axe",()->new AxeItem(ModToolTiers.SUNKEN_SILVER,8.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_SHOVEL = ITEMS.register("sunken_silver_shovel",()->new ShovelItem(ModToolTiers.SUNKEN_SILVER,1.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_HOE = ITEMS.register("sunken_silver_hoe",()->new HoeItem(ModToolTiers.SUNKEN_SILVER,-2, 0.5F,new Item.Properties()));

    public static final RegistryObject<Item> COLD_HEARTED_STEEL_SWORD = ITEMS.register("cold_hearted_steel_sword",()->new ModSwordBaseItem(ModToolTiers.COLD_HEARTED_STEEL,7,-1.2F,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_PICKAXE = ITEMS.register("cold_hearted_steel_pickaxe",()->new PickaxeItem(ModToolTiers.COLD_HEARTED_STEEL,4, -1.5F,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_AXE = ITEMS.register("cold_hearted_steel_axe",()->new AxeItem(ModToolTiers.COLD_HEARTED_STEEL,10.0F, -1.9F,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_SHOVEL = ITEMS.register("cold_hearted_steel_shovel",()->new ShovelItem(ModToolTiers.COLD_HEARTED_STEEL,2.8F, -0.8F,new Item.Properties()));
    public static final RegistryObject<Item> COLD_HEARTED_STEEL_HOE = ITEMS.register("cold_hearted_steel_hoe",()->new HoeItem(ModToolTiers.COLD_HEARTED_STEEL,-1, 1.0F,new Item.Properties()));

    public static final RegistryObject<Item> RINSEI_SWORD = ITEMS.register("rinsei_sword",()->new ModSwordBaseItem(ModToolTiers.RINSEI,8,-1.0F,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_PICKAXE = ITEMS.register("rinsei_pickaxe",()->new PickaxeItem(ModToolTiers.RINSEI,4, -1.1F,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_AXE = ITEMS.register("rinsei_axe",()->new AxeItem(ModToolTiers.RINSEI,12.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_SHOVEL = ITEMS.register("rinsei_shovel",()->new ShovelItem(ModToolTiers.RINSEI,2.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> RINSEI_HOE = ITEMS.register("rinsei_hoe",()->new HoeItem(ModToolTiers.RINSEI,-1, 1.0F,new Item.Properties()));

    //丹药类
    public static final RegistryObject<Item> CHUYUAN_DAN = ITEMS.register("chuyuan_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(16).setMinLevel(1).setTier(1));
    public static final RegistryObject<Item> BAICAOLING_DAN = ITEMS.register("baicaoling_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(56).setMinLevel(10).setTier(2));
    public static final RegistryObject<Item> QIANHUABAO_DAN = ITEMS.register("qianhuabao_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(112).setMinLevel(20).setTier(3));
    public static final RegistryObject<Item> WANPOXUAN_DAN = ITEMS.register("wanpoxuan_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(256).setMinLevel(30).setTier(4));
    public static final RegistryObject<Item> SHIFANGJIE_DAN = ITEMS.register("shifangjie_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(512).setMinLevel(40).setTier(5));
    public static final RegistryObject<Item> HUANYUANYIQI_DAN = ITEMS.register("huanyuanyiqi_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(2026).setMinLevel(50).setTier(6));
    public static final RegistryObject<Item> TAIXUPOWANG_DAN = ITEMS.register("taixupowang_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(4096).setMinLevel(60).setTier(7));
    public static final RegistryObject<Item> QIANWANXINGCHEN_DAN = ITEMS.register("qianwanxingchen_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(8192).setMinLevel(70).setTier(8));
    public static final RegistryObject<Item> YIZAICHUANGSHENG_DAN = ITEMS.register("yizhaichuangsheng_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(16384).setMinLevel(80).setTier(9));
    public static final RegistryObject<Item> GUYUAN_DAN = ITEMS.register("guyuan_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setMaxjingshenli(100));
    public static final RegistryObject<Item> FANQI_DAN = ITEMS.register("fanqi_dan",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingshenlibaifenbi(5));

    public static final RegistryObject<Item> DROSS = ITEMS.register("dross",()->new Item(new Item.Properties()));

    //刻刀类
    public static final RegistryObject<Item> IRON_ENGRAVING_KNIFE = ITEMS.register("iron_engravings_knife",()->new EngravingKnifeItem(new Item.Properties().durability(30)));
    public static final RegistryObject<Item> DIAMOND_ENGRAVING_KNIFE = ITEMS.register("diamond_engravings_knife",()->new EngravingKnifeItem(new Item.Properties().durability(100)));

    //丹炉核心
    public static final RegistryObject<Item> LOW_LEVEL_HEXIN = ITEMS.register("low_level_hexin",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> MIDDLE_LEVEL_HEXIN = ITEMS.register("middle_level_hexin",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> HIGH_LEVEL_HEXIN = ITEMS.register("high_level_hexin",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> TOP_LEVEL_HEXIN = ITEMS.register("top_level_hexin",()->new Item(new Item.Properties()));

    //魂环储存器核心
    public static final RegistryObject<Item> LOW_HUNHUAN_STORAGE_CORE = ITEMS.register("low_hunhuan_storage_core",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> MIDDLE_HUNHUAN_STORAGE_CORE = ITEMS.register("middle_hunhuan_storage_core",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> HIGH_HUNHUAN_STORAGE_CORE = ITEMS.register("high_hunhuan_storage_core",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> TOP_HUNHUAN_STORAGE_CORE = ITEMS.register("top_hunhuan_storage_core",()->new Item(new Item.Properties()));

    // 魂环储存器
    public static final RegistryObject<Item> HUNHUAN_STORAGE_ONE = ITEMS.register("hunhuan_storage_one",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 16666));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_TWO = ITEMS.register("hunhuan_storage_two",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 150000));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_THREE = ITEMS.register("hunhuan_storage_three",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 1900000));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_FOUR = ITEMS.register("hunhuan_storage_four",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 33000000));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_FIVE = ITEMS.register("hunhuan_storage_five",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 120000000));

    //聚魂瓶
    public static final RegistryObject<Item> SOUL_GATHERING_BOTTLE_0 = ITEMS.register("soul_gathering_bottle_0",
            () -> new SoulGatheringBottleItem(new Item.Properties().stacksTo(1),1).setMaxnengliang(1024));

    public static final RegistryObject<Item> SOUL_GATHERING_BOTTLE_1 = ITEMS.register("soul_gathering_bottle_1",
            () -> new SoulGatheringBottleItem(new Item.Properties().stacksTo(1),2).setMaxnengliang(2048));

    public static final RegistryObject<Item> SOUL_GATHERING_BOTTLE_2 = ITEMS.register("soul_gathering_bottle_2",
            () -> new SoulGatheringBottleItem(new Item.Properties().stacksTo(1),3).setMaxnengliang(4096));

    public static final RegistryObject<Item> SOUL_GATHERING_BOTTLE_3 = ITEMS.register("soul_gathering_bottle_3",
            () -> new SoulGatheringBottleItem(new Item.Properties().stacksTo(1),4).setMaxnengliang(8192));

    public static final RegistryObject<Item> SOUL_GATHERING_BOTTLE_4 = ITEMS. register("soul_gathering_bottle_4",
            () -> new SoulGatheringBottleItem(new Item.Properties().stacksTo(1),5).setMaxnengliang(16384));

    public static final RegistryObject<Item> FIRST_DECOMPOSITION_GOSSIP      = ITEMS.register("first_decomposition_gossip", () -> new DecompositionItem(new Item.Properties().fireResistant().stacksTo(1),1));
    public static final RegistryObject<Item> TWO_DECOMPOSITION_GOSSIP      = ITEMS.register("two_decomposition_gossip", () -> new DecompositionItem(new Item.Properties().fireResistant().stacksTo(1),2));
    public static final RegistryObject<Item> THREE_DECOMPOSITION_GOSSIP      = ITEMS.register("three_decomposition_gossip", () -> new DecompositionItem(new Item.Properties().fireResistant().stacksTo(1),3));
    public static final RegistryObject<Item> FOUR_DECOMPOSITION_GOSSIP      = ITEMS.register("four_decomposition_gossip", () -> new DecompositionItem(new Item.Properties().fireResistant().stacksTo(1),4));
    public static final RegistryObject<Item> FIVE_DECOMPOSITION_GOSSIP      = ITEMS.register("five_decomposition_gossip", () -> new DecompositionItem(new Item.Properties().fireResistant().stacksTo(1),5));

    public static final RegistryObject<Item> FANGSHANHUNDAOQI_1 = ITEMS.register("fangshanhundaoqi_1",
            () -> new ThunderProtectionItem(new Item.Properties().durability(256).fireResistant(), 1));

    public static final RegistryObject<Item> FANGSHANHUNDAOQI_2 = ITEMS.register("fangshanhundaoqi_2",
            () -> new ThunderProtectionItem(new Item.Properties().durability(512).fireResistant(), 2));

    public static final RegistryObject<Item> FANGSHANHUNDAOQI_3 = ITEMS.register("fangshanhundaoqi_3",
            () -> new ThunderProtectionItem(new Item.Properties().durability(1024).fireResistant(), 3));

    public static final RegistryObject<Item> FANGSHANHUNDAOQI_4 = ITEMS.register("fangshanhundaoqi_4",
            () -> new ThunderProtectionItem(new Item.Properties().durability(2048).fireResistant(), 4));

    public static final RegistryObject<Item> FANGSHANHUNDAOQI_5 = ITEMS.register("fangshanhundaoqi_5",
            () -> new ThunderProtectionItem(new Item.Properties().durability(4096).fireResistant(), 5));

    //魂技
    //破魂枪
    public static final RegistryObject<SkillPohun1> SKILL_POHUN_1 = ITEMS.register("skill_pohun_1", SkillPohun1::new);
    public static final RegistryObject<SkillPohun2> SKILL_POHUN_2 = ITEMS.register("skill_pohun_2", SkillPohun2::new);
    public static final RegistryObject<SkillPohun3> SKILL_POHUN_3 = ITEMS.register("skill_pohun_3", SkillPohun3::new);
    public static final RegistryObject<SkillPohun4> SKILL_POHUN_4 = ITEMS.register("skill_pohun_4", SkillPohun4::new);
    public static final RegistryObject<SkillPohun5> SKILL_POHUN_5 = ITEMS.register("skill_pohun_5", SkillPohun5::new);
    public static final RegistryObject<SkillPohun6> SKILL_POHUN_6 = ITEMS.register("skill_pohun_6", SkillPohun6::new);
    public static final RegistryObject<SkillPohun7> SKILL_POHUN_7 = ITEMS.register("skill_pohun_7", SkillPohun7::new);
    public static final RegistryObject<SkillPohun8> SKILL_POHUN_8 = ITEMS.register("skill_pohun_8", SkillPohun8::new);
    public static final RegistryObject<SkillPohun9> SKILL_POHUN_9 = ITEMS.register("skill_pohun_9", SkillPohun9::new);

    //八荒戟
    public static final RegistryObject<SkillBahuang1> SKILL_BAHUANG_1 = ITEMS.register("skill_bahuang_1", SkillBahuang1::new);
    public static final RegistryObject<SkillBahuang2> SKILL_BAHUANG_2 = ITEMS.register("skill_bahuang_2", SkillBahuang2::new);
    public static final RegistryObject<SkillBahuang3> SKILL_BAHUANG_3 = ITEMS.register("skill_bahuang_3", SkillBahuang3::new);
    public static final RegistryObject<SkillBahuang4> SKILL_BAHUANG_4 = ITEMS.register("skill_bahuang_4", SkillBahuang4::new);
    public static final RegistryObject<SkillBahuang5> SKILL_BAHUANG_5 = ITEMS.register("skill_bahuang_5", SkillBahuang5::new);
    public static final RegistryObject<SkillBahuang6> SKILL_BAHUANG_6 = ITEMS.register("skill_bahuang_6", SkillBahuang6::new);
    public static final RegistryObject<SkillBahuang7> SKILL_BAHUANG_7 = ITEMS.register("skill_bahuang_7", SkillBahuang7::new);
    public static final RegistryObject<SkillBahuang8> SKILL_BAHUANG_8 = ITEMS.register("skill_bahuang_8", SkillBahuang8::new);
    public static final RegistryObject<SkillBahuang9> SKILL_BAHUANG_9 = ITEMS.register("skill_bahuang_9", SkillBahuang9::new);

    //裂金虎
    public static final RegistryObject<SkillLeijinhu1> SKILL_LEIJINHU_1 = ITEMS.register("skill_leijinhu_1", SkillLeijinhu1::new);
    public static final RegistryObject<SkillLeijinhu2> SKILL_LEIJINHU_2 = ITEMS.register("skill_leijinhu_2", SkillLeijinhu2::new);
    public static final RegistryObject<SkillLeijinhu3> SKILL_LEIJINHU_3 = ITEMS.register("skill_leijinhu_3", SkillLeijinhu3::new);
    public static final RegistryObject<SkillLeijinhu4> SKILL_LEIJINHU_4 = ITEMS.register("skill_leijinhu_4", SkillLeijinhu4::new);
    public static final RegistryObject<SkillLeijinhu5> SKILL_LEIJINHU_5 = ITEMS.register("skill_leijinhu_5", SkillLeijinhu5::new);
    public static final RegistryObject<SkillLeijinhu6> SKILL_LEIJINHU_6 = ITEMS.register("skill_leijinhu_6", SkillLeijinhu6::new);
    public static final RegistryObject<SkillLeijinhu7> SKILL_LEIJINHU_7 = ITEMS.register("skill_leijinhu_7", SkillLeijinhu7::new);
    public static final RegistryObject<SkillLeijinhu8> SKILL_LEIJINHU_8 = ITEMS.register("skill_leijinhu_8", SkillLeijinhu8::new);
    public static final RegistryObject<SkillLeijinhu9> SKILL_LEIJINHU_9 = ITEMS.register("skill_leijinhu_9", SkillLeijinhu9::new);

    //磐石巨猿
    public static final RegistryObject<SkillPanshijuyuan1> SKILL_PANSHIJUYUAN_1 = ITEMS.register("skill_panshijuyuan_1", SkillPanshijuyuan1::new);
    public static final RegistryObject<SkillPanshijuyuan2> SKILL_PANSHIJUYUAN_2 = ITEMS.register("skill_panshijuyuan_2", SkillPanshijuyuan2::new);
    public static final RegistryObject<SkillPanshijuyuan3> SKILL_PANSHIJUYUAN_3 = ITEMS.register("skill_panshijuyuan_3", SkillPanshijuyuan3::new);
    public static final RegistryObject<SkillPanshijuyuan4> SKILL_PANSHIJUYUAN_4 = ITEMS.register("skill_panshijuyuan_4", SkillPanshijuyuan4::new);
    public static final RegistryObject<SkillPanshijuyuan5> SKILL_PANSHIJUYUAN_5 = ITEMS.register("skill_panshijuyuan_5", SkillPanshijuyuan5::new);
    public static final RegistryObject<SkillPanshijuyuan6> SKILL_PANSHIJUYUAN_6 = ITEMS.register("skill_panshijuyuan_6", SkillPanshijuyuan6::new);
    public static final RegistryObject<SkillPanshijuyuan7> SKILL_PANSHIJUYUAN_7 = ITEMS.register("skill_panshijuyuan_7", SkillPanshijuyuan7::new);
    public static final RegistryObject<SkillPanshijuyuan8> SKILL_PANSHIJUYUAN_8 = ITEMS.register("skill_panshijuyuan_8", SkillPanshijuyuan8::new);
    public static final RegistryObject<SkillPanshijuyuan9> SKILL_PANSHIJUYUAN_9 = ITEMS.register("skill_panshijuyuan_9", SkillPanshijuyuan9::new);

    //占位物品
    public static final RegistryObject<Item> ATTRIBUTE_BUTTON = ITEMS.register("attribute_button",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> SOUL_BONE_BUTTON = ITEMS.register("soul_bone_button",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> HUNHUAN_BUTTON = ITEMS.register("hunhuan_button",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHENKAO_BUTTON = ITEMS.register("shenkao_button",()->new Item(new Item.Properties()));

    public static ArrayList<RegistryObject<Item>> KLBUTTON = new ArrayList<>();
    static {
        KLBUTTON.add(ATTRIBUTE_BUTTON);
        KLBUTTON.add(SOUL_BONE_BUTTON);
        KLBUTTON.add(HUNHUAN_BUTTON);
        KLBUTTON.add(SHENKAO_BUTTON);
    }

    public static ArrayList<RegistryObject<Item>> JUHUNPING = new ArrayList<>();
    static {
        JUHUNPING.add(SOUL_GATHERING_BOTTLE_0);
        JUHUNPING.add(SOUL_GATHERING_BOTTLE_1);
        JUHUNPING.add(SOUL_GATHERING_BOTTLE_2);
        JUHUNPING.add(SOUL_GATHERING_BOTTLE_3);
        JUHUNPING.add(SOUL_GATHERING_BOTTLE_4);
    }

    public static ArrayList<RegistryObject<? extends Item>> HUNJILIST = new ArrayList<>();
    static {
        HUNJILIST.add(SKILL_POHUN_1);
        HUNJILIST.add(SKILL_BAHUANG_1);
        HUNJILIST.add(SKILL_LEIJINHU_1);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_1);
        HUNJILIST.add(SKILL_POHUN_2);
        HUNJILIST.add(SKILL_BAHUANG_2);
        HUNJILIST.add(SKILL_LEIJINHU_2);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_2);
        HUNJILIST.add(SKILL_POHUN_3);
        HUNJILIST.add(SKILL_BAHUANG_3);
        HUNJILIST.add(SKILL_LEIJINHU_3);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_3);
        HUNJILIST.add(SKILL_POHUN_4);
        HUNJILIST.add(SKILL_BAHUANG_4);
        HUNJILIST.add(SKILL_LEIJINHU_4);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_4);
        HUNJILIST.add(SKILL_POHUN_5);
        HUNJILIST.add(SKILL_BAHUANG_5);
        HUNJILIST.add(SKILL_LEIJINHU_5);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_5);
        HUNJILIST.add(SKILL_POHUN_6);
        HUNJILIST.add(SKILL_BAHUANG_6);
        HUNJILIST.add(SKILL_LEIJINHU_6);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_6);
        HUNJILIST.add(SKILL_POHUN_7);
        HUNJILIST.add(SKILL_BAHUANG_7);
        HUNJILIST.add(SKILL_LEIJINHU_7);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_7);
        HUNJILIST.add(SKILL_POHUN_8);
        HUNJILIST.add(SKILL_BAHUANG_8);
        HUNJILIST.add(SKILL_LEIJINHU_8);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_8);
        HUNJILIST.add(SKILL_POHUN_9);
        HUNJILIST.add(SKILL_BAHUANG_9);
        HUNJILIST.add(SKILL_LEIJINHU_9);
        HUNJILIST.add(SKILL_PANSHIJUYUAN_9);
    }

    public static ArrayList<RegistryObject<Item>> DUANZAOMOBAN = new ArrayList<>();
    static {
        DUANZAOMOBAN.add(RINSEI_FORGING_TEMPLATE);
    }

    public static ArrayList<RegistryObject<Item>> WUHUNGUOSHI = new ArrayList<>();
    static {
        WUHUNGUOSHI.add(GUOSHI_POHUNQIANG);
        WUHUNGUOSHI.add(GUOSHI_BAHUANGJI);
        WUHUNGUOSHI.add(GUOSHI_LEIJINHU);
        WUHUNGUOSHI.add(GUOSHI_PANSHIJUYUAN);
    }

    public static ArrayList<RegistryObject<Item>> NORMALITEMSLIST = new ArrayList<>();
    static {
        NORMALITEMSLIST.add(EXTREME_COLD_SNOWFLAKE_FRAGMENT);
        NORMALITEMSLIST.add(GUIDE_BOOK);
        NORMALITEMSLIST.add(EXTREME_COLD);

        NORMALITEMSLIST.add(EYE_TRANSFORMATION);
    }

    public static ArrayList<RegistryObject<Item>> SPAWNEGGLIST = new ArrayList<>();
    static {
        SPAWNEGGLIST.add(ICE_CRYSTAL_SPAWN_EGG);
        SPAWNEGGLIST.add(SNOW_DEMON_SPAWN_EGG);
    }

    public static ArrayList<RegistryObject<Item>> HUNGULIST = new ArrayList<>();
    static {
        HUNGULIST.add(SOUL_BEAST_SKULL);
        HUNGULIST.add(SOUL_BEAST_BREASTBONE);
        HUNGULIST.add(SOUL_BEAST_LEFT_HAND_BONE);
        HUNGULIST.add(SOUL_BEAST_RIGHT_HAND_BONE);
        HUNGULIST.add(SOUL_BEAST_LEFT_LEG_BONE);
        HUNGULIST.add(SOUL_BEAST_RIGHT_LEG_BONE);
        HUNGULIST.add(SOUL_BEAST_EXTERNAL_APPENDAGES);

        HUNGULIST.add(FIRST_DECOMPOSITION_GOSSIP);
        HUNGULIST.add(TWO_DECOMPOSITION_GOSSIP);
        HUNGULIST.add(THREE_DECOMPOSITION_GOSSIP);
        HUNGULIST.add(FOUR_DECOMPOSITION_GOSSIP);
        HUNGULIST.add(FIVE_DECOMPOSITION_GOSSIP);

        HUNGULIST.add(FANGSHANHUNDAOQI_1);
        HUNGULIST.add(FANGSHANHUNDAOQI_2);
        HUNGULIST.add(FANGSHANHUNDAOQI_3);
        HUNGULIST.add(FANGSHANHUNDAOQI_4);
        HUNGULIST.add(FANGSHANHUNDAOQI_5);
    }

    public static final List<RegistryObject<Item>> THUNDER_PROTECTION_LIST = List.of(
            FANGSHANHUNDAOQI_1, FANGSHANHUNDAOQI_2, FANGSHANHUNDAOQI_3, FANGSHANHUNDAOQI_4, FANGSHANHUNDAOQI_5
    );

    public static ArrayList<RegistryObject<Item>> COLDPROTECTIONLIST = new ArrayList<>();
    static {
        COLDPROTECTIONLIST.add(LOW_COLD_PROTECTION);
        COLDPROTECTIONLIST.add(MID_COLD_PROTECTION);
        COLDPROTECTIONLIST.add(HIGH_COLD_PROTECTION);
        COLDPROTECTIONLIST.add(TOP_COLD_PROTECTION);
    }

    public static ArrayList<RegistryObject<Item>> HEXIN = new ArrayList<>();
    static {
        HEXIN.add(LOW_LEVEL_HEXIN);
        HEXIN.add(MIDDLE_LEVEL_HEXIN);
        HEXIN.add(HIGH_LEVEL_HEXIN);
        HEXIN.add(TOP_LEVEL_HEXIN);
    }

    public static ArrayList<RegistryObject<Item>> CAOYAOLIST = new ArrayList<>();
    static {
        CAOYAOLIST.add(RED_SPIDER_LILY_ITEM);
        CAOYAOLIST.add(GUYUANCAO_ITEM);
        CAOYAOLIST.add(FANQICAO_ITEM);
    }

    public static ArrayList<RegistryObject<Item>> SEEDSLIST = new ArrayList<>();
    static {
        SEEDSLIST.add(RED_SPIDER_SEEDS);
        SEEDSLIST.add(GUYUANCAO_SEEDS);
        SEEDSLIST.add(FANQICAO_SEEDS);
    }

    public static ArrayList<RegistryObject<Item>> PUTONGITEM = new ArrayList<>();
    static {
        PUTONGITEM.add(EXTREME_COLD_SNOWFLAKE);
        PUTONGITEM.add(THUNDERREALM_SNOWFLAKE);
    }

    public static ArrayList<RegistryObject<Item>> HUNHUAN_STORAGE_CORE = new ArrayList<>();
    static {
        HUNHUAN_STORAGE_CORE.add(LOW_HUNHUAN_STORAGE_CORE);
        HUNHUAN_STORAGE_CORE.add(MIDDLE_HUNHUAN_STORAGE_CORE);
        HUNHUAN_STORAGE_CORE.add(HIGH_HUNHUAN_STORAGE_CORE);
        HUNHUAN_STORAGE_CORE.add(TOP_HUNHUAN_STORAGE_CORE);
    }


    public static ArrayList<RegistryObject<Item>> ENGRAVING_KNIFE = new ArrayList<>();
    static {
        ENGRAVING_KNIFE.add(IRON_ENGRAVING_KNIFE);
        ENGRAVING_KNIFE.add(DIAMOND_ENGRAVING_KNIFE);
    }

    public static ArrayList<RegistryObject<Item>> NEIDANLIST = new ArrayList<>();
    static {
        NEIDANLIST.add(NEIDAN1);
        NEIDANLIST.add(NEIDAN2);
        NEIDANLIST.add(NEIDAN3);
        NEIDANLIST.add(NEIDAN4);
        NEIDANLIST.add(NEIDAN5);
        NEIDANLIST.add(NEIDAN6);
        NEIDANLIST.add(NEIDAN7);
        NEIDANLIST.add(NEIDAN8);
        NEIDANLIST.add(NEIDAN9);
    }


    public static ArrayList<RegistryObject<Item>> hunhuanstorage = new ArrayList<>();
    static {
        hunhuanstorage.add(HUNHUAN_STORAGE_ONE);
        hunhuanstorage.add(HUNHUAN_STORAGE_TWO);
        hunhuanstorage.add(HUNHUAN_STORAGE_THREE);
        hunhuanstorage.add(HUNHUAN_STORAGE_FOUR);
        hunhuanstorage.add(HUNHUAN_STORAGE_FIVE);
    }

    public static ArrayList<RegistryObject<Item>> DEBUG_ITEM_BLOCK = new ArrayList<>();
    static {
        DEBUG_ITEM_BLOCK.add(DANYAO_TEST);
        DEBUG_ITEM_BLOCK.add(DANYAO_JINGSHENLI);
        DEBUG_ITEM_BLOCK.add(TEST_SWORD);
        DEBUG_ITEM_BLOCK.add(DANYAO_DENGJI_JIA);
        DEBUG_ITEM_BLOCK.add(DANYAO_DENGJI_JIAN);
        DEBUG_ITEM_BLOCK.add(TEST_ZHUANSHENG);
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static ArrayList<RegistryObject<Item>> MODSTONE = new ArrayList<>();
    static {
        MODSTONE.add(GRAY_IRON_INGOT);
        MODSTONE.add(CLOUD_PATTERNED_BRONZE_INGOT);
        MODSTONE.add(RED_FIRE_INGOT);
        MODSTONE.add(SUNKEN_SILVER_INGOT);
        MODSTONE.add(COLD_HEARTED_STEEL_INGOT);
        MODSTONE.add(RINSEI_INGOT);

        MODSTONE.add(RUBY);
        MODSTONE.add(AMETHYST);
        MODSTONE.add(SAPPHIRE);
        MODSTONE.add(STARLIGHT_STONE);
    }

    public static ArrayList<RegistryObject<Item>> DANYAOITEM = new ArrayList<>();
    static {
        DANYAOITEM.add(CHUYUAN_DAN);
        DANYAOITEM.add(BAICAOLING_DAN);
        DANYAOITEM.add(QIANHUABAO_DAN);
        DANYAOITEM.add(WANPOXUAN_DAN);
        DANYAOITEM.add(SHIFANGJIE_DAN);
        DANYAOITEM.add(HUANYUANYIQI_DAN);
        DANYAOITEM.add(TAIXUPOWANG_DAN);
        DANYAOITEM.add(QIANWANXINGCHEN_DAN);
        DANYAOITEM.add(YIZAICHUANGSHENG_DAN);
        DANYAOITEM.add(GUYUAN_DAN);
        DANYAOITEM.add(FANQI_DAN);

        DANYAOITEM.add(DROSS);
    }

    public static ArrayList<RegistryObject<Item>> EQUIPMENT = new ArrayList<>();
    static {
        EQUIPMENT.add(GRAY_IRON_HELMET);
        EQUIPMENT.add(GRAY_IRON_CHESTPLATE);
        EQUIPMENT.add(GRAY_IRON_LEGGINGS);
        EQUIPMENT.add(GRAY_IRON_BOOTS);
        EQUIPMENT.add(CLOUD_PATTERNED_BRONZE_HELMET);
        EQUIPMENT.add(CLOUD_PATTERNED_BRONZE_CHESTPLATE);
        EQUIPMENT.add(CLOUD_PATTERNED_BRONZE_LEGGINGS);
        EQUIPMENT.add(CLOUD_PATTERNED_BRONZE_BOOTS);
        EQUIPMENT.add(RED_FIRE_HELMET);
        EQUIPMENT.add(RED_FIRE_CHESTPLATE);
        EQUIPMENT.add(RED_FIRE_LEGGINGS);
        EQUIPMENT.add(RED_FIRE_BOOTS);
        EQUIPMENT.add(SUNKEN_SILVER_HELMET);
        EQUIPMENT.add(SUNKEN_SILVER_CHESTPLATE);
        EQUIPMENT.add(SUNKEN_SILVER_LEGGINGS);
        EQUIPMENT.add(SUNKEN_SILVER_BOOTS);
        EQUIPMENT.add(COLD_HEARTED_STEEL_HELMET);
        EQUIPMENT.add(COLD_HEARTED_STEEL_CHESTPLATE);
        EQUIPMENT.add(COLD_HEARTED_STEEL_LEGGINGS);
        EQUIPMENT.add(COLD_HEARTED_STEEL_BOOTS);
        EQUIPMENT.add(RINSEI_HELMET);
        EQUIPMENT.add(RINSEI_CHESTPLATE);
        EQUIPMENT.add(RINSEI_LEGGINGS);
        EQUIPMENT.add(RINSEI_BOOTS);
    }

    public static ArrayList<RegistryObject<Item>> TOOL = new ArrayList<>();
    static {
        TOOL.add(GRAY_IRON_SWORD);
        TOOL.add(GRAY_IRON_PICKAXE);
        TOOL.add(GRAY_IRON_AXE);
        TOOL.add(GRAY_IRON_SHOVEL);
        TOOL.add(GRAY_IRON_HOE);
        TOOL.add(CLOUD_PATTERNED_BRONZE_SWORD);
        TOOL.add(CLOUD_PATTERNED_BRONZE_PICKAXE);
        TOOL.add(CLOUD_PATTERNED_BRONZE_AXE);
        TOOL.add(CLOUD_PATTERNED_BRONZE_SHOVEL);
        TOOL.add(CLOUD_PATTERNED_BRONZE_HOE);
        TOOL.add(RED_FIRE_SWORD);
        TOOL.add(RED_FIRE_PICKAXE);
        TOOL.add(RED_FIRE_AXE);
        TOOL.add(RED_FIRE_SHOVEL);
        TOOL.add(RED_FIRE_HOE);
        TOOL.add(SUNKEN_SILVER_SWORD);
        TOOL.add(SUNKEN_SILVER_PICKAXE);
        TOOL.add(SUNKEN_SILVER_AXE);
        TOOL.add(SUNKEN_SILVER_SHOVEL);
        TOOL.add(SUNKEN_SILVER_HOE);
        TOOL.add(COLD_HEARTED_STEEL_SWORD);
        TOOL.add(COLD_HEARTED_STEEL_PICKAXE);
        TOOL.add(COLD_HEARTED_STEEL_AXE);
        TOOL.add(COLD_HEARTED_STEEL_SHOVEL);
        TOOL.add(COLD_HEARTED_STEEL_HOE);
        TOOL.add(RINSEI_SWORD);
        TOOL.add(RINSEI_PICKAXE);
        TOOL.add(RINSEI_AXE);
        TOOL.add(RINSEI_SHOVEL);
        TOOL.add(RINSEI_HOE);
    }



}
