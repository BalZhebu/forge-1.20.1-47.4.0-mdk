package com.TovidY.kunluncontinent.entity.hunhuan;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

import static com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent.*;


public class HunhuanRender extends EntityRenderer<HunhuanEntity> {
    public HunhuanRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HunhuanEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        long nianxian = entity.getNianxianSync();
        renderHunhuan(entity, partialTicks, poseStack, (int) nianxian, 0);
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HunhuanEntity entity) {
        return HunhuanEntity.HUNHUAN;
    }
}