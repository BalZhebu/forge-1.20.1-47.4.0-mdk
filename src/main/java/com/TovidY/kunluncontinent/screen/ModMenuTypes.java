package com.TovidY.kunluncontinent.screen;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.screen.attribute.AttributeMenu;
import com.TovidY.kunluncontinent.screen.attribute.AttributeScreen;
import com.TovidY.kunluncontinent.screen.attribute.config.ConfigScreen;
import com.TovidY.kunluncontinent.screen.attribute.hungu.HunguMenu;
import com.TovidY.kunluncontinent.screen.attribute.hungu.HunguScreen;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanScreen;
import com.TovidY.kunluncontinent.screen.attribute.point.PointMenu;
import com.TovidY.kunluncontinent.screen.attribute.point.PointScreen;
import com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoMenu;
import com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoScreen;
import com.TovidY.kunluncontinent.screen.bank.BankMenu;
import com.TovidY.kunluncontinent.screen.bank.BankScreen;
import com.TovidY.kunluncontinent.screen.bank.BankStorageScreen;
import com.TovidY.kunluncontinent.screen.bank.BankTabMenu;
import com.TovidY.kunluncontinent.screen.guide.GuidBookScreen;
import com.TovidY.kunluncontinent.screen.guide.GuideBookMenu;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluMenu;
import com.TovidY.kunluncontinent.screen.liandanlugui.LiandanluScreen;
import com.TovidY.kunluncontinent.screen.spiritgatheringaltar.SpiritGatheringaltarMenu;
import com.TovidY.kunluncontinent.screen.spiritgatheringaltar.SpiritGatheringaltarScreen;
import com.TovidY.kunluncontinent.screen.playernpc.shoumai.SellMenu;
import com.TovidY.kunluncontinent.screen.playernpc.shoumai.SellScreen;
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

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, KlMain.MOD_ID);

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

    public static final RegistryObject<MenuType<PointMenu>> POINT_MENU =
            registerMenuType("point_menu", PointMenu::new);

    public static final RegistryObject<MenuType<HunhuanMenu>> CONFIG_MENU =
            registerMenuType("config_menu", HunhuanMenu::new);

    public static final RegistryObject<MenuType<GuideBookMenu>> GUIDE_BOOK_MENU =
            registerMenuType("guide_book_menu", GuideBookMenu::new);

    public static final RegistryObject<MenuType<SpiritGatheringaltarMenu>> SPIRITGATHERING_MENU =
            registerMenuType("spiritgathering_menu", SpiritGatheringaltarMenu::new);

    public static final RegistryObject<MenuType<SellMenu>> SELL_MENU =
            registerMenuType("sell_menu", SellMenu::new);

    /** 昆仑银行：无槽位面板，余额存在玩家 capability 里。 */
    public static final RegistryObject<MenuType<BankMenu>> BANK_MENU =
            registerMenuType("bank_menu", BankMenu::new);

    /**
     * 银行的<b>页签版</b>（属性面板里的「银行」页签）。
     *
     * <p>和 {@link #BANK_MENU}（方块右键）是<b>两套独立实现</b>：容器类不同、按钮 id 不同、
     * 槽位坐标不同，只是恰好长得像。页签版永不绑方块 → 只能取钱，界面走 190 高的
     * {@code BankStorageScreen}。</p>
     */
    public static final RegistryObject<MenuType<BankTabMenu>> BANK_TAB_MENU =
            registerMenuType("bank_tab_menu", BankTabMenu::create);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> net.minecraftforge.common.extensions.IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ATTRUBUTE_MENU.get(), AttributeScreen::new);
            MenuScreens.register(LIANDANLU_MENU.get(), LiandanluScreen::new);
            MenuScreens.register(HUNGU_MENU.get(), HunguScreen::new);
            MenuScreens.register(GUIDE_BOOK_MENU.get(), GuidBookScreen::new);
            MenuScreens.register(HUNHUAN_MENU.get(), HunhuanScreen::new);
            MenuScreens.register(SHENKAO_MENU.get(), ShenkaoScreen::new);
            MenuScreens.register(POINT_MENU.get(), PointScreen::new);
            MenuScreens.register(CONFIG_MENU.get(), ConfigScreen::new);
            MenuScreens.register(SPIRITGATHERING_MENU.get(), SpiritGatheringaltarScreen::new);
            MenuScreens.register(SELL_MENU.get(), SellScreen::new);
            MenuScreens.register(BANK_MENU.get(), BankScreen::new);
            MenuScreens.register(BANK_TAB_MENU.get(), BankStorageScreen::new);
        }
    }
}
