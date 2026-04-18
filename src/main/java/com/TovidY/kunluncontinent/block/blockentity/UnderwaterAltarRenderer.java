package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class UnderwaterAltarRenderer implements BlockEntityRenderer<UnderwaterAltarTile> {

    public static final ResourceLocation HUNHUAN = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/picture/particletext.png");

    public UnderwaterAltarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(UnderwaterAltarTile tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (!tile.isSummoning()) return;

        int tick = tile.getSummonTick();
        float time = 200 - (tick - partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);

        applySummonColor(time);

        float scale = 1.0f;
        if (time > 180) {
            float progress = (time - 180) / 20f;
            scale = 1.0f - progress;
            poseStack.translate(0, -progress * 0.5, 0);
        }
        poseStack.scale(scale, 1.0f, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 4.0f)); // 加快点旋转速度好观察

        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, HUNHUAN);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float b = 6.0f;
        int r = 255, g = 255, bl = 255, a = 255;

        bufferbuilder.vertex(matrix4f, -b, 0, -b).uv(0, 0).color(r, g, bl, a).endVertex();
        bufferbuilder.vertex(matrix4f, -b, 0, b).uv(0, 1).color(r, g, bl, a).endVertex();
        bufferbuilder.vertex(matrix4f, b, 0, b).uv(1, 1).color(r, g, bl, a).endVertex();
        bufferbuilder.vertex(matrix4f, b, 0, -b).uv(1, 0).color(r, g, bl, a).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }

    private void applySummonColor(float time) {
        if (time < 25) RenderSystem.setShaderColor(1, 1, 1, 1);       // 白
        else if (time < 50) RenderSystem.setShaderColor(1, 1, 0, 1);  // 黄
        else if (time < 75) RenderSystem.setShaderColor(0.8f, 0, 0.8f, 1); // 紫
        else if (time < 100) RenderSystem.setShaderColor(0, 0, 0, 1); // 黑
        else if (time < 125) RenderSystem.setShaderColor(1, 0, 0, 1); // 红
        else if (time < 150) RenderSystem.setShaderColor(1, 0.5f, 0, 1); // 橙金
        else RenderSystem.setShaderColor(1, 0.5f, 0, 1);
    }

}
