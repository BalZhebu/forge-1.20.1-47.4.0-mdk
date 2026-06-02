package com.TovidY.kunluncontinent.screen.attribute.config;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketChangeDisplayMode;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.network.server.PacketToggleConfig;
import com.TovidY.kunluncontinent.network.server.PacketUpdateUIOffset;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ConfigScreen extends AbstractContainerScreen<HunhuanMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(KlMain.MOD_ID, "textures/screens/config_gui.png");

    private int currentPage = 0;
    private static final int ROWS = 6;
    private static final int COLS = 3;
    private static final int PER_PAGE = ROWS * COLS;

    private boolean isOffsetSubPage = false;
    private EditBox offsetEditBox;

    private record ConfigItem(String name, int index) {}

    private final List<ConfigItem> configItems = List.of(
            new ConfigItem("生物魂环显示", 1),
            new ConfigItem("玩家魂环显示", 2),
            new ConfigItem("吸收经验提示", 3),
            new ConfigItem("魂环实体显示", 4),
            new ConfigItem("魂核实体显示", 5),
            new ConfigItem("屏幕UI显示", 6),
            new ConfigItem("屏幕UI位置", 7)
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

        // ==================== 子页面布局逻辑 ====================
        if (this.isOffsetSubPage) {
            this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                int inputWidth = 50;
                int elementGap = 4;
                int subPageY = this.topPos + 65;
                int totalRowWidth = (btnWidth * 2) + inputWidth + (elementGap * 2);
                int subPageStartX = this.leftPos + (this.imageWidth - totalRowWidth) / 2;
                this.addRenderableWidget(Button.builder(Component.literal("向上平移"), b -> {
                    int val = getInputValue();
                    NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(-val, false));
                    cap.setUiOffsetY(cap.getUiOffsetY() - val);
                }).bounds(subPageStartX, subPageY, btnWidth, btnHeight).build());
                this.offsetEditBox = new EditBox(this.font, subPageStartX + btnWidth + elementGap, subPageY, inputWidth, btnHeight, Component.literal("偏移值"));
                this.offsetEditBox.setValue("");
                this.offsetEditBox.setFilter(s -> s.matches("\\d*"));
                this.addRenderableWidget(this.offsetEditBox);
                this.addRenderableWidget(Button.builder(Component.literal("向下平移"), b -> {
                    int val = getInputValue();
                    NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(val, false));
                    cap.setUiOffsetY(cap.getUiOffsetY() + val);
                }).bounds(subPageStartX + btnWidth + inputWidth + (elementGap * 2), subPageY, btnWidth, btnHeight).build());

                int resetBtnW = 80;
                int resetX = this.leftPos + (this.imageWidth - resetBtnW) / 2;
                this.addRenderableWidget(Button.builder(Component.literal("§c重置位置"), b -> {
                    NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(0, true));
                    cap.setUiOffsetY(0);
                }).bounds(resetX, subPageY + btnHeight + 12, resetBtnW, btnHeight).build());

                this.addRenderableWidget(Button.builder(Component.literal("返回菜单"), b -> {
                    this.isOffsetSubPage = false;
                    this.init();
                }).bounds(startX + 150, this.topPos + this.imageHeight - 16 - 25, 60, 20).build());
            });
            return;
        }

        // ==================== 主页面布局逻辑 ====================
        this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            this.addRenderableWidget(Button.builder(getModeComponent(cap.getDamageDisplayMode()), b -> {
                int nextMode = (cap.getDamageDisplayMode() + 1) % 4;
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

                if (item.index == 7) {
                    this.addRenderableWidget(Button.builder(Component.literal("§6" + item.name + " ->"), b -> {
                        this.isOffsetSubPage = true;
                        this.init();
                    }).bounds(bx, by, btnWidth, btnHeight).build());
                } else {
                    this.addRenderableWidget(createToggleButton(bx, by, btnWidth, btnHeight, item.name, item.index, cap));
                }
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

        if (this.isOffsetSubPage) {
            String subTitle = "§6屏幕UI位置变更§7（只支持修改上下位置）";
            int titleX = this.leftPos + (this.imageWidth - this.font.width(subTitle)) / 2;
            gui.drawString(this.font, subTitle, titleX, this.topPos + 30, 0xFFFFFF, true);

            // 【界面内同步监视器】：实时把当前偏移量渲染在按钮下方，方便玩家参考
            this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                String curOffsetStr = "§7当前累计平移值: §e" + cap.getUiOffsetY() + " §7像素";
                int curX = this.leftPos + (this.imageWidth - this.font.width(curOffsetStr)) / 2;
                gui.drawString(this.font, curOffsetStr, curX, this.topPos + 120, 0xFFFFFF, true);
            });
        } else {
            int textX = this.leftPos + 8;
            int textY = this.topPos + this.imageHeight - 15;
            gui.drawString(this.font, "§7* 本配置内容开局即默认选项，根据实际情况修改", textX, textY, 0xFFFFFF, true);
        }
        this.renderTooltip(gui, mouseX, mouseY);
    }

    private int getInputValue() {
        if (this.offsetEditBox == null || this.offsetEditBox.getValue().isEmpty()) {
            return 1;
        }
        try {
            return Math.abs(Integer.parseInt(this.offsetEditBox.getValue()));
        } catch (NumberFormatException e) {
            return 1;
        }
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
            case 2 -> Component.literal(prefix + "§d3D数字");
            default -> Component.literal(prefix + "§c已隐藏");
        };
    }

    @Override
    protected void renderBg(GuiGraphics gui, float p_283065_, int p_281248_, int p_281437_) {
        this.renderBackground(gui);
        gui.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}