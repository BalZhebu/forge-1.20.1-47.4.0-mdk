package com.TovidY.kunluncontinent.tower.skill.jineng;

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

import java.util.EnumSet;

/**
 * 主动词条【玄甲】：临战时给自己套一层护体罡气，短时间大幅减伤。
 *
 * <p>减伤直接用原版 {@code DAMAGE_RESISTANCE}（每级 20%，封顶 60%），
 * 而不是自己再造一个减伤通道 —— 原版的抗性是在 {@code LivingHurtEvent}
 * <b>之后</b>才结算的，不会和本 mod 的伤害公式互相覆盖。</p>
 *
 * <p>等级映射：1~3 级 = 抗性 I（20%），4~6 级 = II（40%），7~9 级 = III（50%），
 * 10 级 = IV（60%）。时长 8 秒 × 等级系数。</p>
 */
public class MobXuanJiaGoal extends Goal {

    private final Mob mob;
    /** 词条等级（1~10），决定减伤强度与时长。 */
    private final int level;
    private int cooldownTicks = 0;
    private int buffTicks = 0;

    public MobXuanJiaGoal(Mob mob) {
        this(mob, 1);
    }

    public MobXuanJiaGoal(Mob mob, int level) {
        this.mob = mob;
        this.level = Math.max(1, Math.min(TowerSkillPool.MAX_LEVEL, level));
        // 纯自身增益，不锁移动/看向，免得和近战追击打架
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }
        if (!mob.isAlive() || buffTicks > 0) return false;
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        // 只有被人贴脸打的时候才值得开（避免一进战斗就浪费）
        return mob.distanceToSqr(target) <= 100.0D;
    }

    @Override
    public void start() {
        cooldownTicks = Math.max(10 * 20, Math.round(30 * 20 / TowerSkillPool.levelMultiplier(level)));

        TowerSkillPool.ShieldActiveSkillNotify(mob, "玄甲", level);

        int amplifier = Math.min(3, (level - 1) / 3);
        buffTicks = Math.round(8 * 20 * TowerSkillPool.levelMultiplier(level));
        mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, buffTicks, amplifier, false, true));

        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    25, 0.6D, 0.9D, 0.6D, 0.05D);
        }
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    @Override
    public void tick() {
        if (buffTicks > 0) {
            buffTicks--;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return buffTicks > 0;
    }

    @Override
    public void stop() {
        buffTicks = 0;
    }
}
