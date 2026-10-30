package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 「禁锢」的锁链环 —— 给身上带 {@link ModEffects#JINGU} 的生物套几道绕身旋转的金环。
 *
 * <h3>为什么在客户端画、不发包</h3>
 * <p>DEBUFF 本身已经同步到所有客户端（原版效果同步），所以这里只要每帧扫一遍
 * 「谁有这个效果」就能画 —— 不需要服务端逐个发包，也不会出现
 * 「效果还在、锁链没了」的不同步。玩家、怪物、NPC 一视同仁。</p>
 *
 * <h3>几何：几道水平环</h3>
 * <p>每个目标画 {@value #RINGS} 道环（腰、胸、肩），半径按 {@code getBbWidth()} 缩放，
 * 各自以不同角速度绕 Y 轴转，看起来就是「被几道锁链缠住」。
 * 环本身是一个平面四边形，{@code skill_ring.fsh} 在里面按半径裁出环带、按角度做链节明暗。</p>
 *
 * <h3>⚠️ 位置必须插值</h3>
 * <p>和第 2 魂技的翅膀同源问题：直接读 {@code getX()} 会让环在玩家走动时每 tick 跳一格。
 * 这里统一用 {@code Mth.lerp(partialTick, xOld, getX())}。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JinguChainRenderer {

    private static final String SHADER_NAME = "skill_ring";

    /** 每个目标画几道环。 */
    private static final int RINGS = 3;

    /** 环中心在身体上的高度比例（相对身高）。 */
    private static final float[] RING_HEIGHT = {0.30F, 0.58F, 0.84F};
    /** 环半径相对 {@code getBbWidth()} 的倍率。 */
    private static final float[] RING_SCALE = {1.05F, 1.15F, 1.00F};
    /** 各环自转角速度（弧度/秒，正负交替）。 */
    private static final float[] RING_SPIN = {1.30F, -0.95F, 1.70F};

    /** 环的透明度。 */
    private static final float RING_ALPHA = 0.85F;

    /** 超过这个距离就不画（省顶点）。 */
    private static final double MAX_DISTANCE = 64.0D;

    private JinguChainRenderer() {
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }

        Vec3 cam = event.getCamera().getPosition();
        float pt = event.getPartialTick();

        // 先扫一遍，没有目标就完全不做（省掉 modelview 的 push/pop）
        boolean any = false;
        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof LivingEntity le && le.hasEffect(ModEffects.JINGU.get())
                    && le.distanceToSqr(cam) <= MAX_DISTANCE * MAX_DISTANCE) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }

        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(SHADER_NAME));
        if (shader == null) {
            return;
        }

        float time = (level.getGameTime() + pt) / 20.0F;

        SkillEffect.setUniform(shader, "Time", time);
        SkillEffect.setUniform(shader, "Intensity", 1.0F);

        PoseStack mv = RenderSystem.getModelViewStack();
        mv.pushPose();
        mv.last().pose().set(event.getPoseStack().last().pose());
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        try {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (Entity e : level.entitiesForRendering()) {
                if (!(e instanceof LivingEntity living) || !living.hasEffect(ModEffects.JINGU.get())) {
                    continue;
                }
                if (living.distanceToSqr(cam) > MAX_DISTANCE * MAX_DISTANCE) {
                    continue;
                }
                appendRings(buffer, living, cam, pt, time);
            }

            BufferUploader.drawWithShader(buffer.end());
        } finally {
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            mv.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /** 给一个生物塞进 {@value #RINGS} 道环的顶点。 */
    private static void appendRings(BufferBuilder buffer, LivingEntity living, Vec3 cam,
                                    float partialTick, float time) {
        float ex = (float) Mth.lerp(partialTick, living.xOld, living.getX());
        float ey = (float) Mth.lerp(partialTick, living.yOld, living.getY());
        float ez = (float) Mth.lerp(partialTick, living.zOld, living.getZ());
        float height = living.getBbHeight();
        float baseRadius = living.getBbWidth() * 0.85F + 0.18F;

        for (int i = 0; i < RINGS; i++) {
            float cy = ey + height * RING_HEIGHT[i];
            float r = baseRadius * RING_SCALE[i];
            float spin = time * RING_SPIN[i];

            float cos = Mth.cos(spin);
            float sin = Mth.sin(spin);

            // 四个角的世界坐标（水平面，绕 Y 轴旋转 spin）
            addCorner(buffer, ex, cy, ez, cam, cos, sin, r, -1.0F, -1.0F);
            addCorner(buffer, ex, cy, ez, cam, cos, sin, r, -1.0F, 1.0F);
            addCorner(buffer, ex, cy, ez, cam, cos, sin, r, 1.0F, 1.0F);
            addCorner(buffer, ex, cy, ez, cam, cos, sin, r, 1.0F, -1.0F);
        }
    }

    /**
     * 一个角：(u,v) ∈ [-1,1]²，旋转 spin 后乘半径，再减相机坐标。
     *
     * <p>顶点色里塞的是 {@code (u+1)/2, (v+1)/2}（即 0~1 的局部 UV）与 alpha ——
     * 见 {@code skill_ring.vsh} 的说明：这样多个环能共用一次 draw。</p>
     */
    private static void addCorner(BufferBuilder buffer, float cx, float cy, float cz, Vec3 cam,
                                  float cos, float sin, float r, float u, float v) {
        float lx = (u * cos - v * sin) * r;
        float lz = (u * sin + v * cos) * r;

        buffer.vertex(cx + lx - (float) cam.x, cy - (float) cam.y, cz + lz - (float) cam.z)
                .color((u + 1.0F) * 0.5F, (v + 1.0F) * 0.5F, 0.0F, RING_ALPHA)
                .endVertex();
    }
}
