package com.TovidY.kunluncontinent.screen.attribute.hunhuan;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.TovidY.kunluncontinent.screen.KluxTabButton;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class HunhuanScreen extends AbstractContainerScreen<HunhuanMenu> {
    private static final ResourceLocation HUNHUAN_ICON = new ResourceLocation(KlMain.MOD_ID, "textures/picture/particletext.png");

    // 翻页与武魂选择逻辑
    private static final int HUNHUAN_PER_PAGE = 10;
    private int selectedWuhunIndex = 0; // 当前界面选中的武魂索引
    private int currentHunhuanPage = 0;  // 当前魂环列表的页码
    private int totalHunhuanCount = 0;

    // 交互按钮
    private Button prevWuhunBtn;
    private Button nextWuhunBtn;
    private Button prevHunhuanPageBtn;
    private Button nextHunhuanPageBtn;

    public HunhuanScreen(HunhuanMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
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

        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, startX, startY, normalSize, normalSize,
                new ItemStack(ModItems.ATTRIBUTE_BUTTON.get()), Component.literal("属性面板"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0))
        ));

        int currentX = startX + normalSize + spacing;
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY, normalSize, normalSize,
                new ItemStack(ModItems.SOUL_BONE_BUTTON.get()), Component.literal("魂骨面板"), false,
                b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(1))
        ));

        currentX += (normalSize + spacing);
        this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                this.font, currentX, startY - 2, selectedSize, selectedSize,
                new ItemStack(ModItems.HUNHUAN_BUTTON.get()), Component.literal("魂环配置"), true, b -> {}
        ));

        currentX += (selectedSize + spacing);
        if (GodClientData.godName != null && !GodClientData.godName.equals("无")) {
            this.addRenderableWidget(new KunlunGuiHelper.KunlunTabButton(
                    this.font, currentX, startY, normalSize, normalSize,
                    new ItemStack(ModItems.SHENKAO_BUTTON.get()), Component.literal("神考面板"), false,
                    b -> NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(3))
            ));
        }

        // 初始化默认选中的武魂索引
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                List<String> list = cap.getWuhunListsname();
                if (list != null && !list.isEmpty()) {
                    int active = cap.getHunhuankuaiguan();
                    if (active >= 0 && active < list.size()) {
                        this.selectedWuhunIndex = active;
                    } else {
                        this.selectedWuhunIndex = 0;
                    }
                }
            });
        }

        // 2. 武魂标题两侧翻页按钮（顶部）
        int titleCenterY = this.topPos + 12;
        int titleCenterX = this.leftPos + this.imageWidth / 2;

        this.prevWuhunBtn = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> switchWuhun(-1))
                .bounds(titleCenterX - 75, titleCenterY, 16, 14).build());

        this.nextWuhunBtn = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> switchWuhun(1))
                .bounds(titleCenterX + 59, titleCenterY, 16, 14).build());

        // 3. 底部魂环备用自动翻页按钮
        int bottomY = this.topPos + 165;
        this.prevHunhuanPageBtn = this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (currentHunhuanPage > 0) currentHunhuanPage--;
        }).bounds(titleCenterX - 35, bottomY, 20, 16).build());

        this.nextHunhuanPageBtn = this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            int maxPage = (int) Math.ceil((double) totalHunhuanCount / HUNHUAN_PER_PAGE) - 1;
            if (currentHunhuanPage < maxPage) currentHunhuanPage++;
        }).bounds(titleCenterX + 15, bottomY, 20, 16).build());
    }

    private void switchWuhun(int dir) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            List<String> list = cap.getWuhunListsname();
            if (list != null && !list.isEmpty()) {
                selectedWuhunIndex += dir;
                if (selectedWuhunIndex < 0) {
                    selectedWuhunIndex = list.size() - 1;
                } else if (selectedWuhunIndex >= list.size()) {
                    selectedWuhunIndex = 0;
                }
                currentHunhuanPage = 0; // 切换武魂时重置魂环页码
            }
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 动态计算与管理按钮状态
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                List<String> list = cap.getWuhunListsname();
                boolean hasMultipleWuhun = list != null && list.size() > 1;
                this.prevWuhunBtn.active = hasMultipleWuhun;
                this.nextWuhunBtn.active = hasMultipleWuhun;

                int maxPage = (int) Math.ceil((double) totalHunhuanCount / HUNHUAN_PER_PAGE) - 1;
                this.prevHunhuanPageBtn.active = currentHunhuanPage > 0;
                this.nextHunhuanPageBtn.active = currentHunhuanPage < maxPage;
                this.prevHunhuanPageBtn.visible = maxPage > 0;
                this.nextHunhuanPageBtn.visible = maxPage > 0;
            });
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        // 1. 调用 KunlunGuiHelper 统一渲染全局水墨手绘背景
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        Player player = this.minecraft.player;
        if (player == null) return;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            List<String> wuhunList = capability.getWuhunListsname();

            String titleText = "§7无武魂";
            String activeWuhunName = null;

            if (wuhunList != null && !wuhunList.isEmpty()) {
                if (selectedWuhunIndex < 0 || selectedWuhunIndex >= wuhunList.size()) {
                    selectedWuhunIndex = 0;
                }
                activeWuhunName = wuhunList.get(selectedWuhunIndex);
                boolean isCurrentActive = (capability.getHunhuankuaiguan() == selectedWuhunIndex);
                titleText = "§6" + activeWuhunName + (isCurrentActive ? " §a[附体中]" : "");
            }

            // 2. 渲染顶部标题与分割线
            int centerX = this.leftPos + this.imageWidth / 2;
            int titleW = this.font.width(titleText);
            gui.drawString(this.font, titleText, centerX - titleW / 2, this.topPos + 14, 0xFFFFFF, true);
            gui.fill(this.leftPos + 10, this.topPos + 28, this.leftPos + this.imageWidth - 10, this.topPos + 29, 0xFF8A6D3B);

            // 3. 渲染魂环展示主体
            if (activeWuhunName != null) {
                renderHunhuan(gui, capability, activeWuhunName, this.leftPos, this.topPos, partialTicks);
            } else {
                totalHunhuanCount = 0;
            }

            // 4. 底部备用分页的页码显示
            int maxPage = (int) Math.ceil((double) totalHunhuanCount / HUNHUAN_PER_PAGE) - 1;
            if (maxPage > 0) {
                String pageStr = (currentHunhuanPage + 1) + " / " + (maxPage + 1);
                int pageW = this.font.width(pageStr);
                gui.drawString(this.font, pageStr, centerX - pageW / 2, this.topPos + 169, 0xE0E0E0, true);
            }
        });
    }

    private void renderHunhuan(GuiGraphics gui, PlayerAttributeCapability capability, String wuhunName, int x, int y, float partialTicks) {
        var monsterMap = capability.getMonsterCapabilityLists();
        if (monsterMap == null) return;

        var monsterList = monsterMap.get(wuhunName);
        if (monsterList == null || monsterList.isEmpty()) {
            totalHunhuanCount = 0;
            String emptyTip = "§7该武魂尚未吸收任何魂环";
            gui.drawString(this.font, emptyTip, x + (this.imageWidth - this.font.width(emptyTip)) / 2, y + 85, 0xAAAAAA, true);
            return;
        }

        totalHunhuanCount = monsterList.size();
        int startIndex = currentHunhuanPage * HUNHUAN_PER_PAGE;
        int endIndex = Math.min(startIndex + HUNHUAN_PER_PAGE, totalHunhuanCount);

        int startOffsetX = 38;
        int startOffsetY = 48;
        int hSpacing = 56;
        int vSpacing = 55;
        int itemsPerRow = 5;

        for (int i = startIndex; i < endIndex; i++) {
            var monsterAttr = monsterList.get(i);
            long nianxian = monsterAttr.getNianxian();
            int localIndex = i - startIndex;
            int row = localIndex / itemsPerRow;
            int col = localIndex % itemsPerRow;

            // 计算网格中心点
            int centerX = x + startOffsetX + col * hSpacing;
            int centerY = y + startOffsetY + row * vSpacing;

            int slotSize = 18; // 若你定义的背景框是 32x32，请改为 32
            KunlunGuiHelper.renderSlotBackground(gui, centerX - slotSize / 2, centerY - slotSize / 2, false);
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(centerX, centerY, 0);
            pose.scale(0.5f, 0.5f, 1.0f);

            float rotation = (this.minecraft.level.getGameTime() + partialTicks) * 0.05f;
            pose.mulPose(com.mojang.math.Axis.ZP.rotation(rotation));

            setHunhuanColor(nianxian);
            gui.blit(HUNHUAN_ICON, -30, -30, 0, 0, 60, 60, 60, 60);
            pose.popPose();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            String indexText = String.valueOf(i + 1);
            int indexW = this.font.width(indexText);
            int displayColor = getNianxianColor((int) nianxian);
            gui.drawString(this.font, indexText, centerX - indexW / 2, centerY - 4, displayColor, true);
            String yearText = nianxian + "年";
            int yearW = this.font.width(yearText);
            gui.drawString(this.font, yearText, centerX - yearW / 2, centerY + 14, 0xE0E0E0, true);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void setHunhuanColor(long nianxian) {
        if (nianxian >= 100000000) {
            RenderSystem.setShaderColor(0.00f, 1.00f, 0.00f, 1.0f);
        } else if (nianxian > 10000000) {
            RenderSystem.setShaderColor(0.00f, 1.00f, 1.00f, 1.0f);
        } else if (nianxian > 1000000) {
            RenderSystem.setShaderColor(1.00f, 0.60f, 0.00f, 1.0f);
        } else if (nianxian > 100000) {
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 1.0f);
        } else if (nianxian >= 10000) {
            RenderSystem.setShaderColor(0.1f, 0.1f, 0.1f, 1.0f);
        } else if (nianxian >= 1000) {
            RenderSystem.setShaderColor(0.7f, 0.0f, 1.0f, 1.0f);
        } else if (nianxian >= 100) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 0.0f, 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private int getNianxianColor(int nianxian) {
        if (nianxian >= 100000000) return 0x999999;
        if (nianxian >= 10000000)  return 0x00A2FF;
        if (nianxian >= 1000000)   return 0xFFD700;
        if (nianxian >= 100000)    return 0xFF0000;
        if (nianxian >= 10000)     return 0x555555;
        if (nianxian >= 1000)      return 0xA020F0;
        if (nianxian >= 100)       return 0xFFFF00;
        return 0xFFFFFF;
    }
}