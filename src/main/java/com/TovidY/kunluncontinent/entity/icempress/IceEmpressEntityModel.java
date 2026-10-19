package com.TovidY.kunluncontinent.entity.icempress;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.TovidY.kunluncontinent.KlMain;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class IceEmpressEntityModel<T extends IceEmpressEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(KlMain.MOD_ID, "ice_empress"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart crown;
	private final ModelPart hair;
	private final ModelPart torso;
	private final ModelPart skirt;
	private final ModelPart train;
	private final ModelPart sleeve_R;
	private final ModelPart sleeve_L;
	private final ModelPart pauldron_R;
	private final ModelPart pauldron_L;

	public IceEmpressEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.crown = root.getChild("crown");
		this.hair = root.getChild("hair");
		this.torso = root.getChild("torso");
		this.skirt = root.getChild("skirt");
		this.train = root.getChild("train");
		this.sleeve_R = root.getChild("sleeve_R");
		this.sleeve_L = root.getChild("sleeve_L");
		this.pauldron_R = root.getChild("pauldron_R");
		this.pauldron_L = root.getChild("pauldron_L");
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(121, 1).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(125, 51).addBox(-0.7F, 1.7F, -0.7F, 1.4F, 1.3F, 1.4F, new CubeDeformation(0.0F))
		.texOffs(139, 45).addBox(-1.4F, 1.8F, -1.7F, 2.8F, 0.8F, 3.2F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition crown = partdefinition.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(1, 38).addBox(-2.2F, 0.1F, -2.2F, 4.4F, 0.9F, 4.4F, new CubeDeformation(0.0F))
		.texOffs(103, 45).addBox(-0.55F, -3.2F, -0.55F, 1.1F, 3.3F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(190, 51).addBox(-0.32F, -5.2F, -0.32F, 0.64F, 2.0F, 0.64F, new CubeDeformation(0.0F))
		.texOffs(49, 56).addBox(-0.18F, -5.9F, -0.18F, 0.36F, 0.7F, 0.36F, new CubeDeformation(0.0F))
		.texOffs(83, 51).addBox(-1.9F, -2.4F, -0.45F, 0.8F, 2.5F, 0.9F, new CubeDeformation(0.0F))
		.texOffs(88, 51).addBox(1.1F, -2.4F, -0.45F, 0.8F, 2.5F, 0.9F, new CubeDeformation(0.0F))
		.texOffs(194, 51).addBox(-2.8F, -1.3F, -0.35F, 0.7F, 1.4F, 0.7F, new CubeDeformation(0.0F))
		.texOffs(198, 51).addBox(2.1F, -1.3F, -0.35F, 0.7F, 1.4F, 0.7F, new CubeDeformation(0.0F))
		.texOffs(202, 51).addBox(-1.55F, -1.4F, -2.0F, 0.6F, 1.5F, 0.65F, new CubeDeformation(0.0F))
		.texOffs(206, 51).addBox(0.95F, -1.4F, -2.0F, 0.6F, 1.5F, 0.65F, new CubeDeformation(0.0F))
		.texOffs(210, 51).addBox(-0.6F, -1.8F, 1.5F, 0.6F, 1.9F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(214, 51).addBox(0.0F, -1.8F, 1.5F, 0.6F, 1.9F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(17, 56).addBox(-0.42F, 0.1F, -2.6F, 0.84F, 0.9F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(218, 51).addBox(-3.1F, 0.6F, -1.3F, 0.5F, 2.0F, 0.55F, new CubeDeformation(0.0F))
		.texOffs(222, 51).addBox(2.6F, 0.6F, -1.3F, 0.5F, 2.0F, 0.55F, new CubeDeformation(0.0F))
		.texOffs(52, 56).addBox(-3.02F, 2.6F, -1.22F, 0.34F, 0.9F, 0.34F, new CubeDeformation(0.0F))
		.texOffs(55, 56).addBox(2.68F, 2.6F, -1.22F, 0.34F, 0.9F, 0.34F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition hair = partdefinition.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(235, 31).addBox(-2.35F, -7.5F, -4.35F, 4.7F, 1.3F, 4.7F, new CubeDeformation(0.0F))
		.texOffs(1, 56).addBox(-2.0F, -6.6F, -4.5F, 4.0F, 1.2F, 0.65F, new CubeDeformation(0.0F))
		.texOffs(78, 23).addBox(-2.6F, -6.6F, 0.0F, 5.2F, 5.6F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(92, 23).addBox(-2.6F, -1.4F, 0.7F, 5.2F, 5.4F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(33, 23).addBox(-2.8F, 3.6F, 1.5F, 5.6F, 5.4F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(48, 23).addBox(-2.8F, 8.6F, 2.3F, 5.6F, 5.4F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(1, 23).addBox(-3.0F, 13.6F, 3.1F, 6.0F, 5.4F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(17, 23).addBox(-3.0F, 18.6F, 3.9F, 6.0F, 4.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(213, 45).addBox(0.6F, 23.0F, 4.7F, 1.8F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(220, 45).addBox(-2.6F, 23.0F, 4.7F, 1.8F, 2.4F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 1).addBox(-2.9F, -5.5F, -1.7F, 0.75F, 7.0F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(169, 1).addBox(2.15F, -5.5F, -1.7F, 0.75F, 7.0F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(176, 1).addBox(-2.5F, -2.8F, -4.4F, 0.65F, 7.3F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(180, 1).addBox(1.85F, -2.8F, -4.4F, 0.65F, 7.3F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(250, 51).addBox(-2.4F, 4.2F, -4.3F, 0.5F, 2.3F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(253, 51).addBox(1.9F, 4.2F, -4.3F, 0.5F, 2.3F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(1, 45).addBox(-2.3F, 1.2F, 1.0F, 0.6F, 0.8F, 3.6F, new CubeDeformation(0.0F))
		.texOffs(11, 45).addBox(1.7F, 1.2F, 1.0F, 0.6F, 0.8F, 3.6F, new CubeDeformation(0.0F))
		.texOffs(61, 45).addBox(-2.3F, 0.8F, 4.4F, 0.6F, 0.8F, 3.4F, new CubeDeformation(0.0F))
		.texOffs(70, 45).addBox(1.7F, 0.8F, 4.4F, 0.6F, 0.8F, 3.4F, new CubeDeformation(0.0F))
		.texOffs(21, 45).addBox(-0.95F, 5.0F, 1.6F, 0.6F, 0.8F, 3.6F, new CubeDeformation(0.0F))
		.texOffs(31, 45).addBox(0.35F, 5.0F, 1.6F, 0.6F, 0.8F, 3.6F, new CubeDeformation(0.0F))
		.texOffs(183, 38).addBox(-0.95F, 4.6F, 5.0F, 0.6F, 0.8F, 4.2F, new CubeDeformation(0.0F))
		.texOffs(194, 38).addBox(0.35F, 4.6F, 5.0F, 0.6F, 0.8F, 4.2F, new CubeDeformation(0.0F))
		.texOffs(103, 1).addBox(-0.3F, 7.6F, 2.2F, 0.6F, 0.8F, 7.8F, new CubeDeformation(0.0F))
		.texOffs(20, 38).addBox(-2.9F, 9.7F, 2.6F, 0.6F, 0.8F, 4.8F, new CubeDeformation(0.0F))
		.texOffs(32, 38).addBox(2.3F, 9.7F, 2.6F, 0.6F, 0.8F, 4.8F, new CubeDeformation(0.0F))
		.texOffs(177, 45).addBox(-1.9F, 14.7F, 3.6F, 0.6F, 0.8F, 3.2F, new CubeDeformation(0.0F))
		.texOffs(186, 45).addBox(1.3F, 14.7F, 3.6F, 0.6F, 0.8F, 3.2F, new CubeDeformation(0.0F))
		.texOffs(21, 56).addBox(-2.2F, 0.9F, 7.8F, 0.3F, 0.6F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(25, 56).addBox(1.9F, 0.9F, 7.8F, 0.3F, 0.6F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(29, 56).addBox(-0.22F, 7.7F, 10.0F, 0.44F, 0.6F, 0.7F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 2.0F));

		PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(193, 14).addBox(-2.7F, -4.2F, -1.7F, 5.4F, 3.4F, 3.4F, new CubeDeformation(0.0F))
		.texOffs(63, 23).addBox(-2.1F, -0.8F, -1.35F, 4.2F, 3.6F, 2.7F, new CubeDeformation(0.0F))
		.texOffs(164, 14).addBox(-1.15F, -4.2F, -1.85F, 2.3F, 6.8F, 0.3F, new CubeDeformation(0.0F))
		.texOffs(12, 56).addBox(-0.55F, -2.9F, -2.05F, 1.1F, 1.3F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(58, 56).addBox(-0.3F, -0.6F, -2.0F, 0.6F, 1.2F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(171, 14).addBox(-3.2F, 2.8F, -1.9F, 6.4F, 2.4F, 3.8F, new CubeDeformation(0.0F))
		.texOffs(205, 38).addBox(-4.5F, -5.1F, -1.6F, 1.8F, 1.7F, 3.2F, new CubeDeformation(0.0F))
		.texOffs(216, 38).addBox(2.7F, -5.1F, -1.6F, 1.8F, 1.7F, 3.2F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition skirt = partdefinition.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(154, 38).addBox(-3.6F, -8.1F, -2.3F, 7.2F, 4.1F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(189, 31).addBox(-4.9F, -4.4F, -3.0F, 9.8F, 4.9F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(134, 31).addBox(-6.3F, 0.1F, -3.7F, 12.6F, 4.9F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(71, 31).addBox(-7.7F, 4.6F, -4.4F, 15.4F, 4.4F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(79, 45).addBox(6.3F, 8.6F, -5.1F, 2.6F, 3.4F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(93, 51).addBox(3.2F, 8.6F, -5.1F, 2.5F, 2.2F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(101, 51).addBox(-5.7F, 8.6F, -5.1F, 2.5F, 2.2F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(87, 45).addBox(-8.9F, 8.6F, -5.1F, 2.6F, 3.4F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(95, 45).addBox(-1.3F, -8.3F, -2.6F, 2.6F, 4.3F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(117, 38).addBox(-1.65F, -4.4F, -3.3F, 3.3F, 4.9F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(66, 38).addBox(-2.05F, 0.1F, -4.0F, 4.1F, 4.9F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(171, 38).addBox(-2.45F, 4.6F, -4.7F, 4.9F, 4.4F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(125, 45).addBox(-2.85F, 8.6F, -5.4F, 5.7F, 3.4F, 0.45F, new CubeDeformation(0.0F))
		.texOffs(37, 56).addBox(-0.35F, -6.4F, -2.9F, 0.7F, 1.0F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(41, 56).addBox(-0.35F, -1.4F, -3.6F, 0.7F, 1.0F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(45, 56).addBox(-0.35F, 7.0F, -5.0F, 0.7F, 1.0F, 0.4F, new CubeDeformation(0.0F))
		.texOffs(138, 1).addBox(-4.7F, -8.2F, -2.1F, 1.3F, 4.7F, 4.2F, new CubeDeformation(0.0F))
		.texOffs(150, 1).addBox(3.4F, -8.2F, -2.1F, 1.3F, 4.7F, 4.2F, new CubeDeformation(0.0F))
		.texOffs(73, 1).addBox(-6.1F, -3.9F, -2.5F, 1.6F, 4.9F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(88, 1).addBox(4.5F, -3.9F, -2.5F, 1.6F, 4.9F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(39, 1).addBox(-7.7F, 0.6F, -2.9F, 1.8F, 4.9F, 5.8F, new CubeDeformation(0.0F))
		.texOffs(56, 1).addBox(5.9F, 0.6F, -2.9F, 1.8F, 4.9F, 5.8F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-9.3F, 5.0F, -3.3F, 2.0F, 4.5F, 6.6F, new CubeDeformation(0.0F))
		.texOffs(20, 1).addBox(7.3F, 5.0F, -3.3F, 2.0F, 4.5F, 6.6F, new CubeDeformation(0.0F))
		.texOffs(236, 1).addBox(-10.7F, 9.0F, -3.7F, 1.8F, 3.0F, 4.1F, new CubeDeformation(0.0F))
		.texOffs(1, 14).addBox(8.9F, 9.0F, -3.7F, 1.8F, 3.0F, 4.1F, new CubeDeformation(0.0F))
		.texOffs(227, 38).addBox(-10.7F, 9.0F, 0.8F, 1.8F, 1.6F, 2.9F, new CubeDeformation(0.0F))
		.texOffs(238, 38).addBox(8.9F, 9.0F, 0.8F, 1.8F, 1.6F, 2.9F, new CubeDeformation(0.0F))
		.texOffs(220, 23).addBox(-4.3F, -8.2F, 1.6F, 1.7F, 4.7F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(228, 23).addBox(2.6F, -8.2F, 1.6F, 1.7F, 4.7F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(202, 23).addBox(-5.7F, -3.9F, 2.1F, 1.7F, 4.9F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(211, 23).addBox(4.0F, -3.9F, 2.1F, 1.7F, 4.9F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(126, 14).addBox(-7.3F, 0.6F, 2.5F, 1.9F, 4.9F, 2.2F, new CubeDeformation(0.0F))
		.texOffs(136, 14).addBox(5.4F, 0.6F, 2.5F, 1.9F, 4.9F, 2.2F, new CubeDeformation(0.0F))
		.texOffs(104, 14).addBox(-9.1F, 5.0F, 2.9F, 2.2F, 4.5F, 2.6F, new CubeDeformation(0.0F))
		.texOffs(115, 14).addBox(6.9F, 5.0F, 2.9F, 2.2F, 4.5F, 2.6F, new CubeDeformation(0.0F))
		.texOffs(77, 38).addBox(-10.5F, 9.0F, 3.3F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(88, 38).addBox(8.5F, 9.0F, 3.3F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(136, 38).addBox(-3.7F, -8.1F, 1.5F, 7.4F, 4.1F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(212, 31).addBox(-5.1F, -4.4F, 2.2F, 10.2F, 4.9F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(105, 31).addBox(-6.5F, 0.1F, 2.9F, 13.0F, 4.9F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(1, 31).addBox(-7.9F, 4.6F, 3.6F, 15.8F, 4.4F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(41, 45).addBox(4.6F, 8.6F, 4.3F, 3.3F, 3.4F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(165, 45).addBox(-2.2F, 8.6F, 4.3F, 4.4F, 2.8F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(51, 45).addBox(-7.9F, 8.6F, 4.3F, 3.3F, 3.4F, 0.8F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition train = partdefinition.addOrReplaceChild("train", CubeListBuilder.create().texOffs(162, 31).addBox(-5.6F, -3.4F, -1.9F, 11.2F, 4.4F, 1.6F, new CubeDeformation(0.0F))
		.texOffs(36, 31).addBox(-7.1F, 0.2F, -0.9F, 14.2F, 3.2F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(212, 14).addBox(5.0F, 2.3F, 1.1F, 3.6F, 1.7F, 4.5F, new CubeDeformation(0.0F))
		.texOffs(184, 1).addBox(-3.4F, 1.8F, 1.1F, 6.8F, 2.2F, 5.5F, new CubeDeformation(0.0F))
		.texOffs(230, 14).addBox(-8.6F, 2.3F, 1.1F, 3.6F, 1.7F, 4.5F, new CubeDeformation(0.0F))
		.texOffs(109, 51).addBox(1.0F, 2.4F, 5.8F, 2.0F, 1.55F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(117, 51).addBox(-3.0F, 2.4F, 5.8F, 2.0F, 1.55F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(57, 51).addBox(8.8F, 2.8F, 3.0F, 1.0F, 1.2F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 51).addBox(-9.8F, 2.8F, 3.0F, 1.0F, 1.2F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 6.0F));

		PartDefinition sleeve_R = partdefinition.addOrReplaceChild("sleeve_R", CubeListBuilder.create().texOffs(148, 23).addBox(-1.0F, -1.3F, -2.3F, 2.0F, 4.8F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(128, 23).addBox(-2.9F, 3.1F, -2.7F, 2.3F, 4.9F, 2.1F, new CubeDeformation(0.0F))
		.texOffs(38, 14).addBox(-4.9F, 7.6F, -3.1F, 2.7F, 4.9F, 2.3F, new CubeDeformation(0.0F))
		.texOffs(14, 14).addBox(-6.7F, 12.1F, -3.5F, 2.9F, 4.9F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(210, 1).addBox(-8.5F, 16.6F, -3.9F, 3.1F, 4.4F, 2.7F, new CubeDeformation(0.0F))
		.texOffs(166, 23).addBox(-1.0F, -1.3F, 0.6F, 2.0F, 4.8F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(146, 14).addBox(-2.9F, 3.1F, 1.0F, 2.3F, 5.9F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(60, 14).addBox(-4.9F, 8.6F, 1.2F, 2.7F, 5.4F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(82, 14).addBox(-6.7F, 13.6F, 1.4F, 2.9F, 5.4F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(44, 38).addBox(-8.5F, 18.6F, 1.6F, 3.1F, 3.9F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(184, 23).addBox(-1.4F, 3.1F, -0.6F, 2.4F, 4.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(126, 38).addBox(0.6F, -1.3F, -0.6F, 0.5F, 4.8F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(106, 23).addBox(-3.8F, 12.1F, -0.8F, 2.8F, 4.9F, 1.8F, new CubeDeformation(0.0F))
		.texOffs(99, 38).addBox(-6.9F, 20.6F, -3.7F, 1.3F, 2.9F, 2.3F, new CubeDeformation(0.0F))
		.texOffs(15, 51).addBox(-8.3F, 20.4F, -3.3F, 1.0F, 2.1F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(29, 51).addBox(-7.4F, 22.8F, -3.1F, 1.1F, 2.2F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(43, 51).addBox(-6.8F, 22.1F, 1.7F, 1.3F, 1.9F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(242, 51).addBox(-6.9F, 21.2F, -2.7F, 0.7F, 1.8F, 0.7F, new CubeDeformation(0.0F))
		.texOffs(73, 56).addBox(-6.78F, 23.0F, -2.62F, 0.46F, 0.8F, 0.54F, new CubeDeformation(0.0F))
		.texOffs(71, 51).addBox(-5.9F, 16.4F, -2.7F, 1.3F, 2.1F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(180, 51).addBox(-5.7F, 18.4F, -2.6F, 0.9F, 1.5F, 0.8F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -4.0F, 0.0F));

		PartDefinition sleeve_L = partdefinition.addOrReplaceChild("sleeve_L", CubeListBuilder.create().texOffs(157, 23).addBox(-1.0F, -1.3F, -2.3F, 2.0F, 4.8F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(138, 23).addBox(0.6F, 3.1F, -2.7F, 2.3F, 4.9F, 2.1F, new CubeDeformation(0.0F))
		.texOffs(49, 14).addBox(2.2F, 7.6F, -3.1F, 2.7F, 4.9F, 2.3F, new CubeDeformation(0.0F))
		.texOffs(26, 14).addBox(3.8F, 12.1F, -3.5F, 2.9F, 4.9F, 2.5F, new CubeDeformation(0.0F))
		.texOffs(223, 1).addBox(5.4F, 16.6F, -3.9F, 3.1F, 4.4F, 2.7F, new CubeDeformation(0.0F))
		.texOffs(175, 23).addBox(-1.0F, -1.3F, 0.6F, 2.0F, 4.8F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(155, 14).addBox(0.6F, 3.1F, 1.0F, 2.3F, 5.9F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(71, 14).addBox(2.2F, 8.6F, 1.2F, 2.7F, 5.4F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(93, 14).addBox(3.8F, 13.6F, 1.4F, 2.9F, 5.4F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(55, 38).addBox(5.4F, 18.6F, 1.6F, 3.1F, 3.9F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(193, 23).addBox(-1.0F, 3.1F, -0.6F, 2.4F, 4.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(131, 38).addBox(-1.1F, -1.3F, -0.6F, 0.5F, 4.8F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(117, 23).addBox(1.0F, 12.1F, -0.8F, 2.8F, 4.9F, 1.8F, new CubeDeformation(0.0F))
		.texOffs(108, 38).addBox(5.6F, 20.6F, -3.7F, 1.3F, 2.9F, 2.3F, new CubeDeformation(0.0F))
		.texOffs(22, 51).addBox(7.3F, 20.4F, -3.3F, 1.0F, 2.1F, 1.9F, new CubeDeformation(0.0F))
		.texOffs(36, 51).addBox(6.3F, 22.8F, -3.1F, 1.1F, 2.2F, 1.5F, new CubeDeformation(0.0F))
		.texOffs(50, 51).addBox(5.5F, 22.1F, 1.7F, 1.3F, 1.9F, 1.7F, new CubeDeformation(0.0F))
		.texOffs(246, 51).addBox(6.2F, 21.2F, -2.7F, 0.7F, 1.8F, 0.7F, new CubeDeformation(0.0F))
		.texOffs(76, 56).addBox(6.32F, 23.0F, -2.62F, 0.46F, 0.8F, 0.54F, new CubeDeformation(0.0F))
		.texOffs(77, 51).addBox(4.6F, 16.4F, -2.7F, 1.3F, 2.1F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(185, 51).addBox(4.8F, 18.4F, -2.6F, 0.9F, 1.5F, 0.8F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -4.0F, 0.0F));

		PartDefinition pauldron_R = partdefinition.addOrReplaceChild("pauldron_R", CubeListBuilder.create().texOffs(195, 45).addBox(-1.5F, -0.5F, -0.9F, 1.9F, 1.9F, 1.8F, new CubeDeformation(0.0F))
		.texOffs(227, 45).addBox(-3.0F, -1.7F, -0.55F, 1.9F, 2.1F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(132, 51).addBox(-4.4F, -3.3F, -0.4F, 1.8F, 2.2F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(170, 51).addBox(-5.3F, -4.7F, -0.25F, 1.3F, 1.8F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(241, 45).addBox(-2.1F, -1.1F, -2.1F, 1.7F, 1.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(146, 51).addBox(-3.1F, -2.5F, -1.9F, 1.4F, 1.8F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(1, 51).addBox(-2.1F, -0.9F, 0.9F, 1.7F, 1.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(158, 51).addBox(-3.1F, -2.1F, 1.1F, 1.4F, 1.6F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(226, 51).addBox(-1.6F, 1.8F, -1.7F, 0.6F, 1.9F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(61, 56).addBox(-1.54F, 3.7F, -1.64F, 0.48F, 0.9F, 0.48F, new CubeDeformation(0.0F))
		.texOffs(234, 51).addBox(-3.6F, 0.8F, -0.45F, 0.5F, 1.6F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(67, 56).addBox(-3.54F, 2.4F, -0.39F, 0.38F, 0.9F, 0.48F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -6.0F, 0.0F));

		PartDefinition pauldron_L = partdefinition.addOrReplaceChild("pauldron_L", CubeListBuilder.create().texOffs(204, 45).addBox(-0.4F, -0.5F, -0.9F, 1.9F, 1.9F, 1.8F, new CubeDeformation(0.0F))
		.texOffs(234, 45).addBox(1.1F, -1.7F, -0.55F, 1.9F, 2.1F, 1.1F, new CubeDeformation(0.0F))
		.texOffs(139, 51).addBox(2.6F, -3.3F, -0.4F, 1.8F, 2.2F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(175, 51).addBox(4.0F, -4.7F, -0.25F, 1.3F, 1.8F, 0.5F, new CubeDeformation(0.0F))
		.texOffs(248, 45).addBox(0.4F, -1.1F, -2.1F, 1.7F, 1.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(152, 51).addBox(1.7F, -2.5F, -1.9F, 1.4F, 1.8F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(8, 51).addBox(0.4F, -0.9F, 0.9F, 1.7F, 1.9F, 1.2F, new CubeDeformation(0.0F))
		.texOffs(164, 51).addBox(1.7F, -2.1F, 1.1F, 1.4F, 1.6F, 0.8F, new CubeDeformation(0.0F))
		.texOffs(230, 51).addBox(1.0F, 1.8F, -1.7F, 0.6F, 1.9F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(64, 56).addBox(1.06F, 3.7F, -1.64F, 0.48F, 0.9F, 0.48F, new CubeDeformation(0.0F))
		.texOffs(238, 51).addBox(3.1F, 0.8F, -0.45F, 0.5F, 1.6F, 0.6F, new CubeDeformation(0.0F))
		.texOffs(70, 56).addBox(3.16F, 2.4F, -0.39F, 0.38F, 0.9F, 0.48F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// 先复位所有部件，再按优先级叠加动画（后调用的会覆盖同一骨骼/通道上的前一个）
		this.root().getAllParts().forEach(ModelPart::resetPose);

		// move：移动循环（2.5s looping）
		this.animate(entity.moveState, IceEmpressAnime.move, ageInTicks);
		// ack：普通攻击（1.6s，非循环，放完停在最后一帧由实体的 timer 复位）
		this.animate(entity.ackState, IceEmpressAnime.ack, ageInTicks);
		// phase2：二阶段转场（2.9s，非循环），放在最后以覆盖 move 的通道
		this.animate(entity.phase2State, IceEmpressAnime.phase2, ageInTicks);

		// 头部朝向目标：叠加在动画之上
		this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot += headPitch * Mth.DEG_TO_RAD;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		crown.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		hair.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		skirt.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		train.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		sleeve_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		sleeve_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		pauldron_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		pauldron_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}