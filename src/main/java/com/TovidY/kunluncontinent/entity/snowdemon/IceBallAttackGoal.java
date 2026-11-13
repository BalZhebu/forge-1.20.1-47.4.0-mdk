package com.TovidY.kunluncontinent.entity.snowdemon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * 雪魔的<b>远程攻击</b>：近战够不着（玩家飞起来 / 站在高处 / 距离太远）时扔冰球。
 *
 * <h3>为什么需要它</h3>
 * <p>以前雪魔靠 {@code breakEnvironment()} 把脚下的方块砸掉来"抓"飞行玩家 ——
 * 那既是赖皮打法（破坏地形），又治不了真正飞高的人。现在改成远程弹射物。</p>
 *
 * <h3>触发条件</h3>
 * <ul>
 *   <li>有仇恨目标，且目标存活；</li>
 *   <li>水平距离 &gt; {@link #MELEE_RANGE}（近战够得着时不扔，避免两套攻击打架）；</li>
 *   <li>距离 ≤ {@link #MAX_RANGE}；</li>
 *   <li>高度差 &gt; {@value #FLY_HEIGHT} 也当作"够不着"（玩家飞起来的情形）。</li>
 * </ul>
 *
 * <p>节奏：每 {@link #INTERVAL} tick（0.8 秒）一颗，和普通攻击共用同一套挥手动画。</p>
 */
public class IceBallAttackGoal extends Goal {

    /** 近战"够得着"的距离：超过它才启用远程。 */
    private static final double MELEE_RANGE = 5.0D;
    /** 最大射程。 */
    private static final double MAX_RANGE = 32.0D;
    /** 高度差超过这个就当玩家飞起来了。 */
    private static final double FLY_HEIGHT = 2.5D;
    /** 发射间隔（0.8 秒 = 16 tick）。 */
    private static final int INTERVAL = 16;
    /** 冰球初速（格/tick）。 */
    private static final float BALL_SPEED = 1.9F;

    private final SnowDemonEntity demon;
    private int cooldown;

    public IceBallAttackGoal(SnowDemonEntity demon) {
        this.demon = demon;
        this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.demon.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double distSqr = this.demon.distanceToSqr(target);
        if (distSqr > MAX_RANGE * MAX_RANGE) {
            return false;
        }
        double dy = target.getY() - this.demon.getY();
        // 人在高处 / 已经飞出近战范围 → 用冰球
        return distSqr > MELEE_RANGE * MELEE_RANGE || dy > FLY_HEIGHT;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        // 起手第一颗略快一点，避免"仇恨了但站着不动"
        this.cooldown = 6;
    }

    @Override
    public void stop() {
        this.cooldown = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.demon.getTarget();
        if (target == null) {
            return;
        }
        this.demon.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (--this.cooldown > 0) {
            return;
        }
        this.cooldown = INTERVAL;
        throwBall(target);
    }

    private void throwBall(LivingEntity target) {
        IceBallEntity ball = new IceBallEntity(this.demon.level(), this.demon);
        double spawnY = this.demon.getEyeY() - 0.8D;
        ball.setPos(this.demon.getX(), spawnY, this.demon.getZ());

        double dx = target.getX() - this.demon.getX();
        double dy = target.getEyeY() - spawnY;
        double dz = target.getZ() - this.demon.getZ();
        ball.shoot(dx, dy, dz, BALL_SPEED, 0.8F);

        this.demon.level().addFreshEntity(ball);
        // 和近战同一套挥手动画
        this.demon.playAttackAnimation();
        this.demon.level().playSound(null, this.demon.getX(), this.demon.getY(), this.demon.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0F, 0.8F);
    }
}
