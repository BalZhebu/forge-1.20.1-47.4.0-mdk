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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * 八荒戟 · 第八魂技「降雷」—— 从天而降的雷柱。
 *
 * <h3>技能本身</h3>
 * <p>吟唱 1 秒后，对自身半径 50 格内的<b>所有</b>敌人各劈一道雷，无视高度。
 * 服务端是<b>每个目标一份</b>特效，所以这里是「一道雷」的实现，
 * 由 {@code SkillBahuang8} 对每个目标各发一次包。</p>
 *
 * <h3>为什么改用着色器</h3>
 * <p>原来每道雷要打约 60 个粒子（22 段主雷 + 3 条 16 段分叉 + 落点环），
 * 目标一多（50 格内十几只怪）就是近千个粒子；而且粒子画的雷线是断的、
 * 段与段之间不连贯。换成着色器后每个目标只有<b>一个四边形</b>，
 * 雷线是逐像素算出来的连续折线。</p>
 *
 * <h3>几何：广告牌</h3>
 * <p>底边中心在目标脚下，沿<b>相机右向量</b>展开 ±{@code radius}，沿世界 Y 抬高
 * {@value #BOLT_HEIGHT} 格。这样无论从哪个角度看都正对镜头，不会出现"从侧面看是一条线"。
 * 顶点仍然是「世界坐标 - 相机坐标」，{@code (u, v)} 在顶点着色器里反算
 * （见 {@code skill_bolt.vsh}）。</p>
 */
@OnlyIn(Dist.CLIENT)
public class Bahuang8ThunderEffect extends SkillEffect {

    private static final String SHADER_NAME = "skill_bolt";

    /**
     * 雷柱高度（格）。与粒子版 {@code head = tp.add(0, 20.0, 0)} 保持一致，
     * 也就是「雷云在目标头顶 20 格处」。
     */
    public static final float BOLT_HEIGHT = 20.0F;

    /** 白热阶段（tick）：刚劈下来的头几刻是最亮的。 */
    private static final float FLASH_TICKS = 3.0F;

    /** 余辉衰减时长（tick）。 */
    private static final float DECAY_TICKS = 18.0F;

    /** 核心色（白热偏青）。 */
    private static final float CORE_R = 0.62F;
    private static final float CORE_G = 0.84F;
    private static final float CORE_B = 1.00F;

    /** 边缘色（幽蓝紫）。 */
    private static final float EDGE_R = 0.20F;
    private static final float EDGE_G = 0.26F;
    private static final float EDGE_B = 0.88F;

    /** 每道雷的形状种子，让攻击多目标时各道雷"形态"不同。 */
    private final float seed;

    public Bahuang8ThunderEffect(int ownerEntityId, Vec3 origin, float radius,
                                 long startTick, int durationTicks) {
        super(ownerEntityId, origin, radius, startTick, durationTicks);
        this.seed = ownerEntityId * 0.61803F;
    }

    @Override
    public void render(Vec3 cameraPos, long gameTime, float partialTick) {
        // 着色器不可用（编译失败 / 被光影包顶掉）时不画 —— 这条分支下服务端没发粒子，
        // 所以会真的看不到东西。玩家的回退办法是打开「技能特效优化」开关。
        ShaderInstance shader = SkillShaders.get(SkillEffectRenderer.shader(SHADER_NAME));
        if (shader == null) {
            return;
        }

        float elapsedSeconds = elapsedSeconds(gameTime, partialTick);
        float elapsedTicks = elapsedSeconds * 20.0F;

        // 生命包络：前 3 tick 全亮（"啪"那一下），之后 18 tick 内衰减到无
        float fade = Mth.clamp(elapsedTicks / FLASH_TICKS, 0.0F, 1.0F)
                * Mth.clamp((durationTicks() - elapsedTicks) / DECAY_TICKS, 0.0F, 1.0F);
        if (fade <= 0.002F) {
            return;
        }

        float halfWidth = Math.max(radius(), 0.2F);
        Vec3 base = anchor();
        float bx = (float) (base.x - cameraPos.x);
        float by = (float) (base.y - cameraPos.y);
        float bz = (float) (base.z - cameraPos.z);
        Vec3 right = cameraRight();

        setUniform(shader, "Base", bx, by, bz);
        setUniform(shader, "Right", (float) right.x, (float) right.y, (float) right.z);
        setUniform(shader, "HalfWidth", halfWidth);
        setUniform(shader, "Height", BOLT_HEIGHT);
        setUniform(shader, "Time", elapsedSeconds);
        setUniform(shader, "Age", progress(gameTime, partialTick));
        setUniform(shader, "Fade", fade);
        setUniform(shader, "Intensity", 1.0F);
        setUniform(shader, "Seed", seed);
        setUniform(shader, "CoreColor", CORE_R, CORE_G, CORE_B);
        setUniform(shader, "EdgeColor", EDGE_R, EDGE_G, EDGE_B);

        // 加法混合做出"发光"。深度测试保留 —— 雷被地形挡住是合理的
        // （在天花板下面看不到屋顶上的雷），比穿墙发亮要自然。
        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        double rx = right.x * halfWidth;
        double rz = right.z * halfWidth;
        double top = by + BOLT_HEIGHT;
        // 广告牌：底边沿 Right 展开，左右两侧沿世界 Y 抬到 top
        buffer.vertex(bx - rx, by, bz - rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(bx + rx, by, bz + rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(bx + rx, top, bz + rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(bx - rx, top, bz - rz).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    /**
     * 相机的水平右向量（世界空间）。
     *
     * <p>广告牌靠它展开。注意必须是<b>世界空间</b>的向量 —— 顶点写的是
     * 「世界坐标 - 相机坐标」，轴仍然与世界对齐，只是原点挪到了相机。</p>
     *
     * <p>相机正俯视 / 正仰视时 {@code look × up} 会退化到零向量（垂直向下看时
     * "水平右"本来就没有唯一定义），这时兜底取世界 +X，避免出现 NaN 顶点。</p>
     */
    private static Vec3 cameraRight() {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f look = camera.getLookVector();
        Vec3 right = new Vec3(look.x(), look.y(), look.z()).cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            return new Vec3(1.0D, 0.0D, 0.0D);
        }
        return right.normalize();
    }

    /** 便于外部拿去做日志/调试。 */
    @Nullable
    public static String shaderId() {
        return SHADER_NAME;
    }
}
