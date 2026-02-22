package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SnowDemonRenderer extends MobRenderer<SnowDemonEntity, SnowDemonModel<SnowDemonEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/entity/snowdemon.png");

    public SnowDemonRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowDemonModel<>(context.bakeLayer(SnowDemonModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    protected void scale(SnowDemonEntity entity, PoseStack poseStack, float partialTickTime) {
        float s = entity.getVisualScale();
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(SnowDemonEntity entity) {
        return TEXTURE;
    }
}
