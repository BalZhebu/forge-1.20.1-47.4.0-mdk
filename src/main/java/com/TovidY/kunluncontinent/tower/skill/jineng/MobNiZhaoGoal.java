package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class MobNiZhaoGoal extends Goal {
    private final Mob mob;
    /** 词条等级（1~10），决定减速强度与时长。 */
    private final int level;
    private int cooldownTicks = 0; // 技能冷却 Tick 计数

    public MobNiZhaoGoal(Mob mob) {
        this(mob, 1);
    }

    public MobNiZhaoGoal(Mob mob, int level) {
        this.mob = mob;
        this.level = Math.max(1, Math.min(com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.MAX_LEVEL, level));
        // 标记此 AI 影响怪物的移动和看向（防止释放技能时发生逻辑冲突）
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * 每 tick 都会检查是否符合释放技能的初始条件
     */
    @Override
    public boolean canUse() {
        // 如果冷却还没到，只进行倒计时，不触发技能检查
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 怪没活着、或者没进入战斗（没有攻击目标），不释放
        if (mob == null || !mob.isAlive() || mob.getTarget() == null) {
            return false;
        }

        // 检查目标是不是活着的玩家，并且距离不能太远（12格范围内）
        LivingEntity target = mob.getTarget();
        return target instanceof Player && target.isAlive() && mob.distanceToSqr(target) <= 144.0D;
    }

    /**
     * 满足 canUse 条件后，立刻在第一 Tick 执行此方法
     */
    @Override
    public void start() {
        // 1. 进入 15 秒冷却时间 (15秒 * 20 Tick = 300 Tick)
        cooldownTicks = 15 * 20;

        // 2. 触发 3D 飘字通知（带词条等级）
        TowerSkillPool.ShieldActiveSkillNotify(mob, "泥沼", level);

        // 3. 施法范围判定：以怪物为中心，拉出一个半径 8 格的立方体检测区域
        AABB area = mob.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        List<Player> nearbyPlayers = mob.level().getEntitiesOfClass(Player.class, area);

        // 4. 让范围内的所有玩家陷入泥沼。
        //    减速强度：1级 = 缓慢III（-45%），等级每 +1 强度 +1，最高缓慢V；
        //    时长随等级系数（1级 6 秒，10级约 11.4 秒）。
        int slowAmp = Math.min(level + 1, 4);
        int duration = Math.round(6 * 20 * com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.levelMultiplier(level));
        for (Player player : nearbyPlayers) {
            if (player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        duration,
                        slowAmp,
                        false,  // 不是环境效果
                        true    // 显示粒子，让玩家感知到脚下泥沼的特效
                ));
            }
        }

        // 5. 播放恶劣环境音效（施法反馈）
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.GRAVEL_BREAK,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 0.6F);
    }

    /**
     * 持续执行条件（因为是一次性瞬发的大招，释放完立即结束）
     */
    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
