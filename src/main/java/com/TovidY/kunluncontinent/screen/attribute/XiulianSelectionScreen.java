package com.TovidY.kunluncontinent.screen.attribute;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.PacketXiulianChoice;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// XiulianSelectionScreen.java (仅客户端运行)

public class XiulianSelectionScreen extends Screen {
    private final int xiulianSeconds;

    public XiulianSelectionScreen(int xiulianSeconds) {
        super(Component.literal("修炼选择"));
        this.xiulianSeconds = xiulianSeconds;
    }

    @Override
    protected void init() {
        int minutes = (int) Math.ceil(xiulianSeconds / 60.0f);
        int buttonWidth = 120;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 按钮 1：挂机打坐
        this.addRenderableWidget(Button.builder(Component.literal("挂机打坐 (" + minutes + "分钟)"), button -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketXiulianChoice(0));
            this.onClose();
        }).bounds(centerX - buttonWidth - 10, centerY + 10, buttonWidth, buttonHeight).build());

        // 按钮 2：一键闭关（跳过）
        this.addRenderableWidget(Button.builder(Component.literal("§c一键闭关 (直接完成)"), button -> {
            NetworkHandler.INSTANCE.sendToServer(new PacketXiulianChoice(1));
            this.onClose();
        }).bounds(centerX + 10, centerY + 10, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int minutes = (int) Math.ceil(xiulianSeconds / 60.0f);
        String tipText = "当前拥有可修炼时间: §e" + minutes + "§r 分钟 (" + xiulianSeconds + "秒)";
        String selectText = "请选择你的修炼方式：";

        graphics.drawCenteredString(this.font, tipText, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        graphics.drawCenteredString(this.font, selectText, this.width / 2, this.height / 2 - 15, 0xAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
