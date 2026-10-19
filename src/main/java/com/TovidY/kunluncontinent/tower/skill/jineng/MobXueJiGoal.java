package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.tower.skill.TowerSkillPool;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * 主动词条【血祭】：燃烧自身精血，把代价换成一次无视防御的吸血爆发。
 *
 * <p><b>为什么伤害用 {@code damageSources().magic()}：</b>
 * 本 mod 的 {@code CombatEventHandler} 会把所有"有攻击者"的伤害按
 * 攻防公式重算一遍，如果用 {@code mobAttack} 就变成了"看怪物物攻"，
 * 血祭的定位（按目标最大生命的百分比真伤）会被完全吃掉。
 * {@code magic()} 的伤害源不带实体，正好绕过那条重算通道，数值可控。</p>
 *
 * <p>代价：扣除自身 {@link #SELF_COST_RATIO} 当前生命；血量低于 30% 时不会释放，
 * 免得词条自己把怪献祭死。全程不改动任何方块。</p>
 */
public class MobXueJiGoal extends Goal {

    private final Mob mob;
    /** 词条等级（1~10），决定伤害比例与冷却。 */
    private final int level;
    private int cooldownTicks = 0;

    /** 自身生命代价（当前生命的 10%）。 */
    private static final float SELF_COST_RATIO = 0.10F;

    public MobXueJiGoal(Mob mob) {
        this(mob, 1);
    }

    public MobXueJiGoal(Mob mob, int level) {
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
        // 血量太残就别献祭了
        if (mob.getHealth() <= mob.getMaxHealth() * 0.30F) return false;
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.distanceToSqr(target) <= 100.0D;
    }

    @Override
    public void start() {
        // 冷却随等级缩短：基础 25 秒 ÷ 等级系数，最低 9 秒
        cooldownTicks = Math.max(9 * 20, Math.round(25 * 20 / TowerSkillPool.levelMultiplier(level)));

        TowerSkillPool.ShieldActiveSkillNotify(mob, "血祭", level);

        // 1. 先付代价：扣自己当前血量的 10%
        float cost = Math.max(1.0F, mob.getHealth() * SELF_COST_RATIO);
        mob.setHealth(Math.max(1.0F, mob.getHealth() - cost));
        // 代价要同步回 capability 的生命镜像，否则【濒死】仍按旧血量判定
        mob.getCapability(com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider.CAPABILITY)
                .ifPresent(cap -> cap.setShengming(mob.getHealth()));

        // 2. 再收利息：半径 6 格内玩家按其最大生命 6% × 等级系数掉血
        //    （不用怪物物攻/血量当基数：塔内高层数值是百万级的，会直接秒人）
        float ratio = 0.06F * TowerSkillPool.levelMultiplier(level);
        AABB area = mob.getBoundingBox().inflate(6.0D, 4.0D, 6.0D);
        List<Player> players = mob.level().getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
            if (player.distanceToSqr(mob) > 36.0D) continue;   // 球形判定，半径 6

            float damage = Math.max(1.0F, player.getMaxHealth() * ratio);
            player.hurt(mob.level().damageSources().magic(), damage);

            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        player.getX(), player.getY() + 1.0D, player.getZ(), 6, 0.3D, 0.5D, 0.3D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        player.getX(), player.getY() + 0.8D, player.getZ(), 10, 0.3D, 0.6D, 0.3D, 0.02D);
            }
        }

        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                    30, 0.5D, 0.8D, 0.5D, 0.05D);
        }
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5F, 0.7F);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
