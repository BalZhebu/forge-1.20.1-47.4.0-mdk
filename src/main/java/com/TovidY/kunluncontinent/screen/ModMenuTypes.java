package com.TovidY.kunluncontinent.screen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.screen.attribute.AttributeMenu;
import com.TovidY.kunluncontinent.screen.attribute.AttributeScreen;
import com.TovidY.kunluncontinent.screen.attribute.config.ConfigScreen;
import com.TovidY.kunluncontinent.screen.attribute.hungu.HunguMenu;
import com.TovidY.kunluncontinent.screen.attribute.hungu.HunguScreen;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanScreen;
import com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoMenu;
import com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoScreen;
import com.TovidY.kunluncontinent.screen.guide.GuidBookScreen;
import com.TovidY.kunluncontinent.screen.guide.GuideBookMenu;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluMenu;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluScreen;
import com.TovidY.kunluncontinent.screen.spiritgatheringaltar.SpiritGatheringaltarMenu;
import com.TovidY.kunluncontinent.screen.spiritgatheringaltar.SpiritGatheringaltarScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

//该类用于写入menu
//面板注册

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, KlMain.MOD_ID);

    //常量字段
    public static final RegistryObject<MenuType<AttributeMenu>> ATTRUBUTE_MENU =
            registerMenuType("attrubute_menu", AttributeMenu::new);

    public static final RegistryObject<MenuType<LiandanluMenu>> LIANDANLU_MENU =
            registerMenuType("liandanlu_menu", LiandanluMenu::new);

    public static final RegistryObject<MenuType<HunguMenu>> HUNGU_MENU =
            registerMenuType("hungu_menu", HunguMenu::new);

    public static final RegistryObject<MenuType<HunhuanMenu>> HUNHUAN_MENU =
            registerMenuType("hunhuan_menu", HunhuanMenu::new);

    public static final RegistryObject<MenuType<ShenkaoMenu>> SHENKAO_MENU =
            registerMenuType("shenkao_menu", ShenkaoMenu::new);

    //配置代码
    public static final RegistryObject<MenuType<HunhuanMenu>> CONFIG_MENU =
            registerMenuType("config_menu", HunhuanMenu::new);

    //引导书
    public static final RegistryObject<MenuType<GuideBookMenu>> GUIDE_BOOK_MENU =
            registerMenuType("guide_book_menu", GuideBookMenu::new);

    public static final RegistryObject<MenuType<SpiritGatheringaltarMenu>> SPIRITGATHERING_MENU =
            registerMenuType("spiritgathering_menu",SpiritGatheringaltarMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.ATTRUBUTE_MENU.get(), AttributeScreen::new);
            MenuScreens.register(ModMenuTypes.LIANDANLU_MENU.get(), LiandanluScreen::new);
            MenuScreens.register(ModMenuTypes.HUNGU_MENU.get(), HunguScreen::new);
            MenuScreens.register(ModMenuTypes.GUIDE_BOOK_MENU.get(), GuidBookScreen::new);
            MenuScreens.register(ModMenuTypes.HUNHUAN_MENU.get(), HunhuanScreen::new);
            MenuScreens.register(ModMenuTypes.SHENKAO_MENU.get(), ShenkaoScreen::new);

            MenuScreens.register(ModMenuTypes.CONFIG_MENU.get(), ConfigScreen::new);
            MenuScreens.register(ModMenuTypes.SPIRITGATHERING_MENU.get(), SpiritGatheringaltarScreen::new);

        }
    }

}
