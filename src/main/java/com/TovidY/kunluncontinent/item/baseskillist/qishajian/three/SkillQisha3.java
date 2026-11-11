package com.TovidY.kunluncontinent.item.baseskillist.qishajian.three;

import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaFx;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * 第三魂技 · 七杀步
 *
 * <p>向视线方向<b>冲刺 12 格</b>（超过 10 格），路径上的敌人受伤并被减速；
 * 冲刺时留下一串暗红残影。</p>
 *
 * <p>位移用 {@code teleportTo} 而不是给速度：给速度的话 12 格要飞好几 tick，
 * 中途会被地形/实体挡掉，落点完全不可控。这里先探测终点是否安全再瞬移。</p>
 */
public class SkillQisha3 extends BaseSkillItem {

    private static final double DASH_DISTANCE = 12.0;
    private static final double HIT_RADIUS = 1.8;

    @Override
    public float getBaseCost() { return 90f; }

    @Override
    public float getDamageMultiplier() { return 1.9f; }

    @Override
    public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.three.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        Vec3 look = player.getLookAngle();
        Vec3 start = player.position();

        // 1. 找落点：从最远处往回退，第一个"脚下有支撑且身体能站"的点
        Vec3 end = start;
        for (double d = DASH_DISTANCE; d >= 2.0; d -= 1.0) {
            Vec3 candidate = start.add(look.x * d, 0, look.z * d);
            if (isSafe(level, candidate)) {
                end = candidate;
                break;
            }
        }
        if (end.distanceToSqr(start) < 1.0) {
            return;   // 前方完全堵死，不浪费精神力
        }
        player.teleportTo(end.x, end.y, end.z);
        player.fallDistance = 0.0f;
        player.hurtMarked = true;

        // 2. 路径伤害
        double total = start.distanceTo(end);
        int steps = (int) Math.ceil(total);
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.add(end.subtract(start).scale(i / (double) steps));
            List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(point.x - HIT_RADIUS, point.y - 1.2, point.z - HIT_RADIUS,
                            point.x + HIT_RADIUS, point.y + 2.2, point.z + HIT_RADIUS),
                    e -> e != player && e.isAlive());
            for (LivingEntity target : hits) {
                dealSkillDamage(target, finalDamage, player.damageSources().playerAttack(player));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
            }
        }

        // 3. 表现：着色器 → 沿路径铺一串爆点当残影；粒子 → 环 + 拖尾
        boolean shader = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
        if (shader) {
            ServerPlayer sp = (ServerPlayer) player;
            // ⚠️ 之前只发爆点（贴在身体高度、且很小），第三人称几乎看不见。
            //    现在沿途竖 10 根光柱 + 地面冲击环，整条路径从外面看是一条暗红光带。
            int ghosts = 10;
            for (int i = 1; i <= ghosts; i++) {
                Vec3 point = start.add(end.subtract(start).scale(i / (double) ghosts));
                QishaFx.field(sp, SkillEffectStyles.BEAM_QISHA_PILLAR,
                        point.x, point.y, point.z, 1.1F, 14);
                QishaFx.field(sp, SkillEffectStyles.BURST_QISHA,
                        point.x, point.y + 1.0, point.z, 1.3F, 10);
            }
            // 起点与终点各一圈冲击环，标记冲刺两端
            QishaFx.field(sp, SkillEffectStyles.SHOCK_QISHA, start.x, start.y + 0.1, start.z, 3.0F, 14);
            QishaFx.field(sp, SkillEffectStyles.SHOCK_QISHA, end.x, end.y + 0.1, end.z, 3.0F, 14);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                fx.budget(1600);
                // 沿途 10 根竖向光柱：地面到头顶两格，第三人称也能看清整条轨迹
                for (int i = 1; i <= 10; i++) {
                    Vec3 foot = start.add(end.subtract(start).scale(i / 10.0));
                    fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.CRIMSON_SPORE,
                            foot, 0.35, 3.4, 3, i * 0.5, ParticleFx.TAU, 0.9);
                    fx.circle(ParticleTypes.SOUL, foot.add(0, 0.15, 0), ParticleFx.Axis.Y, 0.7, 10, i * 0.7, 0.05);
                    fx.burst(ParticleTypes.CRIMSON_SPORE, foot.add(0, 1.2, 0), 6, 0.3, true);
                }
                // 起终点冲击环 + 贯通全身的拖尾
                fx.shockRing(ParticleTypes.SOUL, start.add(0, 0.1, 0), 2.6, 1.6, 32);
                fx.shockRing(ParticleTypes.SOUL, end.add(0, 0.1, 0), 3.2, 1.8, 36);
                fx.line(ParticleTypes.CRIMSON_SPORE, start.add(0, 0.9, 0), end.add(0, 0.9, 0), 0.18);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.4f);
    }

    /** 落点是否安全：身体两格非固体、脚下一格有东西撑着（或本来就悬空在飞也行）。 */
    private boolean isSafe(Level level, Vec3 pos) {
        var below = net.minecraft.core.BlockPos.containing(pos.x, pos.y - 0.2, pos.z);
        var feet = net.minecraft.core.BlockPos.containing(pos.x, pos.y + 0.1, pos.z);
        var head = net.minecraft.core.BlockPos.containing(pos.x, pos.y + 1.1, pos.z);
        var stateBelow = level.getBlockState(below);
        if (stateBelow.getCollisionShape(level, below).isEmpty()) {
            return false;   // 踩空
        }
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty();
    }
}
