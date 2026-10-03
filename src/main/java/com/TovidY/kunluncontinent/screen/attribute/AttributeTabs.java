package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class AttributeTabs {

    private AttributeTabs() {
    }

    // ==================== 尺寸常量（原先 4 个 Screen 各抄一份） ====================

    /** 未选中页签边长。 */
    public static final int NORMAL_SIZE = 24;
    /** 选中页签边长（略大，视觉上"凸起"）。 */
    public static final int SELECTED_SIZE = 28;
    /** 页签间距。 */
    public static final int SPACING = 5;
    /** 页签行相对面板左上角的偏移。 */
    public static final int MARGIN_X = 10;
    public static final int OFFSET_Y = -26;
    /** 选中态额外上抬的像素。 */
    private static final int SELECTED_LIFT = 2;
    /** 页签右对齐时用的按钮尺寸（配置按钮）。 */
    public static final int CONFIG_BTN_SIZE = 18;
    /** 配置按钮右边缘距面板右边距。 */
    public static final int CONFIG_BTN_MARGIN = 10;
    /** 配置按钮距面板顶。 */
    public static final int CONFIG_BTN_TOP = 8;

    /**
     * 一个页签的完整定义。
     *
     * @param id        页签 id（同时是 {@link PacketSyncPage} 的 pageIndex）
     * @param label     页签名称（tooltip）
     * @param icon      页签图标
     * @param provider  服务端打开该面板用的 MenuProvider
     * @param visible   是否显示（默认恒显示；神考那种要判断成神才给的走这个）
     */

    public record Tab(int id,
                      String label,
                      Supplier<ItemStack> icon,
                      MenuProvider provider,
                      BooleanSupplier visible) {

        public Component title() {
            return Component.literal(label);
        }

        public boolean isVisible() {
            return visible == null || visible.getAsBoolean();
        }
    }

    /** 全部页签，按显示顺序排列。**新增面板在这里加一行即可。** */

    private static final List<Tab> TABS = new ArrayList<>();

    /**
     * 注册一个页签。
     *
     * @param id       pageIndex（0 起，不能与已有重复）
     * @param label    页签名
     * @param icon     页签图标
     * @param provider 服务端 MenuProvider（各面板的 {@code *Menu.Provider}）
     * @param visible  显示条件（null = 恒显示）
     */
    public static void tab(int id, String label, Supplier<ItemStack> icon,
                           MenuProvider provider, @Nullable BooleanSupplier visible) {
        TABS.add(new Tab(id, label, icon, provider, visible));
    }

    /** 已注册的页签数。 */
    public static int count() {
        return TABS.size();
    }

    /** 按 id 取页签。 */
    public static Tab byId(int id) {
        for (Tab t : TABS) {
            if (t.id() == id) return t;
        }
        return null;
    }

    // ==================== 页签 id 常量 ====================

    public static final int PAGE_ATTRIBUTE = 0;
    public static final int PAGE_HUNGU = 1;
    public static final int PAGE_HUNHUAN = 2;
    public static final int PAGE_SHENKAO = 3;
    public static final int PAGE_CONFIG = 4;
    /** 属性点面板：插在属性面板右侧第二位。 */
    public static final int PAGE_POINT = 5;

    /** 是否已成神（神考页签的显示条件）。 */
    private static boolean isGod() {
        return GodClientData.godName != null && !GodClientData.godName.equals("无");
    }

    /**
     * 注册全部内置页签。由 {@link KlMain} 或首次使用时调用（幂等）。
     *
     * <p>⚠️ 必须在类加载后调用一次。推荐在 {@code ModMenuTypes.ClientModEvents#onClientSetup}
     * 或各 Screen 的静态块里调 {@link #ensureRegistered()}。</p>
     */
    private static boolean registered = false;

    public static synchronized void ensureRegistered() {
        if (registered) return;
        registered = true;

        // 顺序 = 页签显示顺序
        tab(PAGE_ATTRIBUTE, "属性面板",
                () -> new ItemStack(ModItems.ATTRIBUTE_BUTTON.get()),
                new AttributeMenu.Provider(), null);

        tab(PAGE_POINT, "点数面板",
                () -> new ItemStack(ModItems.RESET_SCROLL.get()),
                new com.TovidY.kunluncontinent.screen.attribute.point.PointMenu.Provider(), null);

        tab(PAGE_HUNGU, "魂骨面板",
                () -> new ItemStack(ModItems.SOUL_BONE_BUTTON.get()),
                new com.TovidY.kunluncontinent.screen.attribute.hungu.HunguMenu.Provider(), null);

        tab(PAGE_HUNHUAN, "魂环配置",
                () -> new ItemStack(ModItems.HUNHUAN_BUTTON.get()),
                new com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu.Provider(), null);

        tab(PAGE_SHENKAO, "神考面板",
                () -> new ItemStack(ModItems.SHENKAO_BUTTON.get()),
                new com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoMenu.Provider(),
                AttributeTabs::isGod);
    }
    /**
     * 页签宿主接口 —— 让本工具类能访问 {@code leftPos/topPos/font/imageWidth}
     * 和 {@code addRenderableWidget}。
     *
     * <p>这几个成员在 {@code Screen} / {@code AbstractContainerScreen} 里都是
     * <b>protected</b>，静态工具类无法直接访问，所以由各 Screen 实现本接口暴露出来。</p>
     */
    public interface Host {
        int leftPos();

        int topPos();

        int imageWidth();

        net.minecraft.client.gui.Font font();

        void addWidget(net.minecraft.client.gui.components.AbstractWidget widget);
    }

    /**
     * 在面板顶部生成整排页签按钮。
     *
     * <p>调用前请先 {@code super.init()}（要用 {@code leftPos/topPos/imageWidth}）。</p>
     *
     * @param host         目标 Screen（实现 {@link Host}）
     * @param currentPageId 当前面板自己的 id（决定哪个页签高亮）
     */
    public static void buildTabs(Host host, int currentPageId) {
        ensureRegistered();
        buildTabs(host, host.leftPos() + MARGIN_X, host.topPos() + OFFSET_Y, currentPageId);
    }

    /**
     * 在指定起点生成整排页签按钮（自定义面板可用这个覆盖位置）。
     *
     * @param startX 起始 X（面板坐标）
     * @param startY 起始 Y（面板坐标）
     */
    public static void buildTabs(Host host, int startX, int startY, int currentPageId) {
        ensureRegistered();
        int currentX = startX;
        for (Tab tab : TABS) {
            if (!tab.isVisible()) continue;
            boolean selected = tab.id() == currentPageId;
            int size = selected ? SELECTED_SIZE : NORMAL_SIZE;
            int y = startY - (selected ? SELECTED_LIFT : 0);
            final int id = tab.id();
            host.addWidget(new KunlunGuiHelper.KunlunTabButton(
                    host.font(), currentX, y, size, size,
                    tab.icon().get(), tab.title(), selected,
                    b -> {
                        if (!selected) {
                            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(id));
                        }
                    }
            ));
            currentX += size + SPACING;
        }
    }

    /**
     * 右上角"配置"按钮（原来只有属性面板有，现统一提供）。
     *
     * @param host 目标 Screen
     */
    public static void buildConfigButton(Host host) {
        int btnSize = CONFIG_BTN_SIZE;
        int btnX = host.leftPos() + host.imageWidth() - btnSize - CONFIG_BTN_MARGIN;
        int btnY = host.topPos() + CONFIG_BTN_TOP;
        host.addWidget(new KunlunGuiHelper.HandDrawnButton(
                host.font(), btnX, btnY, btnSize, btnSize,
                Component.literal("⚙"),
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(PAGE_CONFIG)),
                () -> List.of(Component.literal("打开配置界面"))
        ));
    }
}
