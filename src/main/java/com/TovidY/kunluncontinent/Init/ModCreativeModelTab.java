package com.TovidY.kunluncontinent.Init;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.block.ModBlocks;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

//创造物品栏
public class ModCreativeModelTab {
    public static final DeferredRegister<CreativeModeTab> KUNLUN_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KlMain.MOD_ID);

    public static final RegistryObject<CreativeModeTab> KUNLUN_TAB =
            KUNLUN_TABS.register("kunlun_tab" ,()-> CreativeModeTab.builder()
                    .icon(()->new ItemStack(ModBlocks.CULTIVATION_PLATFORM.get()))
                    .title(Component.translatable("itemGroup.kunlun_tab"))
                    .displayItems((pParameters, pOutput) ->{

                        for (RegistryObject<Item> itemRegistryObject : ModItems.DEBUG_ITEM_BLOCK){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Block> itemRegistryObject : ModBlocks.MODBLOCKS){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.hunhuanstorage){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.ENGRAVING_KNIFE){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.HEXIN){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.NEIDANLIST){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Block> itemRegistryObject : ModBlocks.LIANDANLULIST){
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.DANYAOITEM) {
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Block> itemRegistryObject : ModBlocks.MODORE) {
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.MODSTONE) {
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.EQUIPMENT) {
                            pOutput.accept(itemRegistryObject.get());
                        }

                        for (RegistryObject<Item> itemRegistryObject : ModItems.TOOL) {
                            pOutput.accept(itemRegistryObject.get());
                        }

                    }).build());



//    public static final RegistryObject<CreativeModeTab> KUNLUN_BLOCK_TAB =
//            KUNLUN_TABS.register("kunlun_block_tab" ,()-> CreativeModeTab.builder()
//                    .icon(()->new ItemStack(ModItems.EVA.get()))
//                    .title(Component.translatable("itemGroup.kunlun_block_tab"))
//                    .displayItems((pParameters, pOutput) ->{
//                        pOutput.accept(ModItems.AWM.get());
//                    }).withTabsBefore(KUNLUN_TAB.getKey())
//                    .build());

    public static void register(IEventBus eventBus){
        KUNLUN_TABS.register(eventBus);
    }
}
