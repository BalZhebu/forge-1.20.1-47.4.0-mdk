package com.TovidY.kunluncontinent.entity.lonebamboo;

import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 孤竹模型。
 *
 * <p>继承 {@link HierarchicalModel}（而不是 {@code EntityModel}）的唯一原因是
 * 只有它提供 {@code animate(...)} —— Blockbench 导出的关键帧动画必须靠它播放。
 * 顺带好处：{@code HierarchicalModel} 自己已经实现了 {@code renderToBuffer}
 * （渲染 root 整棵树），所以这里不再手写那一段。</p>
 */
public class LoneBambooModel<T extends Entity> extends HierarchicalModel<T> {
	// 实体渲染层 ID。注册用的是 EntityInit.ClientModEvents#registerLayerDefinitions，
	// 渲染器用 context.bakeLayer(LoneBambooModel.LAYER_LOCATION) 取模型。
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(new ResourceLocation(KlMain.MOD_ID, "lone_bamboo"), "main");

	/** 根节点。动画是按<b>名字</b>在整棵树里查部件的，所以必须把 root 暴露给 HierarchicalModel。 */
	private final ModelPart root;

	// 下面这些字段动画用不到（动画靠名字查找），留着是方便以后手动摆某个部件
	private final ModelPart bone;
	private final ModelPart bone5;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart stem;

	public LoneBambooModel(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
		this.bone5 = this.bone.getChild("bone5");
		this.bone2 = root.getChild("bone2");
		this.bone3 = root.getChild("bone3");
		this.bone4 = root.getChild("bone4");
		this.stem = root.getChild("stem");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(10, 4).addBox(0.025F, -30.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 3).addBox(0.025F, -23.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 12).addBox(0.025F, -19.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 7).addBox(0.025F, -28.0F, 2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 6).addBox(0.025F, -26.0F, 2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 16).addBox(0.025F, -21.0F, 2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 9).addBox(0.025F, -17.0F, 2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 6).addBox(0.025F, -20.0F, 3.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 4).addBox(0.025F, -16.0F, 3.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 10).addBox(0.025F, -26.0F, 3.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 8).addBox(0.025F, -24.0F, 3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 0).addBox(0.025F, -23.0F, 4.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 0).addBox(0.025F, -29.0F, 4.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition bone5 = bone.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(12, 12).addBox(0.025F, -30.0F, -2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 13).addBox(0.025F, -23.0F, -2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 0).addBox(0.025F, -19.0F, -2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 3).addBox(0.025F, -28.0F, -3.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 10).addBox(0.025F, -26.0F, -3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 12).addBox(0.025F, -21.0F, -3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 6).addBox(0.025F, -17.0F, -3.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 9).addBox(0.025F, -20.0F, -4.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 8).addBox(0.025F, -16.0F, -4.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 12).addBox(0.025F, -26.0F, -4.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 14).addBox(0.025F, -24.0F, -4.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 15).addBox(0.025F, -23.0F, -5.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 0).addBox(0.025F, -29.0F, -5.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(16, 16).addBox(1.0F, -30.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 0).addBox(1.0F, -23.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 2).addBox(1.0F, -19.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 4).addBox(2.0F, -28.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 0).addBox(2.0F, -26.0F, 0.025F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 1).addBox(2.0F, -21.0F, 0.025F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 6).addBox(2.0F, -17.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(8, 18).addBox(3.0F, -20.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(12, 15).addBox(3.0F, -16.0F, 0.025F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 8).addBox(3.0F, -26.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 2).addBox(3.0F, -24.0F, 0.025F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(10, 18).addBox(4.0F, -23.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(14, 15).addBox(4.0F, -29.0F, 0.025F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(18, 10).addBox(-2.0F, -30.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(12, 18).addBox(-2.0F, -23.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 12).addBox(-2.0F, -19.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(14, 18).addBox(-3.0F, -28.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 3).addBox(-3.0F, -26.0F, 0.025F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 4).addBox(-3.0F, -21.0F, 0.025F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 14).addBox(-3.0F, -17.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(16, 18).addBox(-4.0F, -20.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(16, 0).addBox(-4.0F, -16.0F, 0.025F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 16).addBox(-4.0F, -26.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 5).addBox(-4.0F, -24.0F, 0.025F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 18).addBox(-5.0F, -23.0F, 0.025F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(16, 3).addBox(-5.0F, -29.0F, 0.025F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition stem = partdefinition.addOrReplaceChild("stem", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -31.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity instanceof LoneBambooEntity bamboo) {
			this.animate(bamboo.idleAnimationState, LoneBambooidle.idle, ageInTicks);
		}
	}
}