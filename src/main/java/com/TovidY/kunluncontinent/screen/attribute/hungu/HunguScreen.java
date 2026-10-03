package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapability;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;

public class HunguScreen extends AbstractContainerScreen<HunguMenu> implements AttributeTabs.Host {

    public HunguScreen(HunguMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 330;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;

        // 顶部页签：全部由注册表统一生成（新增面板只需在 AttributeTabs 注册一行）
        AttributeTabs.buildTabs(this, AttributeTabs.PAGE_HUNGU);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        gui.drawString(this.font, "§6魂骨配置面板", this.leftPos + 12, this.topPos + 10, 0xFFFFFF, true);
        gui.fill(this.leftPos + 10, this.topPos + 22, this.leftPos + this.imageWidth - 10, this.topPos + 23, 0xFF8A6D3B);

        // =========================================================================
        // 渲染骨骼节点的金色魂力连接线
        // =========================================================================
        int cX = this.leftPos + 245;
        gui.fill(cX - 1, this.topPos + 34, cX, this.topPos + 82, 0x44FFD700);  // 脊椎主纵线
        gui.fill(cX - 26, this.topPos + 56, cX + 26, this.topPos + 57, 0x44FFD700); // 躯干臂骨横线
        gui.fill(cX - 26, this.topPos + 82, cX + 26, this.topPos + 83, 0x44FFD700); // 腿骨外附横线

        // 渲染 Slot 衬底
        for (Slot slot : this.menu.slots) {
            int slotX = this.leftPos + slot.x - 1;
            int slotY = this.topPos + slot.y - 1;
            boolean isHunguSlot = slot.index < 7;
            KunlunGuiHelper.renderSlotBackground(gui, slotX, slotY, isHunguSlot);
        }

        // =========================================================================
        // 左侧属性展示
        // =========================================================================
        int textX = this.leftPos + 12;
        int textY = this.topPos + 28;
        gui.drawString(this.font, "§e【魂骨属性汇总】", textX, textY, 0xFFFFFF, true);

        Map<String, Float> liveStats = calculateLiveBoneStats();

        final int[] currentLine = {1};
        renderStat(gui, "生命加成", liveStats.getOrDefault("maxshengming", 0f), textX, textY, currentLine);
        renderStat(gui, "攻击加成", liveStats.getOrDefault("gongji", 0f), textX, textY, currentLine);
        renderStat(gui, "防御加成", liveStats.getOrDefault("fangyu", 0f), textX, textY, currentLine);
        renderStat(gui, "暴击率", liveStats.getOrDefault("baojilv", 0f), textX, textY, currentLine);
        renderStat(gui, "暴击伤害", liveStats.getOrDefault("baojishanghai", 0f), textX, textY, currentLine);
        renderStat(gui, "吸血加成", liveStats.getOrDefault("xixue", 0f), textX, textY, currentLine);
        renderStat(gui, "闪避加成", liveStats.getOrDefault("shanbi", 0f), textX, textY, currentLine);
        renderStat(gui, "命中加成", liveStats.getOrDefault("mingzhong", 0f), textX, textY, currentLine);
        renderStat(gui, "抗暴加成", liveStats.getOrDefault("kangbao", 0f), textX, textY, currentLine);
        renderStat(gui, "生命恢复", liveStats.getOrDefault("shengminghuifu", 0f), textX, textY, currentLine);

        if (currentLine[0] == 1) {
            gui.drawString(this.font, "§7未穿戴魂骨", textX + 4, textY + 14, 0x88AAAAAA, false);
        }
    }

    private Map<String, Float> calculateLiveBoneStats() {
        Map<String, Float> stats = new HashMap<>();
        for (int i = 0; i < 7 && i < this.menu.slots.size(); i++) {
            ItemStack stack = this.menu.slots.get(i).getItem();
            if (!stack.isEmpty()) {
                stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
                    List<String> active = attr.getActiveAttributes();
                    for (String key : active) {
                        float val = getAttrValueByKey(attr, key);
                        if (val > 0) {
                            stats.put(key, stats.getOrDefault(key, 0f) + val);
                        }
                    }
                });
            }
        }
        return stats;
    }

    private float getAttrValueByKey(ItemAttributeCapability attr, String key) {
        return switch (key) {
            case "gongji" -> attr.getGongji();
            case "fangyu" -> attr.getFangyu();
            case "maxshengming" -> attr.getMaxshengming();
            case "baojilv" -> attr.getBaojilv();
            case "baojishanghai" -> attr.getBaojishanghai();
            case "xixue" -> attr.getXixue();
            case "shanbi" -> attr.getShanbi();
            case "mingzhong" -> attr.getMingzhong();
            case "wuchuan" -> attr.getWuchuan();
            case "kangbao" -> attr.getKangbao();
            case "shengminghuifu" -> attr.getShengminghuifu();
            default -> 0f;
        };
    }

    private void renderStat(GuiGraphics gui, String label, float value, int x, int baseY, int[] lineCounter) {
        if (value > 0.001f) {
            int y = baseY + (lineCounter[0] * 12);
            String formattedVal = KunlunGuiHelper.formatLargeNumber(value);
            String text = "§7" + label + ": §a+" + formattedVal;
            gui.drawString(this.font, text, x, y, 0xFFFFFF, false);
            lineCounter[0]++;
        }
    }


    // ==================== AttributeTabs.Host 实现（暴露 protected 成员给工具类） ====================

    @Override
    public int leftPos() {
        return this.leftPos;
    }

    @Override
    public int topPos() {
        return this.topPos;
    }

    @Override
    public int imageWidth() {
        return this.imageWidth;
    }

    @Override
    public net.minecraft.client.gui.Font font() {
        return this.font;
    }

    @Override
    public void addWidget(net.minecraft.client.gui.components.AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }
}
