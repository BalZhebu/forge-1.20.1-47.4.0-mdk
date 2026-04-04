package com.TovidY.kunluncontinent.screen;


import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class KluxTabButton extends Button {
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/screens/anniu.png");
    private final ItemStack icon;
    private final Component tooltip;
    private final boolean selected;

    // 图片的确切尺寸
    private static final int TEX_W = 28;
    private static final int TEX_H = 33;

    public KluxTabButton(int x, int y, int width, int height, ItemStack icon, Component tooltip, boolean selected, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.tooltip = tooltip;
        this.selected = selected;
    }
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();

        if (selected) {
            float bgScale = 1.2f;
            float offX = (this.width - (TEX_W * bgScale)) / 2f;
            float offY = (this.height - (TEX_H * bgScale)) / 2f;

            guiGraphics.pose().translate(this.getX() + offX, this.getY() + offY, 0);
            guiGraphics.pose().scale(bgScale, bgScale, bgScale);
            guiGraphics.blit(BUTTON_TEXTURE, 0, 0, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);
        } else {
            guiGraphics.setColor(1.2F, 1.2F, 1.2F, 1.0F);
            guiGraphics.blit(BUTTON_TEXTURE, this.getX(), this.getY(), 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();

        float scale = selected ? 1.5f : 1.2f;
        float iconSize = 16 * scale;

        float renderX = this.getX() + (this.width - iconSize) / 2f;

        float renderY = (this.getY() + (this.height - iconSize) / 2f) + 2.0f;

        guiGraphics.pose().translate(renderX, renderY, 0);
        guiGraphics.pose().scale(scale, scale, scale);

        guiGraphics.renderItem(this.icon, 0, 0);

        guiGraphics.pose().popPose();

        if (this.isHovered()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
        }
    }
}