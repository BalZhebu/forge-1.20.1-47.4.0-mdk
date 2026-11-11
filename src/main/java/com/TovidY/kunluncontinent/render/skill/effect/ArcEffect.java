package com.TovidY.kunluncontinent.render.skill.effect;

import com.TovidY.kunluncontinent.render.skill.SkillEffect;
import com.TovidY.kunluncontinent.render.skill.SkillEffectRenderer;
import com.TovidY.kunluncontinent.render.skill.SkillShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 「横向刀光」—— 天使 1「圣光斩」专用。
 *
 * <h3>它长什么样</h3>
 * <p>施法者身前一段<b>水平弧形光刃</b>：刀根在 {@value #INNER_RADIUS} 格、刀尖在
 * {@code radius} 格（= 判定半径 7），张角 ±60°（和判定扇形一致），
 * 刀尖比刀根低 {@value #OUTER_DROP} 格 —— 一点点斜度而已，
 * 但平视时不会薄成一条线。整道刃在 0.7 秒里从一侧扫到另一侧，身后只留很短的拖尾。</p>
 *
 * <h3>⚠️ 别再做回"竖直柱面墙"</h3>
 * <p>第一版是把弧带做成绕人的竖直墙（高 2.2 格），结果是"一根竖条绕着玩家转"，
 * 看起来像一堵横向移动的墙，完全不像斩击。横斩的展开方向必须是<b>半径</b>，
 * 高度只用来兜住"平视别消失"这一条。</p>
 *
 * <h3>几何怎么来</h3>
 * <p>沿角度切成 {@value #SEGMENTS} 段四边形，内圈 / 外圈各一组顶点，
 * 直接在世界坐标算好再减相机坐标；{@code skill_arc.vsh} 只负责反算 (角度, 半径)。</p>
 */
@OnlyIn(Dist.CLIENT)
public class ArcEffect extends SkillEffect {

    private static final String SHADER_NAME = "skill_arc";

    /** 弧带切成多少段（段数越多越圆滑，顶点数是 4×段数）。 */
    private static final int SEGMENTS = 32;

    /** 单侧张角（度）：总张角 = 120°，和第一魂技的判定扇形一致。 */
    private static final float HALF_ANGLE_DEG = 60.0F;

    /** 刀根半径（格）：太小会在脚下糊成一团。 */
    private static final float INNER_RADIUS = 1.20F;

    /** 刃面基准高度（相对脚底，格）：齐腰最像"横斩"。 */
    private static final float PLANE_LIFT = 0.95F;

    /**
     * 外圈比内圈低多少（格）。
     *
     * <p>纯水平面在平视 / 背后视角会薄成一条线。让刀尖略微下沉 0.4 格
     * （7 格跨度上不到 3°），既不影响"横斩"的观感，又能在平视时留出可见面积。</p>
     */
    private static final float OUTER_DROP = 0.40F;

    /** 刃身 / 刃边配色（默认值的赋值在构造器里）。 */
    private final float[] coreColor;
    private final float[] edgeColor;

    private static final float FADE_IN_TICKS = 2.0F;
    private static final float FADE_OUT_TICKS = 9.0F;

    /** 默认（天使）配色：白金刃身 + 金边。 */
    public ArcEffect(int ownerEntityId, Vec3 origin, float radius,
                     long startTick, int durationTicks) {
        this(ownerEntityId, origin, radius, startTick, durationTicks, 0xFFF5C7, 0xDB9E1F);
    }

    /**
     * 自定义配色版本 —— 七杀剑的"红色剑波"就是靠这个复用同一套 fsh。
     *
     * @param coreRgb 刃身色 0xRRGGBB
     * @param edgeRgb 刃边色 0xRRGGBB
     */
    public ArcEffect(int ownerEntityId, Vec3 origin, float radius,
                     long startTick, int durationTicks, int coreRgb, int edgeRgb) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
        this.coreColor = rgb(coreRgb);
        this.edgeColor = rgb(edgeRgb);
    }

    private static float[] rgb(int rgb) {
        return new float[]{ ((rgb >> 16) & 0xFF) / 255.0F,
                            ((rgb >> 8) & 0xFF) / 255.0F,
                            (rgb & 0xFF) / 255.0F };
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(SHADER_NAME));
        if (shader == null) {
            return;
        }

        float elapsedSeconds = elapsedSeconds(gameTime, partialTick);
        float elapsedTicks = elapsedSeconds * 20.0F;
        float fade = Mth.clamp(elapsedTicks / FADE_IN_TICKS, 0.0F, 1.0F)
                * Mth.clamp((durationTicks() - elapsedTicks) / FADE_OUT_TICKS, 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        Vec3 center = anchor(partialTick);
        float radius = radius();

        // 水平朝向：优先用施法者当前 yaw（插值，否则会跟着 tick 抖）；拿不到就用 +Z
        Entity owner = ownerEntity();
        float yaw = owner != null ? Mth.rotLerp(partialTick, owner.yRotO, owner.getYRot()) : 0.0F;
        double yawRad = Math.toRadians(yaw);
        float dx = (float) -Math.sin(yawRad);
        float dz = (float) Math.cos(yawRad);
        // 右向量 = (-dz, dx)：与 (dx,dz) 正交，用来把角度摊开成弧
        float rx = -dz;
        float rz = dx;

        float cx = (float) (center.x - cameraPos.x);
        float cz = (float) (center.z - cameraPos.z);
        float baseY = (float) (center.y - cameraPos.y) + PLANE_LIFT;

        setUniform(shader, "Center", cx, cz);
        setUniform(shader, "Dir", dx, dz);
        setUniform(shader, "Radius", radius);
        setUniform(shader, "Inner", INNER_RADIUS);
        setUniform(shader, "HalfAngle", (float) Math.toRadians(HALF_ANGLE_DEG));
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.35F);
        setUniform(shader, "CoreColor", coreColor[0], coreColor[1], coreColor[2]);
        setUniform(shader, "EdgeColor", edgeColor[0], edgeColor[1], edgeColor[2]);

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float half = (float) Math.toRadians(HALF_ANGLE_DEG);
        float outerY = baseY - OUTER_DROP;

        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = -half + 2.0F * half * i / SEGMENTS;
            float a1 = -half + 2.0F * half * (i + 1) / SEGMENTS;

            // 内圈（刀根）与外圈（刀尖）各一组坐标
            float inX0 = cx + (dx * Mth.cos(a0) + rx * Mth.sin(a0)) * INNER_RADIUS;
            float inZ0 = cz + (dz * Mth.cos(a0) + rz * Mth.sin(a0)) * INNER_RADIUS;
            float inX1 = cx + (dx * Mth.cos(a1) + rx * Mth.sin(a1)) * INNER_RADIUS;
            float inZ1 = cz + (dz * Mth.cos(a1) + rz * Mth.sin(a1)) * INNER_RADIUS;

            float outX0 = cx + (dx * Mth.cos(a0) + rx * Mth.sin(a0)) * radius;
            float outZ0 = cz + (dz * Mth.cos(a0) + rz * Mth.sin(a0)) * radius;
            float outX1 = cx + (dx * Mth.cos(a1) + rx * Mth.sin(a1)) * radius;
            float outZ1 = cz + (dz * Mth.cos(a1) + rz * Mth.sin(a1)) * radius;

            buffer.vertex(inX0, baseY, inZ0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(outX0, outerY, outZ0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(outX1, outerY, outZ1).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            buffer.vertex(inX1, baseY, inZ1).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
