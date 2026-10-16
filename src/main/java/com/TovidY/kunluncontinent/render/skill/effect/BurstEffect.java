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

/**
 * 「贴在生物身上」的屏幕对齐特效 —— 裂金虎 8/9 共用。
 *
 * <p>和前几套的区别在于<b>几何和锚点语义</b>：</p>
 * <ul>
 *   <li>{@code skill_sigil} 是铺在<u>地面</u>的水平面；{@code skill_bolt} 是竖直广告牌
 *       （沿世界 Y 展开，正俯视会退化成一条线）；</li>
 *   <li>这一套是<b>真正的屏幕对齐广告牌</b>：沿相机的 Right / Up 两个轴各展开 ±HalfSize，
 *       所以从任何角度看（包括正上方往下看）都是一个正对镜头的正方形；</li>
 *   <li>锚点放在<b>生物的身体中心</b>（脚底 + 身高的一半），并且跟随该生物移动，
 *       所以特效是"长在它身上"的，而不是"砸在地上"的。</li>
 * </ul>
 *
 * <h3>⚠️ 刻意不做 polygonOffset</h3>
 * <p>地面法阵要靠 {@code polygonOffset(-2,-2)} 把自己拉到地面之上避免 z-fighting；
 * 这一套<b>不能</b>这么做 —— 它的用途正是"在生物体内炸开"：深度测试保留，
 * 于是核心会被生物自己的模型挡住，只有撑出轮廓的那部分露出来，
 * 视觉上就是"光从它体内炸出来"。把深度偏移加上反而会变成一张贴在身上的贴图。</p>
 *
 * <h3>三个样式靠 {@link Style} 区分</h3>
 * <table>
 *   <tr><td>{@link #LIETIAN_BURST}</td><td>裂天：体内金色爆裂</td></tr>
 *   <tr><td>{@link #POJIE_VORTEX}</td><td>猛虎破界：目标身后的黑色旋涡</td></tr>
 *   <tr><td>{@link #POJIE_LIGHT}</td><td>猛虎破界：旋涡打出的金色光芒</td></tr>
 * </table>
 */
@OnlyIn(Dist.CLIENT)
public class BurstEffect extends SkillEffect {

    /** 体内爆裂（加法混合，金色）。 */
    public static final String SHADER_BURST = "skill_burst";

    /**
     * 黑色旋涡（<b>普通 alpha 混合</b>）。
     *
     * <p>必须是普通混合 —— 加法混合下黑色等于"什么都没画"（黑×α + 背景 = 背景），
     * 画不出"把背景吃掉一块"的旋涡。json 里的 {@code dstrgb} 是 {@code one_minus_srcalpha}。</p>
     */
    public static final String SHADER_VORTEX = "skill_vortex";

    /** 旋涡金光（加法混合，能亮到发白）。 */
    public static final String SHADER_VORTEX_LIGHT = "skill_vortex_light";

    /** 取不到施法者实体时，身体中心按这个身高估算（格）。 */
    private static final float FALLBACK_BODY_HEIGHT = 1.2F;

    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    /**
     * 一个样式的全部外观参数（对应上面三条 fsh + 一组颜色）。
     *
     * @param shader         用哪条 fragment shader
     * @param behind         {@code true} = 沿视线再往前推一点，永远落在目标<b>身后</b>
     * @param behindDistance 身后偏移距离（格）
     * @param coreColor      0xRRGGBB
     * @param edgeColor      0xRRGGBB
     */
    public record Style(String shader, boolean behind, float behindDistance,
                        int coreColor, int edgeColor) {
    }

    /** 裂天（裂金虎 8）：在被命中生物<b>体内</b>炸开的金色光芒。 */
    public static final Style LIETIAN_BURST =
            new Style(SHADER_BURST, false, 0.0F, 0xFFD75C, 0x853305);

