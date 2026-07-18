package com.TovidY.kunluncontinent.entity.hunhe;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

//魂核渲染
public class HunheRender extends EntityRenderer<HunheEntity> {

    public HunheRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HunheEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 1. 玩家配置可见性检查
        net.minecraft.client.player.LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            boolean isVisible = localPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.isConfigOpen(5)).orElse(true);
            if (!isVisible) return;
        }

        float value = entity.getValue();
        float gameTime = entity.level().getGameTime() + partialTicks;

        poseStack.pushPose();

        // 上下平缓悬浮动画
        float floatOffset = (float) Math.sin(gameTime * 0.05f) * 0.08f;
        poseStack.translate(0.0D, 0.6D + floatOffset, 0.0D);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 获取品质颜色 (RGB)
        float[] color = getSoulColor(value, gameTime);
        float r = color[0], g = color[1], b = color[2];

        // 基础大小计算 (加一点点呼吸缩放)
        float pulse = (float) Math.sin(gameTime * 0.1f) * 0.02f;
        float baseScale = (float) (0.25f + Math.sqrt(Math.max(0, value)) * 0.03f) + pulse;

        // 3. 纯代码计算Procedural光晕 (面向玩家)
        renderProceduralGlow(poseStack, baseScale * 2.2f, r, g, b, 0.35f);

        // 4. 渲染纯代码八面体晶核
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime * 1.5f)); // 顺时针自转
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.sin(gameTime * 0.03f) * 10f)); // 微倾斜晃动
        renderOctahedronCore(poseStack, baseScale, r, g, b, 0.85f);
        poseStack.popPose();

        // 还原渲染状态
        RenderSystem.depthMask(true); // 还原深度写入
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 纯代码数学渐变光晕 (不依赖任何贴图，中心亮、边缘完全透明，绝无正方形边框)
     */
    private void renderProceduralGlow(PoseStack poseStack, float size, float r, float g, float b, float maxAlpha) {
        poseStack.pushPose();
        Quaternionf cameraOrientation = this.entityRenderDispatcher.cameraOrientation();
        poseStack.mulPose(cameraOrientation); // 始终面向玩家视角

        Matrix4f mat = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int segments = 24; // 决定光晕圆润度
        float radius = size / 2.0f;
        float slice = (float) (Math.PI * 2 / segments);

        // 用三角扇的方式画一个圆：中心 Alpha 为 maxAlpha，外圈 Alpha 为 0.0f（完美自然过渡）
        for (int i = 0; i < segments; i++) {
            float a1 = i * slice;
            float a2 = (i + 1) * slice;

            float x1 = (float) Math.cos(a1) * radius;
            float y1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float y2 = (float) Math.sin(a2) * radius;

            // 中心点（高亮半透明）
            builder.vertex(mat, 0, 0, 0).color(r, g, b, maxAlpha).endVertex();
            // 边缘点 1（完全透明）
            builder.vertex(mat, x1, y1, 0).color(r, g, b, 0.0f).endVertex();
            // 边缘点 2（完全透明）
            builder.vertex(mat, x2, y2, 0).color(r, g, b, 0.0f).endVertex();
        }

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
    }

    /**
     * 纯代码三维八面体晶核（带上下顶点渐变透明度，极具水晶感）
     */
    private void renderOctahedronCore(PoseStack poseStack, float scale, float r, float g, float b, float alpha) {
        poseStack.pushPose();
        Matrix4f mat = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        float height = scale * 1.3f;   // 晶核上下尖角高度
        float radius = scale * 0.65f;  // 中间腰部半径

        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        // 中间水平平面的 4 个顶点坐标
        float[][] midPoints = {
                {radius, 0, 0},
                {0, 0, radius},
                {-radius, 0, 0},
                {0, 0, -radius}
        };

        // 上下顶点 Alpha 设稍微低一点，中间腰部 Alpha 高一点，呈现水晶透光感
        float topBottomAlpha = alpha * 0.6f;

        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;

            // 1. 上半金字塔 (4个三角形)
            builder.vertex(mat, 0, height, 0).color(r, g, b, topBottomAlpha).endVertex();
            builder.vertex(mat, midPoints[i][0], 0, midPoints[i][2]).color(r, g, b, alpha).endVertex();
            builder.vertex(mat, midPoints[next][0], 0, midPoints[next][2]).color(r, g, b, alpha).endVertex();

            // 2. 下半金字塔 (4个三角形)
            builder.vertex(mat, 0, -height, 0).color(r, g, b, topBottomAlpha).endVertex();
            builder.vertex(mat, midPoints[next][0], 0, midPoints[next][2]).color(r, g, b, alpha).endVertex();
            builder.vertex(mat, midPoints[i][0], 0, midPoints[i][2]).color(r, g, b, alpha).endVertex();
        }

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
    }

    /**
     * 根据魂核 Value 决定色彩
     */
    private float[] getSoulColor(float value, float time) {
        float alphaPulse = 0.85f + (float) Math.sin(time * 0.1f) * 0.1f;

        if (value >= 200) {
            return new float[]{1.0f, 0.85f, 0.2f}; // 神级/百万年：灿金色
        } else if (value >= 170) {
            return new float[]{0.95f, 0.1f, 0.1f}; // 十万年：血红色
        } else if (value >= 130) {
            return new float[]{0.5f, 0.0f, 0.85f}; // 万年：深邃紫黑
        } else if (value >= 80) {
            return new float[]{0.8f, 0.25f, 1.0f}; // 千年：尊贵紫
        } else if (value >= 40) {
            return new float[]{0.95f, 0.85f, 0.1f};// 百年：亮黄色
        } else if (value >= 20) {
            return new float[]{0.9f, 0.95f, 1.0f}; // 十年：冰白色
        } else {
            return new float[]{0.4f, 0.65f, 0.9f}; // 普通：淡蓝色
        }
    }

    @Override
    public ResourceLocation getTextureLocation(HunheEntity entity) {
        return null; // 彻底不使用材质贴图
    }
}