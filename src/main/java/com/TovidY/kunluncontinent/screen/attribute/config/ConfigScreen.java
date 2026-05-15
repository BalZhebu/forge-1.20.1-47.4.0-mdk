package com.TovidY.kunluncontinent.screen.attribute.config;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketChangeDisplayMode;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.network.server.PacketToggleConfig;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ConfigScreen extends AbstractContainerScreen<HunhuanMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/config_gui.png");

    private int currentPage = 0;
    private static final int ROWS = 6;    // 每页3行
    private static final int COLS = 3;    // 每页6列
    private static final int PER_PAGE = ROWS * COLS;

    private record ConfigItem(String name, int index) {}
    private final List<ConfigItem> configItems = List.of(
            new ConfigItem("生物魂环显示", 1),
            new ConfigItem("玩家魂环显示", 2),
            new ConfigItem("吸收经验提示", 3),
            new ConfigItem("魂环实体显示", 4),
            new ConfigItem("魂核实体显示", 5)
    );

    public ConfigScreen(HunhuanMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 322;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        int startX = this.leftPos + 5;
        int startY = this.topPos + 5;
        int btnWidth = 100;
        int btnHeight = 20;

        this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {

            this.addRenderableWidget(Button.builder(getModeComponent(cap.getDamageDisplayMode()), b -> {
                int nextMode = (cap.getDamageDisplayMode() + 1) % 3;
                NetworkHandler.INSTANCE.sendToServer(new PacketChangeDisplayMode(nextMode));
                b.setMessage(getModeComponent(nextMode));
            }).bounds(startX, startY, btnWidth, btnHeight).build());

            int totalPages = (int) Math.ceil((double) (configItems.size() + 1) / PER_PAGE);
            int startIdx = currentPage * PER_PAGE;
            int endIdx = Math.min(startIdx + PER_PAGE, configItems.size() + 1);

            for (int i = startIdx; i < endIdx; i++) {
                if (i == 0) continue;

                ConfigItem item = configItems.get(i - 1);
                int localPos = i % PER_PAGE;
                int col = localPos % COLS;
                int row = localPos / COLS;

                int bx = startX + (col * (btnWidth + 5));
                int by = startY + (row * (btnHeight + 5));

                this.addRenderableWidget(createToggleButton(bx, by, btnWidth, btnHeight, item.name, item.index, cap));
            }

            if (totalPages > 1) {
                if (currentPage > 0) {
                    this.addRenderableWidget(Button.builder(Component.literal("<-"), b -> { currentPage--; init(); })
                            .bounds(this.leftPos + 220, startY, 30, 20).build());
                }
                if (currentPage < totalPages - 1) {
                    this.addRenderableWidget(Button.builder(Component.literal("->"), b -> { currentPage++; init(); })
                            .bounds(this.leftPos + 260, startY, 30, 20).build());
                }
            }
        });

        int btnX = this.leftPos + this.imageWidth - 16 - 5;
        int btnY = this.topPos + this.imageHeight - 16 - 5;
        this.addRenderableWidget(Button.builder(Component.literal("返回"), b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0)))
                .bounds(btnX - 20, btnY - 3, 35, 20).build());

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        int textX = this.leftPos + 8;
        int textY = this.topPos + this.imageHeight - 15;
        gui.drawString(this.font, "§7* 本配置内容开局即默认选项，根据实际情况修改", textX, textY, 0xFFFFFF, true);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    private Button createToggleButton(int x, int y, int w, int h, String name, int index, PlayerAttributeCapability cap) {
        return Button.builder(getToggleMsg(name, cap.isConfigOpen(index)), b -> {
            cap.toggleConfig(index);
            NetworkHandler.INSTANCE.sendToServer(new PacketToggleConfig(index));
            b.setMessage(getToggleMsg(name, cap.isConfigOpen(index)));
        }).bounds(x, y, w, h).build();
    }

    private Component getToggleMsg(String name, boolean isOpen) {
        return Component.literal("§6" + name + (isOpen ? ": §a开启" : ": §c关闭"));
    }

    private Component getModeComponent(int mode) {
        String prefix = "§6伤害提示显示：";
        return switch (mode) {
            case 0 -> Component.literal(prefix + "§a物品栏上方");
            case 1 -> Component.literal(prefix + "§b聊天栏");
            default -> Component.literal(prefix + "§c已隐藏");
        };
    }

    @Override
    protected void renderBg(GuiGraphics gui, float p_283065_, int p_281248_, int p_281437_) {
        this.renderBackground(gui);
        gui.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}