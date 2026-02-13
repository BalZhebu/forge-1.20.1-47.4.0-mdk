package com.TovidY.kunluncontinent.event.client;


import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//玩家物品栏和属性界面渲染

@Mod.EventBusSubscriber({Dist.CLIENT})
public class RenderGui {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGuiEvent.Pre event) {
        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();

        Player entity = Minecraft.getInstance().player;
        if (entity == null) return;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (true) {
            event.getGuiGraphics().blit(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/screens/outline_footer.png"), w / 2 - 127, h - 53, 0, 0, 256, 53, 256, 53);
            event.getGuiGraphics().blit(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/screens/outline_top.png"), 2, 1, 0, 0, 157, 55, 157, 55);
            event.getGuiGraphics().blit(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/screens/fill_exp.png"), w / 2 - 64, h - 26, 0, 0, 130, 3, 130, 3);
        }

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

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}