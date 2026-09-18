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

import java.util.function.Supplier;
import java.util.stream.Stream;

//创造物品栏
public class ModCreativeModelTab {
    public static final DeferredRegister<CreativeModeTab> KUNLUN_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KlMain.MOD_ID);

    private static Supplier<ItemStack> ICON_SUPPLIER;

    public static final RegistryObject<CreativeModeTab> KUNLUN_TAB =
            KUNLUN_TABS.register("kunlun_tab", () -> CreativeModeTab.builder()
                    .icon(() -> {
                        if (ICON_SUPPLIER == null) {
                            ICON_SUPPLIER = () -> new ItemStack(ModBlocks.CULTIVATION_PLATFORM.get());
                        }
                        return ICON_SUPPLIER.get();
                    })
                    .title(Component.translatable("itemGroup.kunlun_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        Stream.of(
                                        ModItems.DEBUG_ITEM_BLOCK.stream(),

                                        Stream.of(ModItems.POHUNQIANG, ModItems.BAHUANGJI),

                                        ModItems.WUHUNGUOSHI.stream(),
                                        ModItems.COINLIST.stream(),
                                        ModItems.BAGLIST.stream(),
                                        ModItems.CAOYAOLIST.stream(),
                                        ModItems.SEEDSLIST.stream(),

                                        Stream.of(ModItems.INSTANT_KILL_SWORD,
                                                ModBlocks.POLAR_ICE_PORTAL_BLOCK,
                                                ModBlocks.THUNDER_REALM_PORTAL_BLOCK,
                                                ModItems.RED_SPIDER_LILY_POTION,
                                                ModItems.STRONG_POTION,
                                                ModBlocks.STONE_STAMP,
                                                ModBlocks.SUMMON_TOWER),

                                        ModItems.PUTONGITEM.stream(),
                                        ModItems.NORMALITEMSLIST.stream(),
                                        ModBlocks.MODBLOCKS.stream(),
                                        Stream.of(ModBlocks.UNDERWATER_ALTAR),
                                        ModItems.HUNGULIST.stream(),
                                        ModItems.JUHUNPING.stream(),
                                        ModItems.DUANZAOMOBAN.stream(),
                                        ModItems.hunhuanstorage.stream(),
                                        ModItems.COLDPROTECTIONLIST.stream(),
                                        ModItems.ENGRAVING_KNIFE.stream(),
                                        ModItems.HEXIN.stream(),
                                        ModItems.HUNHUAN_STORAGE_CORE.stream(),
                                        ModItems.NEIDANLIST.stream(),
                                        ModBlocks.LIANDANLULIST.stream(),
                                        ModItems.DANYAOITEM.stream(),
                                        ModBlocks.MODORE.stream(),
                                        ModItems.MODSTONE.stream(),
                                        ModItems.EQUIPMENT.stream(),
                                        ModItems.TOOL.stream(),
                                        ModBlocks.BLOCKLIST.stream(),
                                        ModItems.SPAWNEGGLIST.stream()

                                )
                                .flatMap(stream -> stream)
                                .map(RegistryObject::get)
                                .forEach(pOutput::accept);
                    }).build());

    public static void register(IEventBus eventBus) {
        KUNLUN_TABS.register(eventBus);
    }
}