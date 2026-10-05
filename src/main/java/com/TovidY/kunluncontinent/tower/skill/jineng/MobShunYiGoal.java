package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.Random;

public class MobShunYiGoal extends Goal {
    private final Mob mob;
    /** 词条等级（1~10），等级越高冷却越短。 */
    private final int level;
    private int cooldownTicks = 0;
    private final Random random = new Random();

    public MobShunYiGoal(Mob mob) {
        this(mob, 1);
    }

    public MobShunYiGoal(Mob mob, int level) {
        this.mob = mob;
        this.level = Math.max(1, Math.min(com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.MAX_LEVEL, level));
        // 标记此 AI 会打断怪物的移动（传送瞬间需要站定）
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * 每 tick 检查是否可以触发瞬移
     */
    @Override
    public boolean canUse() {
        // 10秒冷却倒计时
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 怪活着且处于战斗状态（有攻击目标）时才触发保命/身法瞬移
        return mob != null && mob.isAlive() && mob.getTarget() != null && mob.getTarget().isAlive();
    }

    /**
     * 满足条件，开始施展身法
     */
    @Override
    public void start() {
        // 1. 冷却随等级缩短：基础 10 秒 ÷ 等级系数（1级 10秒，10级约 5.3 秒），最低 4 秒
        cooldownTicks = Math.max(4 * 20,
                Math.round(10 * 20 / com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.levelMultiplier(level)));

        // 2. 触发 3D 飘字提示（带词条等级）
        TowerSkillPool.ShieldActiveSkillNotify(mob, "瞬移", level);

        // 3. 计算 20格直径（半径10格）内的随机坐标
        // random.nextInt(21) - 10 产生 -10 到 +10 的随机数
        double randomX = mob.getX() + (random.nextInt(21) - 10);
        double randomZ = mob.getZ() + (random.nextInt(21) - 10);

        // 高度处理：同一格（+0）到 +3格 高度，这里产生 0~3 的随机偏移
        double randomY = mob.getY() + random.nextInt(2);

        // 4. 安全校验：确保瞬移目的地不是实心方块，防止怪物卡墙窒息
        BlockPos targetPos = BlockPos.containing(randomX, randomY, randomZ);
        BlockState blockState = mob.level().getBlockState(targetPos);

        // 如果目标点不幸是实心方块，往上提，直到找到空气或者最多尝试 3 格
        int attempts = 0;
        while (blockState.isSolid() && attempts < 3) {
            targetPos = targetPos.above();
            blockState = mob.level().getBlockState(targetPos);
            randomY += 1.0D;
            attempts++;
        }

        // 5. 执行空间传送
        // 先在原地播放传送前音效
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);

        // 物理位置瞬移
        mob.teleportTo(randomX, randomY, randomZ);

        // 在新目的地播放传送后音效和粒子反馈（末影人传送粒子）
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);

        if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    20, 0.5D, 0.5D, 0.5D, 0.1D);
        }

        // 传送后立刻让怪物重新看向它的敌人，保持战斗姿态
        if (mob.getTarget() != null) {
            mob.getLookControl().setLookAt(mob.getTarget(), 30.0F, 30.0F);
        }
    }

    /**
     * 瞬发技能，不需要持续引导
     */
    @Override
    public boolean canContinueToUse() {
        return false;
    }
}