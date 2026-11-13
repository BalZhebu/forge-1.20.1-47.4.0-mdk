package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 冰球渲染器 —— 用 {@link IceBallModel} 画 3D 模型（不是物品图标）。
 *
 * <h3>为什么要那两行 translate</h3>
 * <p>Blockbench 导出的模型把方块摆在 {@code PartPose.offset(0, 24, 0)}、盒子是
 * {@code (-3,-7,-4)~(3,-1,2)}，换算成格就是「中心在 (0, 1.25, -0.0625)」。
 * 直接渲染会飘在实体头顶 1.25 格，所以先平移到原点再画。</p>
 */
public class IceBallRenderer extends EntityRenderer<IceBallEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(KlMain.MOD_ID, "textures/entity/ice_ball.png");

    /** 模型方块中心在模型空间的偏移（格）。 */
    private static final float CENTER_Y = 1.25F;
    private static final float CENTER_Z = -0.0625F;

    private final IceBallModel<IceBallEntity> model;

    public IceBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new IceBallModel<>(context.bakeLayer(IceBallModel.LAYER_LOCATION));
        this.shadowRadius = 0.2F;
    }

    @Override
    public ResourceLocation getTextureLocation(IceBallEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(IceBallEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 模型中心 → 实体原点
        poseStack.translate(0.0F, -CENTER_Y, -CENTER_Z);
        // 让它跟着飞行方向转（球体其实无所谓，但贴图有正反面）
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
