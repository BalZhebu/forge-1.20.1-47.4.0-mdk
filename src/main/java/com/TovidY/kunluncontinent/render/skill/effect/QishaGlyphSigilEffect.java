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
 * 「七杀法阵」—— 七杀剑专用的地面法阵（第 4 / 6 / 8 / 9 魂技共用）。
 *
 * <h3>为什么不用 skill_sigil</h3>
 * <p>通用 sigil 着色器只能画等分边 + 星芒 + 爪痕，画不出<b>「七」字样</b>，
 * 也拼不出"六芒星 + 环上符文"这种具体符号。所以这里直接用
 * {@code position_color} 画几何：全部是贴地的四边形，不需要 fsh。</p>
 *
 * <h3>画了什么</h3>
 * <ul>
 *   <li>外圈双环 + 环上 12 枚符文刻度；</li>
 *   <li>中央星形：六芒 / 五芒 / 八芒 / 三角，四种形状；</li>
 *   <li>整层缓慢旋转 + 呼吸式明暗。</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class QishaGlyphSigilEffect extends SkillEffect {

    private static final float FADE_IN = 6.0F;
    private static final float FADE_OUT = 12.0F;

    /** 法阵中央的星形：每个技能换一种，避免"所有技能同一张图"。 */
    public enum Shape {
        /** 六芒星：两个交叠正三角。 */
        HEXAGRAM,
        /** 五芒星：一笔连成的五角星。 */
        PENTA,
        /** 八芒星：两个正方形交叠 45°。 */
        OCTA,
        /** 三角阵：一正一反两个三角 + 内圈。 */
        TRIANGLE
    }

    private final Shape shape;

    public QishaGlyphSigilEffect(int ownerEntityId, Vec3 origin, float radius,
                                 long startTick, int durationTicks) {
        this(ownerEntityId, origin, radius, startTick, durationTicks, Shape.HEXAGRAM);
    }

    public QishaGlyphSigilEffect(int ownerEntityId, Vec3 origin, float radius,
                                 long startTick, int durationTicks, Shape shape) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
        this.shape = shape;
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        float p = progress(gameTime, partialTick);
        float elapsed = p * durationTicks();
        float fade = Mth.clamp(elapsed / FADE_IN, 0.0F, 1.0F)
                * Mth.clamp((durationTicks() - elapsed) / FADE_OUT, 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        Vec3 center = anchor(partialTick);
        float cx = (float) (center.x - cameraPos.x);
        float cz = (float) (center.z - cameraPos.z);
        float y = (float) (center.y - cameraPos.y) + 0.06F;

        float r = radius();
        float rot = elapsed * 0.012F;
        float pulse = 0.75F + 0.25F * Mth.sin(elapsed * 0.35F);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // ① 外环（两道）
        ring(buffer, cx, cz, y, r, 0.10F + 0.06F * pulse, 96, rot, 1.0F, 0.22F, 0.28F, 0.85F * fade);
        ring(buffer, cx, cz, y, r * 0.92F, 0.05F, 96, -rot, 0.75F, 0.10F, 0.14F, 0.7F * fade);

        // ② 环上 12 枚符文刻度
        for (int i = 0; i < 12; i++) {
            float a = rot + i * (Mth.TWO_PI / 12.0F);
            float r0 = r * 0.86F;
            float r1 = r * 0.98F;
            line(buffer, cx, cz, y,
                    cx + Mth.cos(a) * r0, cz + Mth.sin(a) * r0,
                    cx + Mth.cos(a) * r1, cz + Mth.sin(a) * r1,
                    0.07F, 1.0F, 0.30F, 0.34F, 0.9F * fade);
        }

        // ③ 中央星形（按形状画，四种换着用）
        float starR = r * 0.72F;
        switch (shape) {
            case HEXAGRAM -> {
                for (int tri = 0; tri < 2; tri++) {
                    float baseA = -rot * 0.6F + tri * (float) Math.PI / 3.0F;
                    for (int i = 0; i < 3; i++) {
                        float a0 = baseA + i * Mth.TWO_PI / 3.0F;
                        float a1 = baseA + ((i + 1) % 3) * Mth.TWO_PI / 3.0F;
                        line(buffer, cx, cz, y,
                                cx + Mth.cos(a0) * starR, cz + Mth.sin(a0) * starR,
                                cx + Mth.cos(a1) * starR, cz + Mth.sin(a1) * starR,
                                0.10F, 1.0F, 0.18F, 0.22F, 0.95F * fade);
                    }
                }
            }
            case PENTA -> {
                for (int i = 0; i < 5; i++) {
                    float a0 = -rot * 0.6F + i * Mth.TWO_PI / 5.0F;
                    float a1 = -rot * 0.6F + (i + 2) * Mth.TWO_PI / 5.0F;
                    line(buffer, cx, cz, y,
                            cx + Mth.cos(a0) * starR, cz + Mth.sin(a0) * starR,
                            cx + Mth.cos(a1) * starR, cz + Mth.sin(a1) * starR,
                            0.11F, 1.0F, 0.20F, 0.24F, 0.95F * fade);
                }
            }
            case OCTA -> {
                for (int square = 0; square < 2; square++) {
                    float baseA = rot * 0.5F + square * (float) Math.PI / 4.0F;
                    for (int i = 0; i < 4; i++) {
                        float a0 = baseA + i * Mth.TWO_PI / 4.0F;
                        float a1 = baseA + ((i + 1) % 4) * Mth.TWO_PI / 4.0F;
                        line(buffer, cx, cz, y,
                                cx + Mth.cos(a0) * starR, cz + Mth.sin(a0) * starR,
                                cx + Mth.cos(a1) * starR, cz + Mth.sin(a1) * starR,
                                0.10F, 1.0F, 0.22F, 0.26F, 0.95F * fade);
                    }
                }
            }
            case TRIANGLE -> {
                for (int tri = 0; tri < 2; tri++) {
                    float rr = tri == 0 ? starR : starR * 0.62F;
                    float baseA = -rot * 0.7F + tri * (float) Math.PI / 3.0F;
                    for (int i = 0; i < 3; i++) {
                        float a0 = baseA + i * Mth.TWO_PI / 3.0F;
                        float a1 = baseA + ((i + 1) % 3) * Mth.TWO_PI / 3.0F;
                        line(buffer, cx, cz, y,
                                cx + Mth.cos(a0) * rr, cz + Mth.sin(a0) * rr,
                                cx + Mth.cos(a1) * rr, cz + Mth.sin(a1) * rr,
                                0.12F, 1.0F, 0.20F, 0.24F, 0.95F * fade);
                    }
                }
            }
        }

        // ④ 中心光晕（几层同心方块近似）
        for (int i = 3; i >= 1; i--) {
            float s = r * 0.075F * i;
            quad(buffer, cx - s, cz - s, cx + s, cz - s, cx + s, cz + s, cx - s, cz + s, y,
                    1.0F, 0.25F, 0.30F, 0.10F * fade);
        }

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /** 画一圈细环（用等分四边形拼）。 */
    private void ring(BufferBuilder b, float cx, float cz, float y, float radius,
                      float thickness, int segments, float rot,
                      float red, float green, float blue, float alpha) {
        for (int i = 0; i < segments; i++) {
            float a0 = rot + i * Mth.TWO_PI / segments;
            float a1 = rot + (i + 1) * Mth.TWO_PI / segments;
            float r0 = radius - thickness;
            float r1 = radius;
            float x0 = cx + Mth.cos(a0) * r0;
            float z0 = cz + Mth.sin(a0) * r0;
            float x1 = cx + Mth.cos(a0) * r1;
            float z1 = cz + Mth.sin(a0) * r1;
            float x2 = cx + Mth.cos(a1) * r1;
            float z2 = cz + Mth.sin(a1) * r1;
            float x3 = cx + Mth.cos(a1) * r0;
            float z3 = cz + Mth.sin(a1) * r0;
            b.vertex(x0, y, z0).color(red, green, blue, alpha).endVertex();
            b.vertex(x1, y, z1).color(red, green, blue, alpha).endVertex();
            b.vertex(x2, y, z2).color(red, green, blue, alpha).endVertex();
            b.vertex(x3, y, z3).color(red, green, blue, alpha).endVertex();
        }
    }

    /** 贴地粗线：两点之间铺一条宽 thickness 的四边形。 */
    private void line(BufferBuilder b, float cx, float cz, float y,
                      float x0, float z0, float x1, float z1,
                      float thickness, float red, float green, float blue, float alpha) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float len = Mth.sqrt(dx * dx + dz * dz);
        if (len < 1.0E-4F) {
            return;
        }
        float nx = -dz / len * thickness * 0.5F;
        float nz = dx / len * thickness * 0.5F;
        b.vertex(x0 - nx, y, z0 - nz).color(red, green, blue, alpha).endVertex();
        b.vertex(x1 - nx, y, z1 - nz).color(red, green, blue, alpha).endVertex();
        b.vertex(x1 + nx, y, z1 + nz).color(red, green, blue, alpha).endVertex();
        b.vertex(x0 + nx, y, z0 + nz).color(red, green, blue, alpha).endVertex();
    }

    private void quad(BufferBuilder b, float x0, float z0, float x1, float z1,
                      float x2, float z2, float x3, float z3, float y,
                      float red, float green, float blue, float alpha) {
        b.vertex(x0, y, z0).color(red, green, blue, alpha).endVertex();
        b.vertex(x1, y, z1).color(red, green, blue, alpha).endVertex();
        b.vertex(x2, y, z2).color(red, green, blue, alpha).endVertex();
        b.vertex(x3, y, z3).color(red, green, blue, alpha).endVertex();
    }
}
