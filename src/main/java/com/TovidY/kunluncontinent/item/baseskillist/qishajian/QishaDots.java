package com.TovidY.kunluncontinent.item.baseskillist.qishajian;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 七杀剑的<b>持续性伤害调度器</b>（第 6 / 8 / 9 魂技共用）。
 *
 * <p>原版没有"每 N tick 打一次"的通用件，这里用一个静态表存所有正在生效的伤害源：
 * 每条记录 = 目标（或"以施法者为中心的场域"）+ 间隔 + 剩余时长 + 每次伤害。</p>
 *
 * <h3>为什么不用玩家 NBT 计时</h3>
 * <p>第 8 / 9 魂技的目标是<b>别人</b>，玩家掉线或换维度不能把伤害中断掉；
 * 而且一个玩家可能同时挂着三个魂技，NBT 每个键写一套会互相踩。
 * 静态表 + 每条独立 record 最简单。</p>
 *
 * <h3>性能</h3>
 * <p>每 tick 只遍历 ACTIVE（正常 0~3 条）；为 0 时第一行就 return。
 * 离开世界（ServerStoppedEvent）清空，避免跨存档泄漏。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID)
public final class QishaDots {

    /** 一条持续伤害。 */
    private static final class Dot {
        final UUID casterId;
        /** null = 以施法者为圆心的场域（第 6 魂技）。 */
        final UUID targetId;
        final float damagePerPulse;
        final float areaRadius;
        final int interval;
        int remainingTicks;
        int timer;
        final int pulseStyle;
        final float pulseRadius;
        /** 每跳额外降下的剑影样式（0 = 不降）。 */
        final int fallStyle;

        Dot(UUID casterId, UUID targetId, float damagePerPulse, float areaRadius,
            int interval, int durationTicks, int pulseStyle, float pulseRadius, int fallStyle) {
            this.casterId = casterId;
            this.targetId = targetId;
            this.damagePerPulse = damagePerPulse;
            this.areaRadius = areaRadius;
            this.interval = Math.max(1, interval);
            this.remainingTicks = durationTicks;
            this.timer = 0;
            this.pulseStyle = pulseStyle;
            this.pulseRadius = pulseRadius;
            this.fallStyle = fallStyle;
        }
    }

    private static final List<Dot> ACTIVE = new ArrayList<>();

    /** 延迟执行的一次性动作（记录可变，所以不用 record）。 */
    private static final class Delayed {
        final UUID casterId;
        int ticksLeft;
        final Consumer<ServerPlayer> action;

        Delayed(UUID casterId, int ticksLeft, Consumer<ServerPlayer> action) {
            this.casterId = casterId;
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }

    private static final List<Delayed> DELAYED = new ArrayList<>();

    /**
     * 延迟 {@code ticks} 后执行一次动作（用于"法阵先成型、伤害稍后再炸"）。
     *
     * <p>和 {@link #ACTIVE} 共用同一个 tick 循环，所以不需要额外的监听器；
     * 施法者掉线时这条直接丢弃。</p>
     */
    public static void delay(ServerPlayer caster, int ticks, Consumer<ServerPlayer> action) {
        if (caster == null || action == null) {
            return;
        }
        DELAYED.add(new Delayed(caster.getUUID(), Math.max(0, ticks), action));
    }

    private QishaDots() {
    }

    /**
     * 对单个目标持续造成伤害。
     *
     * @param damagePerPulse 每次脉冲的伤害（已经算好）
     * @param interval       脉冲间隔（tick）
     * @param duration       总时长（tick）
     * @param pulseStyle     每次脉冲的着色器样式（0 = 不发光效）
     */
    public static void onTarget(ServerPlayer caster, LivingEntity target, float damagePerPulse,
                                int interval, int duration, int pulseStyle, float pulseRadius) {
        onTarget(caster, target, damagePerPulse, interval, duration, pulseStyle, pulseRadius, 0);
    }

    /** 同上，但每次脉冲额外在该目标头顶降一把剑（{@code fallStyle}）。 */
    public static void onTarget(ServerPlayer caster, LivingEntity target, float damagePerPulse,
                                int interval, int duration, int pulseStyle, float pulseRadius,
                                int fallStyle) {
        if (caster == null || target == null || !target.isAlive()) {
            return;
        }
        ACTIVE.add(new Dot(caster.getUUID(), target.getUUID(), damagePerPulse, 0f,
                interval, duration, pulseStyle, pulseRadius, fallStyle));
    }

