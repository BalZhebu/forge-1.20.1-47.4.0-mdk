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
 * 扩散光波 —— 不周倾落地时从中心向外推开的冲击环。
 *
 * <h3>为什么几何是静态的</h3>
 * <p>四边形从第一帧到最后一帧<b>尺寸不变</b>（边长 = {@code 2 × 最大半径}），
 * 归一化也恒用最大半径，所以 {@code localPos} 始终落在 {@code [-1, 1]}。
 * 真正在动的是 {@code ShockRadius} 这个 uniform —— 光波前沿在归一化空间里的位置。
 * 每帧只改一个 uniform、不用重建顶点。</p>
 *
 * <h3>它的意义不只是好看</h3>
 * <p>光波的推进度是<b>和服务端的伤害扩散用同一个速度</b>的（见
 * {@code PWPlayerTickEvent#handleLieDiLanding}），所以「光波扫到哪里、哪里才受伤」——
 * 不会出现"光波还没到，远处的怪已经掉血了"这种违和感。</p>
 */
@OnlyIn(Dist.CLIENT)
public class ShockwaveEffect extends SkillEffect {

    private static final String SHADER_NAME = "skill_shock";

    /** 扩散阶段时长（tick）。这段时间里 ShockRadius 由 0 推到 1。 */
    public static final float EXPAND_TICKS = 40.0F;

    /** 扩散到位后的淡出时长（tick）。 */
    private static final float FADE_OUT_TICKS = 18.0F;

    /** 环上的等分刻度数。 */
    private static final float TICK_COUNT = 48.0F;

    /** 贴地高度（格）。 */
    private static final float GROUND_LIFT = 0.06F;

    /** 核心色（金）。 */
    private static final float CORE_R = 1.00F;
    private static final float CORE_G = 0.86F;
    private static final float CORE_B = 0.42F;

    /** 外缘色（暗褐金）。 */
    private static final float EDGE_R = 0.42F;
    private static final float EDGE_G = 0.24F;
    private static final float EDGE_B = 0.06F;

    public ShockwaveEffect(int ownerEntityId, Vec3 origin, float radius,
                           long startTick, int durationTicks) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(SHADER_NAME));
        if (shader == null) {
            return;
        }

        float elapsedSeconds = elapsedSeconds(gameTime, partialTick);
        float elapsedTicks = elapsedSeconds * 20.0F;

        // 推进度：0 → 1 走完 EXPAND_TICKS
        float expand = Mth.clamp(elapsedTicks / EXPAND_TICKS, 0.0F, 1.0F);

        // 包络：扩散期间保持全亮，到位后才开始淡出
        float fade = elapsedTicks <= EXPAND_TICKS
                ? Mth.clamp(elapsedTicks / 4.0F, 0.0F, 1.0F)
                : Mth.clamp((durationTicks() - elapsedTicks) / FADE_OUT_TICKS, 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        Vec3 center = anchor();
        float cx = (float) (center.x - cameraPos.x);
        float cy = (float) (center.y - cameraPos.y) + GROUND_LIFT;
        float cz = (float) (center.z - cameraPos.z);
        float maxRadius = radius();

        setUniform(shader, "Center", cx, cz);
        // Radius 是"归一化基准"（最大半径），ShockRadius 才是动态的前沿位置
        setUniform(shader, "Radius", maxRadius);
        setUniform(shader, "ShockRadius", expand);
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.0F);
        setUniform(shader, "TickCount", TICK_COUNT);
        setUniform(shader, "CoreColor", CORE_R, CORE_G, CORE_B);
        setUniform(shader, "EdgeColor", EDGE_R, EDGE_G, EDGE_B);

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.polygonOffset(-2.0F, -2.0F);
        RenderSystem.enablePolygonOffset();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float x0 = cx - maxRadius;
        float x1 = cx + maxRadius;
        float z0 = cz - maxRadius;
        float z1 = cz + maxRadius;
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

    /** 便于外部拿去做日志/调试。 */
    @Nullable
    public static String shaderId() {
        return SHADER_NAME;
    }
}
