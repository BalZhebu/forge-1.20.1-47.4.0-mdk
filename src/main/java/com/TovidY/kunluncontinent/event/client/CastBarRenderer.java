package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 吟唱进度条（原版剑形贴图）。
 *
 * <p>数据源从「两个静态字段」换成 {@link CastClientState} —— 因为吟唱状态现在按实体记账，
 * 这样旁边的人也能摆出结印姿势。画面表现保持原样，没有额外改动。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT)
public class CastBarRenderer {

    private static final ResourceLocation EMPTY_BAR = new ResourceLocation(KlMain.MOD_ID, "textures/gui/empty_skill_progress.png");
    private static final ResourceLocation FULL_BAR = new ResourceLocation(KlMain.MOD_ID, "textures/gui/skill_progress.png");

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        CastClientState.tick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            // 断线 / 切世界：清干净，免得残留状态让别的实体一直举着手
            CastClientState.clearAll();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        CastClientState.Cast cast = CastClientState.get(mc.player.getId());
        if (cast == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick();

        // 平滑插值计算进度 (0.0f ~ 1.0f)
        float smoothRemaining = Math.max(0, cast.remainingTicks - partialTick);
        float progress = 1.0f - (smoothRemaining / cast.maxTicks);
        progress = Math.min(1.0f, Math.max(0.0f, progress));

        int barWidth = 128;
        int barHeight = 72;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = screenWidth / 2 - barWidth / 2;
        int y = screenHeight / 2 + 40;
        int filledWidth = (int) (barWidth * progress);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.blit(EMPTY_BAR, x, y, 0, 0, barWidth, barHeight, barWidth, barHeight);
        if (filledWidth > 0) {
            graphics.blit(FULL_BAR, x, y, 0, 0, filledWidth, barHeight, barWidth, barHeight);
        }

        RenderSystem.disableBlend();

        String titleText = "正在引导魂技...";
        String percentText = (int) (progress * 100) + "%";

        graphics.drawString(mc.font, Component.literal(titleText),
                x + barWidth / 2 - mc.font.width(titleText) / 2,
                y + 12, 0xFFFFAA00, true);
        graphics.drawString(mc.font, Component.literal(percentText),
                x + barWidth / 2 - mc.font.width(percentText) / 2,
                y + barHeight - 10, 0xFFE0E0E0, true);
    }
}
