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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * 八荒寂灭 · <b>寂灭地阵</b>。
 *
 * <h3>技能本身</h3>
 * <p>半径 20 格的地阵，持续 10 轮脉冲、每 30 tick（1.5 秒）一轮，每轮对范围内所有敌人
 * 造成伤害并附加缓慢。地阵在服务端是<b>跟着施法者走</b>的（每轮取一次 {@code player.position()}）。</p>
 *
 * <h3>为什么改用着色器</h3>
 * <p>原来用粒子堆出「二十四边形 + 内环 + 光柱」，问题有三个：
 * 形状精度受粒子密度限制（边线是断的）、每轮要打几千个粒子、放大看就是一串点。
 * 换成逐像素算的程序化图形后，同样的形状只需要<b>一个面</b>，而且是矢量级的锐利。</p>
 *
 * <h3>几何</h3>
 * <p>只画一个边长 {@code 2 × radius} 的正方形，圆形以外的像素在 {@code .fsh} 里
 * {@code discard} 掉。一个面画完整个地阵 —— 对比原来每轮 ~2600 个粒子的开销。</p>
 */
@OnlyIn(Dist.CLIENT)
public class Bahuang9FieldEffect extends SkillEffect {

    private static final String SHADER_NAME = "skill_field";

    /** 一次脉冲的间隔（秒）。与服务端 {@code handleBahuangNine} 的 30 tick 对齐。 */
    private static final float PULSE_PERIOD_SECONDS = 1.5F;

    /** 淡入时长（tick）。 */
    private static final float FADE_IN_TICKS = 15.0F;

    /** 淡出时长（tick）。 */
    private static final float FADE_OUT_TICKS = 45.0F;

    /** 贴地高度（格）。抬高一点点避免和地面 z-fighting，同时看起来像"铺在地上"。 */
    private static final float GROUND_LIFT = 0.04F;

    /** 中心色（猩红）。 */
    private static final float CORE_R = 0.58F;
    private static final float CORE_G = 0.06F;
    private static final float CORE_B = 0.10F;

    /** 边缘色（幽紫）。 */
    private static final float EDGE_R = 0.14F;
    private static final float EDGE_G = 0.03F;
    private static final float EDGE_B = 0.30F;

    public Bahuang9FieldEffect(int ownerEntityId, Vec3 origin, float radius,
                               long startTick, int durationTicks) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        // 着色器不可用（编译失败 / 资源缺失）时直接不画 —— 技能原本的粒子表现还在，
        // 不会让玩家看到"技能凭空没效果"
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

        // 脉冲相位：0 → 1 刚好在服务端结算伤害的那一刻推到边缘
        float pulse = (elapsedSeconds % PULSE_PERIOD_SECONDS) / PULSE_PERIOD_SECONDS;
        // 蓄力感：越靠近结算越亮
        float intensity = 0.85F + 0.45F * pulse;

        Vec3 center = anchor();
        float cx = (float) (center.x - cameraPos.x);
        float cy = (float) (center.y - cameraPos.y) + GROUND_LIFT;
        float cz = (float) (center.z - cameraPos.z);
        float radius = radius();

        // ---- 写 uniform（用 getUniform 而不是 safeGetUniform：后者断言的是"游戏线程"，
        //      而渲染线程与主线程不是同一个；getUniform 断言渲染线程，正好对得上）----
        setUniform(shader, "Center", cx, cz);
        setUniform(shader, "Radius", radius);
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Pulse", pulse);
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", intensity);
        setUniform(shader, "CoreColor", CORE_R, CORE_G, CORE_B);
        setUniform(shader, "EdgeColor", EDGE_R, EDGE_G, EDGE_B);

        // ---- 渲染状态：加法混合做出"发光"，深度测试保留（会被地形正常遮挡）----
        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.polygonOffset(-2.0F, -2.0F);
        RenderSystem.enablePolygonOffset();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float x0 = cx - radius;
        float x1 = cx + radius;
        float z0 = cz - radius;
        float z1 = cz + radius;
        buffer.vertex(x0, cy, z0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(x0, cy, z1).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(x1, cy, z1).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(x1, cy, z0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        // ---- 还原状态，别影响后面的渲染 ----
        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** 便于外部拿去做日志/调试。 */
    @Nullable
    public static String shaderId() {
        return SHADER_NAME;
    }
}
