package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.SyncWuhunDataPacket;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
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

    // 常量与纹理
    private static final int ANIMATION_DURATION = 80;
    private static final int ANIMATION_DELAY_PER_RING = 15;
    private static final ResourceLocation SHENHUAN = new ResourceLocation(KlMain.MOD_ID, "textures/picture/shenhuan.png");
    private static final ResourceLocation HUNHUAN = new ResourceLocation(KlMain.MOD_ID, "textures/picture/particletext.png");

    public static class HunhuanRenderData {
        public final float r, g, b, a;
        public final int nianxian;

        public HunhuanRenderData(int nianxian, boolean isShenhuan) {
            this.nianxian = nianxian;
            float baseAlpha = 0.8f;
            if (isShenhuan) {
                if (nianxian >= 10000000) { r = 0.0f; g = 0.8f; b = 1.0f; a = baseAlpha; }
                else if (nianxian >= 1000000) { r = 1.0f; g = 0.8f; b = 0.0f; a = baseAlpha; }
                else if (nianxian >= 100000) { r = 1.0f; g = 0.0f; b = 0.0f; a = baseAlpha; }
                else if (nianxian >= 10000) { r = 0.1f; g = 0.1f; b = 0.1f; a = baseAlpha; }
                else if (nianxian >= 1000) { r = 0.8f; g = 0.0f; b = 0.8f; a = baseAlpha; }
                else if (nianxian >= 100) { r = 1.0f; g = 1.0f; b = 0.0f; a = baseAlpha; }
                else { r = 1.0f; g = 1.0f; b = 1.0f; a = baseAlpha; }
            } else {
                if (nianxian >= 100000000) { r = 0.0f; g = 1.0f; b = 0.2f; a = 1.0f; }
                else if (nianxian >= 10000000) { r = 0.0f; g = 0.4f; b = 1.0f; a = baseAlpha; }
                else if (nianxian >= 1000000) { r = 1.0f; g = 0.6f; b = 0.1f; a = baseAlpha; }
                else if (nianxian >= 100000) { r = 1.0f; g = 0.0f; b = 0.0f; a = baseAlpha; }
                else if (nianxian >= 10000) { r = 0.1f; g = 0.1f; b = 0.1f; a = baseAlpha; }
                else if (nianxian >= 1000) { r = 0.8f; g = 0.0f; b = 0.8f; a = baseAlpha; }
                else if (nianxian >= 100) { r = 1.0f; g = 1.0f; b = 0.0f; a = baseAlpha; }
                else { r = 1.0f; g = 1.0f; b = 1.0f; a = baseAlpha; }
            }
        }
    }

    // 结构体保存处理好的实体魂环列表
    public static class EntityWuhunCache {
        public final List<HunhuanRenderData> renderDataList = new ArrayList<>();
        public boolean isPlayingAnimation = false;
        public long animationStartTime = 0L;

        public void update(List<Integer> nianxianList) {
            renderDataList.clear();
            if (nianxianList != null) {
                for (int i = 0; i < nianxianList.size(); i++) {
                    int nianxian = nianxianList.get(i);
                    renderDataList.add(new HunhuanRenderData(nianxian, i >= 9));
                }
            }
        }
    }

    // ==========================================
    // 缓存容器（全面使用 UUID 作为 Key，防止内存泄露）
    // ==========================================
    private static final Map<UUID, EntityWuhunCache> entityWuhunCacheMap = new HashMap<>();

    // 获取或创建缓存项
    private static EntityWuhunCache getOrCreateCache(UUID uuid) {
        return entityWuhunCacheMap.computeIfAbsent(uuid, k -> new EntityWuhunCache());
    }

    // 更新网络传输或本地数据的缓存更新入口
    public static void updateNpcWuhunCache(UUID uuid, List<Integer> nianxianList) {
        if (nianxianList == null || nianxianList.isEmpty()) {
            entityWuhunCacheMap.remove(uuid);
        } else {
            EntityWuhunCache cache = getOrCreateCache(uuid);
            cache.update(nianxianList);
        }
    }

    // ==========================================
    // 1. 渲染入口（集成距离剔除与高性能直接读取）
    // ==========================================

    @SubscribeEvent
    public static void renderNpcHunhuan(RenderLivingEvent.Post<?, ?> event) {
        if (!(event.getEntity() instanceof PlayerNpcEntity npc) || !npc.isAlive() || npc.tickCount < 1) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        // 基础距离剔除：超过 48 格直接跳过渲染
        if (localPlayer.distanceToSqr(npc) > 2304) return;

        EntityWuhunCache cache = entityWuhunCacheMap.get(npc.getUUID());
        // 如果缓存为空，进行首次懒加载填充
        if (cache == null) {
            List<Integer> nianxians = getNpcRingNianxians(npc);
            if (nianxians.isEmpty()) return;
            cache = getOrCreateCache(npc.getUUID());
            cache.update(nianxians);
        }

        renderRingsForEntity(npc, cache, event.getPoseStack(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void renderPlayerEventPost(RenderPlayerEvent.Post event) {
        Player localPlayer = Minecraft.getInstance().player;
        Player entity = event.getEntity();
        if (localPlayer == null || entity == null || !entity.isAlive()) return;

        // 距离剔除：超过 48 格直接跳过
        if (localPlayer.distanceToSqr(entity) > 2304) return;

        // 针对本地玩家：只在开关开启时渲染
        if (entity.equals(localPlayer)) {
            boolean isOpen = entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.isConfigOpen(2)).orElse(true);
            if (!isOpen) return;
        }

        EntityWuhunCache cache = entityWuhunCacheMap.get(entity.getUUID());
        // 本地玩家数据懒加载更新
        if (cache == null && entity.equals(localPlayer)) {
            List<Integer> nianxians = getLocalPlayerRingNianxians(entity);
            if (!nianxians.isEmpty()) {
                cache = getOrCreateCache(entity.getUUID());
                cache.update(nianxians);
            }
        }

        if (cache == null || cache.renderDataList.isEmpty()) return;

        renderRingsForEntity(entity, cache, event.getPoseStack(), event.getPartialTick());
    }

    // 统一渲染调度
    private static void renderRingsForEntity(Entity entity, EntityWuhunCache cache, PoseStack poseStack, float partialTick) {
        List<HunhuanRenderData> list = cache.renderDataList;
        int totalRings = list.size();
        int maxDraw = Math.min(20, totalRings);

        for (int count = 0; count < maxDraw; count++) {
            HunhuanRenderData data = list.get(count);
            if (count >= 9) {
                renderShenhuanFast(entity, partialTick, poseStack, data, count, totalRings, cache);
            } else {
                renderHunhuanFast(entity, partialTick, poseStack, data, count, cache);
            }
        }
    }

    // ==========================================
    // 2. 数据获取（仅在改变/初次加载时调用）
    // ==========================================

    private static List<Integer> getNpcRingNianxians(PlayerNpcEntity npc) {
        List<Integer> ringNianxians = new ArrayList<>();
        PlayerAttributeCapability npcCap = npc.getSoulCapability();
        if (npcCap != null && npcCap.getWuhunList() != null) {
            for (MobAttributeCapability r : npcCap.getWuhunList()) {
                if (r != null && r.getNianxian() > 0) {
                    ringNianxians.add((int) r.getNianxian());
                }
            }
        }
        return ringNianxians;
    }

    private static List<Integer> getLocalPlayerRingNianxians(Player player) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(capability -> {
            List<Integer> list = new ArrayList<>();
            if (capability.getHunhuankuaiguan() >= 0 && capability.getWuhunList() != null) {
                for (MobAttributeCapability wuhun : capability.getWuhunList()) {
                    if (wuhun != null) list.add((int) wuhun.getNianxian());
                }
            }
            return list;
        }).orElse(Collections.emptyList());
    }

    // 手动刷新本地玩家魂环缓存（例如在玩家吸收/更换魂环时调用此方法）
    public static void refreshLocalPlayerCache(Player player) {
        if (player != null) {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.update(getLocalPlayerRingNianxians(player));
        }
    }

    // NBT 递归提取年限
    public static void scanCompoundForNianxian(CompoundTag tag, List<Integer> resultList) {
        if (tag == null || tag.isEmpty()) return;

        if (tag.contains("nianxian")) {
            int nianxian = 0;
            byte type = tag.getTagType("nianxian");
            if (type == Tag.TAG_INT) nianxian = tag.getInt("nianxian");
            else if (type == Tag.TAG_LONG) nianxian = (int) tag.getLong("nianxian");
            else if (type == Tag.TAG_DOUBLE) nianxian = (int) tag.getDouble("nianxian");
            else if (type == Tag.TAG_FLOAT) nianxian = (int) tag.getFloat("nianxian");

            if (nianxian > 0) resultList.add(nianxian);
        }

        for (String key : tag.getAllKeys()) {
            Tag subTag = tag.get(key);
            if (subTag instanceof CompoundTag childCompound) {
                scanCompoundForNianxian(childCompound, resultList);
            } else if (subTag instanceof ListTag listTag) {
                for (int i = 0; i < listTag.size(); i++) {
                    if (listTag.get(i) instanceof CompoundTag elemCompound) {
                        scanCompoundForNianxian(elemCompound, resultList);
                    }
                }
            }
        }
    }

    // ==========================================
    // 3. 极速渲染管线（零对象创建与零条件计算）
    // ==========================================

    public static void renderHunhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, EntityWuhunCache cache) {
        KLRenderApi.renderStart(HUNHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        float progress = 1f;
        if (cache.isPlayingAnimation && entity instanceof Player) {
            progress = getAnimationProgressFast(cache, count, 9);
        }

        matrix4f.translate(0, 0.25f + (count * 0.01f), 0);

        float time = entity.level().getGameTime() + partialTick;
        float rotationAngle = (float) Math.PI * 0.005f * time * (count % 2 == 0 ? -1 : 1);
        matrix4f.rotate(rotationAngle, 0.0F, 1.0F, 0.0F);

        float currentScale = (0.28f + (count * 0.11f)) * progress;
        matrix4f.scale(currentScale, 1.0f, currentScale);

        // 直接采用缓存计算好的 RGBA 颜色，免去 if 判断
        RenderSystem.setShaderColor(data.r, data.g, data.b, data.a);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float s = 6.0f;
        bufferbuilder.vertex(matrix4f, -s, 0, -s).uv(0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, -s, 0, s).uv(0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, s, 0, s).uv(1, 1).endVertex();
        bufferbuilder.vertex(matrix4f, s, 0, -s).uv(1, 0).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        KLRenderApi.renderEnd(poseStack);
    }

    private static void renderShenhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, int totalRingCount, EntityWuhunCache cache) {
        poseStack.pushPose();
        KLRenderApi.renderStart(SHENHUAN, poseStack);

        float progress = 1f;
        if (cache.isPlayingAnimation && entity instanceof Player) {
            progress = getAnimationProgressFast(cache, count, totalRingCount);
        }

        float bodyYaw = entity.getYRot();
        float yawRad = (float) Math.toRadians(bodyYaw);
        float zOffset = 0.6f + (count - 9) * 0.15f;

        poseStack.translate(Mth.sin(yawRad) * zOffset * progress, 1.0f, -Mth.cos(yawRad) * zOffset * progress);
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw + 180f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        float rotationAngle = (entity.level().getGameTime() + partialTick) * 0.6f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

        float scale = 0.22f * progress;
        poseStack.scale(scale, scale, scale);
        Matrix4f matrix4f = poseStack.last().pose();

        if (progress >= 1f) {
            matrix4f.scale(0.4f + count * 0.12f, 1, 0.4f + count * 0.12f);
            float pulse = 1.0f + (float) Math.sin((entity.level().getGameTime() + partialTick) * 0.01f) * 0.15f;
            matrix4f.scale(pulse, 1, pulse);
        }

        // 直接采用缓存好的 RGBA 颜色
        RenderSystem.setShaderColor(data.r, data.g, data.b, data.a);

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

    // ==========================================
    // 4. 优化后的动画计算
    // ==========================================

    private static float getAnimationProgressFast(EntityWuhunCache cache, int ringIndex, int totalRingCount) {
        if (!cache.isPlayingAnimation) return 1f;

        long currentTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
        long ringStartTime = cache.animationStartTime + (long) ringIndex * ANIMATION_DELAY_PER_RING;

        if (currentTime < ringStartTime) return 0f;

        float progress = Math.min(1f, Math.max(0f, (float) (currentTime - ringStartTime) / ANIMATION_DURATION));
        if (progress >= 1f && ringIndex == totalRingCount - 1) {
            cache.isPlayingAnimation = false;
        }
        return progress;
    }

    // ==========================================
    // 5. 网络同步与玩家事件
    // ==========================================

    public static void updatePlayerWuhunData(Player player, List<Integer> wuhunNianxianList, boolean isPlayingAnimation, long animationStartTime) {
        if (wuhunNianxianList.isEmpty()) {
            entityWuhunCacheMap.remove(player.getUUID());
        } else {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.update(wuhunNianxianList);
            cache.isPlayingAnimation = isPlayingAnimation;
            cache.animationStartTime = animationStartTime;
        }
    }

    public static void startOpenAnimation(Player player) {
        long currentTime = player.level().getGameTime();

        if (player.level().isClientSide) {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.animationStartTime = currentTime;
            cache.isPlayingAnimation = true;
        } else {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                List<Integer> nianxianList = new ArrayList<>();
                if (capability.getWuhunList() != null) {
                    for (MobAttributeCapability wuhun : capability.getWuhunList()) {
                        if (wuhun != null) nianxianList.add((int) wuhun.getNianxian());
                    }
                }
                SyncWuhunDataPacket packet = new SyncWuhunDataPacket(player.getUUID(), nianxianList, true, currentTime);
                for (ServerPlayer serverPlayer : ((ServerLevel) player.level()).getPlayers(p -> true)) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
                }
            });
        }
    }

    public static void sendCloseNotification(Player player) {
        if (!player.level().isClientSide) {
            SyncWuhunDataPacket packet = new SyncWuhunDataPacket(player.getUUID(), Collections.emptyList(), false, 0L);
            for (ServerPlayer serverPlayer : ((ServerLevel) player.level()).getPlayers(p -> true)) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                if (capability.getHunhuankuaiguan() >= 0) {
                    startOpenAnimation(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                if (capability.getHunhuankuaiguan() >= 0) {
                    startOpenAnimation(player);
                }
            });
        }
    }
}