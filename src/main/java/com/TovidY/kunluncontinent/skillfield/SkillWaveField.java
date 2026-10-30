package com.TovidY.kunluncontinent.skillfield;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 通用的<b>「光波领域」</b>：一个圆形范围，每隔一段时间从圆心向外推一圈光波，
 * 波前扫到谁、谁才受伤。
 *
 * <h3>为什么要有这一层</h3>
 * <p>「持续伤害范围」在项目里出现过两次（天使 8 圣裁之域、天使 9 终焉救赎），
 * 两次的诉求都一样：<b>视觉的扩散波必须和掉血同速</b> ——
 * 不能「波还没扫到，远处的怪已经掉血」。所以判定不做「每 N tick 全体扣一次」，
 * 而是做<b>环带扫描</b>：本 tick 的波前在 {@code prevRadius → waveRadius} 之间，
 * 水平距离落进这个环带的生物才结算一次。</p>
 *
 * <p>这样还有一个天然好处：<b>同一个波不会重复命中</b>同一个目标，
 * 不需要记「打过谁」的表。</p>
 *
 * <h3>锚点</h3>
 * <p>范围钉死在 {@code center}（不跟人走）—— 领域就该留在原地，
 * 施法者跑出去也不会把领域拖走。</p>
 *
 * <h3>同一个玩家只留一个</h3>
 * <p>重复释放会先收掉旧的，避免两个 20 格范围叠着扫。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SkillWaveField {

    /**
     * 一个领域的全部参数。
     *
     * @param radius       范围半径（格，水平距离）
     * @param damage       每一波对命中的目标造成的伤害（已算好倍率）
     * @param delayTicks   开启后等多久才开始第一波（给"天降巨物砸地"留时间）
     * @param waveTicks    一圈波扩散所需的 tick（= 相邻两波之间的间隔 = 掉血间隔）
     * @param waves        一共推几波
     * @param confine      范围内生物能不能走出去（{@code true} = 拉回边界）
     * @param jingu        命中的目标是否附加禁锢（10 秒）
     * @param waveStyleId  每波开始时发的冲击波样式 id（{@code <0} = 不发）
     * @param hitStyleId   命中时贴在目标身上的特效样式 id（{@code <0} = 不发）
     * @param hitRadius    命中特效的半径（格）
     * @param hitTicks     命中特效的时长（tick）
     * @param particleWave 每波开始时是否发一圈粒子冲击环（粒子版用）
     * @param executeChance 命中时额外掷一次「斩杀」的概率（0 = 不斩；第九魂技用 0.01）
     */
    public record Spec(double radius, float damage,
                       int delayTicks, int waveTicks, int waves,
                       boolean confine, boolean jingu,
                       int waveStyleId, int hitStyleId,
                       float hitRadius, int hitTicks,
                       boolean particleWave, float executeChance) {
    }

    /** 命中时附加的禁锢时长：10 秒。 */
    private static final int JINGU_TICKS = 200;

    private static final List<SkillWaveField> ACTIVE = new ArrayList<>();

    private final ServerLevel level;
    private final ServerPlayer owner;
    private final Vec3 center;
    private final Spec spec;
    private final long firstWaveTick;
    private final long endTick;

    private int waveIndex;
    private long nextWaveTick;
    private double prevRadius;
    private double waveRadius;
    private boolean finished;

    private SkillWaveField(ServerLevel level, ServerPlayer owner, Vec3 center, Spec spec, long now) {
        this.level = level;
        this.owner = owner;
        this.center = center;
        this.spec = spec;
        this.firstWaveTick = now + Math.max(0, spec.delayTicks());
        this.nextWaveTick = firstWaveTick;
        // 最后一波扩散完再多留 1 秒，让视觉上的余辉走完
        this.endTick = firstWaveTick + (long) spec.waveTicks() * Math.max(1, spec.waves()) + 20L;
    }

    /**
     * 在指定点开一个光波领域。同一施法者已有领域时先收掉旧的。
     */
    public static void spawn(ServerPlayer player, ServerLevel level, Vec3 center, Spec spec) {
        long now = level.getGameTime();
        for (Iterator<SkillWaveField> it = ACTIVE.iterator(); it.hasNext(); ) {
            SkillWaveField f = it.next();
            if (f.owner == player) {
                f.finished = true;
                it.remove();
            }
        }
        ACTIVE.add(new SkillWaveField(level, player, center, spec, now));
    }

    // ==================================================================================
    //  心跳
    // ==================================================================================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        for (Iterator<SkillWaveField> it = ACTIVE.iterator(); it.hasNext(); ) {
            SkillWaveField f = it.next();
            f.tick();
            if (f.finished) {
                it.remove();
            }
        }
    }

    private void tick() {
        long now = level.getGameTime();

        if (!owner.isAlive() || owner.isRemoved() || owner.level() != level || now >= endTick) {
            finished = true;
            return;
        }
        if (now < firstWaveTick) {
            return;   // 还在"巨物下落"的等待期
        }

        // 该起新一波了吗
        if (waveIndex < spec.waves() && now >= nextWaveTick) {
            startWave(now);
        }
        // 推进波前（这一 tick 扫过一段环带）
        advanceWave();
        // 边界：每 2 tick 检查一次就够
        if (spec.confine() && (now & 1L) == 0L) {
            confine();
        }
    }

    private void startWave(long now) {
        waveIndex++;
        prevRadius = 0.0D;
        waveRadius = 0.0D;
        nextWaveTick = now + spec.waveTicks();

        if (spec.waveStyleId() >= 0) {
            // 波形视觉和判定同一 tick 起步 → "波扫到哪、哪才掉血"
            SkillFieldFx.atPoint(owner, spec.waveStyleId(),
                    center.x, center.y, center.z,
                    (float) spec.radius(), spec.waveTicks() + 20);
        }
        if (spec.particleWave()) {
            // 粒子版没有"逐帧扩散"的几何，用一圈冲击环示意这一波开始了
            ParticleFx fx = ParticleFx.of(level, owner);
            if (fx != null) {
                fx.budget(900);
                fx.shockRing(ParticleTypes.FIREWORK, center.add(0, 0.1, 0), spec.radius(), 0.8, 64);
            }
        }
    }

    private void advanceWave() {
        if (waveRadius >= spec.radius()) {
            return;
        }
        prevRadius = waveRadius;
        waveRadius = Math.min(spec.radius(), waveRadius + spec.radius() / Math.max(1, spec.waveTicks()));
        pulse(prevRadius, waveRadius);
    }

    /** 结算「水平距离落在 (from, to] 之间」的所有生物。 */
    private void pulse(double from, double to) {
        AABB box = new AABB(center.x - to, center.y - spec.radius() - 4, center.z - to,
                center.x + to, center.y + spec.radius() + 4, center.z + to);
        List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && e.isAlive() && horizontalDistanceSqr(e, center) <= to * to);

        for (LivingEntity target : hits) {
            double d = Math.sqrt(horizontalDistanceSqr(target, center));
            // 第一帧 (from == 0) 要包含圆心上的目标；后续帧排除上一帧已经扫过的
            boolean inBand = from <= 1.0E-4D ? d <= to : (d > from && d <= to);
            if (!inBand) {
                continue;
            }

            BaseSkillItem.dealSkillDamage(target, spec.damage(), level.damageSources().mobAttack(owner));
            if (!target.isAlive()) {
                continue;
            }

            // 斩杀：只对「已经挨过这一波」的目标掷一次，概率给 0 就等于没有
            if (spec.executeChance() > 0.0F && level.random.nextFloat() < spec.executeChance()) {
                // 真伤灌满，绕过攻防重算；不用 kill() 以免跳过死亡结算
                target.hurt(level.damageSources().magic(), target.getHealth() + 10.0F);
                if (!target.isAlive()) {
                    continue;
                }
            }

            if (spec.jingu() && !target.hasEffect(ModEffects.JINGU.get())) {
                target.addEffect(new MobEffectInstance(ModEffects.JINGU.get(), JINGU_TICKS, 0, false, true, true));
            }
            if (spec.hitStyleId() >= 0) {
                // 金色命中特效贴在被打的实体身上
                SkillFieldFx.atEntity(target, spec.hitStyleId(), spec.hitRadius(), spec.hitTicks());
            }
        }
    }

    /** 把跑出范围的生物拉回边界内侧。 */
    private void confine() {
        double r = spec.radius();
        AABB box = new AABB(center.x - r - 4, center.y - r - 8, center.z - r - 4,
                center.x + r + 4, center.y + r + 8, center.z + r + 4);
        List<LivingEntity> outside = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && e.isAlive() && horizontalDistanceSqr(e, center) > r * r);

        for (LivingEntity e : outside) {
            double dx = e.getX() - center.x;
            double dz = e.getZ() - center.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d < 1.0E-4D) {
                continue;
            }
            // 贴回边界内侧 0.15 格，避免下一 tick 又判定为"在外面"而来回抖动
            double k = Math.max(0.0D, (r - 0.15D)) / d;
            e.teleportTo(center.x + dx * k, e.getY(), center.z + dz * k);
        }
    }

    /** 只看水平距离：地形起伏不该让"圈里的怪"被判到圈外。 */
    private static double horizontalDistanceSqr(LivingEntity e, Vec3 center) {
        double dx = e.getX() - center.x;
        double dz = e.getZ() - center.z;
        return dx * dx + dz * dz;
    }
}
