package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.screen.ModMenuTypes;
import com.TovidY.kunluncontinent.screen.attribute.AttributeMenu;
import com.TovidY.kunluncontinent.screen.attribute.hungu.HunguMenu;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import com.TovidY.kunluncontinent.screen.attribute.shenkao.ShenkaoMenu;
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
            if (player != null) {
                if (msg.pageIndex == 0) {
                    NetworkHooks.openScreen(player, new AttributeMenu.Provider());
                } else if (msg.pageIndex == 1) {
                    NetworkHooks.openScreen(player, new HunguMenu.Provider());
                }else if (msg.pageIndex == 2) {
                    NetworkHooks.openScreen(player, new HunhuanMenu.Provider());
                }else if (msg.pageIndex == 3) {
                    NetworkHooks.openScreen(player, new ShenkaoMenu.Provider());
                }else if (msg.pageIndex == 4) {
                    NetworkHooks.openScreen(player, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("配置选项");
                        }
                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                            return new AttributeMenu(id, inv) {
                                @Override
                                public MenuType<?> getType() {
                                    return ModMenuTypes.CONFIG_MENU.get();
                                }
                            };
                        }
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
