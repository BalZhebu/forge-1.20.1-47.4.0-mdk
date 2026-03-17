package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import static com.TovidY.kunluncontinent.entity.hunhuan.HunhuanEntity.HUNHUAN;

//怪物身上的魂环渲染

@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PWRenderLivingEvent {

    @SubscribeEvent
    public static void renderLivingEventPost(RenderLivingEvent.Post event){
        LivingEntity entity = event.getEntity();
        if(entity == null) return;
        if (entity.tickCount < 1 || !entity.isAlive()) return;
        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
            if (capability.getNianxian() > 0) {
                renderHunhuan(entity, event.getPartialTick(), event.getPoseStack(), (int) capability.getNianxian(), 0);
            }
        });
    }

    public static void renderHunhuan(Entity entity, float partialTick, PoseStack poseStack, int nianxian, int count){

        KLRenderApi.renderStart(HUNHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        renderAnimation(matrix4f, nianxian, (entity.level().getGameTime() + partialTick), count);
        renderHunhuanAttribute(matrix4f, nianxian, (entity.level().getGameTime() + partialTick), count);
        float heightOffset = 0.22f;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, -6f, heightOffset, -6f).uv(0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, -6f, heightOffset, 6f).uv(0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, 6f, heightOffset, 6f).uv(1, 1).endVertex();
        bufferbuilder.vertex(matrix4f, 6f, heightOffset, -6f).uv(1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 6f, heightOffset, -6f).uv(1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 6f, heightOffset, 6f).uv(1, 1).endVertex();
        bufferbuilder.vertex(matrix4f, -6f, heightOffset, 6f).uv(0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, -6f, heightOffset, -6f).uv(0, 0).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        KLRenderApi.renderEnd(poseStack);
    }

    public static void renderHunhuanAttribute(Matrix4f matrix4f, long nianxian, float partialTick, int count) {
        if (nianxian >= 100000000) {
            RenderSystem.setShaderColor(0.0f, 1.0f, 0.2f, 1.0f);
            float scaleBase = 2.0f + (float) Math.sin(partialTick * 0.05f) * 0.2f;
            matrix4f.scale(scaleBase, 1.0f, scaleBase);
        }
        else if (nianxian >= 10000000) {
            RenderSystem.setShaderColor(0.0f, 0.4f, 1.0f, 0.9f);
            float breathing = 1.6f + (float) Math.sin(partialTick * 0.02f) * 0.1f;
            matrix4f.scale(breathing, 1.0f, breathing);
        } else if (nianxian >= 1000000) {
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.0f, 0.8f);
            float s = 1.4f + (float) Math.sin(partialTick * 0.02f) * 0.1f;
            matrix4f.scale(s, 1.0f, s);
        } else if (nianxian >= 100000) {
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 0.7f);
            matrix4f.scale(1.2f, 1.0f, 1.2f);
        } else if (nianxian >= 10000) {
            RenderSystem.setShaderColor(0.00f, 0.00f, 0.00f, 1.0f);
            matrix4f.scale(1.0f, 1.0f, 1.0f);
        } else if (nianxian >= 1000) {
            RenderSystem.setShaderColor(0.8f, 0.0f, 0.8f, 0.5f);
            matrix4f.scale(0.8f, 1.0f, 0.8f);
        } else if (nianxian >= 100) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 0.0f, 0.5f);
            matrix4f.scale(0.6f, 1.0f, 0.6f);
        } else if (nianxian >= 1) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.8f);
            matrix4f.scale(0.4f, 1.0f, 0.4f);
        }
    }

    public static void renderAnimation(Matrix4f matrix4f, int nianxian, float partialTick, int count) {
        matrix4f.rotate((float) Math.PI * 0.005f * partialTick * (count % 2 == 0 ? -1 : 1), 0.0F, 1.0F, 0.0F);
        matrix4f.translate(0, 0.01f * count, 0);
    }
}
