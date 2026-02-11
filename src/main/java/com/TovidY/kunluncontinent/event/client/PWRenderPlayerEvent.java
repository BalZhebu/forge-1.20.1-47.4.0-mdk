package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.SyncWuhunDataPacket;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.*;

//玩家的魂环渲染
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PWRenderPlayerEvent {
    private static final int ANIMATION_DURATION = 80;
    private static final int ANIMATION_DELAY_PER_RING = 15;

    private static final Map<Player, Long> playerShenhuanAnimationStartTime = new HashMap<>();


    private static final ResourceLocation SHENHUAN = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID,"textures/picture/shenhuan.png");
    private static final ResourceLocation HUNHUAN = ResourceLocation.fromNamespaceAndPath(KlMain.MOD_ID, "textures/picture/particletext.png");

    private static final Map<Player, Long> playerOpenAnimationStartTime = new HashMap<>();
    private static final Map<Player, Boolean> playerIsPlayingOpenAnimation = new HashMap<>();
    private static final Map<UUID, List<Integer>> otherPlayerWuhunData = new HashMap<>();
    private static final Map<UUID, Boolean> otherPlayerIsPlayingOpenAnimation = new HashMap<>();
    private static final Map<UUID, Long> otherPlayerAnimationStartTime = new HashMap<>();


    public static void updatePlayerWuhunData(Player player, List<Integer> wuhunNianxianList,
                                             boolean isPlayingAnimation, long animationStartTime) {
        if (wuhunNianxianList.isEmpty()) {
            otherPlayerWuhunData.remove(player.getUUID());
            otherPlayerIsPlayingOpenAnimation.remove(player.getUUID());
            otherPlayerAnimationStartTime.remove(player.getUUID());
        } else {
            otherPlayerWuhunData.put(player.getUUID(), wuhunNianxianList);
            otherPlayerIsPlayingOpenAnimation.put(player.getUUID(), isPlayingAnimation);
            otherPlayerAnimationStartTime.put(player.getUUID(), animationStartTime);
        }
    }

    public static void startOpenAnimation(Player player) {
        long currentTime = player.level().getGameTime();
        playerOpenAnimationStartTime.put(player, currentTime);
        playerIsPlayingOpenAnimation.put(player, true);

        playerShenhuanAnimationStartTime.put(player, currentTime + 9 * ANIMATION_DELAY_PER_RING + ANIMATION_DURATION);

        if (!player.level().isClientSide) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                List<Integer> nianxianList = new ArrayList<>();
                if (capability.getWuhunList() != null) {
                    for (MobAttributeCapability wuhun : capability.getWuhunList()) {
                        if (wuhun != null) {
                            nianxianList.add((int) wuhun.getNianxian());
                        }
                    }
                }
                SyncWuhunDataPacket packet = new SyncWuhunDataPacket(player.getUUID(), nianxianList, true, currentTime);
                for (ServerPlayer serverPlayer : ((ServerLevel)player.level()).getPlayers(p -> true)) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
                }
            });
        }
    }

    public static boolean isPlayingOpenAnimation(Player player) {
        if (player.level().isClientSide) {
            return player.equals(Minecraft.getInstance().player) ?
                    playerIsPlayingOpenAnimation.getOrDefault(player, false) :
                    otherPlayerIsPlayingOpenAnimation.getOrDefault(player.getUUID(), false);
        }
        return false;
    }

    public static float getAnimationProgress(Player player, int ringIndex, int totalRingCount) {
        if (player.equals(Minecraft.getInstance().player)) {
            if (!isPlayingOpenAnimation(player)) return 1f;

            long startTime = playerOpenAnimationStartTime.getOrDefault(player, 0L);
            long currentTime = player.level().getGameTime();
            long ringStartTime = startTime + ringIndex * ANIMATION_DELAY_PER_RING;

            if (currentTime < ringStartTime) return 0f;

            float progress = Math.min(1f, Math.max(0f, (float)(currentTime - ringStartTime) / ANIMATION_DURATION));

            if (progress >= 1f && ringIndex == totalRingCount - 1) {
                playerIsPlayingOpenAnimation.put(player, false);
            }

            return progress;
        } else {
            if (!isPlayingOpenAnimation(player)) return 1f;

            long startTime = otherPlayerAnimationStartTime.getOrDefault(player.getUUID(), 0L);
            long currentTime = Minecraft.getInstance().level.getGameTime();
            long ringStartTime = startTime + ringIndex * ANIMATION_DELAY_PER_RING;

            if (currentTime < ringStartTime) return 0f;

            return Math.min(1f, Math.max(0f, (float)(currentTime - ringStartTime) / ANIMATION_DURATION));
        }
    }

