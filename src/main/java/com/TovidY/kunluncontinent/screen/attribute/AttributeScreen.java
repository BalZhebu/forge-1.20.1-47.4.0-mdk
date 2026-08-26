package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse;

//属性面板渲染
public class AttributeScreen extends AbstractContainerScreen<AttributeMenu> {

    private float xMouse;
    private float yMouse;

    public AttributeScreen(AttributeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 330;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        int normalSize = 24;
        int selectedSize = 28;
        int spacing = 5;
        int startX = this.leftPos + 10;
        int startY = this.topPos - 26;

        // 1. 顶部切换 Tab 按钮
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, startX, startY - 2, selectedSize, selectedSize,
                new ItemStack(ModItems.ATTRIBUTE_BUTTON.get()), Component.literal("属性面板"), true, b -> {}
        ));

        int currentX = startX + selectedSize + spacing;
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY, normalSize, normalSize,
                new ItemStack(ModItems.SOUL_BONE_BUTTON.get()), Component.literal("魂骨面板"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(1))
        ));

        currentX += (normalSize + spacing);
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY, normalSize, normalSize,
                new ItemStack(ModItems.HUNHUAN_BUTTON.get()), Component.literal("魂环配置"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2))
        ));

        currentX += (normalSize + spacing);
        if (GodClientData.godName != null && !GodClientData.godName.equals("无")) {
            this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                    this.font, currentX, startY, normalSize, normalSize,
                    new ItemStack(ModItems.SHENKAO_BUTTON.get()), Component.literal("神考面板"), false,
                    b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(3))
            ));
        }

        // 2. 配置按钮
        int btnSize = 18;
        int btnX = this.leftPos + this.imageWidth - btnSize - 10;
        int btnY = this.topPos + 8;
        this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                this.font, btnX, btnY, btnSize, btnSize,
                Component.literal("⚙"),
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(4)),
                () -> List.of(Component.literal("打开配置界面"))
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;

        // Tooltip 渲染
        for (var child : this.children()) {
            if (child instanceof KunlunGuiHelper.HandDrawnButton btn) {
                btn.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {

        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        gui.drawString(this.font, "§6属性面板", this.leftPos + 12, this.topPos + 10, 0xFFFFFF, true);
        gui.fill(this.leftPos + 10, this.topPos + 22, this.leftPos + this.imageWidth - 10, this.topPos + 23, 0xFF8A6D3B);

        Player player = this.minecraft.player;
        if (player == null) return;

        renderFormattedAttributes(gui, player);

        int entityX = this.leftPos + 260;
        int entityY = this.topPos + 115;

        gui.fill(entityX - 35, entityY - 80, entityX + 35, entityY + 10, 0x40000000);
        gui.renderOutline(entityX - 35, entityY - 80, 70, 90, 0xFF4A5568);

        renderEntityInInventoryFollowsMouse(gui, entityX, entityY, 32, (float) entityX - this.xMouse, (float) entityY - this.yMouse, player);
    }

    private void renderFormattedAttributes(GuiGraphics gui, Player player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(attributes -> {

            boolean isShift = hasShiftDown(); // 获取当前键盘 Shift 状态

            int topStartY = this.topPos + 28;
            int topX = this.leftPos + 12;
            int topWidth = 195;
            int topRowHeight = 13;

            // 1. 顶部 HP/MP/EXP 响应 Shift 展开
            String hpStr = isShift
                    ? String.format("%,d / %,d", (long) player.getHealth(), (long) player.getMaxHealth())
                    : KunlunGuiHelper.formatLargeNumber(player.getHealth()) + " / " + KunlunGuiHelper.formatLargeNumber(player.getMaxHealth());

            String mpStr = isShift
                    ? String.format("%,d / %,d", (long) attributes.getJingshenli(), (long) attributes.getMaxjingshenli())
                    : KunlunGuiHelper.formatLargeNumber(attributes.getJingshenli()) + " / " + KunlunGuiHelper.formatLargeNumber(attributes.getMaxjingshenli());

            String expStr = isShift
                    ? String.format("%,d / %,d", (long) attributes.getJingyan(), (long) attributes.getMaxjingyan())
                    : KunlunGuiHelper.formatLargeNumber(attributes.getJingyan()) + " / " + KunlunGuiHelper.formatLargeNumber(attributes.getMaxjingyan());

            gui.drawString(this.font, "§c生命", topX, topStartY, 0xFFFFFF, false);
            gui.drawString(this.font, "§f" + hpStr, topX + topWidth - this.font.width(hpStr), topStartY, 0xFFFFFF, false);

            gui.drawString(this.font, "§b精神", topX, topStartY + topRowHeight, 0xFFFFFF, false);
            gui.drawString(this.font, "§f" + mpStr, topX + topWidth - this.font.width(mpStr), topStartY + topRowHeight, 0xFFFFFF, false);

            gui.drawString(this.font, "§a经验", topX, topStartY + topRowHeight * 2, 0xFFFFFF, false);
            gui.drawString(this.font, "§f" + expStr, topX + topWidth - this.font.width(expStr), topStartY + topRowHeight * 2, 0xFFFFFF, false);

            int lineY = topStartY + topRowHeight * 3 + 2;
            gui.fill(topX, lineY, topX + topWidth, lineY + 1, 0x40FFFFFF);

            int gridStartY = lineY + 6;
            int col1X = topX;
            int col2X = topX + 102;
            int labelWidth = 42;
            int gridRowHeight = 12;

            // 2. 传入 double 类型的原始数据给 AttributeEntry 处理
            AttributeEntry[] col1Entries = new AttributeEntry[]{
                    new AttributeEntry("等级", attributes.getDengji()),
                    new AttributeEntry("攻击力", ModAttributeAPI.getGongji(player)),
                    new AttributeEntry("防御力", ModAttributeAPI.getFangyu(player)),
                    new AttributeEntry("暴击率", String.format("%.1f%%", ModAttributeAPI.getBaojilv(player))),
                    new AttributeEntry("暴击伤害", String.format("%.1f%%", ModAttributeAPI.getBaojishanghai(player))),
                    new AttributeEntry("生命恢复", ModAttributeAPI.getShengminghuifu(player))
            };

            AttributeEntry[] col2Entries = new AttributeEntry[]{
                    new AttributeEntry("吸血", ModAttributeAPI.getXixue(player)),
                    new AttributeEntry("闪避", ModAttributeAPI.getShanbi(player)),
                    new AttributeEntry("命中", ModAttributeAPI.getMingzhong(player)),
                    new AttributeEntry("物穿", ModAttributeAPI.getWuchuan(player)),
                    new AttributeEntry("抗暴", ModAttributeAPI.getKangbao(player))
            };

            // 渲染左列
            for (int i = 0; i < col1Entries.length; i++) {
                int y = gridStartY + i * gridRowHeight;
                gui.drawString(this.font, "§7" + col1Entries[i].label, col1X, y, 0xFFFFFF, false);
                gui.drawString(this.font, "§e" + col1Entries[i].getValue(isShift), col1X + labelWidth, y, 0xFFFFFF, false);
            }

            // 渲染右列
            for (int i = 0; i < col2Entries.length; i++) {
                int y = gridStartY + i * gridRowHeight;
                gui.drawString(this.font, "§7" + col2Entries[i].label, col2X, y, 0xFFFFFF, false);
                gui.drawString(this.font, "§e" + col2Entries[i].getValue(isShift), col2X + labelWidth, y, 0xFFFFFF, false);
            }

            int maxRows = Math.max(col1Entries.length, col2Entries.length);
            int hintY = gridStartY + (maxRows * gridRowHeight) + 4;
            Component hintText = Component.translatable("gui.kunluncontinent.shift_hint");

            // Shift 按下时高亮提示文本
            int hintColor = isShift ? 0xFFFFAA00 : 0x88AAAAAA;
            gui.drawString(this.font, hintText, topX, hintY, hintColor, false);

            // 右下角基础信息
            int infoX = this.leftPos + 215;
            int infoY = this.topPos + 135;
            int infoW = 105;
            int infoH = 48;

            gui.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0x40000000);
            gui.renderOutline(infoX, infoY, infoW, infoH, 0xFF8A6D3B);

            int xiulianTime = attributes.getXiulianTime();
            String timeText = "§7修炼时间: §a" + (xiulianTime / 60) + "分" + (xiulianTime % 60) + "秒";
            gui.drawString(this.font, timeText, infoX + 4, infoY + 5, 0xFFFFFF, true);

            String tianfuText = "§7天赋等级: §b" + attributes.getXiantianTalent() + " 级";
            gui.drawString(this.font, tianfuText, infoX + 4, infoY + 18, 0xFFFFFF, true);

            float baseRate = attributes.getTupochenggonglv();
            float levelPenalty = attributes.getDengji() * 0.5f;
            float finalDisplayRate = Math.max(5.0f, baseRate - levelPenalty);
            String rateColor = finalDisplayRate <= 5.0f ? "§c" : "§a";
            String rateText = "§7突破概率: " + rateColor + String.format("%.1f%%", finalDisplayRate);
            gui.drawString(this.font, rateText, infoX + 4, infoY + 31, 0xFFFFFF, true);
        });
    }

    // 内部类修改：支持双模数值存储与响应
    private static class AttributeEntry {
        final String label;
        final String rawStringValue;
        final double doubleValue;
        final boolean isNumeric;

        AttributeEntry(String label, String stringValue) {
            this.label = label;
            this.rawStringValue = stringValue;
            this.doubleValue = 0;
            this.isNumeric = false;
        }

        AttributeEntry(String label, double doubleValue) {
            this.label = label;
            this.rawStringValue = null;
            this.doubleValue = doubleValue;
            this.isNumeric = true;
        }

        String getValue(boolean isShift) {
            if (!isNumeric) {
                return rawStringValue;
            }
            if (isShift) {
                if (doubleValue == (long) doubleValue) {
                    return String.format("%,d", (long) doubleValue); // Shift 按下全显千分位整数
                }
                return String.format("%,.1f", doubleValue); // 小数格式
            }
            return KunlunGuiHelper.formatLargeNumber(doubleValue); // 默认缩进格式 (1.2M, 5.3B 等)
        }
    }
}