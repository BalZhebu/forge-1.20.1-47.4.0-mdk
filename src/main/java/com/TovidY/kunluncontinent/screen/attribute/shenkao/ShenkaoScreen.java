package com.TovidY.kunluncontinent.screen.attribute.shenkao;

import com.TovidY.kunluncontinent.godclass.buff.GodBuff;
import com.TovidY.kunluncontinent.godclass.buff.GodBuffs;
import com.TovidY.kunluncontinent.godclass.interfac.GodClientData;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.C2SCheckTaskPacket;
import com.TovidY.kunluncontinent.network.client.C2SEnterCelestial;
import com.TovidY.kunluncontinent.screen.KunlunGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Collections;
import com.TovidY.kunluncontinent.screen.attribute.AttributeTabs;

/**
 * 神考 / 神祇面板。
 *
 * <p>同一块面板按状态分两种形态：</p>
 * <ul>
 *   <li><b>神考中</b>（{@code !isGod}）：显示当前神位、考核阶段、任务目标、奖励。</li>
 *   <li><b>已成神</b>（{@code isGod}）：变成<b>神祇面板</b> —— 不显示考核内容，
 *       改为展示「当前神位 / 称谓 / 被动增幅」，并在角落放「进入神界」按钮。</li>
 * </ul>
 *
 * <p>⚠️ 面板<b>没有任何物品栏槽位</b>（见 {@code ShenkaoMenu}）。</p>
 */
public class ShenkaoScreen extends AbstractContainerScreen<ShenkaoMenu> implements AttributeTabs.Host {

    public ShenkaoScreen(ShenkaoMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 330;
        this.imageHeight = 190;

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        this.imageWidth = 330;
        this.imageHeight = 190;
        super.init();

        // 顶部页签：全部由注册表统一生成（新增面板只需在 AttributeTabs 注册一行）
        AttributeTabs.buildTabs(this, AttributeTabs.PAGE_SHENKAO);

        int top = this.topPos;

        // 未封神：才需要「进度检查」按钮
        if (!GodClientData.isGod) {
            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + this.imageWidth - 85, top + 12, 75, 18,
                    Component.literal("进度检查"),
                    b -> NetworkHandler.INSTANCE.sendToServer(new C2SCheckTaskPacket()),
                    () -> Collections.singletonList(Component.literal("点击检查并提交神考进度"))
            ));
        } else {
            // ⭐ 神祇面板：右下角一个按钮，按当前维度自动切换「进入/离开神界」（省面板空间）
            boolean inCelestial = isInCelestial();
            this.addRenderableWidget(new KunlunGuiHelper.HandDrawnButton(
                    this.font, this.leftPos + this.imageWidth - 95, top + this.imageHeight - 28, 80, 18,
                    Component.literal(inCelestial ? "§6离开神界" : "§b进入神界"),
                    b -> NetworkHandler.INSTANCE.sendToServer(new C2SEnterCelestial()),
                    () -> Collections.singletonList(Component.literal(inCelestial
                            ? "§7离开天境，返回主世界\n§7有主世界重生点 → 回那里\n§7没有 → 回世界出生点"
                            : "§7进入天境\n§7有天境重生点 → 回床边\n§7没有 → 寻一处露天陆地"))
            ));
        }

        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    /**
     * 客户端当前是否在神界维度 —— <b>只用于按钮文字 / tooltip</b>。
     *
     * <p>真正的进/出判定在服务端 {@code CelestialTeleport#toggle} 里做（以服务端维度为准），
     * 这里只影响玩家看到的提示文案。</p>
     */
    private static boolean isInCelestial() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        return mc.level != null && mc.level.dimension()
                == com.TovidY.kunluncontinent.worldgen.ModDimensions.CELESTIAL_REALM_LEVEL_KEY;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int textLeft = this.leftPos + 15;
        int top = this.topPos;

        if (GodClientData.isGod) {
            renderGodPanel(guiGraphics, textLeft, top);
        } else {
            renderExamPanel(guiGraphics, textLeft, top);
        }
    }

    // ==================== 神考中 ====================

    private void renderExamPanel(GuiGraphics g, int textLeft, int top) {
        String titleStr = "当前神位: §c" + (GodClientData.godName.isEmpty() ? "无" : GodClientData.godName);
        String stageStr = "考核阶段: 第 " + GodClientData.currentStage + " 考";
        g.drawString(this.font, titleStr, textLeft, top + 14, 0xFFFFFF, true);
        g.drawString(this.font, stageStr, textLeft, top + 28, 0x55FF55, true);

        g.fill(this.leftPos + 10, top + 40, this.leftPos + this.imageWidth - 10, top + 41, 0xFF8A6D3B);

        String rawContent = GodClientData.currentDesc.replace("\n", "");
        g.drawString(this.font, "§e考核目标: §f" + rawContent, textLeft, top + 48, 0xFFFFFF, true);
        g.drawString(this.font, "当前进度: §e" + GodClientData.taskProgress
                + " §f/ §a" + GodClientData.requiredCount, textLeft, top + 62, 0xAAAAAA, true);

        g.drawString(this.font, "§6考核奖励:", textLeft, top + 80, 0xFFFFFF, true);
        String rText = GodClientData.rewardDesc;
        if (rText != null && !rText.isEmpty()) {
            String[] rewardLines = rText.split("\n");
            for (int i = 0; i < rewardLines.length; i++) {
                g.drawString(this.font, rewardLines[i], textLeft, top + 94 + (i * 11), 0xDDDDDD, true);
            }
        }
    }

    // ==================== 神祇面板 ====================

    /**
     * 神祇面板：神位 + 称谓（占位）+ 被动增幅。
     *
     * <p>⭐ 被动增幅从 {@link GodBuffs#ALL_PASSIVES} 里按神位 id 查，
     * <b>新增神位被动这里自动多一行，不用改这个方法</b>。</p>
     */
    private void renderGodPanel(GuiGraphics g, int textLeft, int top) {
        int y = top + 14;

        // ① 当前神位
        String godName = GodClientData.godName.isEmpty() ? "无" : GodClientData.godName;
        g.drawString(this.font, "§6当前神位: §e" + godName, textLeft, y, 0xFFFFFF, true);
        y += 14;

        // ② 称谓（占位，等做完称谓系统再接）
        g.drawString(this.font, "§6称谓: §7— —（尚未开启）", textLeft, y, 0xFFFFFF, true);
        y += 16;

        // 分割线
        g.fill(this.leftPos + 10, y, this.leftPos + this.imageWidth - 10, y + 1, 0xFF8A6D3B);
        y += 8;

        // ③ 被动增幅
        g.drawString(this.font, "§d◆ 被动增幅", textLeft, y, 0xFFFFFF, true);
        y += 12;

        GodBuff buff = GodBuffs.of(GodClientData.godId);
        if (buff == null) {
            g.drawString(this.font, "§8（该神位暂无被动）", textLeft, y, 0xFFFFFF, true);
        } else {
            g.drawString(this.font, buff.toLine().getString(), textLeft, y, 0xFFFFFF, true);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics gui, float pPartialTick, int pMouseX, int pMouseY) {
        // 统一背景
        KunlunGuiHelper.renderKunlunBackground(gui, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);

        // 槽位衬底：神考/神祇面板**没有槽位**，所以这里什么都不画。
        // （保留原遍历会有 NPE 风险 —— menu.slots 为空列表时无需处理）
    }

    // ==================== AttributeTabs.Host 实现 ====================

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