    /** 猛虎破界（裂金虎 9）：目标身后的黑色旋涡。核心色是那圈金色亮边。 */
    public static final Style POJIE_VORTEX =
            new Style(SHADER_VORTEX, true, 0.90F, 0xFFD152, 0x05040A);

    /** 猛虎破界（裂金虎 9）：旋涡打出的金色光芒。 */
    public static final Style POJIE_LIGHT =
            new Style(SHADER_VORTEX_LIGHT, true, 0.90F, 0xFFE075, 0x993806);

    private final Style style;

    /** 让同一次技能打到的多个目标"形态"各不相同。 */
    private final float seed;

    public BurstEffect(int ownerEntityId, Vec3 origin, float radius,
                       long startTick, int durationTicks, Style style) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
        this.style = style;
        this.seed = ownerEntityId * 0.61803F;
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(style.shader()));
        if (shader == null) {
            return;
        }

        float elapsedSeconds = elapsedSeconds(gameTime, partialTick);
        float elapsedTicks = elapsedSeconds * 20.0F;

        // 生命包络：起手极快（"啪"那一下），回落相对慢一些。
        // 两个系数都按寿命比例取，所以同一条公式既能当 10 tick 的爆闪，
        // 也能当 40 tick 的旋涡。
        float attack = Math.max(1.0F, durationTicks() * 0.08F);
        float decay = Math.max(2.0F, durationTicks() * 0.28F);
        float fade = Mth.clamp(elapsedTicks / attack, 0.0F, 1.0F)
                * Mth.clamp((durationTicks() - elapsedTicks) / decay, 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        // 中心 = 生物身体中心。它死了 / 消失了就退回发包时的坐标（爆点留在原地）。
        Vec3 center = anchor().add(0.0D, bodyHeight() * 0.5D, 0.0D);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 look = new Vec3(camera.getLookVector());
        Vec3 right = look.cross(WORLD_UP);
        if (right.lengthSqr() < 1.0E-6D) {
            // 正俯视 / 正仰视时"水平右"没有唯一定义，兜底取 +X，避免 NaN 顶点
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 up = right.cross(look).normalize();

        if (style.behind()) {
            // 沿视线再往前推 = 离相机更远 = 从玩家视角永远落在目标身后
            center = center.add(look.scale(style.behindDistance()));
        }

        float size = Math.max(radius(), 0.2F);
        float cx = (float) (center.x - cameraPos.x);
        float cy = (float) (center.y - cameraPos.y);
        float cz = (float) (center.z - cameraPos.z);

        setUniform(shader, "Center", cx, cy, cz);
        setUniform(shader, "Right", (float) right.x, (float) right.y, (float) right.z);
        setUniform(shader, "Up", (float) up.x, (float) up.y, (float) up.z);
        setUniform(shader, "HalfSize", size);
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.0F);
        setUniform(shader, "Seed", seed);
        setUniform(shader, "CoreColor", red(style.coreColor()), green(style.coreColor()), blue(style.coreColor()));
        setUniform(shader, "EdgeColor", red(style.edgeColor()), green(style.edgeColor()), blue(style.edgeColor()));

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        double rx = right.x * size;
        double ry = right.y * size;
        double rz = right.z * size;
        double ux = up.x * size;
        double uy = up.y * size;
        double uz = up.z * size;

        buffer.vertex(cx - rx - ux, cy - ry - uy, cz - rz - uz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(cx + rx - ux, cy + ry - uy, cz + rz - uz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(cx + rx + ux, cy + ry + uy, cz + rz + uz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(cx - rx + ux, cy - ry + uy, cz - rz + uz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /** 目标身高（格）。跟随的实体没了就按 {@link #FALLBACK_BODY_HEIGHT} 估。 */
    private float bodyHeight() {
        Entity owner = ownerEntity();
        return owner != null ? owner.getBbHeight() : FALLBACK_BODY_HEIGHT;
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
        return SHADER_BURST;
    }
}
