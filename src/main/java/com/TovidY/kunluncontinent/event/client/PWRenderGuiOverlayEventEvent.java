package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import static net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.*;

//屏幕图标图片渲染代码
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PWRenderGuiOverlayEventEvent {
    public static final ResourceLocation jingshenli = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/jingshenli.png");
    public static final ResourceLocation health = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/health.png");
    public static final ResourceLocation food = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/fill_food.png");
    public static final ResourceLocation exp_kong = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/fill_exp_kong.png");
    public static final ResourceLocation exp = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/fill_exp.png");
    public static final ResourceLocation exp_bar_kong = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/exp_bar_kong.png");
    public static final ResourceLocation exp_bar = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/gui/exp_bar.png");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiOverlayEvent(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        String path = overlay.id().getPath();
        if (path.equals(PLAYER_HEALTH.id().getPath())) {
            event.setCanceled(true);
            renderPlayerHealth(event);
        } else if (path.equals(FOOD_LEVEL.id().getPath())) {
            event.setCanceled(true);
        }
        else if (path.equals(ARMOR_LEVEL.id().getPath()) || path.equals(AIR_LEVEL.id().getPath())) {
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            PoseStack poseStack = event.getGuiGraphics().pose();
            poseStack.pushPose();
            float offsetX = -(screenWidth / 2.0f - 91.0f) + 30.0f;
            float offsetY = -(screenHeight - 39.0f) + 70.0f;

            poseStack.translate(offsetX, offsetY, 0.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        String path = event.getOverlay().id().getPath();
        if (path.equals(ARMOR_LEVEL.id().getPath()) || path.equals(AIR_LEVEL.id().getPath())) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    private static void renderPlayerHealth(RenderGuiOverlayEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            PoseStack pose = guiGraphics.pose();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            double healthRatio = player.getMaxHealth() > 0 ? (double) player.getHealth() / player.getMaxHealth() : 0;
            int targetWidthH = 82;
            int targetHeightH = 5;
            int currentWidthH = (int) (targetWidthH * Math.min(1.0, healthRatio));
            int uWidthH = (int) (134 * Math.min(1.0, healthRatio));
            guiGraphics.blit(health, 61, 12, currentWidthH, targetHeightH, 0, 0, uWidthH, 8, 134, 8);

            double foodRatio = player.getFoodData().getFoodLevel() / 20.0;
            int targetWidthF = 72;
            int targetHeightF = 5;
            int currentWidthF = (int) (targetWidthF * Math.min(1.0, foodRatio));
            int uWidthF = (int) (117 * Math.min(1.0, foodRatio));
            guiGraphics.blit(food, 61, 25, currentWidthF, targetHeightF, 0, 0, uWidthF, 7, 117, 7);

            double jsRatio = capability.getMaxjingshenli() > 0 ? (double) capability.getJingshenli() / capability.getMaxjingshenli() : 0;
            int targetWidthJ = 62;
            int targetHeightJ = 5;
            int currentWidthJ = (int) (targetWidthJ * Math.min(1.0, jsRatio));
            int uWidthJ = (int) (232 * Math.min(1.0, jsRatio));
            guiGraphics.blit(jingshenli, 61, 38, currentWidthJ, targetHeightJ, 0, 0, uWidthJ, 21, 232, 21);

            int expX = screenWidth / 2 - 64;
            int expY = screenHeight - 26;
            double expRatio = capability.getMaxjingyan() > 0 ? (double) capability.getJingyan() / capability.getMaxjingyan() : 0;
            guiGraphics.blit(exp_kong, expX, expY, 0, 0, 130, 3, 130, 3);
            guiGraphics.blit(exp, expX, expY, 0, 0, (int) (130 * Math.min(1.0, expRatio)), 3, 130, 3);

            pose.pushPose();
            pose.scale(0.5f, 0.5f, 1.0f);
            String healthInfo ="生命："+ formatBigNum(player.getHealth()) + "/" + formatBigNum(player.getMaxHealth());
            guiGraphics.drawString(Minecraft.getInstance().font, healthInfo, 150, 25, 0xFFFFFF, true);
            String jsInfo = "精神力: " + formatBigNum(capability.getJingshenli()) + "/" + formatBigNum(capability.getMaxjingshenli());
            guiGraphics.drawString(Minecraft.getInstance().font, jsInfo, 128, 77, 0x55FFFF, true);
            String foodInfo = "体力: " + player.getFoodData().getFoodLevel() + "/20";
            guiGraphics.drawString(Minecraft.getInstance().font, foodInfo, 140, 51, 0xFFCC00, true);
            String expInfo = "经验: " + formatBigNum(capability.getJingyan()) + "/" + formatBigNum(capability.getMaxjingyan());
            int expTextX = (screenWidth / 2) * 2 - (Minecraft.getInstance().font.width(expInfo) / 2);
            guiGraphics.drawString(Minecraft.getInstance().font, expInfo, expTextX, (screenHeight - 32) * 2 + 10, 0xAAAAAA, true);
            pose.popPose();

            pose.pushPose();
            float levelScale = 0.7f;
            int centerX = screenWidth / 2 - 12;
            int centerY = screenHeight - 48 - 2;
            pose.translate(centerX + 10, centerY + 10, 0);
            pose.scale(levelScale, levelScale, 1.0f);
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, String.valueOf(capability.getDengji()), 2, 0, 0xFFD700);
            pose.popPose();
        });
    }

    private static String formatBigNum(float value) {
        if (value >= 100000000) {
            return String.format("%.2f亿", value / 100000000.0);
        }
        return String.valueOf((long) value);
    }

    private static void renderCustomExperienceBar(GuiGraphics guiGraphics, Player player, int width, int height) {
        PoseStack poseStack = guiGraphics.pose();
        int barX = 26;
        int barY = 62;
        int barWidth = 91;
        int barHeight = 5;
        int playerLevel = player.experienceLevel;
        float experienceProgress = player.experienceProgress;
        guiGraphics.blit(exp_bar_kong, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        int filledWidth = (int)(barWidth * experienceProgress);
        guiGraphics.blit(exp_bar, barX, barY, 0, 0, filledWidth, barHeight, barWidth, barHeight);

        poseStack.pushPose();
        float scale = 0.7f;
        poseStack.scale(scale, scale, 1f);
        String levelText = String.valueOf(playerLevel);
        int textWidth = (int)(Minecraft.getInstance().font.width(levelText) * scale);
        int textX = (int)((barX + (barWidth - textWidth)/2) / scale);
        int textY = (int)((barY - 6) / scale);
        guiGraphics.drawString(Minecraft.getInstance().font, levelText, textX, textY, 0x80FF20, false);
        poseStack.popPose();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().getPath().equals("experience_bar")) {
            event.setCanceled(true);
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                renderCustomExperienceBar(event.getGuiGraphics(), player, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
            }
        }
    }
}