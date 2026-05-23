package com.TovidY.kunluncontinent.screen.guide;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class GuidBookScreen extends AbstractContainerScreen<GuideBookMenu> {

    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(KlMain.MOD_ID, "textures/gui/guide_book.png");

    private static final ResourceLocation LEFT_BUTTON = new ResourceLocation(KlMain.MOD_ID, "textures/gui/right_book.png");
    private static final ResourceLocation LEFT_BUTTON_HOVER = new ResourceLocation(KlMain.MOD_ID, "textures/gui/right_book_max.png");
    private static final ResourceLocation RIGHT_BUTTON = new ResourceLocation(KlMain.MOD_ID, "textures/gui/left_book.png");
    private static final ResourceLocation RIGHT_BUTTON_HOVER = new ResourceLocation(KlMain.MOD_ID, "textures/gui/left_book_max.png");

    private int currentPage = 0;
    private List<FormattedCharSequence> cachedLines;
    private static final int LINES_PER_PAGE = 13;
    private static final int TEXT_WIDTH = 115;

    public GuidBookScreen(GuideBookMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 292;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        Component totalContent = Component.translatable("guide.kunlun.chapter1.content");
        this.cachedLines = this.font.split(totalContent, TEXT_WIDTH);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(new ImageButton(x + 245, y + 155, 18, 10, 0, 0, 0, RIGHT_BUTTON, 18, 10, b -> {
            if ((currentPage + 1) * (LINES_PER_PAGE * 2) < cachedLines.size()) {
                currentPage++;
                this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
            }
        }) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                ResourceLocation texture = this.isHovered ? RIGHT_BUTTON_HOVER : RIGHT_BUTTON;
                graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 10);
                RenderSystem.disableBlend();
            }
        });
        this.addRenderableWidget(new ImageButton(x + 25, y + 155, 18, 10, 0, 0, 0, LEFT_BUTTON, 18, 10, b -> {
            if (currentPage > 0) {
                currentPage--;
                this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
            }
        }) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();

                ResourceLocation texture = this.isHovered ? LEFT_BUTTON_HOVER : LEFT_BUTTON;

                graphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 10);
                RenderSystem.disableBlend();
            }
        });
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 绘制标题
        graphics.drawString(this.font, this.title, 25, 15, 0x404040, false);

        int lineY = 12;
        int startLine = currentPage * (LINES_PER_PAGE * 2);

        for (int i = 0; i < LINES_PER_PAGE * 2; i++) {
            int lineIndex = startLine + i;
            if (lineIndex >= cachedLines.size()) break;

            FormattedCharSequence line = cachedLines.get(lineIndex);

            if (i < LINES_PER_PAGE) {
                graphics.drawString(this.font, line, 21, lineY + (i * 11), 0x303030, false);
            } else {
                graphics.drawString(this.font, line, 159, lineY + ((i - LINES_PER_PAGE) * 11), 0x303030, false);
            }
        }
        int totalPages = (int) Math.ceil((double) cachedLines.size() / (LINES_PER_PAGE * 2));
        String pageCounter = (currentPage + 1) + " / " + totalPages;
        graphics.drawString(this.font, pageCounter, this.imageWidth / 2 - this.font.width(pageCounter) / 2 + 30, 160, 0x808080, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(BOOK_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 292, 180);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        if (this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PUT, 1.0F, 1.0F);
        }
        super.onClose();
    }
}