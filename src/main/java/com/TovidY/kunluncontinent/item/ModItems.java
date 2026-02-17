package com.TovidY.kunluncontinent.item;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.armor.ModArmorBaseItem;
import com.TovidY.kunluncontinent.item.armor.ModArmorMaterials;
import com.TovidY.kunluncontinent.item.klitem.DanYaoItem;
import com.TovidY.kunluncontinent.item.klitem.EngravingKnifeItem;
import com.TovidY.kunluncontinent.item.klitem.HunHuanStorageItem;
import com.TovidY.kunluncontinent.item.neidanitems.NeidanItem;
import com.TovidY.kunluncontinent.item.testitemblock.TestLevelUp;
import com.TovidY.kunluncontinent.item.tool.ModSwordBaseItem;
import com.TovidY.kunluncontinent.item.tool.ModToolTiers;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import java.util.ArrayList;

//物品注册
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, KlMain.MOD_ID);
    //测试物品
    public static final RegistryObject<Item> DANYAO_TEST = ITEMS.register("danyao_test",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingyan(999999).setMinLevel(999));
    public static final RegistryObject<Item> DANYAO_JINGSHENLI = ITEMS.register("danyao_jingshenli",()->new DanYaoItem(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().build())).setJingshenlibaifenbi(100).setMinLevel(999));
    public static final RegistryObject<Item> TEST_SWORD = ITEMS.register("test_sword",()->new ModSwordBaseItem(ModToolTiers.TEST_ITEM,3,-1.8F,new Item.Properties()));
    public static final RegistryObject<Item> DANYAO_DENGJI_JIA = ITEMS.register("danyao_dengji_jia",
            () -> new TestLevelUp(new Item.Properties().stacksTo(1), 1));
    public static final RegistryObject<Item> DANYAO_DENGJI_JIAN = ITEMS.register("danyao_dengji_jian",
            () -> new TestLevelUp(new Item.Properties().stacksTo(1), -1));

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

    //矿石宝石
    public static final RegistryObject<Item> GRAY_IRON_INGOT = ITEMS.register("gray_iron_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_INGOT = ITEMS.register("cloud_patterned_bronze_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_INGOT = ITEMS.register("red_fire_ingot",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER = ITEMS.register("sunken_silver",()->new Item(new Item.Properties()));

    public static final RegistryObject<Item> RUBY = ITEMS.register("ruby",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> AMETHYST = ITEMS.register("amethyst",()->new Item(new Item.Properties()));

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

    //工具
    public static final RegistryObject<Item> GRAY_IRON_SWORD = ITEMS.register("gray_iron_sword",()->new ModSwordBaseItem(ModToolTiers.GRAY_IRON,3,-1.8F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_PICKAXE = ITEMS.register("gray_iron_pickaxe",()->new PickaxeItem(ModToolTiers.GRAY_IRON,1, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_AXE = ITEMS.register("gray_iron_axe",()->new AxeItem(ModToolTiers.GRAY_IRON,8.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_SHOVEL = ITEMS.register("gray_iron_shovel",()->new ShovelItem(ModToolTiers.GRAY_IRON,1.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> GRAY_IRON_HOE = ITEMS.register("gray_iron_hoe",()->new HoeItem(ModToolTiers.GRAY_IRON,-2, 0.5F,new Item.Properties()));

    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_SWORD = ITEMS.register("cloud_patterned_bronze_sword",()->new ModSwordBaseItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,5,-1.3F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_PICKAXE = ITEMS.register("cloud_patterned_bronze_pickaxe",()->new PickaxeItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,3, -1.5F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_AXE = ITEMS.register("cloud_patterned_bronze_axe",()->new AxeItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,10.0F, -1.9F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_SHOVEL = ITEMS.register("cloud_patterned_bronze_shovel",()->new ShovelItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,2.8F, -0.8F,new Item.Properties()));
    public static final RegistryObject<Item> CLOUD_PATTERNED_BRONZE_HOE = ITEMS.register("cloud_patterned_bronze_hoe",()->new HoeItem(ModToolTiers.CLOUD_PATTERNED_BRONZE,-1, 1.0F,new Item.Properties()));

    public static final RegistryObject<Item> RED_FIRE_SWORD = ITEMS.register("red_fire_sword",()->new ModSwordBaseItem(ModToolTiers.RED_FIRE,6,-1.0F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_PICKAXE = ITEMS.register("red_fire_pickaxe",()->new PickaxeItem(ModToolTiers.RED_FIRE,2, -1.1F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_AXE = ITEMS.register("red_fire_axe",()->new AxeItem(ModToolTiers.RED_FIRE,12.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_SHOVEL = ITEMS.register("red_fire_shovel",()->new ShovelItem(ModToolTiers.RED_FIRE,2.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> RED_FIRE_HOE = ITEMS.register("red_fire_hoe",()->new HoeItem(ModToolTiers.RED_FIRE,-1, 1.0F,new Item.Properties()));

    public static final RegistryObject<Item> SUNKEN_SILVER_SWORD = ITEMS.register("sunken_silver_sword",()->new ModSwordBaseItem(ModToolTiers.SUNKEN_SILVER,4,-1.5F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_PICKAXE = ITEMS.register("sunken_silver_pickaxe",()->new PickaxeItem(ModToolTiers.SUNKEN_SILVER,1, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_AXE = ITEMS.register("sunken_silver_axe",()->new AxeItem(ModToolTiers.SUNKEN_SILVER,8.0F, -3.0F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_SHOVEL = ITEMS.register("sunken_silver_shovel",()->new ShovelItem(ModToolTiers.SUNKEN_SILVER,1.6F, -2.0F,new Item.Properties()));
    public static final RegistryObject<Item> SUNKEN_SILVER_HOE = ITEMS.register("sunken_silver_hoe",()->new HoeItem(ModToolTiers.SUNKEN_SILVER,-2, 0.5F,new Item.Properties()));

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

    public static final RegistryObject <Item> DROSS = ITEMS.register("dross",()->new Item(new Item.Properties()));

    //刻刀类
    public static final RegistryObject<Item> IRON_ENGRAVING_KNIFE = ITEMS.register("iron_engravings_knife",()->new EngravingKnifeItem(new Item.Properties().durability(30)));
    public static final RegistryObject<Item> DIAMOND_ENGRAVING_KNIFE = ITEMS.register("diamond_engravings_knife",()->new EngravingKnifeItem(new Item.Properties().durability(100)));

    //丹炉核心
    public static final RegistryObject<Item> LOW_LEVEL_HEXIN = ITEMS.register("low_level_hexin",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> MIDDLE_LEVEL_HEXIN = ITEMS.register("middle_level_hexin",()->new Item(new Item.Properties()));
    public static final RegistryObject<Item> HIGH_LEVEL_HEXIN = ITEMS.register("high_level_hexin",()->new Item(new Item.Properties()));


    // 魂环储存器
    public static final RegistryObject<Item> HUNHUAN_STORAGE_ONE = ITEMS.register("hunhuan_storage_one",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 6666));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_TWO = ITEMS.register("hunhuan_storage_two",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 50000));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_THREE = ITEMS.register("hunhuan_storage_three",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 900000));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_FOUR = ITEMS.register("hunhuan_storage_four",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 3000000));

    public static final RegistryObject<Item> HUNHUAN_STORAGE_FIVE = ITEMS.register("hunhuan_storage_five",
            () -> new HunHuanStorageItem(new Item.Properties().stacksTo(1).fireResistant(), 1, 20000000));


    public static ArrayList<RegistryObject<Item>> HEXIN = new ArrayList<>();
    static {
        HEXIN.add(LOW_LEVEL_HEXIN);
        HEXIN.add(MIDDLE_LEVEL_HEXIN);
        HEXIN.add(HIGH_LEVEL_HEXIN);
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
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static ArrayList<RegistryObject<Item>> MODSTONE = new ArrayList<>();
    static {
        MODSTONE.add(GRAY_IRON_INGOT);
        MODSTONE.add(CLOUD_PATTERNED_BRONZE_INGOT);
        MODSTONE.add(RED_FIRE_INGOT);
        MODSTONE.add(SUNKEN_SILVER);

        MODSTONE.add(RUBY);
        MODSTONE.add(AMETHYST);
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
    }



}
