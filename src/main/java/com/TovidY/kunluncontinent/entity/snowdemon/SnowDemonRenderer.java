package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SnowDemonRenderer extends MobRenderer<SnowDemonEntity, SnowDemonNewModel<SnowDemonEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(KlMain.MOD_ID, "textures/entity/snowdemon.png");

    /** 阴影半径：模型高 8.5 格，用 1.8 才协调（原来 0.7 是给旧的小模型用的） */
    private static final float SHADOW_RADIUS = 1.8F;

    public SnowDemonRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowDemonNewModel<>(context.bakeLayer(SnowDemonNewModel.LAYER_LOCATION)), SHADOW_RADIUS);
    }

    @Override
    public ResourceLocation getTextureLocation(SnowDemonEntity entity) {
        return TEXTURE;
    }
}
