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
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * 通用「带状能量体」特效 —— 岩柱 / 巨柱 / 虚空裂隙 / 死神射线共用。
 *
 * <p>几何是一个沿 {@code Dir} 展开 {@code Length}、沿 {@code Side} 展开 ±{@code HalfWidth}
 * 的四边形，形状在 {@code skill_beam.fsh} 里逐像素算（见 {@link BeamPreset}）。</p>
 *
 * <h3>两个方向向量怎么来的</h3>
 * <ul>
 *   <li><b>Dir（带子的长度方向）</b>：竖直光柱恒为 {@code (0,1,0)}；
 *       裂隙 / 射线取施法者的视线（只在首帧解析一次，之后不再跟踪）。</li>
 *   <li><b>Side（带子的宽度方向）</b>：<b>每帧</b>取「同时垂直于 Dir 和相机视线」的向量，
 *       于是无论从哪个角度看，带子在屏幕上都始终有可见宽度 —— 不会退化成一条线。</li>
 * </ul>
 * <p>相机正对 Dir 看过去时这个叉积会退化，兜底换一个参考轴。</p>
 *
 * <p>⚠️ {@code radius} 参数（{@code S2CSkillFieldPacket} 自带）在这个特效里<b>被忽略</b> ——
 * 半宽和长度都来自 {@link BeamPreset}。每个技能注册自己的样式 id，改尺寸就改预设。</p>
 */
@OnlyIn(Dist.CLIENT)
public class BeamEffect extends SkillEffect {

    /** 默认着色器：光柱 / 通用能量带。 */
    public static final String SHADER_BEAM = "skill_beam";

    /** 虚空裂缝：梭形轮廓 + 锯齿撕口 + 近纯黑的内部，用普通 alpha 混合。 */
    public static final String SHADER_RIFT = "skill_rift";

    /** 光刃：月牙轮廓 + 锐利刃口 + 白热核心线。 */
    public static final String SHADER_BLADE = "skill_blade";

    /** 白热阶段（tick）：刚成形那几刻最亮。 */
    private static final float FLASH_TICKS = 3.0F;

    /** 余辉衰减时长（tick）。 */
    private static final float DECAY_TICKS = 20.0F;

    private final BeamPreset preset;

    /**
     * 用哪条 fragment shader。
     *
     * <p>裂缝 / 光刃 / 光柱的<b>几何与方向逻辑完全一样</b>（都是沿 Dir 展开的四边形），
     * 差别只在逐像素图案，所以共用一个 Java 类、只换 fsh —— 不必为每种图案各写一个类。</p>
     */
    private final String shaderName;

    private final float seed;

    /** 长度方向，首帧解析后缓存（裂隙沿视线方向，不该每帧跟着视角转）。 */
    @Nullable
    private Vec3 dir;

    public BeamEffect(int ownerEntityId, Vec3 origin, float radius,
                      long startTick, int durationTicks, BeamPreset preset) {
        this(ownerEntityId, origin, radius, startTick, durationTicks, preset, SHADER_BEAM);
    }

    public BeamEffect(int ownerEntityId, Vec3 origin, float radius,
                      long startTick, int durationTicks, BeamPreset preset, String shaderName) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
        this.preset = preset;
        this.shaderName = shaderName;
        this.seed = ownerEntityId * 0.61803F;
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(shaderName));
        if (shader == null) {
            return;
        }

        float elapsedSeconds = elapsedSeconds(gameTime, partialTick);
        float elapsedTicks = elapsedSeconds * 20.0F;

        float fade = Mth.clamp(elapsedTicks / FLASH_TICKS, 0.0F, 1.0F)
                * Mth.clamp((durationTicks() - elapsedTicks) / DECAY_TICKS, 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        Vec3 direction = resolveDirection();
        if (direction == null) {
            return;
        }
        Vec3 side = computeSide(direction);

        // ---- 起点：施法者位置 + 预设的高度偏移，转成相机相对坐标 ----
        Vec3 anchor = anchor();
        double sx = anchor.x - cameraPos.x;
        double sy = anchor.y - cameraPos.y + preset.startHeight();
        double sz = anchor.z - cameraPos.z;

        setUniform(shader, "Start", (float) sx, (float) sy, (float) sz);
        setUniform(shader, "Dir", (float) direction.x, (float) direction.y, (float) direction.z);
        setUniform(shader, "Side", (float) side.x, (float) side.y, (float) side.z);
        setUniform(shader, "Length", preset.length());
        setUniform(shader, "HalfWidth", preset.halfWidth());
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.0F);
        setUniform(shader, "Core", preset.hollow() ? -1.0F : 1.0F);
        setUniform(shader, "Jagged", preset.jagged());
        setUniform(shader, "Flow", preset.flow());
        setUniform(shader, "Seed", seed);
        setUniform(shader, "CoreColor", red(preset.coreColor()), green(preset.coreColor()), blue(preset.coreColor()));
        setUniform(shader, "EdgeColor", red(preset.edgeColor()), green(preset.edgeColor()), blue(preset.edgeColor()));

        // 加法混合做发光；深度测试保留，柱子会被地形正常遮挡
        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        // 四边形四个角：起点 ± side，终点 ± side
        double w = preset.halfWidth();
        double rx = side.x * w;
        double ry = side.y * w;
        double rz = side.z * w;
        double ex = sx + direction.x * preset.length();
        double ey = sy + direction.y * preset.length();
        double ez = sz + direction.z * preset.length();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(sx - rx, sy - ry, sz - rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(sx + rx, sy + ry, sz + rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(ex + rx, ey + ry, ez + rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(ex - rx, ey - ry, ez - rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /**
     * 解析长度方向。
     *
     * <p>竖直光柱恒为 +Y；裂隙 / 射线取施法者视线 —— <b>只在首帧解析并缓存</b>：
     * 裂隙是"朝着当时那个方向撕开"的，如果每帧跟着视角转，看起来就不是一道固定的裂口了。</p>
     *
     * @return 取不到方向（水平带子但施法者已经不在了）时返回 {@code null}
     */
    @Nullable
    private Vec3 resolveDirection() {
        if (dir != null) {
            return dir;
        }
        if (preset.vertical()) {
            dir = new Vec3(0.0D, 1.0D, 0.0D);
            return dir;
        }
        Entity owner = ownerEntity();
        if (owner == null) {
            return null;
        }
        Vec3 look = owner.getLookAngle();
        if (look.lengthSqr() < 1.0E-6D) {
            return null;
        }
        dir = look.normalize();
        return dir;
    }

    /**
     * 计算宽度方向：<b>每帧</b>取「同时垂直于 Dir 与相机视线」的向量，
     * 这样带子在屏幕上永远有可见宽度。
     *
     * <p>相机正好沿 Dir 看过去（比如顺着射线方向看）时叉积退化到零向量，
     * 依次兜底到「Dir × 世界 Y」和固定轴，避免出现 NaN 顶点。</p>
     */
    private static Vec3 computeSide(Vec3 direction) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f look = camera.getLookVector();
        Vec3 camLook = new Vec3(look.x(), look.y(), look.z());

        Vec3 side = direction.cross(camLook);
        if (side.lengthSqr() < 1.0E-6D) {
            side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (side.lengthSqr() < 1.0E-6D) {
                side = new Vec3(1.0D, 0.0D, 0.0D);
            }
        }
        return side.normalize();
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
    public String shaderId() {
        return shaderName;
    }
}
