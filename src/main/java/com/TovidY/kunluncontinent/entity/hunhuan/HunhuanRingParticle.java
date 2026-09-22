package com.TovidY.kunluncontinent.entity.hunhuan;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 魂环粒子 —— 把魂环贴图本身当成粒子渲染出来，并且**平躺**在地上那个平面里。
 *
 * <p>普通粒子贴图是"永远正对摄像机"的公告板，那样魂环会竖着立起来 —— 不对。
 * 魂环应该像呼啦圈一样横着套在身上，所以这里重写了 {@link #render}：
 * 把四边形从"面向摄像机"改成"法线朝上"，再绕世界 Y 轴自转。</p>
 *
 * <h3>四个阶段</h3>
 * <ol>
 *   <li>{@link Phase#FOLLOW 跟随} —— 玩家还骑在魂环实体上：上下扫过 + 时不时缩放 + 缓慢自转；</li>
 *   <li>{@link Phase#SETTLING 判定} —— 玩家不骑了，先等几个 tick 看清是"吸成功"还是"自己下车"；</li>
 *   <li>{@link Phase#CONVERGE 收束} —— 吸收成功：魂环升到头顶并放大，随后缩小、下移，飞进玩家体内消失；</li>
 *   <li>{@link Phase#FADE 淡出} —— 玩家中途下车：原地淡出。</li>
 * </ol>
 *
 * <p>判定方式：吸收成功时服务端会 {@code discard()} 掉魂环实体，
 * 所以「魂环实体没了」= 成功，「魂环实体还在但玩家不骑了」= 玩家自己下车。
 * 两者都是原版同步的状态，不需要任何自定义包。</p>
 *
 * <p>尺寸与颜色都跟着魂环年限走，见 {@link HunhuanAbsorbFx.Tier}：
 * 十年白、百年黄、千年紫、万年黑、十万年红、百万年橙金、千万年蓝、亿年金绿。</p>
 */
public class HunhuanRingParticle extends TextureSheetParticle {

    /** 注册时缓存下来的贴图集合（直接生成粒子时拿不到 SpriteSet）。 */
    private static SpriteSet hunhuanSprites;
    private static SpriteSet shenhuanSprites;

    /** 把四边形翻成水平的旋转量：绕 X 轴 90°，让局部 +Z（面法线）转到世界 +Y。 */
    private static final float TILT = (float) (Math.PI / 2.0);

    /** 魂环离玩家脚底的高度（格）。 */
    private static final double BASE_Y = 1.30D;
    /** 上下浮动幅度（格）：从脚边一路升到头顶之上再落回来，约 0.3 ~ 2.3 格。 */
    private static final double BOB_AMP = 1.00D;
    /** 上下浮动角速度（弧度/tick），约 2.4 秒一个来回。 */
    private static final double BOB_SPEED = 0.13D;
    /** 自转角速度（弧度/tick），约 6 秒转一圈。 */
    private static final float ROLL_SPEED = 0.05F;
    /** 缩放脉动角速度（弧度/tick），约 3 秒鼓一次。 */
    private static final float PULSE_SPEED = 0.10F;
    /** 缩放脉动的下限倍率（脉动值 0 时）。 */
    private static final float PULSE_MIN = 0.78F;
    /** 缩放脉动的上下幅度（脉动值 0→1 时覆盖 {@code 0.78 → 1.26}）。 */
    private static final float PULSE_RANGE = 0.48F;

    /** 起手淡入时长（tick）。 */
    private static final float FADE_IN = 8.0F;
    /** 下车淡出时长（tick）。 */
    private static final int FADE_TICKS = 12;
    /** 停下后等待多久再判定成功 / 中断（tick）：给实体移除包留点到达时间。 */
    private static final int SETTLE_GRACE = 4;

    /** 收束动画总时长（tick）。 */
    private static final int CONVERGE_TICKS = 22;
    /** 收束动画里"升到头顶并放大"占的比例，剩下的是"缩小下移飞入体内"。 */
    private static final float CONVERGE_LIFT_FRACTION = 0.27F;
    /** 收束起手时魂环在玩家脚底之上的高度（格）—— 头顶之上。 */
    private static final double CONVERGE_HEAD_Y = 2.30D;
    /** 收束终点在玩家脚底之上的高度（格）—— 身体中心。 */
    private static final double CONVERGE_BODY_Y = 0.95D;
    /** 收束起手时相对基准尺寸的放大倍率。 */
    private static final float CONVERGE_HEAD_SCALE = 1.5F;
    /** 收束起手尺寸的上限（四边形半宽），免得亿年魂环铺满半个屏幕。 */
    private static final float CONVERGE_MAX_HALF_SIZE = 3.0F;
    /** 收束时自转加速倍率（像被吸进去一样越转越快）。 */
    private static final float CONVERGE_SPIN_BOOST = 3.0F;

    /** 粒子所处的阶段。 */
    private enum Phase {
        /** 跟随玩家。 */
        FOLLOW,
        /** 玩家不骑了，等待判定成功还是中断。 */
        SETTLING,
        /** 吸收成功：升到头顶 → 缩小下移飞入体内。 */
        CONVERGE,
        /** 玩家中途下车：淡出。 */
        FADE
    }

    /** 骑在魂环上的玩家实体 id。 */
    private final int riderId;
    /** 玩家骑着的那个魂环实体 id（用来判断它有没有被"吸掉"）。 */
    private final int ringId;

    /** 相对玩家脚底的基准高度。 */
    private final double baseY;
    private final double bobAmp;
    private final double bobSpeed;
    /** 基准尺寸：四边形半宽。贴图里的环占满整张图，所以显示直径 ≈ 1.9 × 该值。 */
    private final float baseSize;
    private final float pulseSpeed;
    private final float rollSpeed;

    private Phase phase = Phase.FOLLOW;
    /** 当前阶段已经过的 tick（FADE 阶段允许从负数起步，用来做"多停留一会儿再淡"）。 */
    private int phaseTicks;
    /** 进入收束阶段时的当前位置与尺寸，作为起手关键帧。 */
    private double convergeFromY;
    private float convergeFromSize;

    private HunhuanRingParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
                                int riderId, int ringId, double baseY, double bobAmp, double bobSpeed,
                                float baseSize, float pulseSpeed, float rollSpeed,
                                float r, float g, float b) {
        super(level, x, y, z);
        this.pickSprite(sprites);

        this.riderId = riderId;
        this.ringId = ringId;
        this.baseY = baseY;
        this.bobAmp = bobAmp;
        this.bobSpeed = bobSpeed;
        this.baseSize = baseSize;
        this.pulseSpeed = pulseSpeed;
        this.rollSpeed = rollSpeed;

        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.friction = 1.0F;
        // 包围盒放大到能装下整个四边形，避免大半个环被裁剪掉
        this.setSize(baseSize * 2.2F, baseSize * 2.2F);

        this.setColor(r, g, b);
        this.alpha = 0.0F;
        this.quadSize = baseSize;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        // 记录上一帧位置 / 自转角，渲染时做插值才不会一跳一跳
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        this.roll += this.rollSpeed;
        this.age++;

        Entity rider = this.level.getEntity(this.riderId);
        Entity ring = this.level.getEntity(this.ringId);

        switch (this.phase) {
            case FOLLOW -> {
                boolean riding = ring != null && !ring.isRemoved()
                        && rider != null && rider.getVehicle() == ring;
                if (riding) {
                    tickFollow(rider);
                } else {
                    // 不骑了 —— 但先别下结论：实体移除包可能比"下车"晚一两个 tick 到
                    this.phase = Phase.SETTLING;
                    this.phaseTicks = 0;
                }
            }
            case SETTLING -> {
                this.phaseTicks++;
                if (ring == null || ring.isRemoved()) {
                    // 魂环实体被"吸掉"了 = 吸收成功，起收束动画
                    this.phase = Phase.CONVERGE;
                    this.phaseTicks = 0;
                    this.convergeFromY = this.y;
                    this.convergeFromSize = this.quadSize;
                } else if (this.phaseTicks > SETTLE_GRACE) {
                    // 魂环还在原地 = 玩家自己下车了
                    this.phase = Phase.FADE;
                    this.phaseTicks = 0;
                }
            }
            case CONVERGE -> tickConverge(rider);
            case FADE -> {
                this.phaseTicks++;
                this.alpha = Mth.clamp(1.0F - this.phaseTicks / (float) FADE_TICKS, 0.0F, 1.0F);
                if (this.phaseTicks >= FADE_TICKS) {
                    this.remove();
                }
            }
        }
    }

    /** 跟随：上下扫过 + 时不时缩放。 */
    private void tickFollow(Entity rider) {
        double y = rider.getY() + this.baseY + Math.sin(this.age * this.bobSpeed) * this.bobAmp;
        this.setPos(rider.getX(), y, rider.getZ());

        // 缩放脉动：正弦取三次方 → 平时贴近 1，偶尔鼓一下
        float pulse = (float) Math.pow(0.5D + 0.5D * Math.sin(this.age * this.pulseSpeed), 3.0D);
        this.quadSize = this.baseSize * (PULSE_MIN + PULSE_RANGE * pulse);
        this.alpha = Math.min(1.0F, this.age / FADE_IN);
    }

    /**
     * 收束（吸收成功）：先升到头顶并放大，再缩小、下移，飞进玩家体内。
     *
     * <p>下移用二次曲线，所以"慢慢下移"到末尾才加速，像是被吸进去；
     * 尺寸同样用二次曲线，前半段保持大、后半段才明显缩小。</p>
     */
    private void tickConverge(Entity rider) {
        double footY = rider != null ? rider.getY() : this.y - CONVERGE_BODY_Y;
        double headY = footY + CONVERGE_HEAD_Y;
        double bodyY = footY + CONVERGE_BODY_Y;
        float headSize = Math.min(this.baseSize * CONVERGE_HEAD_SCALE, CONVERGE_MAX_HALF_SIZE);

        float t = this.phaseTicks / (float) CONVERGE_TICKS;

        if (t < CONVERGE_LIFT_FRACTION) {
            // 第一段：从当前位置升到头顶，同时放大（缓出，起步快、收尾稳）
            float k = t / CONVERGE_LIFT_FRACTION;
            float ease = 1.0F - (1.0F - k) * (1.0F - k);
            this.y = this.convergeFromY + (headY - this.convergeFromY) * ease;
            this.quadSize = this.convergeFromSize + (headSize - this.convergeFromSize) * ease;
        } else {
            // 第二段：缩小 + 下移，飞入体内
            float k = (t - CONVERGE_LIFT_FRACTION) / (1.0F - CONVERGE_LIFT_FRACTION);
            this.y = headY + (bodyY - headY) * (k * k);
            this.quadSize = headSize * (1.0F - k * k);
        }

        if (rider != null) {
            this.setPos(rider.getX(), this.y, rider.getZ());
        }
        // 越转越快
        this.roll += this.rollSpeed * CONVERGE_SPIN_BOOST;
        // 最后三成淡出，避免"啪"地消失
        this.alpha = t < 0.7F ? 1.0F : Mth.clamp((1.0F - t) / 0.3F, 0.0F, 1.0F);

        this.phaseTicks++;
        if (this.phaseTicks >= CONVERGE_TICKS) {
            this.remove();
        }
    }

    /**
     * 平躺渲染。
     *
     * <p>父类的实现是让四边形永远正对摄像机（公告板），魂环会竖着立起来。
     * 这里换成固定朝向：四边形落在水平面内、法线朝上，再叠一个绕竖直轴的自转。</p>
     */
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 camPos = camera.getPosition();
        float px = (float) (Mth.lerp(partialTick, this.xo, this.x) - camPos.x());
        float py = (float) (Mth.lerp(partialTick, this.yo, this.y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTick, this.zo, this.z) - camPos.z());

        // 先绕 X 翻成水平，再绕 Y 自转（JOML 是右乘，所以后写的先生效于顶点）
        Quaternionf rot = new Quaternionf()
                .rotateY(Mth.lerp(partialTick, this.oRoll, this.roll))
                .rotateX(TILT);

        Vector3f[] corners = {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float half = this.getQuadSize(partialTick);
        for (Vector3f corner : corners) {
            corner.rotate(rot).mul(half).add(px, py, pz);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTick);

        // 正反两面各输出一次：粒子渲染阶段的面剔除状态不保证，两面都画最稳
        // （就一片四边形，开销可以忽略）
        vertex(buffer, corners[0], u1, v1, light);
        vertex(buffer, corners[1], u1, v0, light);
        vertex(buffer, corners[2], u0, v0, light);
        vertex(buffer, corners[3], u0, v1, light);

        vertex(buffer, corners[3], u0, v1, light);
        vertex(buffer, corners[2], u0, v0, light);
        vertex(buffer, corners[1], u1, v0, light);
        vertex(buffer, corners[0], u1, v1, light);
    }

    private void vertex(VertexConsumer buffer, Vector3f pos, float u, float v, int light) {
        buffer.vertex(pos.x(), pos.y(), pos.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    /** 半透明混合，让魂环边缘柔和。 */
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    /** 恒为全亮：魂环本身发光，不该受环境光影响。 */
    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    /** 包围盒很小但四边形很大，交给原版按包围盒裁剪会被整片剔掉，所以关掉。 */
    @Override
    public boolean shouldCull() {
        return false;
    }

    // ==================================================================================
    //  生成入口
    // ==================================================================================

    /**
     * 给某个正在吸收魂环的玩家挂上一枚魂环。
     *
     * @param rider 骑在魂环上的玩家
     * @param ring  被骑的那个魂环实体
     * @return 生成的粒子；贴图还没加载好时返回 {@code null}
     */
    public static HunhuanRingParticle spawn(ClientLevel level, Entity rider, HunhuanEntity ring) {
        HunhuanAbsorbFx.Tier tier = HunhuanAbsorbFx.Tier.of((int) ring.getNianxianSync());
        SpriteSet sprites = tier.shenhuan() ? shenhuanSprites : hunhuanSprites;
        if (sprites == null) {
            return null;
        }
        float[] tint = tier.tint();
        HunhuanRingParticle particle = new HunhuanRingParticle(level,
                rider.getX(), rider.getY() + BASE_Y, rider.getZ(), sprites,
                rider.getId(), ring.getId(), BASE_Y, BOB_AMP, BOB_SPEED,
                tier.size, PULSE_SPEED, ROLL_SPEED, tint[0], tint[1], tint[2]);
        Minecraft.getInstance().particleEngine.add(particle);
        return particle;
    }

    // ==================================================================================
    //  Provider
    // ==================================================================================

    /**
     * 粒子工厂。构造时把 {@link SpriteSet} 缓存到静态字段，
     * 供 {@link #spawn} 直接生成粒子时取用。
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites, boolean shenhuan) {
            this.sprites = sprites;
            if (shenhuan) {
                shenhuanSprites = sprites;
            } else {
                hunhuanSprites = sprites;
            }
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double xd, double yd, double zd) {
            // 走 /particle 指令时的默认形态：原地悬浮 60 tick 再淡出
            HunhuanRingParticle particle = new HunhuanRingParticle(
                    level, x, y, z, this.sprites, -1, -1, 0.0D, 0.0D, 0.0D,
                    1.20F, PULSE_SPEED, ROLL_SPEED, 1.0F, 1.0F, 1.0F);
            particle.phase = Phase.FADE;
            particle.phaseTicks = -60;
            return particle;
        }
    }
}
