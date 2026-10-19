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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * 主动词条【冰封】：寒潮压境，范围内的玩家行动与挖掘同时变慢。
 *
 * <p><b>只给状态，不放冰、不放雪、不结霜</b> —— 本词条绝不改动任何方块，
 * 冰面/积雪这类"看起来更帅"的表现一律不做（那属于破坏/篡改地形）。</p>
 *
 * <p>强度映射：缓慢 1 级 = II，每 3 级 +1，最高 V；
 * 挖掘疲劳 1 级 = I，每 4 级 +1，最高 III。</p>
 */
public class MobBingFengGoal extends Goal {

    private final Mob mob;
    /** 词条等级（1~10），决定控制强度、时长与冷却。 */
    private final int level;
    private int cooldownTicks = 0;

    public MobBingFengGoal(Mob mob) {
        this(mob, 1);
    }

    public MobBingFengGoal(Mob mob, int level) {
        this.mob = mob;
        this.level = Math.max(1, Math.min(TowerSkillPool.MAX_LEVEL, level));
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }
        if (!mob.isAlive()) return false;
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.distanceToSqr(target) <= 256.0D;
    }

    @Override
    public void start() {
        // 冷却随等级缩短：基础 22 秒 ÷ 等级系数，最低 8 秒
        cooldownTicks = Math.max(8 * 20, Math.round(22 * 20 / TowerSkillPool.levelMultiplier(level)));

        TowerSkillPool.ShieldActiveSkillNotify(mob, "冰封", level);

        float mult = TowerSkillPool.levelMultiplier(level);
        int duration = Math.round(100 * mult);                       // 基础 5 秒
        int slowAmp = Math.min(4, 1 + level / 3);                    // II ~ V
        int fatigueAmp = Math.min(2, level / 4);                     // I ~ III

        AABB area = mob.getBoundingBox().inflate(8.0D, 5.0D, 8.0D);
        List<Player> players = mob.level().getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
            if (player.distanceToSqr(mob) > 64.0D) continue;         // 球形判定，半径 8

            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slowAmp, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, fatigueAmp, false, true));

            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.5D, 0.8D, 0.5D, 0.02D);
            }
        }

        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    40, 4.0D, 2.0D, 4.0D, 0.01D);
        }
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.4F, 0.6F);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
