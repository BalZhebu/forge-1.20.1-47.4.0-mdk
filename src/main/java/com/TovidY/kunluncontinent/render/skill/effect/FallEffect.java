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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 「天降巨物」—— 天使 9「终焉救赎」的砸落段。
 *
 * <h3>它做了什么</h3>
 * <p>一个 {@value #WIDTH} 格宽、{@value #HEIGHT} 格高的巨大发光体，从落点上方
 * {@value #DROP_HEIGHT} 格处加速砸下来，落地后迅速淡出。
 * 几何是<b>沿相机右向量展开的一块竖直四边形</b>（永远正对镜头），
 * 图案在 {@code skill_fall.fsh} 里画：实心主体 + 两侧轮廓 + 底部白热 + 顶部渐隐。</p>
 *
 * <h3>和伤害的关系</h3>
 * <p>伤害<b>不在</b>这里。技能释放时先发这个特效（时长 {@value #DURATION_TICKS} tick），
 * 同时由 {@code SkillWaveField} 延迟 {@code FALL_TICKS} tick 开始第一圈冲击波 ——
 * 巨物"落地"与光波起步同一刻，所以看到的是「砸下来 → 炸开 → 扫过的掉血」。</p>
 */
@OnlyIn(Dist.CLIENT)
public class FallEffect extends SkillEffect {

    private static final String SHADER_NAME = "skill_fall";

    /** 半宽（格）：总宽 9 格，够"巨大"。 */
    private static final float WIDTH = 4.5F;
    /** 发光体自身的长度（格）。 */
    private static final float HEIGHT = 24.0F;
    /** 起始时底部高出落点多少格。 */
    private static final float DROP_HEIGHT = 34.0F;
    /** 整个下落过程占总时长的比例。 */
    private static final float FALL_PORTION = 0.60F;

    /** 特效总时长（tick）：和 {@code SkillTianshi9} 发的一致。 */
    public static final int DURATION_TICKS = 20;

    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    public FallEffect(int ownerEntityId, Vec3 origin, float radius,
                      long startTick, int durationTicks) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(SHADER_NAME));
        if (shader == null) {
            return;
        }

        float p = progress(gameTime, partialTick);
        // 落地后快速淡出（0.6 → 1.0 的区间）
        float fade = p <= FALL_PORTION
                ? 1.0F
                : Mth.clamp((1.0F - p) / (1.0F - FALL_PORTION), 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        Vec3 center = anchor(partialTick);

        // 加速下落：t² 让"越接近地面越快"，比匀速有重量感
        float t = Mth.clamp(p / FALL_PORTION, 0.0F, 1.0F);
        float drop = DROP_HEIGHT * (1.0F - t * t);

        // 相机右向量（水平化）—— 竖直广告牌的宽度方向
        Vec3 look = new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector());
        Vec3 side = look.cross(WORLD_UP);
        if (side.lengthSqr() < 1.0E-6D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();

        float bx = (float) (center.x - cameraPos.x);
        float by = (float) (center.y - cameraPos.y) + drop;
        float bz = (float) (center.z - cameraPos.z);

        setUniform(shader, "Start", bx, by, bz);
        setUniform(shader, "Dir", 0.0F, 1.0F, 0.0F);
        setUniform(shader, "Side", (float) side.x, (float) side.y, (float) side.z);
        setUniform(shader, "Length", HEIGHT);
        setUniform(shader, "HalfWidth", WIDTH);
        setUniform(shader, "Time", elapsedSeconds(gameTime, partialTick));
        setUniform(shader, "Age", p);
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.55F);
        setUniform(shader, "CoreColor", 1.00F, 0.96F, 0.78F);
        setUniform(shader, "EdgeColor", 0.92F, 0.72F, 0.22F);

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float sx = (float) side.x * WIDTH;
        float sz = (float) side.z * WIDTH;
        float top = by + HEIGHT;

        buffer.vertex(bx - sx, by, bz - sz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(bx + sx, by, bz + sz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(bx + sx, top, bz + sz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(bx - sx, top, bz - sz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
