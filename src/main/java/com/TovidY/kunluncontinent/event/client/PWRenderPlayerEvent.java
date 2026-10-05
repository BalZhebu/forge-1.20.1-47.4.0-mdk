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

    /** 逐环展开：原地由小放大（最早的样子）。 */
    public static final int ANIM_EXPAND = 0;
    /** 天降落位：从头顶之上垂落，落定带一点回弹。 */
    public static final int ANIM_DESCEND = 1;
    /** 魂环升腾：自脚下地面之下破地而出，冲天越过环位再缓缓落定 —— 斗罗里魂环就是从脚下升起来的。 */
    public static final int ANIM_ASCEND = 2;

    /** 样式显示名，配置界面直接用这份，避免两处各写一遍。 */
    public static final String[] ANIM_STYLE_NAMES = {"逐环展开", "天降落位", "魂环升腾"};

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
        /** 播放展开动画时用哪种开启方式（见 ANIM_* 常量）。 */
        public int animStyle = ANIM_EXPAND;

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

    private static final Map<UUID, EntityWuhunCache> entityWuhunCacheMap = new HashMap<>();

    private static EntityWuhunCache getOrCreateCache(UUID uuid) {
        return entityWuhunCacheMap.computeIfAbsent(uuid, k -> new EntityWuhunCache());
    }

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

        if (localPlayer.distanceToSqr(entity) > 2304) return;

        if (entity.equals(localPlayer)) {
            boolean isOpen = entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.isConfigOpen(2)).orElse(true);
            if (!isOpen) return;
        }

        EntityWuhunCache cache = entityWuhunCacheMap.get(entity.getUUID());

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
     * 位置用 {@code RenderPlayerEvent.Post} 同款口径：相机相对坐标里平移到玩家脚底即可
     * （Post 事件是在 {@code LivingEntityRenderer.render} 的 {@code popPose()} 之后触发的，
     * 所以它那边也没有模型变换，两边天然同一套空间）。</p>
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
        if (!mc.options.getCameraType().isFirstPerson() || mc.getCameraEntity() != player) {
            return;
        }

        PlayerAttributeCapability cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap == null) {
            return;
        }
        if (!cap.isConfigOpen(12) || !cap.isConfigOpen(2)) {
            return;
        }

        EntityWuhunCache cache = entityWuhunCacheMap.get(player.getUUID());
        if (cache == null) {
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

        double px = Mth.lerp(partialTick, player.xOld, player.getX());
        double py = Mth.lerp(partialTick, player.yOld, player.getY());
        double pz = Mth.lerp(partialTick, player.zOld, player.getZ());

        poseStack.pushPose();
        poseStack.translate(px - camPos.x, py - camPos.y, pz - camPos.z);

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

    /** 魂环平面四边形的半宽（本空间里 1 单位 = 1 格，所以它同时也是该环的半径基准）。 */
    private static final float RING_HALF = 5.0f;
    /** 神环（第 10 枚起）的四边形半宽。 */
    private static final float SHENHUAN_HALF = 4.5f;

    public static void renderHunhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, EntityWuhunCache cache) {
        float time = entity.level().getGameTime() + partialTick;

        // 1. 先算这枚环的基础尺寸与动画相位
        float baseScale = 0.28f + (count * 0.11f);
        float progress = 1f;
        if (cache.isPlayingAnimation && entity instanceof Player) {
            progress = getAnimationProgressFast(cache, count, 9);
        }
        float restY = 0.25f + (count * 0.02f);
        OpenAnim anim = openAnim(cache.animStyle, progress, count, restY, entity);

        poseStack.pushPose();

        // 2. 位置：基础悬浮高度 + 样式附加位移（"魂环升腾"的 y 从地面之下一直冲到环位上方）
        poseStack.translate(anim.x(), restY + anim.y(), anim.z());

        // 3. 尺寸与自转（升腾/坠落途中会额外多转，落位时速度归零）
        float currentScale = baseScale * anim.scaleMul();
        poseStack.scale(currentScale, currentScale, currentScale);

        float rotationDegrees = (time * 1.2f) * (count % 2 == 0 ? -1f : 1f) + anim.spinDeg();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        KLRenderApi.renderStart(HUNHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShaderColor(data.r, data.g, data.b, data.a * anim.alphaMul());

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float s = RING_HALF;
        bufferbuilder.vertex(matrix4f, -s, 0.0f, -s).uv(0.0f, 0.0f).endVertex();
        bufferbuilder.vertex(matrix4f, -s, 0.0f,  s).uv(0.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f,  s).uv(1.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f, -s).uv(1.0f, 0.0f).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        KLRenderApi.renderEnd(poseStack);
        poseStack.popPose();
    }

    private static void renderShenhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, int totalRingCount, EntityWuhunCache cache) {
        // 1. 获取玩家身体朝向并校准
        float bodyYaw = entity.getYRot();
        float time = entity.level().getGameTime() + partialTick;

        float progress = 1f;
        if (cache.isPlayingAnimation && entity instanceof Player) {
            progress = getAnimationProgressFast(cache, count, totalRingCount);
        }

        // 满尺寸（含枚数加成）—— 原来在 progress>=1 时才乘枚数，会在展开末尾"啪"地放大一下，这里统一提前算
        float fullScale = 0.22f * (0.4f + count * 0.12f);
        OpenAnim anim = openAnim(cache.animStyle, progress, count, 1.2f, entity);

        poseStack.pushPose();

        // 2. 统一先平移到背后相对位置（使用局部坐标系，无需手动计算 sin/cos 避免精度误差偏移）
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw + 180f)); // 对齐玩家背部视角

        float zOffset = -0.6f - (count - 9) * 0.15f; // 背部深度
        float yOffset = 1.2f; // 背部高度（约胸口/翅膀位置）
        poseStack.translate(anim.x(), yOffset + anim.y(), zOffset * progress + anim.z());

        // 3. 自转：基础慢转 + 样式附加
        float rotationAngle = time * 0.8f + anim.spinDeg();
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));

        // 4. 缩放与呼吸脉冲（展开动画期间按 anim 缩放，结束后叠加枚数倍率与呼吸）
        float scale = fullScale * anim.scaleMul();
        if (progress >= 1f) {
            float pulse = 1.0f + (float) Math.sin(time * 0.05f) * 0.08f;
            scale *= pulse;
        }
        poseStack.scale(scale, scale, scale);

        KLRenderApi.renderStart(SHENHUAN, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        RenderSystem.setShaderColor(data.r, data.g, data.b, data.a * anim.alphaMul());

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // 【关键修复】：中心对称映射
        float s = SHENHUAN_HALF;
        bufferbuilder.vertex(matrix4f, -s, 0.0f, -s).uv(0.0f, 0.0f).endVertex();
        bufferbuilder.vertex(matrix4f, -s, 0.0f,  s).uv(0.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f,  s).uv(1.0f, 1.0f).endVertex();
        bufferbuilder.vertex(matrix4f,  s, 0.0f, -s).uv(1.0f, 0.0f).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        KLRenderApi.renderEnd(poseStack);
        poseStack.popPose();
    }

    // ==========================================
    //  魂环开启动画：三种"开启方式"
    // ==========================================

    /**
     * 单枚魂环在开启动画里的位移 / 缩放 / 自转 / 透明度偏移。
     *
     * @param x        水平偏移 X（格）
     * @param y        额外高度（格，向上为正）
     * @param z        水平偏移 Z（格）
     * @param spinDeg  额外自转角度（度）
     * @param scaleMul 尺寸倍率
     * @param alphaMul 透明度倍率
     */
    private record OpenAnim(float x, float y, float z, float spinDeg, float scaleMul, float alphaMul) {
        /** 动画结束（或本就没有动画）时的零偏移，保证静止状态和以前完全一致。 */
        static final OpenAnim IDLE = new OpenAnim(0f, 0f, 0f, 0f, 1f, 1f);
    }

    /**
     * 按当前"开启方式"算出一枚魂环的动画偏移。
     *
     * <p>所有样式都只在 {@code progress < 1} 期间生效，动画一结束立刻退回
     * {@link OpenAnim#IDLE}，所以静止时的观感与没有动画时完全一样。</p>
     *
     * @param style    样式下标（见 ANIM_* 常量）
     * @param progress 该枚环的显现进度 0~1
     * @param count    环序号（第几枚魂环）
     * @param restY    这枚环**落定后**的高度（格），"魂环升腾"要用它算"地面之下"的起始点
     * @param entity   承载魂环的实体（用身高决定"头顶之上"的高度）
     */
    private static OpenAnim openAnim(int style, float progress, int count, float restY, Entity entity) {
        float t = Mth.clamp(progress, 0f, 1f);
        if (t >= 1f) {
            return OpenAnim.IDLE;
        }
        float ease = easeOutCubic(t);
        // 起手 1/4 段淡入，避免环"啪"地从无到有
        float fade = Mth.clamp(t * 4f, 0f, 1f);

        return switch (style) {
            // 天降落位：从头顶之上垂落，落定带一点回弹；下落途中转得快，落定后归位
            case ANIM_DESCEND -> {
                float drop = entity.getBbHeight() * 1.35f;
                yield new OpenAnim(0f,
                        drop * (1f - easeOutBack(t)),
                        0f,
                        240f * (1f - ease),
                        0.30f + 0.70f * easeOutCubic(Mth.clamp(t * 1.5f, 0f, 1f)),
                        fade);
            }
            // 魂环升腾：从脚下地面之下破地而出，冲天越过环位，再缓缓落定。
            // 斗罗里魂环的标志出场就是"从脚下升起来"——环从地里钻出来那一瞬间被地面挡住，
            // 看起来就是真的从地里长出来的。
            case ANIM_ASCEND -> {
                float startY = -(restY + 1.1f);   // 起始点：地面之下（世界高度 ≈ -0.85 格）
                float peak = 1.15f;               // 冲出后越过环位的最高点（那一下最霸气）
                float riseT = Math.min(1f, t / 0.62f);                   // 前段：破地上冲
                float settleT = Mth.clamp((t - 0.62f) / 0.38f, 0f, 1f);  // 后段：缓缓落位
                float y = startY + (peak - startY) * easeOutCubic(riseT);
                if (settleT > 0f) {
                    // easeOutBack 的过冲让环落位前先往上轻轻一挑，再稳稳压回环位
                    y = peak * (1f - easeOutBack(settleT));
                }
                yield new OpenAnim(0f,
                        y,
                        0f,
                        420f * (1f - ease),          // 出土时转得最快，落位时刚好停住
                        0.40f + 0.60f * easeOutBack(t),
                        Mth.clamp(t * 5f, 0f, 1f));
            }
            // 逐环展开（默认）：原地由小放大
            default -> new OpenAnim(0f, 0f, 0f, 0f, t, fade);
        };
    }

    /** 三次缓出：起步快、末段缓 —— 用来做"落下 / 收束"的手感。 */
    private static float easeOutCubic(float t) {
        float u = 1f - t;
        return 1f - u * u * u;
    }

    /** 回弹缓出：中途轻微过冲再收回，落位时那一下"顿"。t=1 时正好等于 1，不会留下残差。 */
    private static float easeOutBack(float t) {
        final float c1 = 1.35f;
        final float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
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

    public static void updatePlayerWuhunData(Player player, List<Integer> wuhunNianxianList, boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        if (wuhunNianxianList.isEmpty()) {
            entityWuhunCacheMap.remove(player.getUUID());
        } else {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.update(wuhunNianxianList);
            cache.isPlayingAnimation = isPlayingAnimation;
            cache.animationStartTime = animationStartTime;
            cache.animStyle = animStyle;
        }
    }

    public static void startOpenAnimation(Player player) {
        long currentTime = player.level().getGameTime();

        if (player.level().isClientSide) {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.animationStartTime = currentTime;
            cache.isPlayingAnimation = true;
            // 本地自己开武魂时走的是这条纯客户端分支，样式从自己的 capability 取
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> cache.animStyle = cap.getHunhuanOpenAnim());
        } else {
            // 服务端：把当前魂环列表广播出去，客户端据此刷新渲染缓存并播逐环显现
            PlayerHunhuanAPI.broadcastWuhunRings((ServerPlayer) player, true);
        }
    }

    public static void sendCloseNotification(Player player) {
        if (!player.level().isClientSide) {
            SyncWuhunDataPacket packet = new SyncWuhunDataPacket(player.getUUID(), Collections.emptyList(), false, 0L, ANIM_EXPAND);
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