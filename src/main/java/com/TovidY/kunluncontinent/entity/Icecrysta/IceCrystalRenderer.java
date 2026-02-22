package com.TovidY.kunluncontinent.entity.Icecrysta;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class IceCrystalRenderer extends MobRenderer<IceCrystalEntity, CustomModel<IceCrystalEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/entity/ice_crystal.png");

    public IceCrystalRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomModel<>(context.bakeLayer(CustomModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    protected void scale(IceCrystalEntity entity, PoseStack poseStack, float partialTickTime) {
        entity.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            long nianxian = attr.getNianxian();
            float scaleFactor = 1.0F + (float)nianxian / 20000.0F;
            scaleFactor = Math.max(0.8F, Math.min(scaleFactor, 3.0F));
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        });
    }

    @Override
    public ResourceLocation getTextureLocation(IceCrystalEntity entity) {
        return TEXTURE;
    }
}