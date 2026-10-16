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
 * 通用「地面符文阵」特效 —— 磐石巨猿 / 裂金虎 系列的第 8、9 魂技共用。
 *
 * <p>几何只有一个水平四边形（边长 {@code 2 × radius}），边缘以外的像素在
 * {@code skill_sigil.fsh} 里 {@code discard}。图案（外圈等分刻度、星芒、内圈法阵、
 * 爪痕、外推的冲击环）全部逐像素算出来，用 {@link SigilPreset} 的一组参数区分风格。</p>
 *
 * <p>锚点默认跟随施法者（{@code ownerEntityId}），所以走着放技能时阵法跟着人走 ——
 * 这和服务端的判定范围口径一致。</p>
 */
@OnlyIn(Dist.CLIENT)
public class SigilEffect extends SkillEffect {

    private static final String SHADER_NAME = "skill_sigil";

    /** 冲击环从中心推到边缘一轮的时间（秒）。 */
    private static final float PULSE_PERIOD_SECONDS = 1.5F;

    /** 淡入时长（tick）。 */
    private static final float FADE_IN_TICKS = 10.0F;

    /** 淡出时长（tick）。 */
    private static final float FADE_OUT_TICKS = 25.0F;

    /** 贴地高度（格）。抬高一点避免与地面 z-fighting。 */
    private static final float GROUND_LIFT = 0.05F;

    private final SigilPreset preset;

    public SigilEffect(int ownerEntityId, Vec3 origin, float radius,
                       long startTick, int durationTicks, SigilPreset preset) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
        this.preset = preset;
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        // 着色器不可用时直接不画 —— 这条分支下服务端没发粒子，
        // 玩家的回退办法是打开「技能特效优化」开关。
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

        float pulse = (elapsedSeconds % PULSE_PERIOD_SECONDS) / PULSE_PERIOD_SECONDS;

        Vec3 center = anchor();
        float cx = (float) (center.x - cameraPos.x);
        float cy = (float) (center.y - cameraPos.y) + GROUND_LIFT;
        float cz = (float) (center.z - cameraPos.z);
        float radius = radius();

        setUniform(shader, "Center", cx, cz);
        setUniform(shader, "Radius", radius);
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.0F);
        setUniform(shader, "Pulse", pulse);

        // ---- 外观参数（见 SigilPreset）----
        setUniform(shader, "PolySides", preset.polySides());
        setUniform(shader, "PolyRadius", preset.polyRadius());
        setUniform(shader, "StarPoints", preset.starPoints());
        setUniform(shader, "StarInner", preset.starInner());
        setUniform(shader, "StarOuter", preset.starOuter());
        setUniform(shader, "RuneRadius", preset.runeRadius());
        setUniform(shader, "SlashCount", (float) preset.slashCount());
        setUniform(shader, "SlashAngle", preset.slashAngle());
        setUniform(shader, "ShockCount", (float) preset.shockCount());
        setUniform(shader, "CoreColor", red(preset.coreColor()), green(preset.coreColor()), blue(preset.coreColor()));
        setUniform(shader, "EdgeColor", red(preset.edgeColor()), green(preset.edgeColor()), blue(preset.edgeColor()));

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

        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static float red(int rgb) {
        return ((rgb >> 16) & 0xFF) / 255.0F;
    }

    private static float green(int rgb) {
        return ((rgb >> 8) & 0xFF) / 255.0F;
    }

    private static float blue(int rgb) {
        return (rgb & 0xFF) / 255.0F;
    }

    /** 便于外部拿去做日志/调试。 */
    @Nullable
    public static String shaderId() {
        return SHADER_NAME;
    }
}
