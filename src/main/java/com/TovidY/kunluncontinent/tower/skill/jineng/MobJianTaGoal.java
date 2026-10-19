package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * 主动词条【践踏】：朝目标跃起，落地瞬间以自身为圆心砸出一圈冲击波。
 *
 * <p><b>为什么是"跃起 + 定时落地"而不是真的等物理下落：</b>
 * AI Goal 里没法保证怪物一定会踩到地面（可能跃上台阶、卡在栅栏上），
 * 等 {@code onGround()} 会让技能偶尔"永远不落地"。这里固定
 * {@link #AIR_TICKS} 帧后结算，位置取当时的实际坐标，落到哪算哪，
 * 手感上依然是一次完整的起跳—砸地。</p>
 *
 * <p><b>绝不破坏方块：</b>冲击波只是 {@code ParticleTypes.EXPLOSION} 粒子 +
 * 击退 + 缓慢状态，没有 {@code Explosion} 实体，也不写任何 {@code setBlock}。</p>
 */
public class MobJianTaGoal extends Goal {

    private final Mob mob;
    /** 词条等级（1~10），决定伤害、击退与冷却。 */
    private final int level;
    private int cooldownTicks = 0;

    /** 起跳后多少帧结算砸地。 */
    private static final int AIR_TICKS = 12;

    private int airTicks = 0;
    private boolean impacted = false;

    public MobJianTaGoal(Mob mob) {
        this(mob, 1);
    }

    public MobJianTaGoal(Mob mob, int level) {
        this.mob = mob;
        this.level = Math.max(1, Math.min(TowerSkillPool.MAX_LEVEL, level));
        // 起跳会接管移动与跳跃
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }
        if (!mob.isAlive()) return false;
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        // 2~12 格才值得起跳（太近浪费、太远砸不到）
        double dist = mob.distanceToSqr(target);
        return dist >= 4.0D && dist <= 144.0D;
    }

    @Override
    public void start() {
        // 冷却随等级缩短：基础 18 秒 ÷ 等级系数，最低 6 秒
        cooldownTicks = Math.max(6 * 20, Math.round(18 * 20 / TowerSkillPool.levelMultiplier(level)));
        airTicks = AIR_TICKS;
        impacted = false;

        TowerSkillPool.ShieldActiveSkillNotify(mob, "践踏", level);

        LivingEntity target = mob.getTarget();
        if (target != null) {
            Vec3 dir = new Vec3(target.getX() - mob.getX(), 0.0D, target.getZ() - mob.getZ());
            if (dir.lengthSqr() > 1.0E-4D) {
                dir = dir.normalize().scale(0.55D);
            }
            mob.setDeltaMovement(dir.x, 0.62D, dir.z);
            mob.hurtMarked = true;
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.2F, 0.8F);
    }

    @Override
    public void tick() {
        if (impacted) return;
        if (--airTicks <= 0) {
            impacted = true;
            impact();
        }
    }

    /** 落地结算：范围伤害 + 击退 + 缓慢。 */
    private void impact() {
        float mult = TowerSkillPool.levelMultiplier(level);
        float damage = ModAttributeAPI.getGongji(mob) * 0.45F * mult;

        AABB area = mob.getBoundingBox().inflate(4.5D, 3.0D, 4.5D);
        List<Player> players = mob.level().getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;

            Vec3 away = new Vec3(player.getX() - mob.getX(), 0.0D, player.getZ() - mob.getZ());
            double len = away.length();
            Vec3 push = len > 1.0E-4D ? away.scale(0.9D / len) : Vec3.ZERO;
            // 直接改速度并标记同步，比 knockback() 更适合"以怪物为中心"的冲击波
            player.setDeltaMovement(push.x, 0.45D, push.z);
            player.hurtMarked = true;

            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    Math.round(60 * mult), Math.min(2, level / 4), false, true));

            player.hurt(mob.damageSources().mobAttack(mob), damage);
        }

        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    mob.getX(), mob.getY() + 0.2D, mob.getZ(), 6, 1.6D, 0.2D, 1.6D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    mob.getX(), mob.getY() + 0.3D, mob.getZ(), 30, 2.2D, 0.3D, 2.2D, 0.05D);
        }
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2F, 0.7F);
    }

    @Override
    public boolean canContinueToUse() {
        return !impacted && airTicks > 0;
    }
}
