package com.TovidY.kunluncontinent.screen.attribute.config;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketChangeDisplayMode;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.network.server.PacketToggleConfig;
import com.TovidY.kunluncontinent.network.server.PacketUpdateUIOffset;
import com.TovidY.kunluncontinent.screen.attribute.hunhuan.HunhuanMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen extends AbstractContainerScreen<HunhuanMenu> {

    private int currentPage = 0;
    private static final int COLS = 3;
    private static final int ROWS = 5; // 留出顶部/底部空间
    private static final int PER_PAGE = COLS * ROWS;

    private boolean isOffsetSubPage = false;
    private EditBox offsetEditBox;

    // 配置项定义结构
    private record ConfigItem(String name, int index, Supplier<Component> messageSupplier, Runnable onClick, Supplier<List<Component>> tooltipSupplier) {}

    private final List<ConfigItem> allConfigItems = new ArrayList<>();

    public ConfigScreen(HunhuanMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 330;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        // 屏蔽原生 Inventory/Title 文字渲染
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        if (this.isOffsetSubPage) {
            initSubPage();
        } else {
            initMainPage();
        }
    }

    private void initMainPage() {
        this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            allConfigItems.clear();
            allConfigItems.add(new ConfigItem(
                    "伤害提示显示",
                    -1,
                    () -> getModeComponent(cap.getDamageDisplayMode()),
                    () -> {
                        int nextMode = (cap.getDamageDisplayMode() + 1) % 4;
                        NetworkHandler.INSTANCE.sendToServer(new PacketChangeDisplayMode(nextMode));
                        cap.setDamageDisplayMode(nextMode);
                    },
                    () -> List.of(
                            Component.literal("伤害提示显示设置").withStyle(ChatFormatting.GOLD),
                            Component.literal("§7切换在战斗中伤害数值的显示样式")
                    )
            ));

            List<ConfigItemData> rawItems = List.of(
                    new ConfigItemData("生物魂环显示", 1),
                    new ConfigItemData("玩家魂环显示", 2),
                    new ConfigItemData("吸收经验提示", 3),
                    new ConfigItemData("魂环实体显示", 4),
                    new ConfigItemData("魂核实体显示", 5),
                    new ConfigItemData("屏幕UI显示", 6),
                    new ConfigItemData("屏幕UI位置", 7),
                    new ConfigItemData("聚灵物品渲染", 8),
                    new ConfigItemData("NPC交易品级", 9),
                    new ConfigItemData("粒子特效优化", 10),
                    new ConfigItemData("NPC魂环显示", 11)
            );

            for (ConfigItemData data : rawItems) {
                if (data.index == 7) {
                    allConfigItems.add(new ConfigItem(
                            data.name,
                            7,
                            () -> Component.literal("§6" + data.name + " §e->"),
                            () -> {
                                this.isOffsetSubPage = true;
                                this.init();
                            },
                            () -> List.of(
                                    Component.literal("屏幕UI位置变更").withStyle(ChatFormatting.GOLD),
                                    Component.literal("§7点击进入屏幕UI偏移与缩放设置页面")
                            )
                    ));
                } else {
                    allConfigItems.add(new ConfigItem(
                            data.name,
                            data.index,
                            () -> getToggleMsg(data.name, data.index, cap),
                            () -> {
                                cap.toggleConfig(data.index);
                                NetworkHandler.INSTANCE.sendToServer(new PacketToggleConfig(data.index));
                            },
                            () -> getToggleTooltip(data.index, cap.isConfigOpen(data.index))
                    ));
                }
            }

            // 3. 计算分页与当前页绘制
            int totalItems = allConfigItems.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PER_PAGE));
            if (currentPage >= totalPages) currentPage = totalPages - 1;

            int startIdx = currentPage * PER_PAGE;
            int endIdx = Math.min(startIdx + PER_PAGE, totalItems);

            int startX = this.leftPos + 15;
            int startY = this.topPos + 30;
            int btnWidth = 96;
            int btnHeight = 20;
            int gapX = 6;
            int gapY = 6;

            for (int i = startIdx; i < endIdx; i++) {
                int localIdx = i - startIdx;
                int col = localIdx % COLS;
                int row = localIdx / COLS;

                int bx = startX + col * (btnWidth + gapX);
                int by = startY + row * (btnHeight + gapY);

                ConfigItem item = allConfigItems.get(i);
                this.addRenderableWidget(new HandDrawnButton(
                        bx, by, btnWidth, btnHeight,
                        item.messageSupplier.get(),
                        b -> {
                            item.onClick.run();
                            b.setMessage(item.messageSupplier.get());
                        },
                        item.tooltipSupplier
                ));
            }

            int bottomY = this.topPos + this.imageHeight - 28;

            if (totalPages > 1) {
                if (currentPage > 0) {
                    this.addRenderableWidget(new HandDrawnPageButton(
                            this.leftPos + 15, bottomY, 22, 18, true,
                            b -> { currentPage--; init(); }
                    ));
                }
                if (currentPage < totalPages - 1) {
                    this.addRenderableWidget(new HandDrawnPageButton(
                            this.leftPos + 42, bottomY, 22, 18, false,
                            b -> { currentPage++; init(); }
                    ));
                }
            }

            this.addRenderableWidget(new HandDrawnButton(
                    this.leftPos + this.imageWidth - 65, bottomY, 50, 18,
                    Component.literal("§c返回"),
                    b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0)),
                    () -> List.of(Component.literal("关闭配置界面"))
            ));
        });
    }

    private void initSubPage() {
        this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            int btnWidth = 90;
            int btnHeight = 20;
            int inputWidth = 45;
            int elementGap = 6;

            int subPageY = this.topPos + 55;
            int totalRowWidth = (btnWidth * 2) + inputWidth + (elementGap * 2);
            int subPageStartX = this.leftPos + (this.imageWidth - totalRowWidth) / 2;

            // 第一行：平移
            this.addRenderableWidget(new HandDrawnButton(
                    subPageStartX, subPageY, btnWidth, btnHeight,
                    Component.literal("向上平移"),
                    b -> {
                        int val = getInputValue();
                        NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(-val));
                        cap.setUiOffsetY(cap.getUiOffsetY() - val);
                    }, null
            ));

            this.offsetEditBox = new EditBox(this.font, subPageStartX + btnWidth + elementGap, subPageY, inputWidth, btnHeight, Component.literal("偏移值")) {
                @Override
                public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
                    // 手绘输入框背景
                    gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF0D1117);
                    int borderCol = this.isFocused() ? 0xFFE6B800 : 0xFF4A5568;
                    gui.renderOutline(this.getX(), this.getY(), this.width, this.height, borderCol);
                    super.renderWidget(gui, mouseX, mouseY, partialTick);
                }
            };
            this.offsetEditBox.setValue("5");
            this.offsetEditBox.setFilter(s -> s.matches("\\d*"));
            this.offsetEditBox.setTextColor(0xFFFFFF);
            this.addRenderableWidget(this.offsetEditBox);

            this.addRenderableWidget(new HandDrawnButton(
                    subPageStartX + btnWidth + inputWidth + (elementGap * 2), subPageY, btnWidth, btnHeight,
                    Component.literal("向下平移"),
                    b -> {
                        int val = getInputValue();
                        NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(val));
                        cap.setUiOffsetY(cap.getUiOffsetY() + val);
                    }, null
            ));

            // 第二行：缩放
            int scaleRowY = subPageY + btnHeight + 10;

            this.addRenderableWidget(new HandDrawnButton(
                    subPageStartX, scaleRowY, btnWidth, btnHeight,
                    Component.literal("缩小 (-0.1)"),
                    b -> {
                        float newScale = Math.max(0.2f, (float) (Math.round((cap.getUiScale() - 0.1f) * 10.0) / 10.0));
                        cap.setUiScale(newScale);
                        NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(newScale));
                    }, null
            ));

            this.addRenderableWidget(new HandDrawnButton(
                    subPageStartX + btnWidth + elementGap, scaleRowY, inputWidth, btnHeight,
                    Component.literal("重置缩放"),
                    b -> {
                        cap.setUiScale(1.0f);
                        NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(1.0f));
                    }, null
            ));

            this.addRenderableWidget(new HandDrawnButton(
                    subPageStartX + btnWidth + inputWidth + (elementGap * 2), scaleRowY, btnWidth, btnHeight,
                    Component.literal("放大 (+0.1)"),
                    b -> {
                        float newScale = Math.min(3.0f, (float) (Math.round((cap.getUiScale() + 0.1f) * 10.0) / 10.0));
                        cap.setUiScale(newScale);
                        NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(newScale));
                    }, null
            ));

            // 第三行：重置与返回
            int bottomY = scaleRowY + btnHeight + 15;

            this.addRenderableWidget(new HandDrawnButton(
                    this.leftPos + (this.imageWidth - 110) / 2, bottomY, 110, 20,
                    Component.literal("§c重置位置与缩放"),
                    b -> {
                        NetworkHandler.INSTANCE.sendToServer(new PacketUpdateUIOffset(0, true));
                        cap.setUiOffsetY(0);
                        cap.setUiScale(1.0f);
                    }, null
            ));

            this.addRenderableWidget(new HandDrawnButton(
                    this.leftPos + this.imageWidth - 65, this.topPos + this.imageHeight - 28, 50, 18,
                    Component.literal("返回"),
                    b -> {
                        this.isOffsetSubPage = false;
                        this.init();
                    }, null
            ));
        });
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(gui);

        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        gui.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x80000000); // 投影
        gui.fill(x, y, x + w, y + h, 0xF012161F); // 玄铁深蓝/黑底色

        gui.renderOutline(x, y, w, h, 0xFFD4AF37); // 外金框
        gui.renderOutline(x + 2, y + 2, w - 4, h - 4, 0xFF2A3447); // 内衬暗框
        gui.renderOutline(x + 3, y + 3, w - 6, h - 6, 0xFF8A6D3B); // 内金框

        int cornerSize = 6;
        gui.fill(x - 1, y - 1, x + cornerSize, y + 2, 0xFFE6B800);
        gui.fill(x - 1, y - 1, x + 2, y + cornerSize, 0xFFE6B800);

        gui.fill(x + w - cornerSize, y - 1, x + w + 1, y + 2, 0xFFE6B800);
        gui.fill(x + w - 2, y - 1, x + w + 1, y + cornerSize, 0xFFE6B800);

        gui.fill(x - 1, y + h - 2, x + cornerSize, y + h + 1, 0xFFE6B800);
        gui.fill(x - 1, y + h - cornerSize, x + 2, y + h + 1, 0xFFE6B800);

        gui.fill(x + w - cornerSize, y + h - 2, x + w + 1, y + h + 1, 0xFFE6B800);
        gui.fill(x + w - 2, y + h - cornerSize, x + w + 1, y + h + 1, 0xFFE6B800);

        // 4. 标题分割线
        gui.fill(x + 10, y + 22, x + w - 10, y + 23, 0xFF8A6D3B);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        // 渲染标题与状态文本
        if (this.isOffsetSubPage) {
            String subTitle = "§6修真界面位置微调";
            int titleX = this.leftPos + (this.imageWidth - this.font.width(subTitle)) / 2;
            gui.drawString(this.font, subTitle, titleX, this.topPos + 8, 0xFFFFFF, true);

            this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                String curOffsetStr = "§7当前偏移: §e" + cap.getUiOffsetY() + " px §7| 当前缩放: §e" + String.format("%.1f", cap.getUiScale()) + "x";
                int curX = this.leftPos + (this.imageWidth - this.font.width(curOffsetStr)) / 2;
                gui.drawString(this.font, curOffsetStr, curX, this.topPos + 38, 0xFFFFFF, true);
            });
        } else {
            String title = "§6昆仑面板系统配置";
            gui.drawString(this.font, title, this.leftPos + 15, this.topPos + 8, 0xFFFFFF, true);

            // 页码绘制
            int totalPages = Math.max(1, (int) Math.ceil((double) allConfigItems.size() / PER_PAGE));
            if (totalPages > 1) {
                String pageStr = "§7" + (currentPage + 1) + " / " + totalPages;
                gui.drawString(this.font, pageStr, this.leftPos + 70, this.topPos + this.imageHeight - 23, 0xFFFFFF, false);
            }

            String tip = "§7* 配置更改即时生效";
            gui.drawString(this.font, tip, this.leftPos + 130, this.topPos + this.imageHeight - 23, 0xFFFFFF, true);
        }

        // 渲染自定义悬停 Tooltip（重构悬停逻辑）
        for (var widget : this.children()) {
            if (widget instanceof HandDrawnButton btn && btn.isMouseOver(mouseX, mouseY)) {
                btn.renderCustomTooltip(gui, mouseX, mouseY);
            }
        }
    }

    private int getInputValue() {
        if (this.offsetEditBox == null || this.offsetEditBox.getValue().isEmpty()) return 1;
        try {
            return Math.abs(Integer.parseInt(this.offsetEditBox.getValue()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // =========================================================================
    //  辅助格式化方法
    // =========================================================================
    private record ConfigItemData(String name, int index) {}

    private Component getToggleMsg(String name, int index, PlayerAttributeCapability cap) {
        boolean isOpen = cap.isConfigOpen(index);
        if (index == 9) {
            return Component.literal("§6" + name + ": " + (isOpen ? "§b样式 B" : "§a样式 A"));
        }
        return Component.literal("§6" + name + (isOpen ? ": §a开启" : ": §c关闭"));
    }

    private Component getModeComponent(int mode) {
        String prefix = "§6伤害显示: ";
        return switch (mode) {
            case 0 -> Component.literal(prefix + "§a快捷栏上方");
            case 1 -> Component.literal(prefix + "§b聊天栏");
            case 2 -> Component.literal(prefix + "§d3D数字");
            default -> Component.literal(prefix + "§c已隐藏");
        };
    }

    private static List<Component> getToggleTooltip(int index, boolean isOpen) {
        return switch (index) {
            case 1 -> List.of(
                    Component.literal("生物魂环显示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f渲染周围生物脚下的魂环" : "§c[关闭] §f隐藏周围生物脚下的魂环")
            );
            case 2 -> List.of(
                    Component.literal("玩家魂环显示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f渲染所有玩家的自身魂环" : "§c[关闭] §f隐藏所有玩家的自身魂环")
            );
            case 3 -> List.of(
                    Component.literal("吸收经验提示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f吸收魂环时显示经验获得提示" : "§c[关闭] §f吸收魂环时不显示提示")
            );
            case 4 -> List.of(
                    Component.literal("魂环实体显示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f掉落的魂环实体正常渲染显示" : "§c[关闭] §f隐藏掉落魂环实体")
            );
            case 5 -> List.of(
                    Component.literal("魂核实体显示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f掉落的魂核实体正常渲染显示" : "§c[关闭] §f隐藏掉落魂核实体")
            );
            case 6 -> List.of(
                    Component.literal("屏幕UI显示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f显示屏幕左上角的状态栏" : "§c[关闭] §f隐藏屏幕左上角的状态栏")
            );
            case 8 -> List.of(
                    Component.literal("聚灵物品渲染").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f聚灵祭坛物品显示渲染" : "§c[关闭] §f隐藏聚灵祭坛物品渲染")
            );
            case 10 -> List.of(
                    Component.literal("粒子特效优化").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f降低技能粒子数量与频率，提升流畅度" : "§c[关闭] §f保持完整粒子特效表现")
            );
            case 11 -> List.of(
                    Component.literal("NPC魂环显示").withStyle(ChatFormatting.GOLD),
                    Component.literal(isOpen ? "§a[开启] §f渲染周围 NPC 的脚下魂环" : "§c[关闭] §f隐藏周围 NPC 的脚下魂环")
            );
            default -> List.of();
        };
    }

    // =========================================================================
    //  内部类：纯代码手绘主按钮
    // =========================================================================
    private class HandDrawnButton extends AbstractButton {
        private final Consumer<HandDrawnButton> onPress;
        private final Supplier<List<Component>> tooltipSupplier;

        public HandDrawnButton(int x, int y, int width, int height, Component message, Consumer<HandDrawnButton> onPress, Supplier<List<Component>> tooltipSupplier) {
            super(x, y, width, height, message);
            this.onPress = onPress;
            this.tooltipSupplier = tooltipSupplier;
        }

        @Override
        public void onPress() {
            this.onPress.accept(this);
        }

        @Override
        protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            boolean isHovered = this.isHoveredOrFocused();

            // 1. 按钮内部颜色（悬停发光）
            int bgColor = isHovered ? 0xFF2A364F : 0xFF181F2C;
            int borderColor = isHovered ? 0xFFE6B800 : 0xFF4A5568;

            gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            gui.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

            // 2. 悬停时的四个极小高亮角点
            if (isHovered) {
                gui.fill(this.getX(), this.getY(), this.getX() + 2, this.getY() + 2, 0xFFFFF0A0);
                gui.fill(this.getX() + this.width - 2, this.getY() + this.height - 2, this.getX() + this.width, this.getY() + this.height, 0xFFFFF0A0);
            }

            // 3. 文字居中渲染
            int textColor = isHovered ? 0xFFFFFF : 0xDDDDDD;
            int textX = this.getX() + (this.width - ConfigScreen.this.font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            gui.drawString(ConfigScreen.this.font, this.getMessage(), textX, textY, textColor, true);
        }

        public void renderCustomTooltip(GuiGraphics gui, int mouseX, int mouseY) {
            if (this.tooltipSupplier != null) {
                List<Component> tooltip = this.tooltipSupplier.get();
                if (tooltip != null && !tooltip.isEmpty()) {
                    gui.renderComponentTooltip(ConfigScreen.this.font, tooltip, mouseX, mouseY);
                }
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private class HandDrawnPageButton extends AbstractButton {
        private final boolean isLeftArrow;
        private final Consumer<HandDrawnPageButton> onPress;

        public HandDrawnPageButton(int x, int y, int width, int height, boolean isLeftArrow, Consumer<HandDrawnPageButton> onPress) {
            super(x, y, width, height, Component.empty());
            this.isLeftArrow = isLeftArrow;
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            this.onPress.accept(this);
        }

        @Override
        protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            boolean isHovered = this.isHoveredOrFocused();

            int bgColor = isHovered ? 0xFF3A4B6E : 0xFF1D2636;
            int borderColor = isHovered ? 0xFFE6B800 : 0xFF5C6B73;

            gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            gui.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

            // 手绘三角形箭头
            int arrowColor = isHovered ? 0xFFFFD700 : 0xFFCCCCCC;
            int centerX = this.getX() + this.width / 2;
            int centerY = this.getY() + this.height / 2;

            if (isLeftArrow) {
                gui.fill(centerX - 2, centerY, centerX - 1, centerY + 1, arrowColor);
                gui.fill(centerX - 1, centerY - 1, centerX, centerY + 2, arrowColor);
                gui.fill(centerX, centerY - 2, centerX + 1, centerY + 3, arrowColor);
                gui.fill(centerX + 1, centerY - 3, centerX + 2, centerY + 4, arrowColor);
            } else {
                gui.fill(centerX + 1, centerY, centerX + 2, centerY + 1, arrowColor);
                gui.fill(centerX, centerY - 1, centerX + 1, centerY + 2, arrowColor);
                gui.fill(centerX - 1, centerY - 2, centerX, centerY + 3, arrowColor);
                gui.fill(centerX - 2, centerY - 3, centerX - 1, centerY + 4, arrowColor);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}