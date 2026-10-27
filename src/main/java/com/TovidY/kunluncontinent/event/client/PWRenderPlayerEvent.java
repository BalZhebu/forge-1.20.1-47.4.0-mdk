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

    /**
     * 海神神位的神赐魂环贴图（玩家自绘，512×512）。
     *
     * <p><b>目前停用</b>：观感不满意，专属贴图渲染入口已在 {@link #renderRingsForEntity} 里注释掉，
     * 神赐环暂时按普通神环（{@link #SHENHUAN} + 年限配色）绘制。贴图、映射表、
     * {@link #godRingTexture} 与 {@link #renderGodRingFast} 全部留着，恢复只需解开那一处注释。</p>
     */
    private static final ResourceLocation HAISHEN_HUNHUAN =
            new ResourceLocation(KlMain.MOD_ID, "textures/particle/haishenhunhuan.png");

    /**
     * 神位 id → 神赐魂环贴图。
     *
     * <p>新增一个神位的神环：把图丢进 {@code textures/particle/}，在这里加一行。
     * 没登记的（含旧存档里只标了 shenci、没记神位的环）统一回退海神图。</p>
     */
    private static final Map<String, ResourceLocation> GOD_RING_TEXTURES = Map.of(
            "sea_god", HAISHEN_HUNHUAN
    );

    private static ResourceLocation godRingTexture(String godId) {
        if (godId == null || godId.isEmpty()) {
            return HAISHEN_HUNHUAN;
        }
        ResourceLocation tex = GOD_RING_TEXTURES.get(godId);
        return tex != null ? tex : HAISHEN_HUNHUAN;
    }

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
        /** 这一枚来自哪个神位（空串 = 普通环）。渲染据此选专属贴图。 */
        public final String shenciGod;
        /** 是否为神赐魂环（用专属贴图，且不按年限染色）。 */
        public final boolean shenci;

        public HunhuanRenderData(int nianxian, boolean isShenhuan) {
            this(nianxian, isShenhuan, "");
        }

        public HunhuanRenderData(int nianxian, boolean isShenhuan, String shenciGod) {
            this.nianxian = nianxian;
            this.shenciGod = shenciGod == null ? "" : shenciGod;
            this.shenci = !this.shenciGod.isEmpty();
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
        /** 该玩家是否已成神 —— 第 10 环起是否走顶点渐变由它决定（别人靠同步包带过来）。 */
        public boolean divine = false;
        public boolean isPlayingAnimation = false;
        public long animationStartTime = 0L;
        /** 播放展开动画时用哪种开启方式（见 ANIM_* 常量）。 */
        public int animStyle = ANIM_EXPAND;

        public void update(List<Integer> nianxianList) {
            update(nianxianList, null);
        }

        /**
         * @param shenciGodList 与 {@code nianxianList} 一一对应的「这枚环来自哪个神位」，可为 {@code null}
         */
        public void update(List<Integer> nianxianList, List<String> shenciGodList) {
            renderDataList.clear();
            if (nianxianList != null) {
                for (int i = 0; i < nianxianList.size(); i++) {
                    int nianxian = nianxianList.get(i);
                    String godId = shenciGodList != null && i < shenciGodList.size()
                            ? shenciGodList.get(i) : "";
                    renderDataList.add(new HunhuanRenderData(nianxian, i >= 9, godId));
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
                cache.update(nianxians, getLocalPlayerRingShenciGod(entity));
                cache.divine = isLocalGod(entity);
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
            cache.update(nianxians, getLocalPlayerRingShenciGod(player));
            cache.divine = isLocalGod(player);
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

        // 配置位 15「普通魂环显示」：关掉只保留第 10 环起的那一层
        boolean showNormalRings = localConfig(15);

        for (int count = 0; count < maxDraw; count++) {
            if (count < 9 && !showNormalRings) {
                continue;
            }
            HunhuanRenderData data = list.get(count);
            // 【暂时停用】神赐魂环的专属贴图效果不好看，先关掉 —— 注释掉这一支后，
            // 神赐环会自然落到下面的 count>=9 分支，用普通神环贴图 + 年限配色（= 还原成没做专属图之前的观感）。
            // 后端字段（shenciGod / 同步包 / 赐环逻辑）全部保留，恢复时把注释解开即可。
            // if (data.shenci) {
            //     // 神赐魂环：专属贴图（不吃年限配色）
            //     renderGodRingFast(entity, partialTick, poseStack, data, count, totalRings, cache);
            // } else
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

    /** 与 {@link #getLocalPlayerRingNianxians} 一一对应的神位 id（本地玩家的 capability 自带 {@code getShenciGod()}）。 */
    private static List<String> getLocalPlayerRingShenciGod(Player player) {
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).map(capability -> {
            List<String> flags = new ArrayList<>();
            if (capability.getHunhuankuaiguan() >= 0 && capability.getWuhunList() != null) {
                for (MobAttributeCapability wuhun : capability.getWuhunList()) {
                    if (wuhun != null) flags.add(wuhun.getShenciGodOrDefault());
                }
            }
            return flags;
        }).orElse(Collections.emptyList());
    }

    // 手动刷新本地玩家魂环缓存（例如在玩家吸收/更换魂环时调用此方法）
    public static void refreshLocalPlayerCache(Player player) {
        if (player != null) {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.update(getLocalPlayerRingNianxians(player), getLocalPlayerRingShenciGod(player));
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

    // ==================================================================================
    //  【方案② 顶点渐变】成神后魂环改成「沿圆周四色渐变 + 旋转形成流光」
    //
    //  ⚠️ 一键回退：把 DIVINE_GRADIENT 改成 false 即可，非渐变路径与改动前逐字一致。
    //  ⚠️ 目前只对**本地玩家**生效（别人的 capability 不在客户端）。要让所有玩家都看到，
    //     需要在 SyncWuhunDataPacket 里加一个 divine 布尔并缓存到 EntityWuhunCache。
    // ==================================================================================
    private static final boolean DIVINE_GRADIENT = true;

    /** 四角配色：左上紫 → 左下青 → 右下金 → 右上白。会随环自转连成流动色带。 */
    private static final float[] GRAD_TOP_LEFT = {0.70f, 0.45f, 1.00f};
    private static final float[] GRAD_BOTTOM_LEFT = {0.30f, 0.90f, 1.00f};
    private static final float[] GRAD_BOTTOM_RIGHT = {1.00f, 0.82f, 0.30f};
    private static final float[] GRAD_TOP_RIGHT = {1.00f, 1.00f, 1.00f};

    /**
     * <b>本地玩家</b>是否已成神。
     *
     * <p>只用于初始化自己的缓存；渲染他玩家时一律读 {@code EntityWuhunCache#divine}
     * （别人的 capability 不在客户端，靠 {@code SyncWuhunDataPacket} 带过来）。</p>
     */
    private static boolean isLocalGod(Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (!player.equals(Minecraft.getInstance().player)) {
            return false;
        }
        return player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::isGod).orElse(false);
    }

    /**
     * 读本地玩家的某个配置位（这些开关只影响"我看到的画面"，不跟被渲染的实体走）。
     *
     * <p>默认开：拿不到本地玩家时不做限制。</p>
     */
    private static boolean localConfig(int index) {
        Player local = Minecraft.getInstance().player;
        if (local == null) {
            return true;
        }
        return local.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> cap.isConfigOpen(index)).orElse(true);
    }

    /**
     * 画一枚魂环的四边形。
     *
     * @param gradient  true = 顶点渐变（成神），false = 原来的整体染色（逐字保留原逻辑）
     * @param ownColor  非渐变时的口径：true = 不染年限色（原色显示，神赐环专用）
     * @param alphaMul  开启动画的透明度倍率
     */
    private static void drawRingQuad(Matrix4f matrix4f, float half, HunhuanRenderData data,
                                     boolean ownColor, boolean gradient, float alphaMul) {
        float alpha = data.a * alphaMul;
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        if (gradient) {
            // ⚠️ 必须换成带顶点色的 shader：KLRenderApi#renderStart 设的是 position_tex，
            //    那个管线根本不读 color 属性，不换的话顶点色会被静默忽略（改了像没改）。
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);
            // 底色交给顶点色，shaderColor 只负责整体透明度
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            buffer.vertex(matrix4f, -half, 0.0f, -half).uv(0.0f, 0.0f)
                    .color(GRAD_TOP_LEFT[0], GRAD_TOP_LEFT[1], GRAD_TOP_LEFT[2], alpha).endVertex();
            buffer.vertex(matrix4f, -half, 0.0f, half).uv(0.0f, 1.0f)
                    .color(GRAD_BOTTOM_LEFT[0], GRAD_BOTTOM_LEFT[1], GRAD_BOTTOM_LEFT[2], alpha).endVertex();
            buffer.vertex(matrix4f, half, 0.0f, half).uv(1.0f, 1.0f)
                    .color(GRAD_BOTTOM_RIGHT[0], GRAD_BOTTOM_RIGHT[1], GRAD_BOTTOM_RIGHT[2], alpha).endVertex();
            buffer.vertex(matrix4f, half, 0.0f, -half).uv(1.0f, 0.0f)
                    .color(GRAD_TOP_RIGHT[0], GRAD_TOP_RIGHT[1], GRAD_TOP_RIGHT[2], alpha).endVertex();

            BufferUploader.drawWithShader(buffer.end());
            return;
        }

        // ---- 原逻辑（未成神 / 开关关闭）：整体染色 ----
        // 显式设回 position_tex —— 上一枚环如果走了渐变分支会把 shader 换掉
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        if (ownColor) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        } else {
            RenderSystem.setShaderColor(data.r, data.g, data.b, alpha);
        }

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix4f, -half, 0.0f, -half).uv(0.0f, 0.0f).endVertex();
        buffer.vertex(matrix4f, -half, 0.0f, half).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(matrix4f, half, 0.0f, half).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(matrix4f, half, 0.0f, -half).uv(1.0f, 0.0f).endVertex();

        BufferUploader.drawWithShader(buffer.end());
    }

    // ==================================================================================
    //  【前 9 环 · 符文圈】环本身保持年限原色，外面套一圈缓慢反向旋转的符文刻度
    //
    //  为什么不用「明暗渐变 + 镶边」：亮暗扫过 + 双重轮廓在自转时一直在闪，看着头晕。
    //  符文是**静态色块**、转速只有主环的 1/4 且方向相反，观感是「环在符文里转」，
    //  有结构感但不闪。
    //
    //  ⚠️ 一键回退：RING_RUNE = false（回落到 RING_ORNAMENT 那套，两套全关就是原样）。
    //
    //  【现状：默认关闭】用户实测前 9 环彼此贴得很近，外面再加一圈符文长条看起来很奇怪，
    //  已决定不做前 9 环装饰 —— 只保留第 10 环起的顶点渐变。
    //  代码留着（两套都在），想再试把下面这个开关打开即可，渲染分支不用改。
    // ==================================================================================
    private static final boolean RING_RUNE = false;

    /** 符文个数。 */
    private static final int RUNE_COUNT = 12;
    /** 符文所在半径，相对四边形半宽（调大往外挪、调小往内挪）。 */
    private static final float RUNE_RADIUS_RATIO = 0.90f;
    /** 符文切向长度，相对四边形半宽。 */
    private static final float RUNE_LENGTH_RATIO = 0.22f;
    /** 符文径向宽度（本空间单位，1 = 1 格）。 */
    private static final float RUNE_WIDTH = 0.12f;
    /** 符文圈自转速度（度/tick）。负数 = 与主环反向；主环是 1.2，这里只有它的 1/4。 */
    private static final float RUNE_SPIN_PER_TICK = -0.30f;
    /** 符文抬高量，避免与环面同深度闪烁。 */
    private static final float RUNE_Y_OFFSET = 0.004f;

    /**
     * 在环平面（XZ）上画一圈符文刻度。
     *
     * <p>纯色四边形，不吃贴图、不参与主环自转 —— 所以它的观感是稳定的，
     * 只有缓慢的反向位移。颜色取该环年限色提亮版，前 9 环各是各的色。</p>
     */
    private static void drawRuneRing(Matrix4f matrix4f, float half, HunhuanRenderData data,
                                     float time, float alphaMul) {
        float a = Math.min(1f, data.a * alphaMul + 0.15f);
        float r = Math.min(1f, data.r * 0.55f + 0.45f);
        float g = Math.min(1f, data.g * 0.55f + 0.45f);
        float b = Math.min(1f, data.b * 0.55f + 0.45f);

        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        float radius = half * RUNE_RADIUS_RATIO;
        float halfLen = half * RUNE_LENGTH_RATIO * 0.5f;
        float halfWid = RUNE_WIDTH * 0.5f;
        float baseDeg = time * RUNE_SPIN_PER_TICK;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < RUNE_COUNT; i++) {
            double ang = Math.toRadians(baseDeg + i * (360.0 / RUNE_COUNT));
            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);

            // 径向单位向量 (cos, sin)，切向单位向量 (-sin, cos)
            float cx = cos * radius;
            float cz = sin * radius;
            float tx = -sin;
            float tz = cos;

            // 四角 = 中心 ± 切向*半长 ± 径向*半宽
            float ax = cx + tx * halfLen + cos * halfWid;
            float az = cz + tz * halfLen + sin * halfWid;
            float bx = cx - tx * halfLen + cos * halfWid;
            float bz = cz - tz * halfLen + sin * halfWid;
            float dx = cx + tx * halfLen - cos * halfWid;
            float dz = cz + tz * halfLen - sin * halfWid;
            float ex = cx - tx * halfLen - cos * halfWid;
            float ez = cz - tz * halfLen - sin * halfWid;

            buffer.vertex(matrix4f, ax, RUNE_Y_OFFSET, az).color(r, g, b, a).endVertex();
            buffer.vertex(matrix4f, bx, RUNE_Y_OFFSET, bz).color(r, g, b, a).endVertex();
            buffer.vertex(matrix4f, ex, RUNE_Y_OFFSET, ez).color(r, g, b, a).endVertex();
            buffer.vertex(matrix4f, dx, RUNE_Y_OFFSET, dz).color(r, g, b, a).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
    }
    // ==================================================================================
    //  【旧方案 · 已默认关闭】前 9 环「同色明暗渐变 + 同色镶边」
    //
    //  用户反馈：自转时亮暗扫过 + 双重轮廓一直在闪，看着头晕。
    //  现在改用上面的符文圈；这一套**默认关闭**，留着只为随时对比 / 回退：
    //  RING_RUNE = false 且 RING_ORNAMENT = true 即恢复。
    // ==================================================================================
    private static final boolean RING_ORNAMENT = false;

    /** 镶边环比主环放大多少。 */
    private static final float RIM_SCALE = 1.06f;
    /** 镶边环的透明度（相对主环）。 */
    private static final float RIM_ALPHA = 0.38f;
    /** 镶边环相对主环抬高的量，避免两个 quad 同深度闪烁。 */
    private static final float RIM_Y_OFFSET = 0.003f;

    /**
     * 前 9 环：同色相明暗渐变。
     *
     * <p>四个角按 (0.72 / 1.0 / 1.25 / 0.55) 的亮度系数缩放同一个颜色 ——
     * 色相完全不变，只是环面有了亮暗过渡，自转时看起来像一道光沿环扫过。</p>
     */
    private static void drawTintedGradientQuad(Matrix4f matrix4f, float half,
                                               HunhuanRenderData data, float alphaMul) {
        float a = data.a * alphaMul;
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, a);

        float r = data.r;
        float g = data.g;
        float b = data.b;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.vertex(matrix4f, -half, 0.0f, -half).uv(0.0f, 0.0f)
                .color(r * 0.72f, g * 0.72f, b * 0.72f, a).endVertex();
        buffer.vertex(matrix4f, -half, 0.0f, half).uv(0.0f, 1.0f)
                .color(r, g, b, a).endVertex();
        buffer.vertex(matrix4f, half, 0.0f, half).uv(1.0f, 1.0f)
                .color(Math.min(1f, r * 1.25f), Math.min(1f, g * 1.25f), Math.min(1f, b * 1.25f), a).endVertex();
        buffer.vertex(matrix4f, half, 0.0f, -half).uv(1.0f, 0.0f)
                .color(r * 0.55f, g * 0.55f, b * 0.55f, a).endVertex();

        BufferUploader.drawWithShader(buffer.end());
    }

    /**
     * 前 9 环：外侧一圈同色镶边（提亮 + 半透明 + 略放大）。
     *
     * <p>作用是"描边"——把原本偏透明、偏弱的年限色压出一圈实边，
     * 远看颜色更明确，近看有层次。</p>
     */
    private static void drawRimQuad(Matrix4f matrix4f, float half,
                                    HunhuanRenderData data, float alphaMul) {
        float a = Math.min(1f, data.a * alphaMul * RIM_ALPHA);
        float r = Math.min(1f, data.r * 0.5f + 0.5f);
        float g = Math.min(1f, data.g * 0.5f + 0.5f);
        float b = Math.min(1f, data.b * 0.5f + 0.5f);

        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, a);

        float s = half * RIM_SCALE;
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix4f, -s, RIM_Y_OFFSET, -s).uv(0.0f, 0.0f).endVertex();
        buffer.vertex(matrix4f, -s, RIM_Y_OFFSET, s).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(matrix4f, s, RIM_Y_OFFSET, s).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(matrix4f, s, RIM_Y_OFFSET, -s).uv(1.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }

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

        // 前 9 环（只对成神玩家）：环体保持年限原色，外面套一圈符文；
        // 两套装饰都关掉时回到原来的整体纯色。
        boolean divine = cache.divine;
        if (divine && RING_RUNE) {
            drawRingQuad(matrix4f, RING_HALF, data, false, false, anim.alphaMul());
            drawRuneRing(matrix4f, RING_HALF, data, time, anim.alphaMul());
        } else if (divine && RING_ORNAMENT) {
            drawTintedGradientQuad(matrix4f, RING_HALF, data, anim.alphaMul());
            drawRimQuad(matrix4f, RING_HALF, data, anim.alphaMul());
        } else {
            drawRingQuad(matrix4f, RING_HALF, data, false, false, anim.alphaMul());
        }

        KLRenderApi.renderEnd(poseStack);
        poseStack.popPose();
    }

    private static void renderShenhuanFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, int totalRingCount, EntityWuhunCache cache) {
        renderBackRingFast(entity, partialTick, poseStack, data, count, totalRingCount, cache, SHENHUAN, false);
    }

    /**
     * 神赐魂环（封神赠送的那一枚）：几何与普通神环完全一致，只按神位换专属贴图。
     *
     * <p>{@code ownColor = true} → 不做年限染色（{@code setShaderColor(1,1,1,a)}），
     * 让玩家自己画的美术按原色显示；至于大小/位置仍沿用神环那套"背后的环"。</p>
     */
    private static void renderGodRingFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data, int count, int totalRingCount, EntityWuhunCache cache) {
        renderBackRingFast(entity, partialTick, poseStack, data, count, totalRingCount, cache,
                godRingTexture(data.shenciGod), true);
    }

    /**
     * 背部环（下标 ≥ 9 那一层）的通用绘制。
     *
     * @param texture  使用的贴图
     * @param ownColor true = 用白色染色保留贴图本色；false = 按年限配色
     */
    private static void renderBackRingFast(Entity entity, float partialTick, PoseStack poseStack, HunhuanRenderData data,
                                           int count, int totalRingCount, EntityWuhunCache cache,
                                           ResourceLocation texture, boolean ownColor) {
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

        // 2. 先旋转对齐背部朝向，再沿局部 +Z 往身后推。
        //
        //    ⚠️ 符号已用矩阵脚本验证：绕 Y 转 (-bodyYaw+180) 后，局部 +Z 恰好就是
        //    **角色背后**（与 -facing 的点积 = +1.000，完全同向）。
        //    所以 zOffset 必须取【正】才是往身后走。
        //    原代码取 -0.6f（负值 = 沿局部 -Z = 身前），所以魂环跑到玩家脸前。
        poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw + 180f)); // 对齐玩家背部视角

        // 背部深度（正值 = 沿局部 +Z 推出去 = 往身后）
        float zOffset = 0.6f + (count - 9) * 0.15f;
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

        KLRenderApi.renderStart(texture, poseStack);
        Matrix4f matrix4f = poseStack.last().pose();

        // 成神 → 顶点渐变；神赐环（ownColor）仍然保留美术本色，不参与渐变
        drawRingQuad(matrix4f, SHENHUAN_HALF, data, ownColor,
                DIVINE_GRADIENT && localConfig(14) && !ownColor && cache.divine, anim.alphaMul());

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

    public static void updatePlayerWuhunData(Player player, List<Integer> wuhunNianxianList, List<String> shenciGodList,
                                             boolean divine,
                                             boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        if (wuhunNianxianList.isEmpty()) {
            entityWuhunCacheMap.remove(player.getUUID());
        } else {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.update(wuhunNianxianList, shenciGodList);
            cache.divine = divine;
            cache.isPlayingAnimation = isPlayingAnimation;
            cache.animationStartTime = animationStartTime;
            cache.animStyle = animStyle;
        }
    }

    /** 老签名（不带成神标记）。 */
    public static void updatePlayerWuhunData(Player player, List<Integer> wuhunNianxianList, List<String> shenciGodList,
                                             boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        updatePlayerWuhunData(player, wuhunNianxianList, shenciGodList, false,
                isPlayingAnimation, animationStartTime, animStyle);
    }

    /** 老签名（不带神赐 / 成神标记）。 */
    public static void updatePlayerWuhunData(Player player, List<Integer> wuhunNianxianList,
                                             boolean isPlayingAnimation, long animationStartTime, int animStyle) {
        updatePlayerWuhunData(player, wuhunNianxianList, null, false, isPlayingAnimation, animationStartTime, animStyle);
    }

    public static void startOpenAnimation(Player player) {
        long currentTime = player.level().getGameTime();

        if (player.level().isClientSide) {
            EntityWuhunCache cache = getOrCreateCache(player.getUUID());
            cache.animationStartTime = currentTime;
            cache.isPlayingAnimation = true;
            // 本地自己开武魂时走的是这条纯客户端分支，样式从自己的 capability 取
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> {
                        cache.animStyle = cap.getHunhuanOpenAnim();
                        // 这条分支会先建好缓存，之后 renderPlayerEventPost 就不再走懒加载，
                        // 所以成神标记必须在这里补上，否则渐变永远不生效。
                        cache.divine = cap.isGod();
                    });
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