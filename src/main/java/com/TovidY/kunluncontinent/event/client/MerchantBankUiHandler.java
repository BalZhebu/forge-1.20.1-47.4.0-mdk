package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.bank.BankAccount;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketToggleConfig;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 商人界面的「<b>自动取币</b>」开关（纯客户端渲染 / 点击后发包切 bit）。
 *
 * <h3>位置：面板正下方居中</h3>
 * <p>按钮 {@code x = guiLeft + (xSize - 按钮宽) / 2，y = guiTop + ySize + 3}，即黏在交易面板
 * （276×166）<b>底边外侧正中</b>，离开边框 3px；按钮左侧再贴一个「银行存量」小框
 * （金 / 银 / 铜 当前数量），左边界对齐面板左边缘 —— 合起来正好横跨面板下沿。</p>
 *
 * <p>⚠️ <b>千万别放右侧</b>：JEI / REI 的物品列表就挂在界面右边，会把按钮整块盖住（已踩）。
 * 放底部还有一个好处 —— 原版面板底部之外是纯留白，不会遮住任何元素，也不用 Mixin 改
 * {@code MerchantScreen#init}。</p>
 *
 * <h3>只在「价格用魂币」的商人上显形</h3>
 * <p>开关本身是通用的（原版村民也走同一个 {@code MerchantScreen}），但村民用绿宝石交易，
 * 显示这个按钮没有意义。所以每帧检查 {@code offers} 里有没有魂币价格 —— 没有就 {@code visible = false}。
 * 这样也能自然处理「界面先建、offers 后到」的时序：{@code Init.Post} 时 offers 还是空的，
 * 等 {@code ClientboundMerchantOffersPacket} 到了，下一帧按钮自己就出来了。</p>
 *
 * <p>真正的「补币」发生在服务端，见 {@code bank/BankAutoTrade}。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MerchantBankUiHandler {

    private static final int BTN_W = 64;
    private static final int BTN_H = 16;

    /** 离面板边框留 3px：贴太近像原版的一部分，太远又看不出是从属关系。 */
    private static final int GAP = 3;

    // ---- 按钮左侧的「银行存量」小框 ----
    /** 框与按钮之间的空隙。 */
    private static final int BOX_GAP = 3;
    /** 框高 = 上边距 4 + 三行 × 行距 9 + 余量。 */
    private static final int BOX_H = 32;
    /** 框太窄（窗口被拉变形）就别画了，免得数字糊成一团。 */
    private static final int BOX_MIN_W = 46;
    private static final int BOX_LINE_H = 9;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof MerchantScreen screen)) {
            return;
        }
        int x = screen.getGuiLeft() + (screen.getXSize() - BTN_W) / 2;
        // 屏幕特别矮（GUI 缩放很大）时兜一下，别跑到画面外
        int y = Math.min(screen.getGuiTop() + screen.getYSize() + GAP, screen.height - BTN_H - 2);
        // ⚠️ 不能直接 screen.addRenderableWidget(...)：Screen 里是 protected。
        //    Forge 的 Init.Post#addListener 就是往里加控件，内部走同一个 add 回调。
        event.addListener(new AutoTradeButton(Minecraft.getInstance().font, x, y));
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof MerchantScreen screen)) {
            return;
        }
        AutoTradeButton button = find(screen);
        if (button == null) {
            return;
        }

        boolean relevant = hasCoinOffer(screen.getMenu());
        button.visible = relevant;
        if (!relevant) {
            return;
        }

        // 状态由服务端同步过来的 capability 决定 —— 不在本地乐观翻转，
        // 否则「点两次回原样」的时候客户端和服务端会对不上。
        button.setMessage(enabled()
                ? Component.literal("§a自动取币 §f开")
                : Component.literal("§8自动取币 §7关"));
        renderBankBox(event.getGuiGraphics(), screen, button);
        button.renderTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
    }

    /**
     * 按钮左侧的「银行存量」小框：金 / 银 / 铜 各一行，只写<b>当前数量</b>（不写上限）。
     *
     * <p>框的右边界顶到按钮左边（留 {@link #BOX_GAP}），左边界对齐交易面板左边缘 ——
     * 这样「框 + 按钮」正好横跨面板整个下沿，不会显得偏在一边。</p>
     *
     * <p>数字直接读客户端 capability（余额随 {@code SPacketPlayerAttribute} 同步），
     * 所以面板里看到的就是银行的实时余额，不需要额外发包。</p>
     */
    private static void renderBankBox(GuiGraphics gui, MerchantScreen screen, AutoTradeButton button) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int left = screen.getGuiLeft();
        int right = button.getX() - BOX_GAP;
        int w = right - left;
        if (w < BOX_MIN_W) {
            return;
        }
        // 屏幕太矮时和按钮一起往上收，别掉出画面
        int y = Math.min(button.getY(), screen.height - BOX_H - 2);

        gui.fill(left, y, right, y + BOX_H, 0x33000000);
        gui.renderOutline(left, y, w, BOX_H, 0xFF3A4354);

        Font font = Minecraft.getInstance().font;
        int tx = left + 6;
        int ty = y + 4;
        for (BankAccount.Coin coin : BankAccount.Coin.values()) {
            String name = coin.displayName().substring(0, 1);   // 金 / 银 / 铜
            gui.drawString(font, name, tx, ty, coin.color(), true);
            gui.drawString(font, BankAccount.fmt(BankAccount.getStored(player, coin)),
                    tx + font.width(name) + 4, ty, 0xF0F0F0, true);
            ty += BOX_LINE_H;
        }
    }

    // ==================== 小工具 ====================

    private static AutoTradeButton find(Screen screen) {
        for (var child : screen.children()) {
            if (child instanceof AutoTradeButton button) {
                return button;
            }
        }
        return null;
    }

    /** 这一屏的交易里有没有用魂币结算的价格。 */
    private static boolean hasCoinOffer(MerchantMenu menu) {
        MerchantOffers offers = menu == null ? null : menu.getOffers();
        if (offers == null || offers.isEmpty()) {
            return false;
        }
        for (MerchantOffer offer : offers) {
            if (BankAccount.Coin.byItem(offer.getCostA().getItem()) != null) {
                return true;
            }
            if (BankAccount.Coin.byItem(offer.getCostB().getItem()) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean enabled() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return true;
        }
        PlayerAttributeCapability cap = mc.player
                .getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        return cap == null || cap.isConfigOpen(BankAccount.CONFIG_AUTO_TRADE);
    }

    /** 带类型的按钮，方便 {@link #find} 认出来（不靠 message 猜）。 */
    private static class AutoTradeButton extends KunlunGuiHelper.HandDrawnButton {
        AutoTradeButton(net.minecraft.client.gui.Font font, int x, int y) {
            super(font, x, y, BTN_W, BTN_H,
                    Component.literal("自动取币"),
                    b -> NetworkHandler.INSTANCE.sendToServer(
                            new PacketToggleConfig(BankAccount.CONFIG_AUTO_TRADE)),
                    () -> List.of(
                            Component.literal("§6交易自动取币"),
                            Component.literal("§7选中左侧商品时，若背包魂币不足，"),
                            Component.literal("§7自动从§e昆仑银行§7取出差额放入交易栏。"),
                            Component.literal("§8关闭后将不再调用银行魂币：钱不够就是买不了。"),
                            Component.literal(enabled() ? "§8当前: §a开启" : "§8当前: §c关闭")));
        }
    }
}
