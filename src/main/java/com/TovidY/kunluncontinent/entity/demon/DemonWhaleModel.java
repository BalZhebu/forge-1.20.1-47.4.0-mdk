package com.TovidY.kunluncontinent.entity.demon;

import com.TovidY.kunluncontinent.KlMain;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class DemonWhaleModel<T extends DemonWhaleEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(KlMain.MOD_ID, "textures/entity/custommodel.png"), "main");
	private final ModelPart root;
	private final ModelPart All;
	private final ModelPart Body;
	private final ModelPart Back;
	private final ModelPart Backfin;
	private final ModelPart Tail;
	private final ModelPart Tail2;
	private final ModelPart Tail3;
	private final ModelPart Tail4;
	private final ModelPart Fin;
	private final ModelPart Head;
	private final ModelPart Bottom;
	private final ModelPart Finright;
	private final ModelPart Finleft;

	public DemonWhaleModel(ModelPart root) {
		this.root = root;
		this.All = root.getChild("All");
		this.Body = this.All.getChild("Body");
		this.Back = this.Body.getChild("Back");
		this.Backfin = this.Back.getChild("Backfin");
		this.Tail = this.Back.getChild("Tail");
		this.Tail2 = this.Tail.getChild("Tail2");
		this.Tail3 = this.Tail2.getChild("Tail3");
		this.Tail4 = this.Tail3.getChild("Tail4");
		this.Fin = this.Tail4.getChild("Fin");
		this.Head = this.Body.getChild("Head");
		this.Bottom = this.Head.getChild("Bottom");
		this.Finright = this.Body.getChild("Finright");
		this.Finleft = this.Body.getChild("Finleft");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition All = partdefinition.addOrReplaceChild("All", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

		PartDefinition Body = All.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(78, 82).addBox(-8.0F, 0.0F, -21.0F, 16.0F, 12.0F, 21.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, 12.0F));

		PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(0, 82).addBox(-9.0F, -9.0F, -21.0F, 18.0F, 10.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));

		PartDefinition Back = Body.addOrReplaceChild("Back", CubeListBuilder.create(), PartPose.offset(-0.2273F, 0.7899F, -1.2476F));

		PartDefinition Back_r1 = Back.addOrReplaceChild("Back_r1", CubeListBuilder.create().texOffs(126, 115).addBox(-1.0F, -9.0F, 11.0F, 12.0F, 10.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7727F, 11.2101F, -10.7524F, 0.1745F, 0.0F, 0.0F));

		PartDefinition Back_r2 = Back.addOrReplaceChild("Back_r2", CubeListBuilder.create().texOffs(62, 115).addBox(-7.0F, -5.0F, -17.0F, 14.0F, 9.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2273F, -2.8626F, 17.229F, -0.0436F, 0.0F, 0.0F));

		PartDefinition Backfin = Back.addOrReplaceChild("Backfin", CubeListBuilder.create(), PartPose.offset(0.2273F, -9.9122F, -1.357F));

		PartDefinition Backfin_r1 = Backfin.addOrReplaceChild("Backfin_r1", CubeListBuilder.create().texOffs(38, 168).addBox(-1.0F, -3.0F, 1.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.1493F, 8.9911F, -1.5272F, 0.0F, 0.0F));

		PartDefinition Backfin_r2 = Backfin.addOrReplaceChild("Backfin_r2", CubeListBuilder.create().texOffs(52, 144).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.2811F, 12.0318F, -1.6553F, 0.0059F, -0.0141F));

		PartDefinition Backfin_r3 = Backfin.addOrReplaceChild("Backfin_r3", CubeListBuilder.create().texOffs(170, 189).addBox(-2.0F, -3.0F, 0.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -3.5F, 6.0622F, -1.3526F, 0.0F, 0.0F));

		PartDefinition Backfin_r4 = Backfin.addOrReplaceChild("Backfin_r4", CubeListBuilder.create().texOffs(188, 108).addBox(-2.0F, -7.0F, 0.0F, 4.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F));

		PartDefinition Tail = Back.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(0, 144).addBox(-5.0F, -7.9704F, 0.3053F, 10.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2273F, 0.5854F, 17.4681F));

		PartDefinition Tail_r1 = Tail.addOrReplaceChild("Tail_r1", CubeListBuilder.create().texOffs(124, 144).addBox(-5.0F, -1.0F, -9.5F, 10.0F, 5.0F, 17.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 0.0F, 9.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition Tail2 = Tail.addOrReplaceChild("Tail2", CubeListBuilder.create(), PartPose.offset(0.0F, -1.3798F, 15.1802F));

		PartDefinition Tail2_r1 = Tail2.addOrReplaceChild("Tail2_r1", CubeListBuilder.create().texOffs(154, 30).addBox(-4.0F, -2.5F, -8.0F, 8.0F, 7.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 8.0F, 0.0436F, 0.0F, 0.0F));

		PartDefinition Tail3 = Tail2.addOrReplaceChild("Tail3", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5244F, 12.0322F));

		PartDefinition Tail3_r1 = Tail3.addOrReplaceChild("Tail3_r1", CubeListBuilder.create().texOffs(140, 166).addBox(-3.0F, -4.5F, -9.0F, 6.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition Tail4 = Tail3.addOrReplaceChild("Tail4", CubeListBuilder.create(), PartPose.offset(-0.0541F, -2.4304F, 13.2488F));

		PartDefinition Tail4_r1 = Tail4.addOrReplaceChild("Tail4_r1", CubeListBuilder.create().texOffs(88, 184).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0541F, 0.2906F, -0.4874F, 0.0827F, -0.0183F, 0.0201F));

		PartDefinition Fin = Tail4.addOrReplaceChild("Fin", CubeListBuilder.create(), PartPose.offset(3.0541F, 1.0737F, -6.4361F));

		PartDefinition Fin_r1 = Fin.addOrReplaceChild("Fin_r1", CubeListBuilder.create().texOffs(160, 0).addBox(-1.0F, -1.0F, -11.0F, 15.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.2494F, -2.8547F, 21.6838F, 0.1355F, 0.2595F, 0.035F));

		PartDefinition Fin_r2 = Fin.addOrReplaceChild("Fin_r2", CubeListBuilder.create().texOffs(58, 184).addBox(-7.0F, -1.0F, -8.0F, 7.0F, 2.0F, 8.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(15.7166F, -3.2044F, 24.3397F, 0.1415F, -0.3892F, -0.054F));

		PartDefinition Fin_r3 = Fin.addOrReplaceChild("Fin_r3", CubeListBuilder.create().texOffs(180, 156).addBox(0.0F, -1.0F, 0.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-19.6116F, -1.5278F, 11.6046F, 0.1841F, -0.7769F, -0.1298F));

		PartDefinition Fin_r4 = Fin.addOrReplaceChild("Fin_r4", CubeListBuilder.create().texOffs(152, 95).addBox(-14.0F, -1.0F, -11.0F, 15.0F, 2.0F, 11.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(9.2494F, -2.8547F, 21.6838F, 0.1355F, -0.2595F, -0.035F));

		PartDefinition Fin_r5 = Fin.addOrReplaceChild("Fin_r5", CubeListBuilder.create().texOffs(28, 184).addBox(0.0F, -1.0F, -8.0F, 7.0F, 2.0F, 8.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(-21.7166F, -3.2044F, 24.3397F, 0.1415F, 0.3892F, 0.054F));

		PartDefinition Fin_r6 = Fin.addOrReplaceChild("Fin_r6", CubeListBuilder.create().texOffs(96, 166).addBox(-7.0F, -2.0F, -1.0F, 8.0F, 4.0F, 14.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1767F, 0.734F, 0.119F));

		PartDefinition Fin_r7 = Fin.addOrReplaceChild("Fin_r7", CubeListBuilder.create().texOffs(0, 182).addBox(-3.0F, -1.0F, 0.0F, 3.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.6827F, -2.4507F, 18.6152F, 0.1372F, -0.3027F, -0.0411F));

		PartDefinition Fin_r8 = Fin.addOrReplaceChild("Fin_r8", CubeListBuilder.create().texOffs(180, 176).addBox(0.0F, -1.0F, 0.0F, 3.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-26.6827F, -2.4507F, 18.6152F, 0.1372F, 0.3027F, 0.0411F));

		PartDefinition Fin_r9 = Fin.addOrReplaceChild("Fin_r9", CubeListBuilder.create().texOffs(178, 144).addBox(-6.0F, -1.0F, 0.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(13.6116F, -1.5278F, 11.6046F, 0.1841F, 0.7769F, 0.1298F));

		PartDefinition Fin_r10 = Fin.addOrReplaceChild("Fin_r10", CubeListBuilder.create().texOffs(142, 186).addBox(-6.0F, -1.0F, 0.0F, 6.0F, 2.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-9.6834F, -1.6839F, 12.7905F, 0.2575F, -1.0326F, -0.2223F));

		PartDefinition Fin_r11 = Fin.addOrReplaceChild("Fin_r11", CubeListBuilder.create().texOffs(114, 186).addBox(0.0F, -1.0F, 0.0F, 6.0F, 2.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(3.6834F, -1.6839F, 12.7905F, 0.2575F, 1.0326F, 0.2223F));

		PartDefinition Fin_r12 = Fin.addOrReplaceChild("Fin_r12", CubeListBuilder.create().texOffs(52, 166).addBox(-1.0F, -2.0F, -1.0F, 8.0F, 4.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.1767F, -0.734F, -0.119F));

		PartDefinition Head = Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(92, 61).addBox(-7.0F, 0.0F, -13.0F, 14.0F, 8.0F, 13.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-5.0046F, 0.0F, -35.9125F, 10.0F, 8.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.9093F, -20.6747F));

		PartDefinition Head_r1 = Head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(0, 44).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0046F, 0.0F, -35.9125F, 0.0436F, 0.0F, 0.0F));

		PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(92, 30).addBox(-8.0F, -4.0F, -23.0F, 8.0F, 8.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 4.0F, -13.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition Head_r3 = Head.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(0, 113).addBox(0.0F, -4.0F, -23.0F, 8.0F, 8.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 4.0F, -13.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition Bottom = Head.addOrReplaceChild("Bottom", CubeListBuilder.create().texOffs(92, 0).addBox(-6.0F, -2.0038F, -36.9128F, 12.0F, 8.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.602F, 0.7318F));

		PartDefinition Bottom_r1 = Bottom.addOrReplaceChild("Bottom_r1", CubeListBuilder.create().texOffs(62, 142).addBox(-8.0F, -9.0F, -15.0F, 16.0F, 9.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.3073F, -1.0571F, -0.0873F, 0.0F, 0.0F));

		PartDefinition Finright = Body.addOrReplaceChild("Finright", CubeListBuilder.create(), PartPose.offset(-8.0F, 1.25F, -15.75F));

		PartDefinition Finright_r1 = Finright.addOrReplaceChild("Finright_r1", CubeListBuilder.create().texOffs(152, 79).addBox(1.5F, 0.0F, -4.5F, 22.5F, 0.0F, 7.5F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(-25.1163F, 16.8901F, 17.4898F, 0.0F, 0.3054F, -0.5672F));

		PartDefinition Finright_r2 = Finright.addOrReplaceChild("Finright_r2", CubeListBuilder.create().texOffs(154, 51).addBox(0.0F, 0.0F, -7.5F, 10.5F, 0.0F, 7.5F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(-5.0509F, 4.1071F, 13.134F, 0.0F, 0.7854F, -0.5672F));

		PartDefinition Finright_r3 = Finright.addOrReplaceChild("Finright_r3", CubeListBuilder.create().texOffs(188, 122).addBox(-7.5F, -0.75F, 0.0F, 7.5F, 1.5F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-27.4529F, 18.3787F, 12.1677F, 0.0F, 1.4399F, -0.5672F));

		PartDefinition Finright_r4 = Finright.addOrReplaceChild("Finright_r4", CubeListBuilder.create().texOffs(160, 13).addBox(-7.5F, -2.25F, -11.25F, 10.5F, 6.0F, 7.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.0F, 0.1745F, -0.5672F));

		PartDefinition Finright_r5 = Finright.addOrReplaceChild("Finright_r5", CubeListBuilder.create().texOffs(146, 61).addBox(-27.0F, -1.5F, 0.0F, 27.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.2543F, 5.5108F, -0.2995F, 0.0F, 0.48F, -0.5672F));

		PartDefinition Finleft = Body.addOrReplaceChild("Finleft", CubeListBuilder.create(), PartPose.offset(8.0F, 1.5F, -15.5F));

		PartDefinition Finleft_r1 = Finleft.addOrReplaceChild("Finleft_r1", CubeListBuilder.create().texOffs(0, 168).addBox(-3.0F, -2.25F, -11.25F, 10.5F, 6.0F, 7.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.25F, 7.75F, 0.0F, -0.1745F, 0.5672F));

		PartDefinition Finleft_r2 = Finleft.addOrReplaceChild("Finleft_r2", CubeListBuilder.create().texOffs(188, 130).addBox(0.0F, -0.75F, 0.0F, 7.5F, 1.5F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(27.4529F, 18.1287F, 11.9177F, 0.0F, -1.4399F, 0.5672F));

		PartDefinition Finleft_r3 = Finleft.addOrReplaceChild("Finleft_r3", CubeListBuilder.create().texOffs(180, 168).addBox(-10.5F, 0.0F, -7.5F, 10.5F, 0.0F, 7.5F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(5.0509F, 3.8571F, 12.884F, 0.0F, -0.7854F, 0.5672F));

		PartDefinition Finleft_r4 = Finleft.addOrReplaceChild("Finleft_r4", CubeListBuilder.create().texOffs(152, 87).addBox(-24.0F, 0.0F, -4.5F, 22.5F, 0.0F, 7.5F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(25.1163F, 16.6401F, 17.2398F, 0.0F, -0.3054F, 0.5672F));

		PartDefinition Finleft_r5 = Finleft.addOrReplaceChild("Finleft_r5", CubeListBuilder.create().texOffs(146, 70).addBox(0.0F, -1.5F, 0.0F, 27.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2543F, 5.2608F, -0.5495F, 0.0F, -0.48F, 0.5672F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.walkAnimationState, DemonWhaleMove.move, ageInTicks);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		All.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}