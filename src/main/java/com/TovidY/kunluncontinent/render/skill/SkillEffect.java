package com.TovidY.kunluncontinent.render.skill;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * 一个「技能场域特效」：锚定在世界的某个点上，有一致的起止时间，逐帧绘制。
 *
 * <p>继承它就能接进 {@link SkillEffectRenderer}，不用管注册、生命周期、渲染时机。
 * 具体长什么样（画几个面、写哪些 uniform）由子类决定。</p>
 *
 * <h3>生命周期</h3>
 * <p>特效由服务端发包创建（见 {@code S2CSkillFieldPacket}），客户端按
 * {@code startTick + durationTicks} 自己判断过期，到点自动丢弃 —— 不需要服务端再发"结束"包。</p>
 *
 * <h3>锚点跟随</h3>
 * <p>默认锚点跟着 {@code ownerEntityId} 那个实体走。这一点很重要：本项目的技能场
 * （比如八荒寂灭的地阵）在服务端是每轮脉冲取一次 {@code player.position()}，
 * 也就是<b>跟着施法者移动的</b>；客户端如果固定在发包那一刻的坐标，走着放技能时
 * 阵法会和判定范围脱节。</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class SkillEffect {

    /** 施展者实体 id；{@code -1} 表示不跟随任何实体。 */
    private final int ownerEntityId;

    /** 锚点兜底坐标（施法者已经不在了 / 没有施法者时用）。 */
    private final Vec3 fallbackOrigin;

    /** 场域半径（格）。 */
    private final float radius;

    private final int durationTicks;
    private final long startTick;

    protected SkillEffect(int ownerEntityId, Vec3 fallbackOrigin, float radius,
                          long startTick, int durationTicks) {
        this.ownerEntityId = ownerEntityId;
        this.fallbackOrigin = fallbackOrigin;
        this.radius = radius;
        this.startTick = startTick;
        this.durationTicks = Math.max(1, durationTicks);
    }

    public float radius() {
        return radius;
    }

    public int durationTicks() {
        return durationTicks;
    }

    /** 是否已经放完。由渲染器每帧调用，返回 true 就会被丢弃。 */
    public boolean isFinished(long gameTime) {
        return gameTime - startTick > durationTicks;
    }

    /** 从开始到现在经过的秒数（含插值），驱动着色器里的自转等。 */
    protected float elapsedSeconds(long gameTime, float partialTick) {
        return (gameTime - startTick + partialTick) / 20.0F;
    }

    /** 整体推进度 {@code 0 → 1}。 */
    protected float progress(long gameTime, float partialTick) {
        return Mth.clamp((gameTime - startTick + partialTick) / (float) durationTicks, 0.0F, 1.0F);
    }

    /**
     * 施法者实体。
     *
     * @return 没有 owner、实体已消失、或不在当前客户端世界里时返回 {@code null}
     */
    @Nullable
    protected Entity ownerEntity() {
        if (ownerEntityId < 0) {
            return null;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity owner = level.getEntity(ownerEntityId);
        return owner != null && owner.isAlive() ? owner : null;
    }

    /** 当前锚点：优先跟随施法者，取不到就退回发包时的坐标。 */
    protected Vec3 anchor() {
        Entity owner = ownerEntity();
        return owner != null ? owner.position() : fallbackOrigin;
    }

    /**
     * 按名字设一个 float / vec2 / vec3 / vec4 uniform；uniform 没声明就静默跳过。
     *
     * <p>⚠️ 两个坑：</p>
     * <ul>
     *   <li>必须用 {@code getUniform} 而不是 {@code safeGetUniform} ——
     *       前者断言<b>渲染</b>线程（调用点就在渲染线程），后者断言<b>游戏</b>线程，会直接炸；</li>
     *   <li>自定义 uniform 一定要在 {@code shaders/core/*.json} 的 {@code uniforms} 数组里声明，
     *       否则这里永远拿到 null，表现为"参数改了没反应"。</li>
     * </ul>
     */
    protected static void setUniform(ShaderInstance shader, String name, float... values) {
        Uniform uniform = shader.getUniform(name);
        if (uniform == null) {
            return;
        }
        switch (values.length) {
            case 1 -> uniform.set(values[0]);
            case 2 -> uniform.set(values[0], values[1]);
            case 3 -> uniform.set(values[0], values[1], values[2]);
            default -> uniform.set(values);
        }
    }

    /**
     * 绘制一帧。
     *
     * @param cameraPos   相机世界坐标。顶点必须写成「世界坐标 - cameraPos」。
     *                    {@code ModelViewMat} 由 {@link SkillEffectRenderer} 在渲染前
     *                    统一设成相机视图矩阵（只含旋转，不含平移），两者正好配套 ——
     *                    视图空间坐标 = {@code R⁻¹ · (world - cameraPos)}。
     *                    <b>不要</b>再乘 poseStack，也不要指望引擎自动填好这个矩阵。
     * @param gameTime    当前游戏刻
     * @param partialTick 帧间插值系数
     */
    public abstract void render(Vec3 cameraPos, long gameTime, float partialTick);
}
