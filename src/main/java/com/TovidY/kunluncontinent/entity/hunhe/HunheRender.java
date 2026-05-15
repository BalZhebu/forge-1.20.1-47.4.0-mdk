package com.TovidY.kunluncontinent.entity.hunhe;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

//魂核渲染

public class HunheRender extends EntityRenderer<HunheEntity> {
    public HunheRender(EntityRendererProvider.Context context) {
        super(context);
    }

    public static final ResourceLocation TEXT = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/entity/hunhe.png");

    public void render(HunheEntity entity, float v, float v1, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        net.minecraft.client.player.LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            boolean isVisible = localPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.isConfigOpen(5)).orElse(true);
            if (!isVisible) return;
        }
        renderHunhe(entity, v1, poseStack, entity.getValue());
    }

    private void renderHunhe(HunheEntity entity, float v1, PoseStack poseStack, float value) {
        KLRenderApi.renderStart(TEXT, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        float time = entity.level().getGameTime() + v1;
        float v = (time) * 0.02f % 20 / 20;
        float size = (float) (0.2f + Math.sqrt(value) * 0.05f);
        renderAttibute(matrix4f, time, value);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        renderHeptagonCore(bufferbuilder, matrix4f, size, v);
        BufferUploader.drawWithShader(bufferbuilder.end());
        float ringSpeed = time * 0.06f;
        float ringRadius = size * 2.6f;
        renderPerfectRing(poseStack, bufferbuilder, ringRadius, ringSpeed, 0.6f);
        renderPerfectRing(poseStack, bufferbuilder, ringRadius, -ringSpeed, -0.6f);
        KLRenderApi.renderEnd(poseStack);
    }

    private void renderHeptagonCore(BufferBuilder buffer, Matrix4f mat, float size, float v) {
        int sides = 7;
        float radius = size * 1.2f;
        float height = size * 1.8f;
        float slice = (float) (Math.PI * 2 / sides);
        for (int i = 0; i < sides; i++) {
            float a1 = i * slice;
            float a2 = (i + 1) * slice;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float z2 = (float) Math.sin(a2) * radius;
            vertex(buffer, mat, x1, 0, z1, 0, v);
            vertex(buffer, mat, 0, height, 0, 0.5f, v + 0.1f);
            vertex(buffer, mat, x2, 0, z2, 1, v);
            vertex(buffer, mat, x1, 0, z1, 0, v);
            vertex(buffer, mat, x2, 0, z2, 1, v);
            vertex(buffer, mat, 0, -height, 0, 0.5f, v + 0.1f);
        }
    }

    private void renderPerfectRing(PoseStack poseStack, BufferBuilder buffer, float radius, float rot, float tilt) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotation(tilt));
        poseStack.mulPose(Axis.YP.rotation(rot));
        Matrix4f mat = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        int precision = 40;
        float slice = (float) (Math.PI * 2 / precision);
        float h = radius * 0.05f;

        for (int i = 0; i < precision; i++) {
            float a1 = i * slice;
            float a2 = (i + 1) * slice;
            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float z2 = (float) Math.sin(a2) * radius;

            drawQuad(buffer, mat, x1, -h, z1, x2, -h, z2, x2, h, z2, x1, h, z1);
            float inner = radius * 0.96f;
            float ix1 = (float) Math.cos(a1) * inner;
            float iz1 = (float) Math.sin(a1) * inner;
            float ix2 = (float) Math.cos(a2) * inner;
            float iz2 = (float) Math.sin(a2) * inner;
            drawQuad(buffer, mat, ix2, -h, iz2, ix1, -h, iz1, ix1, h, iz1, ix2, h, iz2);
        }
        BufferUploader.drawWithShader(buffer.end());
        poseStack.popPose();
    }

    private void drawQuad(BufferBuilder buffer, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(buffer, mat, x1, y1, z1, 0, 0);
        vertex(buffer, mat, x2, y2, z2, 1, 0);
        vertex(buffer, mat, x3, y3, z3, 1, 1);
        vertex(buffer, mat, x3, y3, z3, 1, 1);
        vertex(buffer, mat, x4, y4, z4, 0, 1);
        vertex(buffer, mat, x1, y1, z1, 0, 0);
    }

    private void vertex(BufferBuilder buffer, Matrix4f mat, float x, float y, float z, float u, float v) {
        buffer.vertex(mat, x, y, z).uv(u, v).endVertex();
    }

    private void renderAttibute(Matrix4f matrix4f, float v, float value) {
        matrix4f.translate( 0, 0.8f,0);
        matrix4f.translate( 0, (float) Math.sin(v*0.2f)*0.3f,0);
        matrix4f.rotate((float)Math.PI*v*0.02f, 0.0F, 1.0F, 0.0F);
        renderColor(value);
    }

    private void renderColor(float value) {
        if (value>=200){
            RenderSystem.setShaderColor(1.0f, 0.0f, 1.0f, 1.0f);
        }else if(value>=170){
            RenderSystem.setShaderColor(1.0f, 0.1f, 0.1f,0.2f);
        }else if(value>=130){
            RenderSystem.setShaderColor(0.0f, 1.0f, 1.0f,0.2f);
        }else if(value>=80){
            RenderSystem.setShaderColor(1.0f, 0.0f, 1.0f,0.2f);
        }else if(value>=40){
            RenderSystem.setShaderColor(0.2f, 0.2f, 1.0f,0.2f);
        }else if(value>=20){
            RenderSystem.setShaderColor(1.0f, 1.0f, 0.0f,0.2f);
        }else if(value>=0){
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f,0.2f);
        }
    }

    private void renderTriangle(BufferBuilder bufferbuilder, Matrix4f matrix4f, PoseStack poseStack, float x, float y, float z, float v) {
        bufferbuilder.vertex(matrix4f, x, 0, 0).uv(0, v).endVertex();
        bufferbuilder.vertex(matrix4f, 0, y, 0).uv(0, v+0.05f).endVertex();
        bufferbuilder.vertex(matrix4f, 0, 0, z).uv(1, v+0.05f).endVertex();
        bufferbuilder.vertex(matrix4f, 0, 0, z).uv(1, v+0.05f).endVertex();
        bufferbuilder.vertex(matrix4f, 0, y, 0).uv(0, v+0.05f).endVertex();
        bufferbuilder.vertex(matrix4f, x, 0, 0).uv(0, v).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(HunheEntity p_114482_) {
        return null;
    }
}
