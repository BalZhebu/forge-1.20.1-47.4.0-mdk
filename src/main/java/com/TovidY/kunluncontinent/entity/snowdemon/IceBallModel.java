package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 冰球（雪魔远程攻击弹射物）的 3D 模型。
 *
 * <p>来源：Blockbench 导出的 {@code CustomModel}，这里只做了三件事：</p>
 * <ol>
 *   <li>改名（{@code CustomModel} → {@code IceBallModel}，和已有的 {@code CustomModel} 区分开）；</li>
 *   <li>{@code LAYER_LOCATION} 换成 {@code kunluncontinent:ice_ball}（原来写的是占位 {@code modid}）；</li>
 *   <li>补齐包名与 import。</li>
 * </ol>
 *
 * <p>几何完全没有改：6×6×6 的一个方块，贴图 32×32。
 * 注意它在模型坐标里<b>不在原点</b>（方块中心在 y=20、z=-1，也就是 (0, 1.25, -0.0625) 格），
 * 所以 {@link IceBallRenderer} 渲染时会先把它平移到实体原点，否则冰球会飘在头顶。</p>
 */
public class IceBallModel<T extends Entity> extends EntityModel<T> {

    /** 模型层 id —— 与 {@code EntityInit.ClientModEvents#registerLayerDefinitions} 对应。 */
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(KlMain.MOD_ID, "ice_ball"), "main");

    private final ModelPart bb_main;

    public IceBallModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -7.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // 冰球是刚体，没有骨骼动画
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
