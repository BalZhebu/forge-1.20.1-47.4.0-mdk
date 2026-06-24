package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.render.DamageIndicatorRenderer;
import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class MobHuiFuGoal extends Goal {
    private final Mob mob;
    private int cooldownTicks = 0;

    public MobHuiFuGoal(Mob mob) {
        this.mob = mob;
        // 恢复生命是瞬发治愈，不需要锁死移动或看向
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    /**
     * 每 tick 检查是否可以触发恢复
     */
    @Override
    public boolean canUse() {
        // 1. 25秒冷却倒计时 (25秒 * 20 Tick = 500 Tick)
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 2. 基本条件：怪活着且正在战斗
        if (mob == null || !mob.isAlive() || mob.getTarget() == null || !mob.getTarget().isAlive()) {
            return false;
        }

        // 3. 智能判定：只有当怪物的当前血量低于其最大血量的 85% 时，才允许释放（防止满血或健康状态下白白浪费技能）
        return mob.getHealth() < (mob.getMaxHealth() * 0.85F);
    }

    /**
     * 满足条件，运转功法恢复气血
     */
    @Override
    public void start() {
        // 进入 25 秒冷却
        cooldownTicks = 25 * 20;

        // 1. 触发 3D 飘字提示（顶出 "§4§l✨ 恢复！ ✨"）
        TowerSkillPool.ShieldActiveSkillNotify(mob, "恢复");

        // 2. 核心：通过你的 Capability 系统动态计算并给予治愈
        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // 获取怪物此时此刻的最终最大生命值上限（完美包容了年限基础血量与【浑厚】加成后的最终值）
            float maxHp = cap.getMaxshengming();

            // 计算 20% 的恢复气血量
            float healAmount = maxHp * 0.20f;

            // 使用原生的 heal 方法进行安全治疗（原生系统会自动截断，确保不会超过最大生命值上限）
            mob.heal(healAmount);

            // 同时更新你 Capability 内部记录的当前生命值（保持数据同步）
            cap.setShengming(mob.getHealth());

            // 顺便触发一个好玩的视觉效果：在怪物头顶飘出一个绿色的恢复数字（比如 "+1500.0"）
            String healStr = "+" + String.format("%.1f", healAmount);
            net.minecraft.world.phys.Vec3 mobPos = new net.minecraft.world.phys.Vec3(mob.getX(), mob.getY() + mob.getBbHeight() + 0.2D, mob.getZ());
            DamageIndicatorRenderer.addIndicator(healStr, 0xFF00FF00, mobPos); // 0xFF00FF00 为纯绿色
        });

        // 3. 播放充满生机的治愈音效
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 1.2F);

        // 4. 召唤绿色生命粒子特效
        if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    15, 0.4D, 0.6D, 0.4D, 0.05D);
        }
    }

    /**
     * 瞬发技能，不需要持续执行
     */
    @Override
    public boolean canContinueToUse() {
        return false;
    }
}