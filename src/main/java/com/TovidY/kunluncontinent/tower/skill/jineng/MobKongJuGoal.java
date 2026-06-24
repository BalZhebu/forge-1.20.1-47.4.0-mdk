package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class MobKongJuGoal extends Goal {
    private final Mob mob;
    private int cooldownTicks = 0;

    public MobKongJuGoal(Mob mob) {
        this.mob = mob;
        // 咆哮施法时施加一瞬间的动作关注
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    /**
     * 每 tick 检查是否可以触发恐惧
     */
    @Override
    public boolean canUse() {
        // 20秒冷却倒计时 (20秒 * 20 Tick = 400 Tick)
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 怪活着且处于战斗状态（有攻击目标）时才触发
        return mob != null && mob.isAlive() && mob.getTarget() != null && mob.getTarget().isAlive();
    }

    /**
     * 满足条件，释放恐惧气场
     */
    @Override
    public void start() {
        // 进入 20 秒冷却时间
        cooldownTicks = 20 * 20;

        TowerSkillPool.ShieldActiveSkillNotify(mob, "恐惧");

        AABB area = mob.getBoundingBox().inflate(15.0D, 8.0D, 15.0D);
        List<Player> nearbyPlayers = mob.level().getEntitiesOfClass(Player.class, area);

        for (Player player : nearbyPlayers) {
            if (player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DARKNESS, 5 * 20, 0, false, true
                ));
                player.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 4 * 20, 0, false, true
                ));
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 3 * 20, 1, false, true
                ));
            }
        }

        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.WITHER_AMBIENT,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.2F, 0.5F);

        if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    30, 1.0D, 0.5D, 1.0D, 0.1D);
        }
    }

    /**
     * 瞬发夺魂咆哮，不需要持续执行
     */
    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
