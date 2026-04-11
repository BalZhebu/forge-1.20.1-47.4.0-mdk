package com.TovidY.kunluncontinent.entity.snowdemon;// Made with Blockbench 5.1.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class SnowDemonModel<T extends SnowDemonEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/entity/snowdemon.png"), "main");

	private final ModelPart root;

	private final ModelPart zuotui;
	private final ModelPart youtui;
	private final ModelPart bone2;
	private final ModelPart shenti;
	private final ModelPart lian;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart zuoshou;
	private final ModelPart youshou;

	public SnowDemonModel(ModelPart root) {
		this.root = root;
		this.zuotui = root.getChild("zuotui");
		this.youtui = root.getChild("youtui");
		this.bone2 = root.getChild("bone2");
		this.shenti = root.getChild("shenti");
		this.lian = root.getChild("lian");
		this.bone5 = root.getChild("bone5");
		this.bone6 = this.bone5.getChild("bone6");
		this.zuoshou = root.getChild("zuoshou");
		this.youshou = root.getChild("youshou");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition zuotui = partdefinition.addOrReplaceChild("zuotui", CubeListBuilder.create().texOffs(22, 40).addBox(-2.0F, 7.7839F, -3.911F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(14, 43).addBox(-2.0F, 6.7839F, -2.911F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 40).addBox(-2.0F, 0.7839F, -1.911F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 38).addBox(-2.0F, 7.7839F, -4.911F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 40).addBox(0.0F, 7.7839F, -4.911F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 14.0F, 3.0F));

		PartDefinition youtui = partdefinition.addOrReplaceChild("youtui", CubeListBuilder.create().texOffs(22, 40).addBox(-1.0F, 7.0F, -3.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(14, 43).addBox(-1.0F, 6.0F, -2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 40).addBox(-1.0F, 0.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 38).addBox(-1.0F, 7.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 40).addBox(1.0F, 7.0F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 15.0F, 2.0F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 12).addBox(-6.0F, -11.0F, -2.0F, 12.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(30, 34).addBox(-5.0F, -12.5F, 3.0F, 10.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 2.0F));

		PartDefinition shenti = partdefinition.addOrReplaceChild("shenti", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -3.0F, 12.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 36).addBox(-5.0F, -4.0F, -4.0F, 10.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 28).addBox(-6.0F, -5.0F, 3.0F, 12.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(36, 0).addBox(-5.0F, -4.0F, 6.0F, 10.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-5.0F, -6.0F, -2.0F, 10.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(34, 24).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 7).addBox(-4.0F, 2.0F, -4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 26).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 28).addBox(-4.0F, -7.0F, -1.0F, 8.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(50, 9).addBox(-3.0F, -7.0F, -2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 2.0F));

		PartDefinition lian = partdefinition.addOrReplaceChild("lian", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, -16.0F, -2.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 46).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, -1.0F));

		PartDefinition bone5 = partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(50, 40).addBox(-4.0F, -22.0F, 7.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 9).addBox(-3.0F, -18.0F, 8.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 24).addBox(-1.0F, -19.0F, 7.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 11).addBox(-3.0F, -20.0F, 7.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

		PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(50, 46).addBox(5.0F, -22.0F, 7.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 15).addBox(4.0F, -20.0F, 7.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition zuoshou = partdefinition.addOrReplaceChild("zuoshou", CubeListBuilder.create().texOffs(28, 46).addBox(-1.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(38, 50).addBox(0.0F, -4.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(54, 46).addBox(3.0F, -4.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 48).addBox(3.0F, -4.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 52).addBox(4.0F, -2.0F, 1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 52).addBox(4.0F, -2.0F, -2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 42).addBox(0.0F, -5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 44).addBox(0.0F, -5.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 54).addBox(0.0F, 0.0F, 3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 34).addBox(3.0F, 0.0F, 3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 50).addBox(0.0F, 3.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(34, 12).addBox(0.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(22, 50).addBox(1.0F, -2.0F, -4.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 9.0F, 3.0F));

		PartDefinition cube_r1 = zuoshou.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(22, 36).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9457F, 11.2957F, -0.621F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition cube_r2 = zuoshou.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 54).addBox(-1.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9457F, 11.2957F, -1.621F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition cube_r3 = zuoshou.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(54, 19).addBox(-1.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9457F, 11.2957F, -1.621F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition cube_r4 = zuoshou.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(12, 50).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.968F, 8.3973F, -0.8471F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition youshou = partdefinition.addOrReplaceChild("youshou", CubeListBuilder.create().texOffs(28, 46).mirror().addBox(-1.0F, -2.0F, -3.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(38, 50).mirror().addBox(-2.0F, -4.0F, -3.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 46).mirror().addBox(-5.0F, -4.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 48).mirror().addBox(-5.0F, -4.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(48, 52).mirror().addBox(-6.0F, -2.0F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(52, 52).mirror().addBox(-6.0F, -2.0F, -3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 42).mirror().addBox(-2.0F, -5.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 44).mirror().addBox(-2.0F, -5.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(32, 54).mirror().addBox(-2.0F, 0.0F, 2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 34).mirror().addBox(-5.0F, 0.0F, 2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 50).mirror().addBox(-4.0F, 3.0F, -3.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(34, 12).mirror().addBox(-5.0F, -3.0F, -4.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(22, 50).mirror().addBox(-4.0F, -2.0F, -5.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 9.0F, 4.0F));

		PartDefinition cube_r5 = youshou.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(22, 36).mirror().addBox(-2.0F, -3.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.9457F, 11.2957F, -1.621F, -0.2609F, 0.0028F, -0.0084F));

		PartDefinition cube_r6 = youshou.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 54).mirror().addBox(0.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.9457F, 11.2957F, -2.621F, -0.2609F, 0.0028F, -0.0084F));

		PartDefinition cube_r7 = youshou.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(54, 19).mirror().addBox(0.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.9457F, 11.2957F, -2.621F, -0.2609F, 0.0028F, -0.0084F));

		PartDefinition cube_r8 = youshou.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(12, 50).mirror().addBox(-2.0F, -3.0F, -2.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.968F, 8.3973F, -1.8471F, -0.2609F, 0.0028F, -0.0084F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
//		this.lian.yRot = netHeadYaw * ((float)Math.PI / 180F);
//		this.lian.xRot = headPitch * ((float)Math.PI / 180F);

		this.animate(entity.walkAnimationState, SnowDemonAnimation.move, ageInTicks);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		zuotui.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		youtui.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		shenti.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		lian.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone5.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		zuoshou.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		youshou.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}