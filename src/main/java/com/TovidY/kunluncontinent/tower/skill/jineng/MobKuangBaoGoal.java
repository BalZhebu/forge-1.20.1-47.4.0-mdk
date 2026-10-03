package com.TovidY.kunluncontinent.tower.skill.jineng;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapabilityProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.EnumSet;
import java.util.UUID;

public class MobKuangBaoGoal extends Goal {
    private final Mob mob;
    /** 词条等级（1~10），决定物攻加成强度。 */
    private final int level;
    private int cooldownTicks = 0; // 冷却计时器
    private int durationTicks = 0; // 持续时间计时器
    private boolean isBuffed = false;

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("9a4561b3-4f12-4c22-b912-fa81023bc411");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("1b5612c4-2a13-3d44-c813-ab92034cd222");

    public MobKuangBaoGoal(Mob mob) {
        this(mob, 1);
    }

    public MobKuangBaoGoal(Mob mob, int level) {
        this.mob = mob;
        this.level = Math.max(1, Math.min(com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.MAX_LEVEL, level));
        // 标记此 AI 影响怪物的看向（释放技能期间锁定目标）
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    // 触发条件：有攻击目标，且技能不在冷却中，且当前没开启狂暴
    @Override
    public boolean canUse() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && !isBuffed;
    }

    // 满足条件瞬间释放
    @Override
    public void start() {
        isBuffed = true;
        cooldownTicks = 30 * 20;
        mob.getCapability(MobAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float baseWugong = cap.getGongji();
            // 物攻加成 = 基础 50% × 等级系数（1级=+50%，每级+10%，10级=+95%）
            float bonusWugong = baseWugong * 0.50f * com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.levelMultiplier(level);
            com.TovidY.kunluncontinent.tower.skill.MobTempAttributeManager.applyTempWugong(mob, bonusWugong, 10);
        });

        net.minecraft.world.entity.ai.attributes.AttributeInstance speed = mob.getAttribute(Attributes.ATTACK_SPEED);
        if (speed != null && speed.getModifier(ATTACK_SPEED_MODIFIER_UUID) == null) {
            speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    ATTACK_SPEED_MODIFIER_UUID, "KuangBao SPEED", 1.00D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        com.TovidY.kunluncontinent.tower.skill.TowerSkillPool.ShieldActiveSkillNotify(mob, "狂暴", level);

        mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 200, 0, false, false));
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    // 每一帧的逻辑更新
    @Override
    public void tick() {
        if (isBuffed) {
            durationTicks--;
            if (durationTicks <= 0) {
                stop(); // 时间到了，卸载Buff
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return isBuffed && durationTicks > 0;
    }

    @Override
    public void stop() {
        if (isBuffed) {
            isBuffed = false;
            cooldownTicks = 15 * 20;

            AttributeInstance dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);

            AttributeInstance speed = mob.getAttribute(Attributes.ATTACK_SPEED);
            if (speed != null) speed.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }
    }
}