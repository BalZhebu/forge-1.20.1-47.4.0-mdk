package com.TovidY.kunluncontinent.tower.floor;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kunluncontinent", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientGodEffectManager {

    private static boolean isRitualActive = false;
    private static int currentTick = 0;
    private static int maxTicks = 300; // 默认初始化为 300 tick (15秒)
    private static String renderingGodName = "";

    public static void startRitual(int ticks, String godName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        isRitualActive = true;
        currentTick = 0;
        maxTicks = 300;
        renderingGodName = godName;
        mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isRitualActive) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            isRitualActive = false;
            return;
        }

        currentTick++;

        double remainingProgress = (double) currentTick / maxTicks;
        double radius = 2.5D * (1.0D - remainingProgress + 0.1D);
        double angle = currentTick * 0.25D;

        double x = player.getX() + radius * Math.cos(angle);
        double y = player.getY() + (currentTick % 30) * 0.08D;
        double z = player.getZ() + radius * Math.sin(angle);

        player.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 0, 0.03D, 0);
        player.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.1D, player.getZ(), 0, 0.05D, 0);
        if (currentTick % 2 == 0) {
            player.level().addParticle(ParticleTypes.GLOW, player.getX() + (player.getRandom().nextDouble() - 0.5), player.getY() + 2.3D, player.getZ() + (player.getRandom().nextDouble() - 0.5), 0, -0.02D, 0);
        }

        if (currentTick >= maxTicks) {
            isRitualActive = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);

            player.playSound(net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(CustomizeGuiOverlayEvent.DebugText event) {
        if (!isRitualActive) return;

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // 基于 300 满额 tick 计算绝对百分比
        double progress = Math.min(1.0D, (double) currentTick / maxTicks);
        int progressPercent = (int) (progress * 100);

        int totalBars = 20;
        int filledBars = (int) (progress * totalBars);
        StringBuilder progressBar = new StringBuilder("§7[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                progressBar.append("§6■");
            } else {
                progressBar.append("§8□");
            }
        }
        progressBar.append("§7]");

        String titleText = "§e§l★ " + renderingGodName + " §6§l神位种子融合中... ★";
        String barStr = progressBar.toString();
        String percentText = "§b融合进度: " + progressPercent + "%";

        int centerY = height / 2 + 40;
        net.minecraft.client.gui.GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.drawString(mc.font, titleText, (int)(width / 2.0F - mc.font.width(titleText) / 2.0F), centerY, 0xFFFFFF, true);
        guiGraphics.drawString(mc.font, barStr, (int)(width / 2.0F - mc.font.width(barStr) / 2.0F), centerY + 15, 0xFFFFFF, true);
        guiGraphics.drawString(mc.font, percentText, (int)(width / 2.0F - mc.font.width(percentText) / 2.0F), centerY + 30, 0xFFFFFF, true);
    }
}