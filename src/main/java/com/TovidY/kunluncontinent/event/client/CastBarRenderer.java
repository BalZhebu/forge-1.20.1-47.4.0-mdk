package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT)
public class CastBarRenderer {
    public static int remainingTicks = 0;
    public static int maxTicks = 0;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0) {
            remainingTicks--;
        }
    }
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (remainingTicks <= 0) return;
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int width = 120;
        int height = 8;
        int x = mc.getWindow().getGuiScaledWidth() / 2 - width / 2;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + 60;
        float progress = 1.0f - ((float) remainingTicks / maxTicks);
        int filledWidth = (int) (width * progress);
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0x88000000);
        graphics.fill(x, y, x + width, y + height, 0xFF333333);
        graphics.fill(x, y, x + filledWidth, y + height, 0xFFFFAA00);
        String text = "正在引导魂技...";
        graphics.drawCenteredString(mc.font, text, x + width / 2, y - 10, 0xFFFFFF);
        String percent = (int)(progress * 100) + "%";
        graphics.drawCenteredString(mc.font, percent, x + width / 2, y + height + 2, 0xAAAAAA);
    }
}
