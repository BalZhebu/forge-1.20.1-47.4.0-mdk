package com.TovidY.kunluncontinent.block.blockentity;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SpiritGatheringAltarRenderer implements BlockEntityRenderer<SpiritGatheringAltherBlockEntity> {

    private final ItemRenderer itemRenderer;

    public SpiritGatheringAltarRenderer(BlockEntityRendererProvider.Context context) {
        // ✨ 从渲染上下文中获取原版的物品渲染器
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SpiritGatheringAltherBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        int score = blockEntity.getDataAccess().get(0);

        // ------------------ 1. 渲染中心纯代码光晕（常年渲染，不归开关管） ------------------
        if (score > 0) {
            float r = 1.0f, g = 1.0f, b = 1.0f;
            if (score > 300) { r = 1.0f; g = 0.85f; b = 0.0f; }       // 金
            else if (score > 140) { r = 1.0f; g = 0.1f; b = 0.1f; }  // 红
            else if (score > 5) { r = 0.6f; g = 0.1f; b = 1.0f; }    // 紫
            else { r = 0.3f; g = 0.3f; b = 0.9f; }

            float time = (Minecraft.getInstance().level.getGameTime() + partialTick) * 0.05f;
            float pulse = (float) Math.sin(time) * 0.08f;
            float baseSize = 0.8f + pulse;
            float maxAlpha = 0.25f + (float) Math.sin(time * 2.0f) * 0.05f;

            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D); // 光晕在方块中心

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            renderProceduralGlow(poseStack, baseSize, r, g, b, maxAlpha);

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }

        // ✨ 在渲染物品之前，检查客户端配置是否开启了物品渲染
        boolean shouldRenderItems = true;
        var localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            var capLazy = localPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY);
            if (capLazy.isPresent()) {
                // 如果开关 8 是关闭状态，标记为 false
                if (!capLazy.resolve().get().isConfigOpen(8)) {
                    shouldRenderItems = false;
                }
            }
        }

        // 只有在开关开启时，才执行物品渲染逻辑
        if (shouldRenderItems) {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

                // 计算全局时间，用于控制旋转和上下漂浮
                float gameTime = Minecraft.getInstance().level.getGameTime() + partialTick;

                // 环绕半径：物品离中心的距离（0.20格）
                float radius = 0.20f;
                // 基础高度：按照你调整后的高度 0.6D
                double floatY = 1.0D + (Math.sin(gameTime * 0.08f) * 0.05D);

                // 遍历 3 个槽位
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue; // 空槽位不渲染

                    poseStack.pushPose();

                    // 计算当前槽位物品在等边三角形中的角度 (i * 120度)
                    float angle = (float) (i * (Math.PI * 2 / 3) + (gameTime * 0.02f));

                    // 算出相对于大阵中心的 X 和 Z 轴偏移量
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    // 移动坐标系：先移到方块底座中心(0.5)，再移到算好的浮空环绕位
                    poseStack.translate(0.5D + offsetX, floatY, 0.5D + offsetZ);

                    // 让物品自身保持缓缓自转
                    float itemRotation = gameTime * 1.5f;
                    poseStack.mulPose(Axis.YP.rotationDegrees(itemRotation));

                    // 缩放大小
                    poseStack.scale(0.5F, 0.5F, 0.5F);

                    // 调用原版渲染方法
                    BakedModel bakedmodel = this.itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0);
                    this.itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, packedOverlay, bakedmodel);

                    poseStack.popPose();
                }
            });
        }
    }

    /**
     * 纯代码数学渐变光晕方法（完好无损地回归！）
     */
    private void renderProceduralGlow(PoseStack poseStack, float size, float r, float g, float b, float maxAlpha) {
        poseStack.pushPose();
        Quaternionf cameraOrientation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        poseStack.mulPose(cameraOrientation);

        Matrix4f mat = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int segments = 24;
        float radius = size / 2.0f;
        float slice = (float) (Math.PI * 2 / segments);

        for (int i = 0; i < segments; i++) {
            float a1 = i * slice;
            float a2 = (i + 1) * slice;

            float x1 = (float) Math.cos(a1) * radius;
            float y1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float y2 = (float) Math.sin(a2) * radius;

            builder.vertex(mat, 0, 0, 0).color(r, g, b, maxAlpha).endVertex();
            builder.vertex(mat, x1, y1, 0).color(r, g, b, 0.0f).endVertex();
            builder.vertex(mat, x2, y2, 0).color(r, g, b, 0.0f).endVertex();
        }

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
    }
}