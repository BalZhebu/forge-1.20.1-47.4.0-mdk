package com.TovidY.kunluncontinent.entity.snowdemon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class SnowDemonNewModel<T extends SnowDemonEntity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(KlMain.MOD_ID, "textures/entity/snowdemon.png"), "main");

	private final ModelPart root;

	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart arm_l;
	private final ModelPart arm_r;
	private final ModelPart leg_l;
	private final ModelPart leg_r;

	public SnowDemonNewModel(ModelPart root) {
		this.root = root;
		this.torso = root.getChild("torso");
		this.head = root.getChild("head");
		this.arm_l = root.getChild("arm_l");
		this.arm_r = root.getChild("arm_r");
		this.leg_l = root.getChild("leg_l");
		this.leg_r = root.getChild("leg_r");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-20.0F, -28.0F, -11.0F, 40.0F, 8.0F, 26.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-28.0F, -52.0F, -14.0F, 56.0F, 24.0F, 28.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-16.0F, -56.0F, 10.0F, 32.0F, 20.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(21.0F, -63.0F, -12.0F, 14.0F, 15.0F, 25.0F, new CubeDeformation(0.0F))
				.texOffs(1, 1).addBox(-35.0F, -63.0F, -11.0F, 7.0F, 5.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-28.0F, -60.0F, -21.0F, 56.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(128, 64).addBox(2.0F, -48.0F, -17.0F, 18.0F, 6.0F, 2.6F, new CubeDeformation(0.0F))
				.texOffs(128, 64).addBox(-20.0F, -48.0F, -17.0F, 18.0F, 6.0F, 2.6F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(22.0F, -66.0F, 0.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(30.0F, -80.0F, 0.0F, 10.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-36.0F, -66.0F, 0.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-40.0F, -80.0F, 0.0F, 10.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-15.0F, -33.0F, -24.0F, 30.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 64).addBox(-10.0F, -36.0F, -24.5F, 20.0F, 3.0F, 1.1F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, -42.0F, -19.0F, 16.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-28.0F, -54.0F, -14.0F, 56.0F, 28.0F, 26.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(5.0F, -48.0F, -20.4F, 6.0F, 14.0F, 1.8F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-11.0F, -48.0F, -20.4F, 6.0F, 14.0F, 1.8F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.0F, 2.0F));

		PartDefinition arm_l = partdefinition.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, 28.0F, -13.0F, 16.0F, 20.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(2.0F, 12.0F, -14.0F, 18.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(2.0F, -12.0F, -12.0F, 18.0F, 26.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(6.0F, -44.0F, 10.0F, 10.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(2.0F, -32.0F, -13.0F, 18.0F, 22.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(23.0F, -32.0F, 2.0F));

		PartDefinition arm_r = partdefinition.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(0, 0).addBox(-20.0F, 28.0F, -13.0F, 16.0F, 20.0F, 22.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-20.0F, 12.0F, -14.0F, 18.0F, 18.0F, 24.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-20.0F, -12.0F, -12.0F, 18.0F, 26.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-16.0F, -44.0F, 10.0F, 10.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-20.0F, -32.0F, -13.0F, 18.0F, 22.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(-23.0F, -32.0F, 2.0F));

		PartDefinition leg_l = partdefinition.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, 10.0F, -14.0F, 26.0F, 12.0F, 34.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-8.0F, -8.0F, -12.0F, 18.0F, 20.0F, 26.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-10.0F, -26.0F, -13.0F, 22.0F, 20.0F, 28.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-6.0F, 16.0F, -20.0F, 17.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(11.0F, 0.0F, 0.0F));

		PartDefinition leg_r = partdefinition.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(4, 0).addBox(-15.0F, 10.0F, -14.0F, 26.0F, 12.0F, 34.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-10.0F, -8.0F, -12.0F, 18.0F, 20.0F, 26.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-12.0F, -26.0F, -13.0F, 22.0F, 20.0F, 28.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-11.0F, 16.0F, -20.0F, 17.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-11.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 先复位，再按顺序叠加动画（后调用的在同一骨骼/通道上覆盖先调用的）
		this.root().getAllParts().forEach(ModelPart::resetPose);

		// 移动：走路循环
		this.animate(entity.walkAnimationState, snow_demonAnimation.move, ageInTicks);
		// 普通攻击：右拳发力（会覆盖走路，保证出拳清晰）
		this.animate(entity.attackAnimationState, snow_demonAnimation.ack, ageInTicks);
		// 技能：跳起砸地（优先级最高）
		this.animate(entity.jumpAnimationState, snow_demonAnimation.jump, ageInTicks);

		// 头部朝向目标：叠加在动画之上，让怪物会看着你
		this.head.yRot += netHeadYaw * ((float) Math.PI / 180F);
		this.head.xRot += headPitch * ((float) Math.PI / 180F);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		arm_l.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		arm_r.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_l.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_r.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
