package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import com.TovidY.kunluncontinent.screen.attribute.AttributeMenu;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * 面板切换请求（客户端 → 服务端）：按页签 id 打开对应面板。
 *
 * <p>页签 id → MenuProvider 的映射在
 * {@link com.TovidY.kunluncontinent.screen.attribute.AttributeTabs} 注册表里，
 * 本类只负责按 id 查表并 {@link NetworkHooks#openScreen}，<b>没有 if-else 分支</b> ——
 * 新增面板不用再改这里。</p>
 */
public class PacketSyncPage {
    private final int pageIndex;

    public PacketSyncPage(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public static void encode(PacketSyncPage msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.pageIndex);
    }

    public static PacketSyncPage decode(FriendlyByteBuf buffer) {
        return new PacketSyncPage(buffer.readInt());
    }

    public static void handle(PacketSyncPage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            openPage(player, msg.pageIndex);
        });
        ctx.get().setPacketHandled(true);
    }

    /** 按页签 id 打开面板。抽成 public 便于其它地方直接调用（如命令/指令）。 */
    public static void openPage(ServerPlayer player, int pageIndex) {
        // 配置面板特殊：复用 AttributeMenu 但换 MenuType，让 MenuScreens 路由到 ConfigScreen
        if (pageIndex == AttributeTabs.PAGE_CONFIG) {
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("配置选项");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    return new AttributeMenu(id, inv) {
                        @Override
                        public MenuType<?> getType() {
                            return ModMenuTypes.CONFIG_MENU.get();
                        }
                    };
                }
            });
            return;
        }

        AttributeTabs.Tab tab = AttributeTabs.byId(pageIndex);
        if (tab == null) return;
        // 页签有可见性条件时同样拦截（例如未成神不许开神考面板）
        if (!tab.isVisible()) return;
        NetworkHooks.openScreen(player, tab.provider());
    }
}
