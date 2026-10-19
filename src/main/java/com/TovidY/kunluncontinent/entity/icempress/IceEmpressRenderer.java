package com.TovidY.kunluncontinent.entity.icempress;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 雪帝渲染器。
 *
 * <p>贴图路径 {@code assets/kunluncontinent/textures/entity/ice_empress.png}，
 * 和模型 UV 一样是 <b>256×256</b>。</p>
 *
 * <p>模型整体放大 {@link #MODEL_SCALE} 倍（用户要求"建模放大 2 倍左右"）。
 * 这里只负责画；<b>命中箱在 {@code EntityInit} 里改成了 2.0×5.2</b>，
 * 两边必须同时改，否则会出现"看着很大、打不中"或者"打空气也掉血"。</p>
 */
public class IceEmpressRenderer extends MobRenderer<IceEmpressEntity, IceEmpressEntityModel<IceEmpressEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(KlMain.MOD_ID, "textures/entity/ice_empress.png");

    /** 模型放大倍数（渲染专用）。 */
    private static final float MODEL_SCALE = 2.0F;

    /** 模型放大后高约 5.2 格，阴影半径同步放大。 */
    private static final float SHADOW_RADIUS = 1.0F;

    public IceEmpressRenderer(EntityRendererProvider.Context context) {
        super(context, new IceEmpressEntityModel<>(context.bakeLayer(IceEmpressEntityModel.LAYER_LOCATION)), SHADOW_RADIUS);
    }

    @Override
    public void scale(IceEmpressEntity entity, PoseStack poseStack, float partialTickTime) {
        // 缩放原点在脚底，所以放大后不会陷进地里
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(IceEmpressEntity entity) {
        return TEXTURE;
    }
}
