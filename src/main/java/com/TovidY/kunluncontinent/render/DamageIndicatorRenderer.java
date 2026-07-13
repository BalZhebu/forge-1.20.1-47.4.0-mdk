package com.TovidY.kunluncontinent.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DamageIndicatorRenderer {
    private static final List<IndicatorInstance> INDICATORS = new CopyOnWriteArrayList<>();
    public static class IndicatorInstance {
        public final String text;
        public final int color;
        public double x, y, z;
        public int age;
        public final int maxAge = 30;
        public final double speedY;

        public IndicatorInstance(String text, int color, Vec3 pos) {
            this.text = text;
            this.color = color;
            this.x = pos.x + (Math.random() - 0.5) * 0.5;
            this.y = pos.y;
            this.z = pos.z + (Math.random() - 0.5) * 0.5;
            this.speedY = 0.04 + Math.random() * 0.02;
        }

        public void tick() {
            this.y += speedY; // 往上飘
            this.age++;
        }
    }

    public static void addIndicator(String text, int color, Vec3 pos) {
        INDICATORS.add(new IndicatorInstance(text, color, pos));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Iterator<IndicatorInstance> iterator = INDICATORS.iterator();
            while (iterator.hasNext()) {
                IndicatorInstance instance = iterator.next();
                instance.tick();
                if (instance.age >= instance.maxAge) {
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (INDICATORS.isEmpty()) return;

            Minecraft mc = Minecraft.getInstance();
            Vec3 cameraPos = event.getCamera().getPosition();
            PoseStack poseStack = event.getPoseStack();
            Font font = mc.font;
            EntityRenderDispatcher renderDispatcher = mc.getEntityRenderDispatcher();

            for (IndicatorInstance ind : INDICATORS) {
                poseStack.pushPose();

                double renderX = ind.x - cameraPos.x;
                double renderY = ind.y - cameraPos.y;
                double renderZ = ind.z - cameraPos.z;
                poseStack.translate(renderX, renderY, renderZ);

                poseStack.mulPose(renderDispatcher.cameraOrientation());

                float scale = 0.03F;
                poseStack.scale(-scale, -scale, scale);

                Matrix4f matrix4f = poseStack.last().pose();
                float textWidth = (float) (-font.width(ind.text) / 2);

                int alpha = 255;
                if (ind.age > 20) {
                    float ratio = (float) (ind.maxAge - ind.age) / 10f;
                    alpha = (int) (ratio * 255);
                }

                int finalColor = (ind.color & 0x00FFFFFF) | (alpha << 24);

                font.drawInBatch(ind.text, textWidth, 0, finalColor, false, matrix4f, net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

                poseStack.popPose();
            }
        }
    }
}
