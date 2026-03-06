package com.TovidY.kunluncontinent.screen.attribute.hunhuan;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class HunhuanScreen extends AbstractContainerScreen<HunhuanMenu> {
    // 资源路径
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/hunhuan_gui.png");
    private static final ResourceLocation HUNHUAN_ICON = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/hunhuan.png");

    // 翻页逻辑变量
    private static final int HUNHUAN_PER_PAGE = 10;
    private int currentPage = 0;
    private int totalHunhuanCount = 0;
    private Button prevPageButton;
    private Button nextPageButton;

    public HunhuanScreen(HunhuanMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 322;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();

        // --- 原有的页签按钮 ---
        this.addRenderableWidget(Button.builder(Component.literal("属性"), b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0))).bounds(this.leftPos + 5, this.topPos - 20, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("魂骨"), b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(1))).bounds(this.leftPos + 47, this.topPos - 20, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("魂环"), b -> {}).bounds(this.leftPos + 89, this.topPos - 20, 40, 20).build());

        // --- 新增的翻页按钮 ---
        int buttonY = this.topPos + 155; // 根据你的背景图调整 Y
        int centerX = this.leftPos + this.imageWidth / 2;

        this.prevPageButton = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (currentPage > 0) currentPage--;
        }).bounds(centerX - 30, buttonY, 20, 20).build());

        this.nextPageButton = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            int maxPage = (int) Math.ceil((double) totalHunhuanCount / HUNHUAN_PER_PAGE) - 1;
            if (currentPage < maxPage) currentPage++;
        }).bounds(centerX + 10, buttonY, 20, 20).build());


        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 更新按钮状态
        int maxPage = (int) Math.ceil((double) totalHunhuanCount / HUNHUAN_PER_PAGE) - 1;
        this.prevPageButton.active = currentPage > 0;
        this.nextPageButton.active = currentPage < maxPage;

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // 1. 渲染背景
        guiGraphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // 2. 渲染魂环主体
        renderHunhuan(guiGraphics, this.minecraft.player, this.leftPos, this.topPos, partialTicks);
    }

    private void renderHunhuan(GuiGraphics guiGraphics, Player player, int x, int y, float partialTicks) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            int k = capability.getHunhuankuaiguan();
            if (capability.getWuhunListsname().size() <= k || k < 0) return;

            String wuhunName = capability.getWuhunListsname().get(k);
            var monsterList = capability.getMonsterCapabilityLists().get(wuhunName);
            if (monsterList == null) return;

            totalHunhuanCount = monsterList.size();
            int startIndex = currentPage * HUNHUAN_PER_PAGE;
            int endIndex = Math.min(startIndex + HUNHUAN_PER_PAGE, totalHunhuanCount);

            int startOffsetX = 35;
            int startOffsetY = 25;
            int hSpacing = 55;
            int vSpacing = 50;
            int itemsPerRow = 5;

            for (int i = startIndex; i < endIndex; i++) {
                var monsterAttr = monsterList.get(i);
                long nianxian = monsterAttr.getNianxian();
                int localIndex = i - startIndex;
                int row = localIndex / itemsPerRow;
                int col = localIndex % itemsPerRow;

                int centerX = x + startOffsetX + col * hSpacing;
                int centerY = y + startOffsetY + row * vSpacing;

                PoseStack pose = guiGraphics.pose();
                pose.pushPose();
                pose.translate(centerX, centerY, 0);
                pose.scale(0.7f, 0.5f, 1.0f);
                float rotation = (player.level().getGameTime() + partialTicks) * 0.05f;
                pose.mulPose(com.mojang.math.Axis.ZP.rotation(rotation));
                setHunhuanColor(nianxian, player.level().getGameTime() + partialTicks);
                guiGraphics.blit(HUNHUAN_ICON, -30, -30, 0, 0, 60, 60, 60, 60);
                pose.popPose();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                String indexText = String.valueOf(i + 1);
                int indexW = font.width(indexText);
                int displayColor = getNianxianColor((int) nianxian);
                guiGraphics.drawString(font, indexText, centerX - indexW / 2, centerY - 4, displayColor, true);
                String yearText = nianxian + "年";
                int yearW = font.width(yearText);
                guiGraphics.drawString(font, yearText, centerX - yearW / 2, centerY + 18, 0xFFFFFF, true);
            }

            RenderSystem.setShaderColor(1, 1, 1, 1);
        });
    }

    private void setHunhuanColor(long nianxian, float gameTime) {
        if (nianxian >= 100000000) {
            RenderSystem.setShaderColor(0.00f, 1.00f, 0.00f, 1.0f);
        } else if (nianxian > 10000000) {
            RenderSystem.setShaderColor(0.00f, 1.00f, 1.00f, 1.0f);
        } else if (nianxian > 1000000) {
            RenderSystem.setShaderColor(1.00f, 0.60f, 0.00f, 1.0f);
        } else if (nianxian > 100000) { // 十万年：红色
            RenderSystem.setShaderColor(1.0f, 0, 0, 1.0f);
        } else if (nianxian >= 10000) { // 万年：黑色
            RenderSystem.setShaderColor(0.1f, 0.1f, 0.1f, 1.0f);
        } else if (nianxian >= 1000) { // 千年：紫色
            RenderSystem.setShaderColor(0.7f, 0, 1.0f, 1.0f);
        } else if (nianxian >= 100) { // 百年：黄色
            RenderSystem.setShaderColor(1.0f, 1.0f, 0, 1.0f);
        } else { // 十年：白色
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private int getNianxianColor(int nianxian) {
        if (nianxian >= 100000000) { // 亿年
            return 0x999999; // 灰色
        } else if (nianxian >= 10000000) { // 千万年
            return 0x00A2FF; // 蓝色 (亮蓝)
        } else if (nianxian >= 1000000) { // 百万年
            return 0xFFD700; // 橙金色 (金黄色)
        } else if (nianxian >= 100000) { // 十万年
            return 0xFF0000; // 红色
        } else if (nianxian >= 10000) { // 万年
            return 0x333333; // 深黑色/深灰色
        } else if (nianxian >= 1000) { // 千年
            return 0xA020F0; // 紫色
        } else if (nianxian >= 100) { // 百年
            return 0xFFFF00; // 黄色
        }
        return 0xFFFFFF; // 十年/普通：白色
    }


}