package com.TovidY.kunluncontinent.entity.snowdemon;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SnowDemonModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/entity/snowdemon"), "main");
	private final ModelPart bone;
	private final ModelPart bone9;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart bone8;

	public SnowDemonModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bone9 = this.bone.getChild("bone9");
		this.bone2 = root.getChild("bone2");
		this.bone3 = root.getChild("bone3");
		this.bone4 = root.getChild("bone4");
		this.bone5 = root.getChild("bone5");
		this.bone6 = this.bone5.getChild("bone6");
		this.bone7 = root.getChild("bone7");
		this.bone8 = this.bone7.getChild("bone8");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(22, 40).addBox(3.0F, -1.0F, -2.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(14, 43).addBox(3.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 40).addBox(3.0F, -8.0F, 0.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 38).addBox(3.0F, -1.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 40).addBox(5.0F, -1.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 24.0F, 2.0F));

		PartDefinition bone9 = bone.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(22, 40).addBox(3.0F, -1.0F, -2.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(14, 43).addBox(3.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 40).addBox(3.0F, -8.0F, 0.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 38).addBox(3.0F, -1.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 40).addBox(5.0F, -1.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, 0.0F, 0.0F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 12).addBox(-6.0F, -11.0F, -2.0F, 12.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(30, 34).addBox(-5.0F, -12.5F, 3.0F, 10.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 2.0F));

		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -17.0F, -3.0F, 12.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 36).addBox(-5.0F, -16.0F, -4.0F, 10.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 28).addBox(-6.0F, -17.0F, 3.0F, 12.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(36, 0).addBox(-5.0F, -16.0F, 6.0F, 10.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-5.0F, -18.0F, -2.0F, 10.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(34, 24).addBox(-4.0F, -17.0F, -4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 7).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 26).addBox(-4.0F, -18.0F, -3.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 28).addBox(-4.0F, -19.0F, -1.0F, 8.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(50, 9).addBox(-3.0F, -19.0F, -2.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 2.0F));

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, -16.0F, -2.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 46).addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, -1.0F));

		PartDefinition bone5 = partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(50, 40).addBox(-4.0F, -22.0F, 7.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 9).addBox(-3.0F, -18.0F, 8.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 24).addBox(-1.0F, -19.0F, 7.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 11).addBox(-3.0F, -20.0F, 7.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(50, 46).addBox(5.0F, -22.0F, 7.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 15).addBox(4.0F, -20.0F, 7.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition bone7 = partdefinition.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(28, 46).addBox(6.0F, -16.0F, 1.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(38, 50).addBox(7.0F, -18.0F, 1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(54, 46).addBox(10.0F, -18.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 48).addBox(10.0F, -18.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 52).addBox(11.0F, -16.0F, 4.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 52).addBox(11.0F, -16.0F, 1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 42).addBox(7.0F, -19.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 44).addBox(7.0F, -19.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 54).addBox(7.0F, -14.0F, 6.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 34).addBox(10.0F, -14.0F, 6.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 50).addBox(7.0F, -11.0F, 1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(34, 12).addBox(7.0F, -17.0F, 0.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(22, 50).addBox(8.0F, -16.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bone7.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(22, 36).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9457F, -2.7043F, 2.379F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition cube_r2 = bone7.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 54).addBox(-1.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.9457F, -2.7043F, 1.379F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition cube_r3 = bone7.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(54, 19).addBox(-1.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9457F, -2.7043F, 1.379F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition cube_r4 = bone7.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(12, 50).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.968F, -5.6027F, 2.1529F, -0.2609F, -0.0028F, 0.0084F));

		PartDefinition bone8 = bone7.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(28, 46).mirror().addBox(-7.0F, -16.0F, 1.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(38, 50).mirror().addBox(-8.0F, -18.0F, 1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 46).mirror().addBox(-11.0F, -18.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 48).mirror().addBox(-11.0F, -18.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(48, 52).mirror().addBox(-12.0F, -16.0F, 4.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(52, 52).mirror().addBox(-12.0F, -16.0F, 1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 42).mirror().addBox(-8.0F, -19.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 44).mirror().addBox(-8.0F, -19.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(32, 54).mirror().addBox(-8.0F, -14.0F, 6.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(54, 34).mirror().addBox(-11.0F, -14.0F, 6.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 50).mirror().addBox(-10.0F, -11.0F, 1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(34, 12).mirror().addBox(-11.0F, -17.0F, 0.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(22, 50).mirror().addBox(-10.0F, -16.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r5 = bone8.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(22, 36).mirror().addBox(-2.0F, -3.0F, -2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.9457F, -2.7043F, 2.379F, -0.2609F, 0.0028F, -0.0084F));

		PartDefinition cube_r6 = bone8.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 54).mirror().addBox(0.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.9457F, -2.7043F, 1.379F, -0.2609F, 0.0028F, -0.0084F));

		PartDefinition cube_r7 = bone8.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(54, 19).mirror().addBox(0.0F, -3.0F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.9457F, -2.7043F, 1.379F, -0.2609F, 0.0028F, -0.0084F));

		PartDefinition cube_r8 = bone8.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(12, 50).mirror().addBox(-2.0F, -3.0F, -2.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.968F, -5.6027F, 2.1529F, -0.2609F, 0.0028F, -0.0084F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone5.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone7.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}