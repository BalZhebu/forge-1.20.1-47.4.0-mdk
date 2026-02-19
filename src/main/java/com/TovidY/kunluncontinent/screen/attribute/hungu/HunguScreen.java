package com.TovidY.kunluncontinent.screen.attribute.hungu;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketSyncPage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HunguScreen extends AbstractContainerScreen<HunguMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/screens/hungu.png");

    public HunguScreen(HunguMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 322;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("属性"), b -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketSyncPage(0));
        }).bounds(this.leftPos + 5, this.topPos - 20, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("魂骨"), b -> {})
                .bounds(this.leftPos + 47, this.topPos - 20, 40, 20).build());
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

        int modelX = this.leftPos + 160;
        int modelY = this.topPos + 85;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, modelX, modelY, 30, (float) modelX - mouseX, (float) modelY - mouseY, this.minecraft.player);
    }
}
