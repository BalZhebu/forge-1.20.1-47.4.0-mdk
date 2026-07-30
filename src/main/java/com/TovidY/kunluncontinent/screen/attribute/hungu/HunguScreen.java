package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapability;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KluxTabButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class HunguScreen extends AbstractContainerScreen<HunguMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(KlMain.MOD_ID, "textures/screens/hungu.png");

    public HunguScreen(HunguMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 322;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        this.imageWidth = 322;
        this.imageHeight = 178;
        super.init();

        int normalSize = 28;
        int selectedSize = 33;
        int spacing = 6;
        int startX = this.leftPos + 8;
        int startY = this.topPos - 28;

        this.addRenderableWidget(new KluxTabButton(startX, startY - 2, normalSize, normalSize,
                new ItemStack(ModItems.ATTRIBUTE_BUTTON.get()), Component.literal("属性面板"), false, b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0));
        }));

        int currentX = startX + normalSize + spacing;
        this.addRenderableWidget(new KluxTabButton(currentX, startY - 5, selectedSize, selectedSize,
                new ItemStack(ModItems.SOUL_BONE_BUTTON.get()), Component.literal("魂骨面板"), true, b -> {
        }));

        currentX += (selectedSize + spacing);
        this.addRenderableWidget(new KluxTabButton(currentX, startY - 2, normalSize, normalSize,
                new ItemStack(ModItems.HUNHUAN_BUTTON.get()), Component.literal("魂环配置"), false, b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(2));
        }));

        currentX += (normalSize + spacing);
        if (GodClientData.godName != null && !GodClientData.godName.equals("无")) {
            this.addRenderableWidget(new KluxTabButton(currentX, startY - 2, normalSize, normalSize,
                    new ItemStack(ModItems.SHENKAO_BUTTON.get()), Component.literal("神考面板"), false, b -> {
                NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(3));
            }));
        }

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // 模型渲染
        int modelX = this.leftPos + 160;
        int modelY = this.topPos + 85;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, modelX, modelY, 30, (float) modelX - mouseX, (float) modelY - mouseY, this.minecraft.player);

        // -------------------------- 零延迟动态渲染属性 --------------------------
        int textX = this.leftPos + 7;
        int textY = this.topPos + 5;
        guiGraphics.drawString(this.font, "§6[魂骨属性总和]", textX, textY, 0xFFFFFF, true);

        // 实时遍历当前 GUI 绑定的 Container 前 7 个魂骨槽位
        Map<String, Float> liveStats = calculateLiveBoneStats();

        final int[] currentLine = {1};
        renderStat(guiGraphics, "生命加成: +", liveStats.getOrDefault("maxshengming", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "攻击加成: +", liveStats.getOrDefault("gongji", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "防御加成: +", liveStats.getOrDefault("fangyu", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "暴击率加成: +", liveStats.getOrDefault("baojilv", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "暴击伤害加成: +", liveStats.getOrDefault("baojishanghai", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "吸血加成: +", liveStats.getOrDefault("xixue", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "闪避加成: +", liveStats.getOrDefault("shanbi", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "命中加成: +", liveStats.getOrDefault("mingzhong", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "抗暴加成: +", liveStats.getOrDefault("kangbao", 0f), textX, textY, currentLine);
        renderStat(guiGraphics, "生命恢复: +", liveStats.getOrDefault("shengminghuifu", 0f), textX, textY, currentLine);
    }

    /**
     * 零延迟遍历 7 个槽位并实时汇总属性
     */
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

    private void renderStat(GuiGraphics guiGraphics, String label, float value, int x, int baseY, int[] lineCounter) {
        if (value > 0.001f) {
            int y = baseY + (lineCounter[0] * 12);
            String text = label + String.format("%.1f", value);
            guiGraphics.drawString(this.font, text, x, y, 0x00FF00, false);
            lineCounter[0]++;
        }
    }
}