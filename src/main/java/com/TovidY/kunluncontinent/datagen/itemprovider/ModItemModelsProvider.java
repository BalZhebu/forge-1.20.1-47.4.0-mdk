package com.TovidY.kunluncontinent.datagen.itemprovider;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
        ResourceLocation sharedTexture = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "item/ceshi");
        for (RegistryObject<Item> itemRegistry : ModItems.DEBUG_ITEM_BLOCK) {
            Item item = itemRegistry.get();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            withExistingParent(id.getPath(), ResourceLocation.withDefaultNamespace("item/generated"))
                    .texture("layer0", sharedTexture);
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
                ResourceLocation.withDefaultNamespace("item/handheld"))
                .texture("layer0",
                        ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "item/" + id.getPath()));
    }

}
