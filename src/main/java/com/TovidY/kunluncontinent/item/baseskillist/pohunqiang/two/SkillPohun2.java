package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.two;

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
            for (int i = 0; i < 8; i++) {
                double px = player.getX() + lookDirection.x * i * 0.5;
                double py = player.getY() + 1.2 + lookDirection.y * i * 0.5;
                double pz = player.getZ() + lookDirection.z * i * 0.5;
                serverLevel.sendParticles(ParticleTypes.CRIT, px, py, pz, 5, 0.1, 0.1, 0.1, 0.2);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0, 0, 0, 0);
            }
            AABB damageArea = player.getBoundingBox().inflate(2.0, 1.0, 2.0).expandTowards(lookDirection.scale(4.0));
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, damageArea,
                    entity -> entity != player && entity.isAlive());
            float finalDamage = 10.0f * getDamageMultiplier() * powerMultiplier;
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().mobAttack(player), finalDamage);
                target.knockback(0.5, -lookDirection.x, -lookDirection.z);
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        target.getX(), target.getY() + 1, target.getZ(),
                        10, 0.2, 0.2, 0.2, 0.1);
            }
            player.displayClientMessage(Component.literal("§c§l第二魂技：枪芒！"), true);
        }
    }
}