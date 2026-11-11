package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 面板顶部那排页签按钮的<b>客户端绘制/构建</b>部分。
 *
 * <h3>为什么要跟 {@link AttributeTabs} 分开</h3>
 * <p>{@code PacketSyncPage#openPage} 在<b>服务端</b>执行，而它需要查页签表
 * （{@link AttributeTabs#ensureRegistered()} / {@link AttributeTabs#byId(int)}）。
 * 这些代码原来和 {@code Screen}/{@code GuiGraphics}/{@code KunlunTabButton} 混在同一个类里，
 * 于是专用服务器一开面板就要加载客户端类 → {@code NoClassDefFoundError} / dist 校验崩服。</p>
 *
 * <p>现在：{@link AttributeTabs} 只留纯数据（页签表 + 常量 + 菜单 Provider，服务端可用），
 * 本类只留客户端构建，且带 {@link OnlyIn @OnlyIn(Dist.CLIENT)}。</p>
 *
 * <p>⚠️ 新增页签请加到 {@link AttributeTabs#ensureRegistered()}，不要写到这里。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class AttributeTabBar {

    private AttributeTabBar() {
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
     * @param host          目标 Screen（实现 {@link Host}）
     * @param currentPageId 当前面板自己的 id（决定哪个页签高亮）
     */
    public static void buildTabs(Host host, int currentPageId) {
        AttributeTabs.ensureRegistered();
        requestGodDataSync();
        buildTabs(host, host.leftPos() + AttributeTabs.MARGIN_X,
                host.topPos() + AttributeTabs.OFFSET_Y, currentPageId);
    }

    /**
     * ⭐ 打开面板时向服务端要一次神考数据（幂等、零副作用）。
     *
     * <p><b>修的 bug</b>：神考面板"消失，必须重启游戏才恢复"。
     * 客户端 {@code GodClientData} 是静态字段，只要某次服务端发包时机不对
     * （登出 / 换维度 / 发包早于客户端 ready），它就会卡在错误的旧值上，
     * 而客户端<b>从不主动要</b> → UI 一直错，只有重登才恢复。</p>
     *
     * <p>这里在每次打开面板时对账一次，把状态拉回与服务端一致。
     * <b>不在包里判断"玩家有没有神考"</b> —— 那样又回到"用有问题的字段做判断"的死循环。</p>
     */
    private static void requestGodDataSync() {
        try {
            NetworkHandler.INSTANCE.sendToServer(
                    new com.TovidY.kunluncontinent.network.client.C2SRequestGodSync());
        } catch (Throwable ignored) {
            // 单机/无网络通道时忽略即可（数据本来就在本地）
        }
    }

    /**
     * 在指定起点生成整排页签按钮（自定义面板可用这个覆盖位置）。
     *
     * @param startX 起始 X（面板坐标）
     * @param startY 起始 Y（面板坐标）
     */
    public static void buildTabs(Host host, int startX, int startY, int currentPageId) {
        AttributeTabs.ensureRegistered();
        int currentX = startX;
        for (AttributeTabs.Tab tab : AttributeTabs.TABS) {
            if (!tab.isVisible()) continue;
            boolean selected = tab.id() == currentPageId;
            int size = selected ? AttributeTabs.SELECTED_SIZE : AttributeTabs.NORMAL_SIZE;
            int y = startY - (selected ? AttributeTabs.SELECTED_LIFT : 0);
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
            currentX += size + AttributeTabs.SPACING;
        }

        // 动态标题页签（当前只有神考/神祇面板）
        for (AttributeTabs.DynamicTab tab : AttributeTabs.DYNAMIC_TABS) {
            if (!tab.isVisible()) continue;
            boolean selected = tab.id() == currentPageId;
            int size = selected ? AttributeTabs.SELECTED_SIZE : AttributeTabs.NORMAL_SIZE;
            int y = startY - (selected ? AttributeTabs.SELECTED_LIFT : 0);
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
            currentX += size + AttributeTabs.SPACING;
        }
    }

    /**
     * 右上角"配置"按钮（原来只有属性面板有，现统一提供）。
     *
     * @param host 目标 Screen
     */
    public static void buildConfigButton(Host host) {
        int btnSize = AttributeTabs.CONFIG_BTN_SIZE;
        int btnX = host.leftPos() + host.imageWidth() - btnSize - AttributeTabs.CONFIG_BTN_MARGIN;
        int btnY = host.topPos() + AttributeTabs.CONFIG_BTN_TOP;
        host.addWidget(new KunlunGuiHelper.HandDrawnButton(
                host.font(), btnX, btnY, btnSize, btnSize,
                Component.literal("⚙"),
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(AttributeTabs.PAGE_CONFIG)),
                () -> List.of(Component.literal("打开配置界面"))
        ));
    }
}
