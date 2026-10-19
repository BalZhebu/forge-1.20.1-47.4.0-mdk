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
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * 主动词条【缚魂】：把周围玩家朝自己拽过来。
 *
 * <p>拉拽用「给玩家一个朝向怪物的速度 + {@code hurtMarked}」实现 ——
 * 服务端改完速度必须标记同步，否则客户端不认这个冲量，玩家看起来完全没被拉。
 * 刻意<b>不用</b> {@code teleportTo}：直接传送玩家会有跨区块/卡墙风险，
 * 而且对玩家来说手感非常糟。</p>
 *
 * <p>不涉及任何方块操作。</p>
 */
public class MobFuHunGoal extends Goal {

    private final Mob mob;
    /** 词条等级（1~10），决定拉拽力度、减速时长与冷却。 */
    private final int level;
    private int cooldownTicks = 0;

    public MobFuHunGoal(Mob mob) {
        this(mob, 1);
    }

    public MobFuHunGoal(Mob mob, int level) {
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
        if (target == null || !target.isAlive()) return false;
        // 3~14 格：太近没必要拉，太远拉不动
        double dist = mob.distanceToSqr(target);
        return dist >= 9.0D && dist <= 196.0D;
    }

    @Override
    public void start() {
        // 冷却随等级缩短：基础 20 秒 ÷ 等级系数，最低 7 秒
        cooldownTicks = Math.max(7 * 20, Math.round(20 * 20 / TowerSkillPool.levelMultiplier(level)));

        TowerSkillPool.ShieldActiveSkillNotify(mob, "缚魂", level);

        double power = Math.min(2.0D, 0.7D + 0.05D * level);
        int slowDuration = Math.round(40 * TowerSkillPool.levelMultiplier(level));

        AABB area = mob.getBoundingBox().inflate(12.0D, 6.0D, 12.0D);
        List<Player> players = mob.level().getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
            if (player.distanceToSqr(mob) > 144.0D) continue;   // AABB 是方的，再补一次球形判定

            Vec3 toMob = new Vec3(mob.getX() - player.getX(), 0.0D, mob.getZ() - player.getZ());
            if (toMob.lengthSqr() < 1.0E-4D) continue;
            Vec3 pull = toMob.normalize().scale(power);

            player.setDeltaMovement(pull.x, 0.35D, pull.z);
            player.hurtMarked = true;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    slowDuration, Math.min(2, level / 4), false, true));

            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL,
                        player.getX(), player.getY() + 1.0D, player.getZ(), 8, 0.3D, 0.5D, 0.3D, 0.02D);
            }
        }

        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.5F, 0.6F);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
