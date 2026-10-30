package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.item.baseskillist.tianshi.AngelWings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 天使之翼的<b>持续</b>翅膀虚影（天使 2）。
 *
 * <h3>为什么不用 S2CSkillFieldPacket</h3>
 * <p>那个包是「一次性特效」，时长到了客户端自己丢弃。而翅膀的要求是
 * <b>「只要玩家不手动关就一直挂着」</b> —— 没有"关闭"包可以用（技能是开关型的），
 * 所以干脆不发包：{@code angelWingsActive} 这个字段本来就同步到客户端
 * （它影响飞行伤害削减，见 {@code CombatEventHandler}），这里每帧读一次就行。</p>
 *
 * <p>好处是天然满足"手动关闭才消失"：自动收翼 / 手动收翼都会把这个字段置 false，
 * 虚影下一帧就没了，不会出现"翅膀还在飘、状态已经关了"。</p>
 *
 * <h3>只画本地玩家</h3>
 * <p>capability 只同步给本人（{@code SPacketPlayerAttribute} 是发给自己 的），
 * 别的客户端读不到 {@code angelWingsActive}。所以现在只有自己能看见自己的翅膀。
 * 要让别人也看见，得先加一条广播 —— 那是另一个改动，别在这里偷偷发世界粒子凑数。</p>
 *
 * <h3>几何 / 着色器</h3>
 * <p>一块贴在玩家背后的竖直四边形，法线 = 玩家朝向；{@code skill_wing.fsh} 在里面画 6 条羽毛。
 * 顶点 = 世界坐标 − 相机坐标，ModelViewMat 必须手动从 {@code event.getPoseStack()} 同步
 * （见 {@code shader-notes.md} 第一节，漏了就会被推到远裁剪面外）。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AngelWingRenderer {

    private static final String SHADER_NAME = "skill_wing";

    /** 单侧半宽（格）：翅膀张开的总宽 = 4 格。 */
    private static final float HALF_WIDTH = 2.05F;
    /** 半高（格）：总高 3.2 格，够从肩膀罩到小腿上方。 */
    private static final float HALF_HEIGHT = 1.60F;
    /** 平面中心相对脚底的高度（格）。 */
    private static final float CENTER_LIFT = 1.10F;
    /**
     * 往身后推的距离（格）。
     *
     * <p>太近会和玩家模型 / 披风 / 背包逐帧穿插，表现出来就是"闪"（z-fighting）；
     * 太远又像飘在背后。<b>0.5 格</b>是能让开模型厚度的最小实用值。</p>
     */
    private static final float BACK_OFFSET = 0.50F;

    /** 虚影强度：刻意压低，要"看得见轮廓"而不是"一对实体翅膀"。 */
    private static final float INTENSITY = 0.50F;

    private AngelWingRenderer() {
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        if (!AngelWings.isActive(player)) {
            return;
        }

        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(SHADER_NAME));
        if (shader == null) {
            return;
        }

        Vec3 cam = event.getCamera().getPosition();
        float pt = event.getPartialTick();

        // ⚠️ 位置与朝向都必须做帧间插值。
        //    直接用 getX()/getYRot() 是「上一 tick 的离散值」，而相机是插值过的 ——
        //    玩家一走一动，翅膀就会每 tick 跳一格，看起来就是疯狂闪烁。
        float bodyYaw = Mth.rotLerp(pt, player.yBodyRotO, player.yBodyRot);
        float yawRad = bodyYaw * Mth.DEG_TO_RAD;
        float dx = -Mth.sin(yawRad);
        float dz = Mth.cos(yawRad);
        // 右向量（水平、垂直于朝向）
        float rx = -dz;
        float rz = dx;

        float px = (float) Mth.lerp(pt, player.xOld, player.getX());
        float py = (float) Mth.lerp(pt, player.yOld, player.getY());
        float pz = (float) Mth.lerp(pt, player.zOld, player.getZ());

        float cx = (px - (float) cam.x) - dx * BACK_OFFSET;
        float cy = (py - (float) cam.y) + CENTER_LIFT;
        float cz = (pz - (float) cam.z) - dz * BACK_OFFSET;

        float fade = player.isInvisible() ? 0.35F : 1.0F;

        // ---- uniform ----
        SkillEffect.setUniform(shader, "Center", cx, cy, cz);
        SkillEffect.setUniform(shader, "Right", rx, 0.0F, rz);
        SkillEffect.setUniform(shader, "Up", 0.0F, 1.0F, 0.0F);
        SkillEffect.setUniform(shader, "HalfWidth", HALF_WIDTH);
        SkillEffect.setUniform(shader, "HalfHeight", HALF_HEIGHT);
        SkillEffect.setUniform(shader, "Time", (player.tickCount + event.getPartialTick()) / 20.0F);
        SkillEffect.setUniform(shader, "Age", 1.0F);
        SkillEffect.setUniform(shader, "Fade", fade);
        SkillEffect.setUniform(shader, "Intensity", INTENSITY);
        SkillEffect.setUniform(shader, "CoreColor", 1.00F, 0.98F, 0.86F);
        SkillEffect.setUniform(shader, "EdgeColor", 0.82F, 0.63F, 0.16F);

        // ---- 把 ModelViewMat 同步成相机视图矩阵（世界渲染阶段没人替我们做）----
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

            float w = HALF_WIDTH;
            float h = HALF_HEIGHT;
            // 四角：中心 ± 右向量×w ± 上向量×h
            buffer.vertex(cx - rx * w, cy - h, cz - rz * w).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(cx + rx * w, cy - h, cz + rz * w).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(cx + rx * w, cy + h, cz + rz * w).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(cx - rx * w, cy + h, cz - rz * w).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

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
}
