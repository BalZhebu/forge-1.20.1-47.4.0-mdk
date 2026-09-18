package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.two;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillPohun2 extends BaseSkillItem {
    public SkillPohun2() {
        super();
    }

    @Override
    public int getCastTime() {
        return 20; // 吟唱1秒 (20 ticks)
    }

    @Override
    public int getCooldownTicks() {
        return 120; // 6秒冷却
    }

    @Override
    public float getDamageMultiplier() {
        return 1.6f; // 基础伤害倍率
    }

    @Override
    public float getBaseCost() {
        return 120f; // 基础精神力消耗
    }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.two.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 lookDirection = player.getLookAngle().normalize();
            player.setDeltaMovement(lookDirection.x * 2.5, 0.1, lookDirection.z * 2.5);
            player.hurtMarked = true;

            // ---- 特效：突进轴线上的锥形枪芒 + 环绕螺旋气流 ----
            ParticleFx fx = ParticleFx.of(level, player);
            Vec3 origin = player.getEyePosition();
            double rot = serverLevel.getGameTime() * 0.35;
            if (fx != null) {
                fx.budget(1200);
                fx.cone(ParticleTypes.CRIT, origin, lookDirection, 4.5, 0.5, 22, rot, 5);
                fx.helixAround(ParticleTypes.SOUL_FIRE_FLAME, origin, lookDirection, 0.7, 3.6, 2.2, 2, rot, 30);
                fx.slash(ParticleTypes.SWEEP_ATTACK, origin, lookDirection, new Vec3(0, 1, 0), 2.0, 150, 2, rot);
                fx.burst(ParticleTypes.ENCHANTED_HIT, origin, 16, 0.35, true);
            }

            AABB damageArea = player.getBoundingBox().inflate(2.0, 1.0, 2.0).expandTowards(lookDirection.scale(4.0));
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, damageArea,
                    entity -> entity != player && entity.isAlive());
            float finalDamage = 10.0f * getDamageMultiplier() * powerMultiplier;
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().mobAttack(player), finalDamage);
                target.knockback(0.5, -lookDirection.x, -lookDirection.z);
                if (fx != null) {
                    Vec3 hitPos = target.position().add(0, target.getBbHeight() * 0.6, 0);
                    fx.sphereBlades(ParticleTypes.CRIT, ParticleTypes.ENCHANTED_HIT, hitPos, 0.6, 10, 0.5, rot);
                    fx.burst(ParticleTypes.SOUL, hitPos, 12, 0.3, false);
                }
            }
            player.displayClientMessage(Component.literal("§c§l第二魂技：枪芒！"), true);
        }
    }
}