    /** 以施法者为圆心的场域伤害（第 6 魂技）。 */
    public static void onArea(ServerPlayer caster, float radius, float damagePerPulse,
                              int interval, int duration, int pulseStyle, float pulseRadius) {
        onArea(caster, radius, damagePerPulse, interval, duration, pulseStyle, pulseRadius, 0);
    }

    /** 同上，每次脉冲对每个被命中的目标额外降一把剑。 */
    public static void onArea(ServerPlayer caster, float radius, float damagePerPulse,
                              int interval, int duration, int pulseStyle, float pulseRadius,
                              int fallStyle) {
        if (caster == null) {
            return;
        }
        ACTIVE.add(new Dot(caster.getUUID(), null, damagePerPulse, radius,
                interval, duration, pulseStyle, pulseRadius, fallStyle));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (DELAYED.isEmpty() && ACTIVE.isEmpty()) {
            return;
        }

        // 先跑延迟任务（法阵成型后的那一击）
        if (!DELAYED.isEmpty()) {
            Iterator<Delayed> di = DELAYED.iterator();
            while (di.hasNext()) {
                Delayed d = di.next();
                if (--d.ticksLeft > 0) {
                    continue;
                }
                di.remove();
                ServerPlayer caster = findPlayer(d.casterId);
                if (caster != null) {
                    d.action.accept(caster);
                }
            }
        }
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Dot> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Dot dot = it.next();
            dot.remainingTicks--;
            dot.timer++;
            if (dot.timer >= dot.interval) {
                dot.timer = 0;
                if (!pulse(dot)) {
                    it.remove();
                    continue;
                }
            }
            if (dot.remainingTicks <= 0) {
                it.remove();
            }
        }
    }

    /** @return false = 施法者/目标已经不在了，这条记录作废 */
    private static boolean pulse(Dot dot) {
        ServerPlayer caster = findPlayer(dot.casterId);
        if (caster == null || !(caster.level() instanceof ServerLevel level)) {
            return false;
        }

        if (dot.targetId != null) {
            var entity = level.getEntity(dot.targetId);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
                return false;
            }
            BaseSkillItem.dealSkillDamage(target, dot.damagePerPulse,
                    level.damageSources().indirectMagic(caster, caster));
            emitPulse(caster, level, target.position().add(0, target.getBbHeight() * 0.6, 0), dot);
            return true;
        }

        // 场域：以施法者为中心
        List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(caster.position(), caster.position()).inflate(dot.areaRadius),
                e -> e != caster && e.isAlive());
        for (LivingEntity target : hits) {
            BaseSkillItem.dealSkillDamage(target, dot.damagePerPulse,
                    level.damageSources().indirectMagic(caster, caster));
            emitPulse(caster, level, target.position().add(0, target.getBbHeight() * 0.6, 0), dot);
        }
        return true;
    }

    /** 每次脉冲的表现：着色器模式发一个爆点，粒子模式发一小簇粒子。 */
    private static void emitPulse(ServerPlayer caster, ServerLevel level, Vec3 at, Dot dot) {
        if (SkillFxToggle.useShader(caster)) {
            if (dot.pulseStyle > 0) {
                QishaFx.field(caster, dot.pulseStyle, at.x, at.y, at.z, dot.pulseRadius, 8);
            }
            if (dot.fallStyle > 0) {
                // 剑从天而降打在<b>被攻击者</b>身上（不是在施法者身上）
                QishaFx.field(caster, dot.fallStyle, at.x, at.y - 0.6, at.z, 1.6F, 10);
            }
            return;
        }
        ParticleFx fx = ParticleFx.of(level, caster);
        if (fx != null) {
            fx.budget(420);
            // 一柄剑光从上方扎下来打在<b>被命中者</b>身上
            fx.line(ParticleTypes.SOUL_FIRE_FLAME, at.add(0, 6.0, 0), at, 0.22);
            fx.line(ParticleTypes.CRIMSON_SPORE, at.add(0.15, 6.0, 0.15), at, 0.30);
            fx.circle(ParticleTypes.SOUL, at, ParticleFx.Axis.Y, 0.9, 12, 0, 0.04);
            fx.burst(ParticleTypes.CRIMSON_SPORE, at, 16, 0.6, true);
            fx.burst(ParticleTypes.SOUL, at, 8, 0.4, true);
        }
    }

    private static ServerPlayer findPlayer(UUID id) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(id);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
        DELAYED.clear();
    }
}