private static void renderShenhuan(Entity entity, float partialTick, PoseStack poseStack, int nianxian, int count, int totalRingCount) {
    poseStack.pushPose();
    KLRenderApi.renderStart(SHENHUAN, poseStack);
    float progress = 1f;
    if (entity instanceof Player player && isPlayingOpenAnimation(player)) {
        progress = getAnimationProgress(player, count, totalRingCount);
    }
    float bodyYaw = entity.getYRot();
    float yawRad = (float) Math.toRadians(bodyYaw);
    float backX = Mth.sin(yawRad);
    float backZ = -Mth.cos(yawRad);
    float currentY = 1.0f;
    float zOffset = 0.6f + (count - 9) * 0.15f;
    poseStack.translate(backX * zOffset * progress, currentY, backZ * zOffset * progress);
    poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw + 180f));
    poseStack.mulPose(Axis.XP.rotationDegrees(90));
    float rotationSpeed = 0.6f;
    float rotationAngle = (entity.level().getGameTime() + partialTick) * rotationSpeed;
    poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
    float scale = 0.22f * progress;
    poseStack.scale(scale, scale, scale);
    Matrix4f matrix4f = poseStack.last().pose();
    if (progress >= 1f) {
        renderHunhuanscale(matrix4f, nianxian, (entity.level().getGameTime() + partialTick), count);
    }
    renderShenhuanColor(matrix4f, nianxian, (entity.level().getGameTime() + partialTick), count);
    // 绘制
    BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    bufferbuilder.vertex(matrix4f, -5f, 0.1f, -5f).uv(0, 0).endVertex();
    bufferbuilder.vertex(matrix4f, -5f, 0.1f, 5f).uv(0, 1).endVertex();
    bufferbuilder.vertex(matrix4f, 5f, 0.1f, 5f).uv(1, 1).endVertex();
    bufferbuilder.vertex(matrix4f, 5f, 0.1f, -5f).uv(1, 0).endVertex();
    BufferUploader.drawWithShader(bufferbuilder.end());
    KLRenderApi.renderEnd(poseStack);
    poseStack.popPose();
}

    private static void renderShenhuanColor(Matrix4f matrix4f, int nianxian, float partialTick, int count) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float alpha = 0.8f;
        if (nianxian >= 10000000) {
            RenderSystem.setShaderColor(0.0f, 0.8f, 1.0f, alpha);
        } else if(nianxian >= 1000000) {
            RenderSystem.setShaderColor(1.0f, 0.8f, 0.0f, alpha);
        } else if(nianxian >= 100000) {
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, alpha);
        } else if(nianxian >= 10000) {
            RenderSystem.setShaderColor(0.1f, 0.1f, 0.1f, alpha);
        } else if(nianxian >= 1000) {
            RenderSystem.setShaderColor(0.8f, 0.0f, 0.8f, alpha);
        } else if(nianxian >= 100) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 0.0f, alpha);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        }
    }

    @SubscribeEvent
    public static void renderPlayerEventPost(RenderPlayerEvent.Post event) {
        PoseStack poseStack = event.getPoseStack();
        Player entity = event.getEntity();
        if (entity == null) return;

        // 获取数据逻辑
        if (entity.equals(Minecraft.getInstance().player)) {
            entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                if (capability.getHunhuankuaiguan() >= 0 && capability.getWuhunList() != null) {
                    List<MobAttributeCapability> list = capability.getWuhunList();
                    for (int count = 0; count < Math.min(20, list.size()); count++) {
                        MobAttributeCapability wuhun = list.get(count);
                        if (wuhun != null) {
                            // 统一调用 renderHunhuan，动画逻辑全在里面
                            renderHunhuan(entity, event.getPartialTick(), poseStack, (int) wuhun.getNianxian(), count);
                        }
                    }
                }
            });
        } else {
            // 其他玩家逻辑同理
            List<Integer> nianxianList = otherPlayerWuhunData.get(entity.getUUID());
            if (nianxianList != null) {
                for (int count = 0; count < Math.min(20, nianxianList.size()); count++) {
                    renderHunhuan(entity, event.getPartialTick(), poseStack, nianxianList.get(count), count);
                }
            }
        }
    }

    public static void renderHunhuan(Entity entity, float partialTick, PoseStack poseStack, int nianxian, int count) {
        KLRenderApi.renderStart(HUNHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        // 1. 获取动画进度
        float progress = 1f;
        if (entity instanceof Player player) {
            progress = getAnimationProgress(player, count, 9);
        }

        // 2. 高度设定：调高至 0.25f 避免蹲下穿模
        // 同时也给 Y 轴加了极小的偏移 (0.01f)，防止多枚魂环在同一高度导致贴图闪烁
        float footY = 0.25f + (count * 0.01f);
        matrix4f.translate(0, footY, 0);

        // 3. 旋转逻辑：匀速转动，没有任何呼吸抖动
        float time = entity.level().getGameTime() + partialTick;
        float rotationAngle = (float)Math.PI * 0.005f * time * (count % 2 == 0 ? -1 : 1);
        matrix4f.rotate(rotationAngle, 0.0F, 1.0F, 0.0F);

        // 4. 缩放逻辑（调整间隔）：
        // 基础大小设定为 0.28f（离身体很近）
        // 间隔设定为 0.11f（比之前的 0.08 稍微大了一点点，让魂环之间有清晰的空隙）
        float targetScale = 0.28f + (count * 0.11f);
        float currentScale = targetScale * progress;
        matrix4f.scale(currentScale, 1.0f, currentScale);

        // 5. 颜色与透明度：固定 0.8f，移除所有淡化效果
        renderHunhuanColorWithAlpha(nianxian, 1.0f);

        // 6. 绘制
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float s = 6.0f; // 模型原始大小
        bufferbuilder.vertex(matrix4f, -s, 0, -s).uv(0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, -s, 0, s).uv(0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, s, 0, s).uv(1, 1).endVertex();
        bufferbuilder.vertex(matrix4f, s, 0, -s).uv(1, 0).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        KLRenderApi.renderEnd(poseStack);
    }

    public static void renderAnimation(Matrix4f matrix4f, int nianxian, float partialTick, int count) {
        matrix4f.rotate((float)Math.PI*0.005f*partialTick*(count%2==0? -1:1), 0.0F, 1.0F, 0.0F);
        matrix4f.translate(0, 0.01f*count, 0);
    }

    private static void renderHunhuanColorWithAlpha(int nianxian, float alphaModifier) {
        // 强制使用固定不透明度，不随动画进度变化
        float baseAlpha = 0.8f;

        if (nianxian >= 10000000) RenderSystem.setShaderColor(0.0f, 0.6f, 1.0f, baseAlpha);
        else if(nianxian >= 1000000) RenderSystem.setShaderColor(1.0f, 0.6f, 0.1f, baseAlpha);
        else if(nianxian >= 100000) RenderSystem.setShaderColor(1.0f, 0, 0, baseAlpha);
        else if(nianxian >= 10000) RenderSystem.setShaderColor(0.1f, 0.1f, 0.1f, baseAlpha);
        else if(nianxian >= 1000) RenderSystem.setShaderColor(0.8f, 0.0f, 0.8f, baseAlpha);
        else if(nianxian >= 100) RenderSystem.setShaderColor(1.0f, 1.0f, 0, baseAlpha);
        else RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, baseAlpha);
    }

    public static void renderHunhuanColor(Matrix4f matrix4f, int nianxian, float partialTick, int count) {
        if (nianxian >= 10000000) {
            RenderSystem.setShaderColor(0.0f, 0.6f, 1.0f, 0.8f);
        } else if(nianxian >= 1000000) {
            RenderSystem.setShaderColor(1.0f, 0.6f, 0.1f, 0.8f);
        } else if(nianxian >= 100000) {
            RenderSystem.setShaderColor(1.0f, 0, 0, 0.6f);
        } else if(nianxian >= 10000) {
            RenderSystem.setShaderColor(0, 0f, 0, 0.6f);
        } else if(nianxian >= 1000) {
            RenderSystem.setShaderColor(1.0f, 0f, 1.0f, 0.4f);
        } else if(nianxian >= 100) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 0, 0.4f);
        } else if(nianxian >= 1) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.8f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.8f);
        }
    }

    public static void renderHunhuanscale(Matrix4f matrix4f, int nianxian, float partialTick, int count) {
        matrix4f.scale(0.4f+count*0.12f, 1, 0.4f+count*0.12f);
        matrix4f.scale(1.0f+(float)Math.sin(partialTick*0.01f)*0.15f, 1, 1.0f+(float)Math.sin(partialTick*0.01f)*0.15f);
    }

    public static void sendCloseNotification(Player player) {
        if (!player.level().isClientSide) {
            SyncWuhunDataPacket packet = new SyncWuhunDataPacket(
                    player.getUUID(),
                    Collections.emptyList(),
                    false,
                    0L
            );

            for (ServerPlayer serverPlayer : ((ServerLevel)player.level()).getPlayers(p -> true)) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
            }
        }
    }

    //加入游戏魂环同步
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                if (capability.getHunhuankuaiguan() >= 0) {
                    PWRenderPlayerEvent.startOpenAnimation(player);
                }
            });
        }
    }

    //进入维度魂环同步
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                if (capability.getHunhuankuaiguan() >= 0) {
                    PWRenderPlayerEvent.startOpenAnimation(player);
                }
            });
        }
    }

}