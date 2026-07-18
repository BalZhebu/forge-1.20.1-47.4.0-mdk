package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.tower.floor.ClientTimerManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModUiRenderHandler {

    public static final ResourceLocation jingshenli = new ResourceLocation(KlMain.MOD_ID, "textures/gui/testing/jingshenli.png");
    public static final ResourceLocation health = new ResourceLocation(KlMain.MOD_ID, "textures/gui/testing/healthy.png");
    public static final ResourceLocation food = new ResourceLocation(KlMain.MOD_ID, "textures/gui/testing/baoshidu.png");
    public static final ResourceLocation exp = new ResourceLocation(KlMain.MOD_ID, "textures/gui/testing/exp.png");
    public static final ResourceLocation tubza = new ResourceLocation(KlMain.MOD_ID, "textures/gui/testing/tubza.png");

    private static final int BAR_X = 58;
    private static final int BAR_W = 87;

    public static int TEST_X = 48;
    public static int TEST_Y = 100;

    private static final int HEALTH_Y = 30;
    private static final int FOOD_Y = 42;
    private static final int JINGSHEN_Y = 53;
    private static final int EXP_Y = 65;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Player entity = Minecraft.getInstance().player;
        if (entity == null) return;

        // 幻境剩余时间渲染（不随 UI 缩放平移）
        int seconds = ClientTimerManager.getRemainingSeconds();
        if (seconds > 0 && !Minecraft.getInstance().options.hideGui) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = Minecraft.getInstance().font;
            String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
            int textColor = (seconds <= 10) ? 0xFF5555 : 0x55FFFF;
            Component labelComponent = Component.literal("[幻境剩余时间] ").withStyle(Style.EMPTY.withColor(0xFF55FF));
            Component timeComponent = Component.literal(timeStr).withStyle(Style.EMPTY.withColor(textColor));
            Component finalComponent = Component.empty().append(labelComponent).append(timeComponent);
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            int x = (width - font.width(finalComponent)) / 2;
            int y = height - 85;

            guiGraphics.drawString(font, finalComponent, x, y, 0xFFFFFF, true);
        }

        // 背景图标（tubza）渲染
        entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isConfigOpen(6)) {
                PoseStack pose = event.getGuiGraphics().pose();
                pose.pushPose();

                // 应用平移与缩放
                pose.translate(0.0f, (float) cap.getUiOffsetY(), 0.0f);
                float scale = cap.getUiScale();
                pose.scale(scale, scale, 1.0f);

                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                event.getGuiGraphics().blit(tubza, 2, 1, 170, 104, 0, 0, 256, 128, 256, 128);

                RenderSystem.depthMask(true);
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();

                pose.popPose();
            }
        });

        // 打坐/修炼时间文本
        boolean isMeditating = entity.getVehicle() instanceof net.minecraft.world.entity.decoration.ArmorStand;
        if (isMeditating) {
            int x = event.getWindow().getGuiScaledWidth();
            int y = event.getWindow().getGuiScaledHeight();
            entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                int timeLeft = cap.getXiulianTime();
                int color = timeLeft > 120 ? 0x00FFFF : 0xFF0000;
                String timerText = "§l当前剩余修炼时间: §e" + (timeLeft / 60) + "分" + (timeLeft % 60) + "秒";
                event.getGuiGraphics().drawCenteredString(Minecraft.getInstance().font, timerText, x / 2, y - 100, color);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) return;

        NamedGuiOverlay overlay = event.getOverlay();
        String path = overlay.id().getPath();

        // 替换原版血条/状态栏
        if (path.equals("player_health") || path.contains("health")) {
            event.setCanceled(true);
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.isConfigOpen(6)) {
                    PoseStack pose = event.getGuiGraphics().pose();
                    pose.pushPose();

                    // 应用平移与缩放
                    pose.translate(0.0f, (float) cap.getUiOffsetY(), 0.0f);
                    float scale = cap.getUiScale();
                    pose.scale(scale, scale, 1.0f);

                    renderPlayerHealth(event);

                    pose.popPose();
                }
            });
        }
        else if (path.equals("food_level") || path.contains("food")) {
            event.setCanceled(true); // 隐藏原版饱食度
        }
    }

    private static void renderPlayerHealth(RenderGuiOverlayEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            PoseStack pose = guiGraphics.pose();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // 生命条
            double healthRatio = player.getMaxHealth() > 0 ? (double) player.getHealth() / player.getMaxHealth() : 0;
            int currentWidthH = (int) (BAR_W * Math.min(1.0, healthRatio));
            int uWidthH = (int) (127 * Math.min(1.0, healthRatio));
            guiGraphics.blit(health, BAR_X, HEALTH_Y, currentWidthH, 4, 0, 0, uWidthH, 4, 127, 4);

            // 体力条
            double foodRatio = player.getFoodData().getFoodLevel() / 20.0;
            int currentWidthF = (int) (BAR_W * Math.min(1.0, foodRatio));
            int uWidthF = (int) (127 * Math.min(1.0, foodRatio));
            guiGraphics.blit(food, BAR_X, FOOD_Y, currentWidthF, 4, 0, 0, uWidthF, 4, 127, 4);

            // 精神力条
            double jsRatio = capability.getMaxjingshenli() > 0 ? (double) capability.getJingshenli() / capability.getMaxjingshenli() : 0;
            int currentWidthJ = (int) (BAR_W * Math.min(1.0, jsRatio));
            int uWidthJ = (int) (127 * Math.min(1.0, jsRatio));

            pose.pushPose();
            pose.translate(0.0f, 0.5f, 0.0f);
            guiGraphics.blit(jingshenli, BAR_X, JINGSHEN_Y, currentWidthJ, 4, 0, 0, uWidthJ, 4, 127, 4);
            pose.popPose();

            // 经验条
            double expRatio = capability.getMaxjingyan() > 0 ? (double) capability.getJingyan() / capability.getMaxjingyan() : 0;
            int currentWidthE = (int) (BAR_W * Math.min(1.0, expRatio));
            int uWidthE = (int) (127 * Math.min(1.0, expRatio));
            guiGraphics.blit(exp, BAR_X, EXP_Y, currentWidthE, 4, 0, 0, uWidthE, 4, 127, 4);

            // 缩放文字（这里继承外部整体缩放的基础上再缩小一倍 0.5f）
            pose.pushPose();
            pose.scale(0.5f, 0.5f, 1.0f);
            String healthInfo = "生命：" + formatBigNum(player.getHealth()) + "/" + formatBigNum(player.getMaxHealth());
            String foodInfo = "体力: " + player.getFoodData().getFoodLevel() + "/20";
            String jsInfo = "精神力: " + formatBigNum(capability.getJingshenli()) + "/" + formatBigNum(capability.getMaxjingshenli());
            String expInfo = "经验: " + formatBigNum(capability.getJingyan()) + "/" + formatBigNum(capability.getMaxjingyan());

            int textWidthH = Minecraft.getInstance().font.width(healthInfo);
            int textWidthF = Minecraft.getInstance().font.width(foodInfo);
            int textWidthJ = Minecraft.getInstance().font.width(jsInfo);
            int textWidthE = Minecraft.getInstance().font.width(expInfo);

            int centerTargetX2 = (BAR_X * 2) + BAR_W;
            guiGraphics.drawString(Minecraft.getInstance().font, healthInfo, centerTargetX2 - (textWidthH / 2), HEALTH_Y * 2, 0xFFFFFF, true);
            guiGraphics.drawString(Minecraft.getInstance().font, foodInfo, centerTargetX2 - (textWidthF / 2), FOOD_Y * 2, 0xFFFFFF, true);
            guiGraphics.drawString(Minecraft.getInstance().font, jsInfo, centerTargetX2 - (textWidthJ / 2), JINGSHEN_Y * 2, 0x55FFFF, true);
            guiGraphics.drawString(Minecraft.getInstance().font, expInfo, centerTargetX2 - (textWidthE / 2), EXP_Y * 2, 0x00FF00, true);
            pose.popPose();

            // 等级渲染
            pose.pushPose();
            float levelScale = 0.7f;
            pose.scale(levelScale, levelScale, 1.0f);
            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    String.valueOf(capability.getDengji()),
                    TEST_X,
                    TEST_Y,
                    0xFFD700
            );
            pose.popPose();
        });
    }

    private static String formatBigNum(float value) {
        if (value >= 100000000) return String.format("%.2f亿", value / 100000000.0);
        return String.valueOf((long) value);
    }
}