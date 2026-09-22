package com.TovidY.kunluncontinent.entity.hunhuan;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import static com.TovidY.kunluncontinent.event.client.PWRenderLivingEvent.renderHunhuan;
import static com.TovidY.kunluncontinent.event.client.PWRenderPlayerEvent.*;

//魂环渲染
public class HunhuanRender extends EntityRenderer<HunhuanEntity> {
    public HunhuanRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HunhuanEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 有人骑上去 = 正在被吸收：把地面上这个魂环藏起来。
        // 否则玩家身下会同时压着一个环，看着很怪；吸收动画交给粒子层（HunhuanRingParticle）。
        // 乘客是原版同步的，所以玩家中途下车（吸收中断）会自动恢复显示，不需要额外状态。
        if (!entity.getPassengers().isEmpty()) {
            return;
        }

        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.isConfigOpen(4)) {
                    long nianxian = entity.getNianxianSync();
                    renderHunhuan(entity, partialTicks, poseStack, (int) nianxian, 0);
                }
            });
        }

        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HunhuanEntity entity) {
        return HunhuanEntity.HUNHUAN;
    }
}