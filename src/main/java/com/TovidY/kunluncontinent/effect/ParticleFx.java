package com.TovidY.kunluncontinent.effect;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * ParticleFx —— 数学函数驱动的粒子特效工具类。
 *
 * <p>设计目标：把“圆环 / 多边形 / 魔法阵 / 螺旋 / 球面 / 剑气 / 光柱 / 波形”等形状
 * 全部抽象成可复用的三角函数求值过程，技能类只需描述“想要什么形状、多大、多高、多密”，
 * 不必再手写 {@code for (int i = 0; i < 360; i += 10)} 这类散装循环。</p>
 *
 * <h3>三个核心控制维度</h3>
 * <ul>
 *   <li><b>形状（Shape）</b>：所有形状方法都接受点密度 / 层数 / 分段数参数，
 *       通过 {@link #lod(double)} 可整体降密（联动玩家的“粒子优化”开关）。</li>
 *   <li><b>尺寸（Size）</b>：{@link #size(double)} 提供全局半径缩放，
 *       配合每个方法的 radius / height 构成“自定义高度 + 自定义半径”的双层控制。</li>
 *   <li><b>预算（Budget）</b>：{@link #budget(int)} 为一次技能调用设置粒子上限，
 *       超出后自动静默丢弃，防止某个技能在大范围内把服务器打爆。</li>
 * </ul>
 *
 * <h3>精确落点原理</h3>
 * <p>原版 {@code ServerLevel#sendParticles} 在 {@code count == 0} 时，
 * 客户端会在<b>精确坐标</b>生成 1 个粒子，并把 {@code (xDist,yDist,zDist) * speed}
 * 当作初速度。本类全部精确形状都走这条通道，因此可以做到
 * “算出一个点 → 粒子就落在那个点上”，而不是像 {@code count > 0} 那样被
 * 高斯噪声打散。需要体积感时才使用 {@link #bloom}（批量低成本撒点）。</p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * ParticleFx fx = ParticleFx.of(level, player);
 * if (fx == null) return;                       // 只在服务端生效
 * fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.ENCHANT, center, ParticleFx.Axis.Y, 4.5, rot, 2, 0.0);
 * fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.FLAME, base, 1.2, 6.0, 4, rot, 1.2, 0.6);
 * fx.slash(ParticleTypes.SWEEP_ATTACK, eye, look, up, 5.0, 140, 4, 0.0);
 * }</pre>
 */
public final class ParticleFx {

    public static final double TAU = Math.PI * 2.0;

    public enum Axis {
        X, Y, Z
    }

    private final ServerLevel level;

    private double lod = 1.0;

    private double sizeScale = 1.0;

    private int budget = 3000;

    private ParticleFx(ServerLevel level) {
        this.level = level;
    }

    public static ParticleFx of(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return new ParticleFx(serverLevel);
        }
        return null;
    }

    /** 以施法者为基准构建，自动读取其“粒子优化”开关决定细节倍率。 */
    public static ParticleFx of(Level level, Player caster) {
        ParticleFx fx = of(level);
        if (fx == null) {
            return null;
        }
        return fx.lod(lodOf(caster));
    }

    /** 读取玩家粒子优化开关（capability 配置位 10）→ 返回 0.5，否则 1.0。 */
    public static double lodOf(Player player) {
        if (player == null) {
            return 1.0;
        }
        var cap = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).orElse(null);
        if (cap != null && cap.isConfigOpen(10)) {
            return 0.5;
        }
        return 1.0;
    }

    /** 细节倍率，范围 0.15 ~ 4.0。 */
    public ParticleFx lod(double factor) {
        this.lod = Math.max(0.15, Math.min(4.0, factor));
        return this;
    }

    /** 全局半径缩放。 */
    public ParticleFx size(double factor) {
        this.sizeScale = Math.max(0.01, factor);
        return this;
    }

    /** 设置本次特效的粒子预算上限。 */
    public ParticleFx budget(int maxParticles) {
        this.budget = Math.max(1, maxParticles);
        return this;
    }

    /** 重置预算（长流程里分阶段调用时使用）。 */
    public ParticleFx refill(int maxParticles) {
        return budget(maxParticles);
    }

    public double lod() {
        return lod;
    }

    public double size() {
        return sizeScale;
    }

    public int budgetLeft() {
        return budget;
    }

    public ServerLevel level() {
        return level;
    }

    // ==================================================================================
    //  基础发射
    // ==================================================================================

    /** 精确单点：粒子正好落在 pos，速度为 0。 */
    public boolean dot(ParticleOptions type, Vec3 pos) {
        return dot(type, pos, Vec3.ZERO);
    }

    /** 精确单点 + 指定初速度（可拖尾 / 迸射）。 */
    public boolean dot(ParticleOptions type, Vec3 pos, Vec3 velocity) {
        if (budget <= 0) {
            return false;
        }
        budget--;
        level.sendParticles(type, pos.x, pos.y, pos.z, 0, velocity.x, velocity.y, velocity.z, 1.0D);
        return true;
    }

    /** 体积脉冲：在胶囊 / 球状范围内低成本随机播撒，用于火光、烟尘这类“有一团”的效果。 */
    public void bloom(ParticleOptions type, Vec3 pos, int count, double spread, double speed) {
        bloom(type, pos, count, spread, spread, spread, speed);
    }

    /** 体积脉冲：分别指定三个轴的散布半径。 */
    public void bloom(ParticleOptions type, Vec3 pos, int count, double sx, double sy, double sz, double speed) {
        if (budget <= 0) {
            return;
        }
        int n = Math.max(1, (int) Math.round(count * Math.max(0.4, lod)));
        n = Math.min(n, budget);
        budget -= n;
        level.sendParticles(type, pos.x, pos.y, pos.z, n, sx, sy, sz, speed);
    }

    /** 直线段：从 from 到 to 按 step 间距铺粒子，jitter 为随机抖动半径。 */
    public void line(ParticleOptions type, Vec3 from, Vec3 to, double step) {
        line(type, from, to, step, 0.0, 1.0, Vec3.ZERO);
    }

    /**
     * 直线段（全参数）。
     *
     * @param step   间距（会随 lod 自动放大，低配下自动稀疏）
     * @param jitter 随机抖动半径，0 表示严格贴线
     * @param speed  初速度倍率
     * @param drift  初速度方向（会被 speed 缩放）
     */
    public void line(ParticleOptions type, Vec3 from, Vec3 to, double step, double jitter, double speed, Vec3 drift) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-6) {
            return;
        }
        Vec3 dir = delta.scale(1.0 / length);
        double s = step / Math.max(0.3, lod);
        int n = (int) Math.floor(length / s);
        Vec3 vel = drift.scale(speed);
        for (int i = 0; i <= n; i++) {
            Vec3 p = from.add(dir.scale(i * s));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p, vel);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 折线：依次连接给定顶点。 */
    public void polyline(ParticleOptions type, List<Vec3> points, double step, double jitter) {
        for (int i = 0; i + 1 < points.size(); i++) {
            line(type, points.get(i), points.get(i + 1), step, jitter, 1.0, Vec3.ZERO);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 环形迸射：从 center 向外（或向内）径向喷射。 */
    public void burst(ParticleOptions type, Vec3 center, int count, double speed, boolean outward) {
        int n = Math.max(4, (int) Math.round(count * Math.max(0.4, lod)));
        for (int i = 0; i < n; i++) {
            double a = TAU * i / n;
            Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
            dot(type, center, dir.scale(outward ? speed : -speed));
        }
    }

    // ==================================================================================
    //  圆环 / 冲击环
    // ==================================================================================

    /** 基准平面圆环（最常用：水平光圈 / 魔法阵外圈）。 */
    public void circle(ParticleOptions type, Vec3 center, Axis axis, double radius, int points, double phase, double jitter) {
        int n = points(points);
        double r = radius * sizeScale;
        for (int i = 0; i < n; i++) {
            double a = phase + TAU * i / n;
            Vec3 p = center.add(map(axis, Math.cos(a) * r, 0, Math.sin(a) * r));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 任意朝向的圆环：由平面内两个正交基 u、v 张开，可做出倾斜 / 竖直的环。 */
    public void circle(ParticleOptions type, Vec3 center, Vec3 u, Vec3 v, double radius, int points, double phase, double jitter) {
        Vec3 uu = u.normalize();
        Vec3 vv = v.normalize();
        int n = points(points);
        double r = radius * sizeScale;
        for (int i = 0; i < n; i++) {
            double a = phase + TAU * i / n;
            Vec3 p = center.add(uu.scale(Math.cos(a) * r)).add(vv.scale(Math.sin(a) * r));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
            if (budget <= 0) {
                return;
            }
        }
    }

    /**
     * 打散 / 分段圆环：把整圈切成 {@code segments} 段，每段只画 {@code fillRatio} 的比例。
     * 用来做符文环、虚线法阵、断裂的封印 —— 这是“三角环打散”的通用做法。
     */
    public void dashedRing(ParticleOptions type, Vec3 center, Axis axis, double radius, int segments, double fillRatio, double phase, double jitter) {
        int segs = Math.max(2, (int) Math.round(segments));
        int per = Math.max(2, (int) Math.round(9 * Math.max(0.4, lod)));
        double arc = TAU / segs;
        double span = arc * clamp(fillRatio, 0.05, 0.95);
        for (int s = 0; s < segs; s++) {
            double start = phase + s * arc;
            for (int i = 0; i < per; i++) {
                double a = start + span * i / (per - 1.0);
                Vec3 p = center.add(map(axis, Math.cos(a) * radius * sizeScale, 0, Math.sin(a) * radius * sizeScale));
                dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
                if (budget <= 0) {
                    return;
                }
            }
        }
    }

    /**
     * 自定义高度的多层圆环（“光筒 / 环柱”）：
     * {@code height} 决定总高，{@code layers} 决定分层数，{@code taper} 决定上下半径比。
     */
    public void ringStack(ParticleOptions type, Vec3 base, Axis axis, double radius, double height, int layers, int points, double phase, double taper, double twist) {
        int ln = Math.max(1, (int) Math.round(layers * Math.max(0.5, lod)));
        for (int l = 0; l < ln; l++) {
            double t = ln == 1 ? 0.0 : l / (ln - 1.0);
            double r = radius * (1.0 + (taper - 1.0) * t);
            Vec3 c = base.add(map(axis, 0, height * t, 0));
            circle(type, c, axis, r, points, phase + twist * t, 0.0);
        }
    }

    /** 贴地冲击环：可指定厚度（多层同心环 + 层间抬升）。 */
    public void shockRing(ParticleOptions type, Vec3 center, double radius, double thickness, int points) {
        int layers = Math.max(1, (int) Math.round(thickness * 4));
        for (int l = 0; l < layers; l++) {
            circle(type, center.add(0, l * 0.25, 0), Axis.Y, radius * (1.0 - l * 0.03), points, l * 0.25, 0.0);
        }
    }

    // ==================================================================================
    //  多边形 / 星形
    // ==================================================================================

    /** 正多边形（3 = 三角、6 = 六芒阵骨架、8 = 八卦/八荒轮廓）。边数不会随 lod 减少。 */
    public void polygon(ParticleOptions type, Vec3 center, Axis axis, double radius, int sides, double rot, double jitter) {
        int n = Math.max(3, sides);
        Vec3[] v = new Vec3[n];
        for (int i = 0; i < n; i++) {
            double a = rot + TAU * i / n;
            v[i] = center.add(map(axis, Math.cos(a) * radius * sizeScale, 0, Math.sin(a) * radius * sizeScale));
        }
        for (int i = 0; i < n; i++) {
            line(type, v[i], v[(i + 1) % n], 0.45, jitter, 1.0, Vec3.ZERO);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 星形 / 芒阵：内外半径交替，{@code tips} 为角数（5 = 五芒星，6 = 六芒）。 */
    public void star(ParticleOptions type, Vec3 center, Axis axis, double rOuter, double rInner, int tips, double rot, double jitter) {
        int t = Math.max(3, tips);
        int n = t * 2;
        Vec3[] v = new Vec3[n];
        for (int i = 0; i < n; i++) {
            double r = (i % 2 == 0 ? rOuter : rInner) * sizeScale;
            double a = rot + TAU * i / n;
            v[i] = center.add(map(axis, Math.cos(a) * r, 0, Math.sin(a) * r));
        }
        for (int i = 0; i < n; i++) {
            line(type, v[i], v[(i + 1) % n], 0.4, jitter, 1.0, Vec3.ZERO);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 玫瑰线（花瓣曲线）：一个相位即可扫出 n 花瓣的优雅曲线，适合自然 / 木属性阵纹。 */
    public void roseCurve(ParticleOptions type, Vec3 center, Axis axis, double radius, int petals, double rot, int points, double jitter) {
        int n = points(points);
        int k = Math.max(2, petals);
        double r0 = radius * sizeScale;
        for (int i = 0; i < n; i++) {
            double a = rot + TAU * i / n;
            double r = Math.cos(k * a) * r0;
            Vec3 p = center.add(map(axis, Math.cos(a) * r, 0, Math.sin(a) * r));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
            if (budget <= 0) {
                return;
            }
        }
    }

    // ==================================================================================
    //  魔法阵（旗舰方法）
    // ==================================================================================

    /** 单层魔法阵：外环 + 内环 + 双层多边形 + 刻度 + 悬浮符文点。 */
    public void magicCircle(ParticleOptions ring, ParticleOptions glyph, Vec3 center, Axis axis, double radius, double rot) {
        magicCircle(ring, glyph, center, axis, radius, rot, 1, 0.0);
    }

    /**
     * 多层魔法阵（自定义高度版）。
     *
     * @param layers   层数，> 1 时沿基准轴向上堆叠成“法阵柱”
     * @param layerGap 每层之间的高度间隔
     */
    public void magicCircle(ParticleOptions ring, ParticleOptions glyph, Vec3 center, Axis axis, double radius, double rot, int layers, double layerGap) {
        int ln = Math.max(1, layers);
        for (int l = 0; l < ln; l++) {
            double t = ln == 1 ? 0.0 : l / (ln - 1.0);
            Vec3 c = center.add(map(axis, 0, layerGap * l, 0));
            double r = radius * (1.0 - 0.12 * t);
            double spin = rot + l * 0.55;

            // 外双环
            circle(ring, c, axis, r, 48, spin, 0.0);
            circle(ring, c, axis, r * 0.74, 36, -spin * 1.35, 0.0);
            // 多边形骨架
            polygon(glyph, c, axis, r * 0.9, 6, spin, 0.0);
            polygon(glyph, c, axis, r * 0.56, 3, -spin * 1.6, 0.0);
            // 外沿刻度
            int ticks = Math.max(6, (int) Math.round(12 * Math.max(0.5, lod)));
            for (int i = 0; i < ticks; i++) {
                double a = spin + TAU * i / ticks;
                Vec3 a1 = c.add(map(axis, Math.cos(a) * r * 0.9, 0, Math.sin(a) * r * 0.9));
                Vec3 a2 = c.add(map(axis, Math.cos(a) * r, 0, Math.sin(a) * r));
                line(glyph, a1, a2, 0.32);
            }
            // 悬浮符文
            int runes = Math.max(3, (int) Math.round(8 * Math.max(0.4, lod)));
            for (int i = 0; i < runes; i++) {
                double a = -spin * 0.8 + TAU * i / runes;
                Vec3 p = c.add(map(axis, Math.cos(a) * r * 0.4, 0, Math.sin(a) * r * 0.4));
                bloom(glyph, p, 2, 0.05, 0.01);
            }
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 魔法阵柱：矩阵外环 + 内部竖直光柱，常用于召唤 / 真身类技能。 */
    public void magicPillar(ParticleOptions ring, ParticleOptions glyph, ParticleOptions core, Vec3 base, double radius, double height, double rot) {
        magicCircle(ring, glyph, base, Axis.Y, radius, rot);
        magicCircle(ring, glyph, base.add(0, height, 0), Axis.Y, radius * 0.72, -rot * 1.3);
        column(ring, core, base, radius * 0.32, height, 4, rot, TAU * 0.8, 0.85);
    }

    // ==================================================================================
    //  螺旋 / 立体形状
    // ==================================================================================

    /**
     * 螺旋线。{@code r0 → r1} 控制半径变化，{@code height} 为自定义高度，{@code turns} 为圈数。
     * 这是“把圆环打散成上升气流”最常用的形状。
     */
    public void spiral(ParticleOptions type, Vec3 base, Axis axis, double r0, double r1, double height, double turns, double phase, int points, double jitter) {
        int n = points(points);
        for (int i = 0; i < n; i++) {
            double t = i / (n - 1.0);
            double a = phase + TAU * turns * t;
            double r = (r0 + (r1 - r0) * t) * sizeScale;
            Vec3 p = base.add(map(axis, Math.cos(a) * r, height * t, Math.sin(a) * r));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 多股螺旋（双螺旋 / 三螺旋），用于缠绕枪身、锁链、能量束。 */
    public void helix(ParticleOptions type, Vec3 base, Axis axis, double radius, double height, double turns, int strands, double phase, int points) {
        int n = points(points);
        for (int s = 0; s < strands; s++) {
            double offset = phase + TAU * s / strands;
            for (int i = 0; i < n; i++) {
                double t = i / (n - 1.0);
                double a = offset + TAU * turns * t;
                Vec3 p = base.add(map(axis, Math.cos(a) * radius * sizeScale, height * t, Math.sin(a) * radius * sizeScale));
                dot(type, p);
                if (budget <= 0) {
                    return;
                }
            }
        }
    }

    /** 锥面（枪尖 / 突刺 / 穿透气流）：从 apex 沿 dir 张开到 length，底部半径 baseRadius。 */
    public void cone(ParticleOptions type, Vec3 apex, Vec3 dir, double length, double baseRadius, int points, double phase, int ribs) {
        Vec3 d = dir.normalize();
        Vec3 u = ortho(d);
        Vec3 v = d.cross(u).normalize();
        int n = points(points);
        int k = Math.max(3, ribs);
        for (int i = 1; i <= n; i++) {
            double t = i / (double) n;
            double r = baseRadius * t * sizeScale;
            for (int j = 0; j < k; j++) {
                double a = phase + TAU * j / k;
                Vec3 p = apex.add(d.scale(length * t)).add(u.scale(Math.cos(a) * r)).add(v.scale(Math.sin(a) * r));
                dot(type, p);
            }
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 漩涡 / 龙卷：多股由外向内收束并上升。 */
    public void vortex(ParticleOptions type, Vec3 center, Axis axis, double maxRadius, double height, int arms, int points, double phase, double tightness) {
        int n = points(points);
        for (int a = 0; a < arms; a++) {
            double offset = phase + TAU * a / arms;
            for (int i = 0; i < n; i++) {
                double t = i / (double) n;
                double ang = offset + tightness * t * TAU;
                double r = maxRadius * sizeScale * (1.0 - t * 0.85);
                Vec3 p = center.add(map(axis, Math.cos(ang) * r, height * t, Math.sin(ang) * r));
                dot(type, p);
                if (budget <= 0) {
                    return;
                }
            }
        }
    }

    /** 斐波那契球面：均匀不结块的球形撒点，用于“悬空碎石 / 环绕球体”。coverage 1.0 = 全球面。 */
    public void sphere(ParticleOptions type, Vec3 center, double radius, int count, double coverage, double jitter) {
        int n = Math.max(6, (int) Math.round(count * Math.max(0.4, lod)));
        double r = radius * sizeScale;
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < n; i++) {
            double y = 1.0 - (i / (double) (n - 1)) * 2.0 * clamp(coverage, 0.05, 1.0);
            double ring = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            double theta = golden * i;
            Vec3 p = center.add(new Vec3(Math.cos(theta) * ring * r, y * r, Math.sin(theta) * ring * r));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 穹顶 / 半球罩：一圈圈向上收拢，适合护盾、石肤、封印。 */
    public void dome(ParticleOptions type, Vec3 base, double radius, int rings, int segments) {
        int rc = Math.max(2, (int) Math.round(rings * Math.max(0.5, lod)));
        for (int i = 1; i <= rc; i++) {
            double phi = (Math.PI / 2.0) * (i / (double) rc);
            double r = radius * Math.cos(phi);
            double h = radius * Math.sin(phi);
            circle(type, base.add(0, h * sizeScale, 0), Axis.Y, r, segments, i * 0.35, 0.0);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 圆柱侧面 + 顶底圆：柱状护体 / 岩浆柱。 */
    public void cylinder(ParticleOptions side, ParticleOptions cap, Vec3 base, double radius, double height, int segments, double phase) {
        int n = Math.max(8, (int) Math.round(segments * Math.max(0.5, lod)));
        for (int i = 0; i < n; i++) {
            double a = phase + TAU * i / n;
            double x = Math.cos(a) * radius * sizeScale;
            double z = Math.sin(a) * radius * sizeScale;
            for (double h = 0; h <= height; h += 0.8) {
                dot(side, base.add(x, h, z));
            }
        }
        if (cap != null) {
            circle(cap, base, Axis.Y, radius, segments, phase, 0.0);
            circle(cap, base.add(0, height, 0), Axis.Y, radius, segments, -phase, 0.0);
        }
    }

    /** 垂直于 dir 的圆环：围绕任意射线 / 柱体的一圈（枪轨、剑气缠绕环的基准）。 */
    public void ringAround(ParticleOptions type, Vec3 center, Vec3 dir, double radius, int points, double phase, double jitter) {
        Vec3 d = dir.normalize();
        Vec3 u = ortho(d);
        Vec3 v = d.cross(u).normalize();
        circle(type, center, u, v, radius, points, phase, jitter);
    }

    /**
     * 围绕任意方向 dir 的螺旋（枪轨 / 缠绕气流）。
     * 与 {@link #spiral} 的区别是：本方法跟随射线方向，而不是垂直世界轴。
     */
    public void spiralAround(ParticleOptions type, Vec3 origin, Vec3 dir, double r0, double r1, double length, double turns, double phase, int points, double jitter) {
        Vec3 d = dir.normalize();
        Vec3 u = ortho(d);
        Vec3 v = d.cross(u).normalize();
        int n = points(points);
        for (int i = 0; i < n; i++) {
            double t = i / (n - 1.0);
            double a = phase + TAU * turns * t;
            double r = (r0 + (r1 - r0) * t) * sizeScale;
            Vec3 p = origin.add(d.scale(length * t)).add(u.scale(Math.cos(a) * r)).add(v.scale(Math.sin(a) * r));
            dot(type, jitter > 0 ? p.add(jitter(jitter)) : p);
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 沿任意方向的多股螺旋（双螺旋缠绕枪身）。 */
    public void helixAround(ParticleOptions type, Vec3 origin, Vec3 dir, double radius, double length, double turns, int strands, double phase, int points) {
        Vec3 d = dir.normalize();
        Vec3 u = ortho(d);
        Vec3 v = d.cross(u).normalize();
        int n = points(points);
        for (int s = 0; s < strands; s++) {
            double offset = phase + TAU * s / strands;
            for (int i = 0; i < n; i++) {
                double t = i / (n - 1.0);
                double a = offset + TAU * turns * t;
                Vec3 p = origin.add(d.scale(length * t))
                        .add(u.scale(Math.cos(a) * radius * sizeScale))
                        .add(v.scale(Math.sin(a) * radius * sizeScale));
                dot(type, p);
                if (budget <= 0) {
                    return;
                }
            }
        }
    }

    /**
     * 光柱（自定义高度）。
     *
     * @param strands 外围股数（缠绕在柱面上的螺旋股）
     * @param twist   沿高度的扭转弧度，越大越有“缠绕感”
     * @param taper   顶部半径比例（1.0 直筒，< 1 收束，> 1 外扩）
     */
    public void column(ParticleOptions shell, ParticleOptions core, Vec3 base, double radius, double height, int strands, double phase, double twist, double taper) {
        int n = Math.max(6, (int) Math.round(24 * Math.max(0.4, lod)));
        double r0 = radius * sizeScale;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double h = height * t;
            double r = r0 * (1.0 + (taper - 1.0) * t);
            for (int s = 0; s < strands; s++) {
                double a = phase + twist * t + TAU * s / strands;
                dot(shell, base.add(Math.cos(a) * r, h, Math.sin(a) * r));
            }
            if (core != null) {
                dot(core, base.add(0, h, 0));
            }
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 环绕光柱群：在半径为 ringRadius 的圆上竖直立起 count 根光柱。 */
    public void pillars(ParticleOptions type, Vec3 center, double ringRadius, int count, double height, double phase) {
        for (int i = 0; i < count; i++) {
            double a = phase + TAU * i / count;
            Vec3 b = center.add(Math.cos(a) * ringRadius * sizeScale, 0, Math.sin(a) * ringRadius * sizeScale);
            for (double h = 0; h <= height; h += 0.7) {
                dot(type, b.add(0, h, 0));
            }
            if (budget <= 0) {
                return;
            }
        }
    }

    // ==================================================================================
    //  战斗语义形状（剑气 / 刀光 / 枪芒）
    // ==================================================================================

    /**
     * 弧形斩击（月牙 / 剑气）。
     *
     * @param origin       弧心（通常是眼睛位置）
     * @param dir          起始方向
     * @param rotationAxis 扫掠轴（水平横扫用世界 Y 轴；竖劈用“右向量”）
     * @param arcDeg       张开角度
     * @param thickness    弧的层数 → 视觉厚度
     */
    public void slash(ParticleOptions type, Vec3 origin, Vec3 dir, Vec3 rotationAxis, double radius, double arcDeg, double thickness, double phase) {
        Vec3 d = dir.normalize();
        Vec3 axis = rotationAxis.normalize();
        double half = Math.toRadians(arcDeg) / 2.0;
        int n = Math.max(8, (int) Math.round(arcDeg / 4.0 * Math.max(0.4, lod)) + 6);
        int layers = Math.max(1, (int) Math.round(thickness));
        for (int l = 0; l < layers; l++) {
            double r = radius * (1.0 - l * 0.05) * sizeScale;
            for (int i = 0; i < n; i++) {
                double a = phase + (-half + 2.0 * half * i / (n - 1.0));
                dot(type, origin.add(rotate(d, axis, a).scale(r)));
            }
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 多道剑气（扇形展开）：一次挥出 blades 道彼此错开的斩击弧。 */
    public void fanBlades(ParticleOptions type, Vec3 origin, Vec3 dir, Vec3 rotationAxis, int blades, double radius, double spreadDeg, double thickness) {
        int b = Math.max(1, blades);
        Vec3 axis = rotationAxis.normalize();
        double spread = Math.toRadians(spreadDeg);
        for (int i = 0; i < b; i++) {
            double offset = b == 1 ? 0.0 : -spread / 2.0 + spread * i / (b - 1.0);
            Vec3 d = rotate(dir.normalize(), axis, offset);
            slash(type, origin, d, axis, radius, Math.max(30.0, spreadDeg * 0.7), thickness, 0.0);
        }
    }

    /** 剑气球面：球面均匀布“剑”并向外刺出，是剑类技能最华丽的收招式。 */
    public void sphereBlades(ParticleOptions edge, ParticleOptions tip, Vec3 center, double radius, int seeds, double bladeLength, double spin) {
        int n = Math.max(8, (int) Math.round(seeds * Math.max(0.4, lod)));
        double r = radius * sizeScale;
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < n; i++) {
            double y = 1.0 - 2.0 * i / (n - 1.0);
            double ring = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            double theta = golden * i + spin;
            Vec3 normal = new Vec3(Math.cos(theta) * ring, y, Math.sin(theta) * ring);
            Vec3 p0 = center.add(normal.scale(r));
            Vec3 p1 = center.add(normal.scale(r + bladeLength * sizeScale));
            line(edge, p0, p1, 0.28);
            dot(tip, p1);
            if (budget <= 0) {
                return;
            }
        }
        circle(edge, center, Axis.Y, r * 0.98, 28, spin, 0.0);
        circle(edge, center, Axis.X, r * 0.98, 28, -spin * 1.3, 0.0);
        circle(edge, center, Axis.Z, r * 0.98, 28, spin * 1.7, 0.0);
    }

    /** 十字斩：两组互相垂直的斩击弧。 */
    public void crossSlash(ParticleOptions type, Vec3 origin, Vec3 dir, Vec3 rotationAxis, double radius, double arcDeg, double thickness) {
        slash(type, origin, dir, rotationAxis, radius, arcDeg, thickness, 0.0);
        slash(type, origin, dir, rotationAxis, radius, arcDeg, thickness, Math.PI / 2.0);
    }

    // ==================================================================================
    //  波形 / 曲线
    // ==================================================================================

    /** 正弦波：沿 forward 推进、以 side 为振幅方向振荡的波形带。 */
    public void wave(ParticleOptions type, Vec3 origin, Vec3 forward, Vec3 side, double length, double amplitude, double cycles, double phase, int points, double yOffset) {
        Vec3 f = forward.normalize();
        Vec3 s = side.normalize();
        int n = points(points);
        for (int i = 0; i < n; i++) {
            double t = i / (n - 1.0);
            double offset = Math.sin(phase + t * TAU * cycles) * amplitude * sizeScale;
            dot(type, origin.add(f.scale(length * t)).add(s.scale(offset)).add(0, yOffset, 0));
            if (budget <= 0) {
                return;
            }
        }
    }

    /** 锯齿 / 闪电折线：沿 forward 前进、以 side 为方向做随机折返，适合雷系技能。 */
    public void lightning(ParticleOptions type, Vec3 from, Vec3 to, int segments, double jag, double phase) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-6) {
            return;
        }
        Vec3 f = delta.scale(1.0 / length);
        Vec3 u = ortho(f);
        Vec3 v = f.cross(u).normalize();
        Vec3 prev = from;
        int n = Math.max(3, (int) Math.round(segments * Math.max(0.5, lod)));
        for (int i = 1; i <= n; i++) {
            double t = i / (double) n;
            Vec3 p = from.add(f.scale(length * t));
            if (i < n) {
                double seed = Math.sin(phase + i * 12.9898) * 43758.5453;
                double a = (frac(seed) - 0.5) * jag;
                double b = (frac(seed * 1.7) - 0.5) * jag;
                p = p.add(u.scale(a)).add(v.scale(b));
            }
            line(type, prev, p, 0.3);
            prev = p;
            if (budget <= 0) {
                return;
            }
        }
    }

    // ==================================================================================
    //  数学工具
    // ==================================================================================

    /** 罗德里格旋转：把向量 v 绕单位轴 axis 旋转 angle 弧度。所有弧形斩击的数学基础。 */
    public static Vec3 rotate(Vec3 v, Vec3 axis, double angle) {
        Vec3 k = axis.normalize();
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return v.scale(c)
                .add(k.cross(v).scale(s))
                .add(k.scale(k.dot(v) * (1.0 - c)));
    }

    /** 求与 dir 正交的单位向量（稳定版，避免 dir 与参考轴共线时退化）。 */
    public static Vec3 ortho(Vec3 dir) {
        Vec3 d = dir.normalize();
        Vec3 ref = Math.abs(d.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        return d.cross(ref).normalize();
    }

    /** 把基准平面内的极坐标 (u, v) 加上轴向偏移 h 映射到世界坐标。 */
    public static Vec3 map(Axis axis, double u, double h, double v) {
        switch (axis) {
            case X:
                return new Vec3(h, u, v);
            case Z:
                return new Vec3(u, v, h);
            case Y:
            default:
                return new Vec3(u, h, v);
        }
    }

    /** 三次缓出，用于时间轴插值（速度由快转慢）。 */
    public static double easeOut(double t) {
        double x = clamp(t, 0, 1);
        return 1.0 - Math.pow(1.0 - x, 3);
    }

    /** 三次缓入。 */
    public static double easeIn(double t) {
        double x = clamp(t, 0, 1);
        return x * x * x;
    }

    /** 平滑插值曲线。 */
    public static double smooth(double t) {
        double x = clamp(t, 0, 1);
        return x * x * (3.0 - 2.0 * x);
    }

    /** 线性插值。 */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** 取小数部分。 */
    public static double frac(double v) {
        return v - Math.floor(v);
    }

    public static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    // ==================================================================================
    //  内部
    // ==================================================================================

    private int points(int requested) {
        return Math.max(3, (int) Math.round(requested * Math.max(0.35, lod)));
    }

    private Vec3 jitter(double radius) {
        return new Vec3(
                (level.random.nextDouble() * 2.0 - 1.0) * radius,
                (level.random.nextDouble() * 2.0 - 1.0) * radius,
                (level.random.nextDouble() * 2.0 - 1.0) * radius);
    }
}
