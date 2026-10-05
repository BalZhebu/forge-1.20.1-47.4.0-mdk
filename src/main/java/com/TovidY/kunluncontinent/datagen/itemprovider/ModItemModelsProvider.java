package com.TovidY.kunluncontinent.datagen.itemprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output,  ExistingFileHelper existingFileHelper) {
        super(output, KlMain.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        //测试物品
        ResourceLocation sharedTexture = new ResourceLocation(KlMain.MOD_ID, "item/ceshi");
        for (RegistryObject<Item> itemRegistry : ModItems.DEBUG_ITEM_BLOCK) {
            Item item = itemRegistry.get();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            withExistingParent(id.getPath(),new ResourceLocation("item/generated"))
                    .texture("layer0", sharedTexture);
        }

        handheldItem(ModItems.INSTANT_KILL_SWORD.get());

        //神界树苗：物品模型直接引用方块模型（原版树苗也是这样）
        withExistingParent("divine_realm_sapling",
                new ResourceLocation(KlMain.MOD_ID, "block/divine_realm_sapling"));

        // ⭐⭐ 神界树叶：**必须单独注册物品模型**，否则手持/掉落形态是紫黑缺失模型。
        // 原因：ModBlockStateProvider 里树叶为了覆盖 28 种状态（distance/persistent/waterlogged）
        // 改用了 getVariantBuilder + partialState 循环，**丢掉了 simpleBlockWithItem
        // 附带的"自动生成 item/xxx.json"这一步**。方块正常但物品紫黑就是这个原因。
        // 原版树叶的物品模型同样是直接引用方块模型。
        withExistingParent("divine_realm_leaves",
                new ResourceLocation(KlMain.MOD_ID, "block/divine_realm_leaves"));

        //药水
        basicItem(ModItems.RED_SPIDER_LILY_POTION.get());
        basicItem(ModItems.STRONG_POTION.get());

        //打火石类似
        for (RegistryObject<Item> itemRegistry : ModItems.PUTONGITEM){
            basicItem(itemRegistry.get());
        }

        //草药
        for (RegistryObject<Item> itemRegistry : ModItems.CAOYAOLIST){
            basicItem(itemRegistry.get());
        }

        //种子
        for (RegistryObject<Item> itemRegistry : ModItems.SEEDSLIST){
            basicItem(itemRegistry.get());
        }

        //普通物品
        for (RegistryObject<Item> itemRegistry : ModItems.NORMALITEMSLIST){
            basicItem(itemRegistry.get());
        }

        //武魂果实
        for (RegistryObject<Item> itemRegistry : ModItems.WUHUNGUOSHI){
            basicItem(itemRegistry.get());
        }

        //锻造模版
        for (RegistryObject<Item> itemRegistry : ModItems.DUANZAOMOBAN){
            basicItem(itemRegistry.get());
        }

        //刻刀类
        for (RegistryObject<Item> itemRegistry : ModItems.ENGRAVING_KNIFE){
            basicItem(itemRegistry.get());
        }

        //魂技类
        for (RegistryObject<? extends Item> itemRegistry : ModItems.HUNJILIST) {
            basicItem(itemRegistry.get());
        }

        //变体魂技：图标直接复用同槽位既有魂技的贴图 —— 一个物品图标对应多条魂技，不新增美术资源
        for (ModItems.SkillVariantEntry entry : ModItems.SKILL_VARIANTS) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(entry.skill().get());
            ResourceLocation source = ForgeRegistries.ITEMS.getKey(entry.iconSource().get());
            if (id == null || source == null) {
                throw new IllegalArgumentException("Skill variant not registered: " + entry);
            }
            withExistingParent(id.getPath(), new ResourceLocation("item/generated"))
                    .texture("layer0", new ResourceLocation(KlMain.MOD_ID, "item/" + source.getPath()));
        }

        //按钮
        for (RegistryObject<Item> itemRegistry : ModItems.KLBUTTON) {
            basicItem(itemRegistry.get());
        }

        //魂骨类
        for (RegistryObject<Item> itemRegistry : ModItems.HUNGULIST){
            basicItem(itemRegistry.get());
        }

        //魂币
        for (RegistryObject<Item> itemRegistryObject : ModItems.COINLIST){
            basicItem(itemRegistryObject.get());
        }

        //钱包
        for (RegistryObject<Item> itemRegistry : ModItems.BAGLIST){
            basicItem(itemRegistry.get());
        }

        //核心类
        for (RegistryObject<Item> itemRegistry : ModItems.HEXIN){
            basicItem(itemRegistry.get());
        }

        //御寒魂导器
        for (RegistryObject<Item> itemRegistry : ModItems.COLDPROTECTIONLIST){
            basicItem(itemRegistry.get());
        }

        //魂环核心类
        for (RegistryObject<Item> itemRegistry : ModItems.HUNHUAN_STORAGE_CORE){
            basicItem(itemRegistry.get());
        }

        // 矿石和锭（使用 basicItem）
        for (RegistryObject<Item> itemRegistry : ModItems.MODSTONE){
            basicItem(itemRegistry.get());
        }

        //内丹
        for (RegistryObject<Item> itemRegistry : ModItems.NEIDANLIST){
            basicItem(itemRegistry.get());
        }

        //丹药(使用 basicItem)
        for (RegistryObject<Item> itemRegistry : ModItems.DANYAOITEM){
            basicItem(itemRegistry.get());
        }

        //魂环收纳器
        for (RegistryObject<Item> itemRegistry : ModItems.hunhuanstorage){
            basicItem(itemRegistry.get());
        }

        // 装备（盔甲使用 basicItem）
        for (RegistryObject<Item> itemRegistry : ModItems.EQUIPMENT){
            basicItem(itemRegistry.get());
        }

        // 工具（剑、镐、斧、锄、铲使用 handheld 父模型）
        for (RegistryObject<Item> itemRegistry : ModItems.TOOL){
            handheldItem(itemRegistry.get());
        }

        //生物蛋
        for (RegistryObject<Item> itemRegistry : ModItems.SPAWNEGGLIST){
            spawnEggItem(itemRegistry.get());
        }

    }

    /**
     * 生成手持工具模型（使用 item/handheld 作为父模型）
     */
    private ItemModelBuilder handheldItem(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            throw new IllegalArgumentException("Item not registered: " + item);
        }

        return withExistingParent(id.getPath(),
                new ResourceLocation("item/handheld"))
                .texture("layer0",
                        new ResourceLocation(KlMain.MOD_ID, "item/" + id.getPath()));
    }

    private void spawnEggItem(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        withExistingParent(id.getPath(),new ResourceLocation("item/template_spawn_egg"));
    }

}
