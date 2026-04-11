package com.TovidY.kunluncontinent.entity.demon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DemonWhaleRenderer extends MobRenderer<DemonWhaleEntity, DemonWhaleModel<DemonWhaleEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/entity/custommodel.png");

    public DemonWhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonWhaleModel<>(context.bakeLayer(DemonWhaleModel.LAYER_LOCATION)), 0.7F);
    }

    // 顺便修正阴影大小：在 Renderer 里
    @Override
    protected void scale(DemonWhaleEntity entity, PoseStack poseStack, float partialTickTime) {
        float s = entity.getVisualScale();
        poseStack.scale(s, s, s);
        this.shadowRadius = 0.7F * s;
    }
    @Override
    public ResourceLocation getTextureLocation(DemonWhaleEntity entity) {
        return TEXTURE;
    }
}
