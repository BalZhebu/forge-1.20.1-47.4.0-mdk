package com.TovidY.kunluncontinent.render.skill.effect;

import com.TovidY.kunluncontinent.render.skill.SkillEffect;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 「暗红剑芒天降」—— 七杀剑第 5 / 8 / 9 魂技共用的下坠剑影。
 *
 * <p>不用 {@code skill_fall}：那个是天使 9 的巨物，颜色写死、形状是"一整根柱"。
 * 这里用 {@code position_color} 直接画一把<b>有剑尖的剑</b>：两个十字交叉的竖直面
 * （正面 + 侧面各一片），保证从任何角度看都是立体的一把剑，而不是一张纸片。</p>
 *
 * <p>下落占前 {@value #FALL_PORTION} 的时长，落地后原地淡出并留一圈冲击环。</p>
 */
@OnlyIn(Dist.CLIENT)
public class QishaSwordFallEffect extends SkillEffect {

    /** 剑身宽（格）。 */
    private static final float WIDTH = 1.1F;
    /** 剑长（格）。 */
    private static final float LENGTH = 14.0F;
    /** 起始高度（格，相对落点）。 */
    private static final float START_HEIGHT = 26.0F;
    private static final float FALL_PORTION = 0.62F;

    public QishaSwordFallEffect(int ownerEntityId, Vec3 origin, float radius,
                                long startTick, int durationTicks) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        float p = progress(gameTime, partialTick);
        float fall = Mth.clamp(p / FALL_PORTION, 0.0F, 1.0F);
        // 先加速后匀速：t^1.6 更像"砸下来"
        float dropped = (float) Math.pow(fall, 1.6F);
        float bottom = START_HEIGHT * (1.0F - dropped);

        float fadeIn = Mth.clamp(p / 0.08F, 0.0F, 1.0F);
        float fadeOut = p <= FALL_PORTION ? 1.0F
                : Mth.clamp((1.0F - p) / (1.0F - FALL_PORTION), 0.0F, 1.0F);
        float alpha = fadeIn * fadeOut;
        if (alpha <= 0.002F) {
            return;
        }

        Vec3 center = anchor(partialTick);
        float cx = (float) (center.x - cameraPos.x);
        float cz = (float) (center.z - cameraPos.z);
        float baseY = (float) (center.y - cameraPos.y) + bottom;
        float topY = baseY + LENGTH;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // 外层光晕（宽一点、淡）
        blade(buffer, cx, cz, baseY, topY, WIDTH * 2.4F, 1.0F, 0.18F, 0.22F, 0.35F * alpha);
        // 正面剑身
        blade(buffer, cx, cz, baseY, topY, WIDTH, 1.0F, 0.35F, 0.40F, 0.95F * alpha);
        // 侧面剑身（转 90°）
        bladeSide(buffer, cx, cz, baseY, topY, WIDTH, 1.0F, 0.35F, 0.40F, 0.95F * alpha);
        // 剑尖
        tip(buffer, cx, cz, baseY, WIDTH * 1.6F, 1.0F, 0.55F, 0.60F, alpha);

        // 落地后：地面冲击环
        if (p > FALL_PORTION) {
            float k = (p - FALL_PORTION) / (1.0F - FALL_PORTION);
            float ringRadius = 1.0F + radius() * k;
            ring(buffer, cx, cz, (float) (center.y - cameraPos.y) + 0.1F,
                    ringRadius, 0.35F, 48, 1.0F, 0.30F, 0.36F, (1.0F - k) * 0.85F);
        }

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /** 沿 Z 轴展开的竖直剑身（正对南方视角）。 */
    private void blade(BufferBuilder b, float cx, float cz, float bottom, float top,
                       float width, float r, float g, float bl, float a) {
        float h = width * 0.5F;
        b.vertex(cx - h, bottom, cz).color(r, g, bl, a).endVertex();
        b.vertex(cx + h, bottom, cz).color(r, g, bl, a).endVertex();
        b.vertex(cx + h * 0.6F, top, cz).color(r, g, bl, a * 0.7F).endVertex();
        b.vertex(cx - h * 0.6F, top, cz).color(r, g, bl, a * 0.7F).endVertex();
    }

    /** 沿 X 轴展开的竖直剑身（侧面，保证 3D 感）。 */
    private void bladeSide(BufferBuilder b, float cx, float cz, float bottom, float top,
                           float width, float r, float g, float bl, float a) {
        float h = width * 0.5F;
        b.vertex(cx, bottom, cz - h).color(r, g, bl, a).endVertex();
        b.vertex(cx, bottom, cz + h).color(r, g, bl, a).endVertex();
        b.vertex(cx, top, cz + h * 0.6F).color(r, g, bl, a * 0.7F).endVertex();
        b.vertex(cx, top, cz - h * 0.6F).color(r, g, bl, a * 0.7F).endVertex();
    }

    /** 剑尖：底边到一点的小三角（用退化四边形近似）。 */
    private void tip(BufferBuilder b, float cx, float cz, float bottom, float halfWidth,
                     float r, float g, float bl, float a) {
        float y = bottom - 0.9F;
        b.vertex(cx - halfWidth, bottom, cz).color(r, g, bl, a).endVertex();
        b.vertex(cx + halfWidth, bottom, cz).color(r, g, bl, a).endVertex();
        b.vertex(cx, y, cz).color(r, g, bl, a).endVertex();
        b.vertex(cx, y, cz).color(r, g, bl, a).endVertex();
    }

    private void ring(BufferBuilder b, float cx, float cz, float y,
                      float radius, float thickness, int segments,
                      float r, float g, float bl, float a) {
        for (int i = 0; i < segments; i++) {
            float a0 = i * Mth.TWO_PI / segments;
            float a1 = (i + 1) * Mth.TWO_PI / segments;
            float r0 = radius - thickness;
            float x0 = cx + Mth.cos(a0) * r0;
            float z0 = cz + Mth.sin(a0) * r0;
            float x1 = cx + Mth.cos(a0) * radius;
            float z1 = cz + Mth.sin(a0) * radius;
            float x2 = cx + Mth.cos(a1) * radius;
            float z2 = cz + Mth.sin(a1) * radius;
            float x3 = cx + Mth.cos(a1) * r0;
            float z3 = cz + Mth.sin(a1) * r0;
            b.vertex(x0, y, z0).color(r, g, bl, a).endVertex();
            b.vertex(x1, y, z1).color(r, g, bl, a).endVertex();
            b.vertex(x2, y, z2).color(r, g, bl, a).endVertex();
            b.vertex(x3, y, z3).color(r, g, bl, a).endVertex();
        }
    }
}
