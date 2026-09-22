package com.TovidY.kunluncontinent.event.client;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.entity.playernpc.PlayerNpcEntity;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.SyncWuhunDataPacket;
import com.TovidY.kunluncontinent.render.KLRenderApi;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
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

    @SubscribeEvent
    public static void renderNpcHunhuan(RenderLivingEvent.Post<?, ?> event) {
        if (!(event.getEntity() instanceof PlayerNpcEntity npc) || !npc.isAlive() || npc.tickCount < 1) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        boolean isOpen = localPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.isConfigOpen(11)).orElse(true);
        if (!isOpen) return;

        if (localPlayer.distanceToSqr(npc) > 2304) return;
        EntityWuhunCache cache = entityWuhunCacheMap.get(npc.getUUID());
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

    /**
     * 第一人称下补渲染"自己身上的魂环"。
     *
     * <p><b>为什么需要单独一份：</b>原版在 {@code LevelRenderer} 里对本地玩家有一条判定
     * （摄像机贴着实体自己时直接跳过），第一人称下**本地玩家实体整个不渲染**，
     * 于是 {@code PlayerRenderer} 不会被调用，{@code RenderPlayerEvent} 也就不会触发 ——
     * 这就是"第一人称看不到自己魂环"的原因。</p>
     *
     * <p>所以这里换到 {@link RenderLevelStageEvent}（第一人称一定会执行）自己补一份。
     * 关键是下面那串变换**与 {@code LivingEntityRenderer.render} 里一字不差**，
     * 这样第一人称看到的魂环与第三人称完全一致，不用去猜模型空间用的是什么单位。</p>
     *
     * <p>总开关是配置 12「第一人称魂环」，默认关闭。</p>
     */
    @SubscribeEvent
    public static void renderFirstPersonRings(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isSpectator()) {
            return;
        }
        // 只在第一人称、且摄像机就挂在自己身上时补渲染
        if (!mc.options.getCameraType().isFirstPerson() || mc.getCameraEntity() != player) {
            return;
        }

        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return;
        }
        // 开关 12 = 第一人称魂环（默认关）；开关 2 = 自身魂环显示，关了就不用画
        if (!cap.isConfigOpen(12) || !cap.isConfigOpen(2)) {
            return;
        }

        EntityWuhunCache cache = entityWuhunCacheMap.get(player.getUUID());
        if (cache == null) {
            // 第一人称下 renderPlayerEventPost 不会跑，懒加载得自己来一次
            List<Integer> nianxians = getLocalPlayerRingNianxians(player);
            if (nianxians.isEmpty()) {
                return;
            }
            cache = getOrCreateCache(player.getUUID());
            cache.update(nianxians);
        }
        if (cache.renderDataList.isEmpty()) {
            return;
        }

        float partialTick = event.getPartialTick();
        Vec3 camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        // ⚠️ 位置必须做 tick 插值。
        // 直接取 player.getX() 拿到的是"当前 tick 的快照"，而画面里其它一切（相机、实体、粒子）
        // 都是用 lerp(xOld, x, partialTick) 插值绘制的 —— 只有这圈魂环每 tick 跳一整格位移。
        // 环本身只有零点几格大，跳一步就是好几个环的宽度，看起来就是疯狂抽动。
        // 这也正是 EntityRenderDispatcher 渲染实体时用的算法，照抄即可。
        double px = Mth.lerp(partialTick, player.xOld, player.getX());
        double py = Mth.lerp(partialTick, player.yOld, player.getY());
        double pz = Mth.lerp(partialTick, player.zOld, player.getZ());

        poseStack.pushPose();
        // 世界坐标 → 相机相对（单位：格）
        poseStack.translate(px - camPos.x, py - camPos.y, pz - camPos.z);
        // ↓↓↓ 以下与 LivingEntityRenderer.render 保持一致，保证和第三人称同一套空间
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        renderRingsForEntity(player, cache, poseStack, partialTick);
        poseStack.popPose();
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

    public static void renderHunhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, EntityWuhunCache cache) {
        poseStack.pushPose();

        float heightOffset = 0.25f + (count * 0.02f);
        poseStack.translate(0.0D, heightOffset, 0.0D);

        float progress = 1f;
        if (cache.isPlayingAnimation && entity instanceof Player) {
            progress = getAnimationProgressFast(cache, count, 9);
        }
        float currentScale = (0.28f + (count * 0.11f)) * progress;
        poseStack.scale(currentScale, currentScale, currentScale);

        float time = entity.level().getGameTime() + partialTick;
        float rotationDegrees = (time * 1.2f) * (count % 2 == 0 ? -1f : 1f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        KLRenderApi.renderStart(HUNHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShaderColor(data.r, data.g, data.b, data.a);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float s = 5.0f;
        bufferbuilder.vertex(matrix4f, -s, 0.0f, -s).uv(0.0f, 0.0f).endVertex();
        bufferbuilder.vertex(matrix4f, -s, 0.0f,  s).uv(0.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f,  s).uv(1.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f, -s).uv(1.0f, 0.0f).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        KLRenderApi.renderEnd(poseStack);
        poseStack.popPose();
    }

    private static void renderShenhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, int totalRingCount, EntityWuhunCache cache) {
        poseStack.pushPose();

        float progress = 1f;
        if (cache.isPlayingAnimation && entity instanceof Player) {
            progress = getAnimationProgressFast(cache, count, totalRingCount);
        }

        // 1. 获取玩家身体朝向并校准
        float bodyYaw = entity.getYRot();

        // 2. 统一先平移到背后相对位置（使用局部坐标系，无需手动计算 sin/cos 避免精度误差偏移）
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw + 180f)); // 对齐玩家背部视角

        float zOffset = -0.6f - (count - 9) * 0.15f; // 背部深度
        float yOffset = 1.2f; // 背部高度（约胸口/翅膀位置）
        poseStack.translate(0.0D, yOffset, zOffset * progress);

        float rotationAngle = (entity.level().getGameTime() + partialTick) * 0.8f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));

        // 4. 缩放与呼吸脉冲
        float scale = 0.22f * progress;
        if (progress >= 1f) {
            scale *= (0.4f + count * 0.12f);
            float pulse = 1.0f + (float) Math.sin((entity.level().getGameTime() + partialTick) * 0.05f) * 0.08f;
            scale *= pulse;
        }
        poseStack.scale(scale, scale, scale);

        KLRenderApi.renderStart(SHENHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShaderColor(data.r, data.g, data.b, data.a);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // 【关键修复】：中心对称映射
        float s = 4.5f;
        bufferbuilder.vertex(matrix4f, -s, 0.0f, -s).uv(0.0f, 0.0f).endVertex();
        bufferbuilder.vertex(matrix4f, -s, 0.0f,  s).uv(0.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f,  s).uv(1.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f, -s).uv(1.0f, 0.0f).endVertex();

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
            // 服务端：把当前魂环列表广播出去，客户端据此刷新渲染缓存并播逐环显现
            PlayerHunhuanAPI.broadcastWuhunRings((ServerPlayer) player, true);
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