package com.TovidY.kunluncontinent.entity.lonebamboo;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 孤竹渲染器。
 *
 * <p>贴图：{@code assets/kunluncontinent/textures/entity/lone_bamboo.png}（64×64，
 * 但 UV 岛按 2 倍坐标绘制，所以模型声明 32×32 是正确的 —— 见 {@code LoneBambooModel}）。
 * 模型层在 {@code EntityInit.ClientModEvents.registerLayerDefinitions} 里烘焙。</p>
 */
public class LoneBambooRenderer extends MobRenderer<LoneBambooEntity, LoneBambooModel<LoneBambooEntity>> {

    /** 贴图位置。public 是因为撑杆跳的竹竿要复用同一张图（见 PoleVaultRenderHandler）。 */
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(KlMain.MOD_ID, "textures/entity/lone_bamboo.png");

    /** 阴影半径（按 1 倍体型算）。 */
    private static final float SHADOW_RADIUS = 0.2F;

    public LoneBambooRenderer(EntityRendererProvider.Context context) {
        super(context, new LoneBambooModel<>(context.bakeLayer(LoneBambooModel.LAYER_LOCATION)), SHADOW_RADIUS);
    }

    /**
     * 按实体自身的随机体型缩放模型。
     *
     * <p>{@code LivingEntityRenderer#scale} 默认是空实现，是官方给「视觉缩放」留的挂点。
     * 它作用在「已经翻转到模型坐标系」之后、渲染模型之前，所以缩放是绕
     * <b>模型原点（脚底）</b> 进行的 —— 放大后竹子仍然是站在地上的。</p>
     *
     * <p>命中箱那边由 {@code LoneBambooEntity#getScale()} 负责，两边共用同一个值，
     * 不会出现「看着很大、打不着」。</p>
     */
    @Override
    protected void scale(LoneBambooEntity entity, PoseStack poseStack, float partialTick) {
        float s = entity.getBambooScale();
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(LoneBambooEntity entity) {
        return TEXTURE;
    }
}
