package com.TovidY.kunluncontinent.entity.hunhe;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
        renderHunhe(entity,v1,poseStack,entity.getValue());
    }

    private void renderHunhe(HunheEntity entity, float v1, PoseStack poseStack, float value) {
        KLRenderApi.renderStart(TEXT,poseStack);
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        float v = (entity.level().getGameTime() + v1)*0.02f%20/20;
        float size = (float) (0.2f + Math.sqrt(value)*0.05f);
        renderAttibute(matrix4f,entity.getLivetime() + v1,value);

        renderTriangle(bufferbuilder,matrix4f,poseStack,size,size*2,size,v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,-size,size*2,size, v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,-size,size*2,-size, v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,size,size*2,-size, v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,size,-size*2,size, v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,-size,-size*2,size, v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,-size,-size*2,-size, v);
        renderTriangle(bufferbuilder,matrix4f,poseStack,size,-size*2,-size, v);
        BufferUploader.drawWithShader(bufferbuilder.end());
        KLRenderApi.renderEnd(poseStack);
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
