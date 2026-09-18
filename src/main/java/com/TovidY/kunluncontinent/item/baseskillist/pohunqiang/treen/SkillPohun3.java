package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.treen;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class SkillPohun3 extends BaseSkillItem {

    @Override public float getBaseCost() { return 150f; }

    @Override public float getDamageMultiplier() { return 1.8f; }

    @Override public int getCastTime() { return 20; }

    @Override public int getCooldownTicks() { return 300; }
    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        float playerGongji = ModAttributeAPI.getGongji(player);
        float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;
        this.runSkillLogic(level, player, powerMultiplier, finalDamage);
    }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        this.runSkillLogic(level, player, powerMultiplier, finalDamage);
    }

    private void runSkillLogic(Level level, Player player, float powerMultiplier, float finalDamage) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * 1.2, 0.1, look.z * 1.2);
        player.hurtMarked = true;

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            AABB area = player.getBoundingBox().inflate(3.5);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive() && player.distanceTo(e) <= 4.0);

            LivingEntity target = targets.stream()
                    .filter(e -> {
                        Vec3 toTarget = e.position().subtract(player.position()).normalize();
                        return look.dot(toTarget) > 0.4;
                    })
                    .min(Comparator.comparingDouble(player::distanceTo))
                    .orElse(null);

            if (target != null) {
                float targetFangyu = ModAttributeAPI.getFangyu(target);
                float effectiveFangyu = targetFangyu * 0.7f;
                float reduction = 100f / (100f + effectiveFangyu);
                float actualDamage = finalDamage * reduction;
                target.hurt(level.damageSources().indirectMagic(player, player), Math.max(1.0f, actualDamage));

                // ---- 特效：锁定目标 → 贯穿突刺线 → 破甲碎片环 ----
                ParticleFx fx = ParticleFx.of(level, player);
                if (fx != null) {
                    Vec3 origin = player.getEyePosition();
                    Vec3 hitPos = target.position().add(0, target.getBbHeight() * 0.55, 0);
                    Vec3 dir = hitPos.subtract(origin).normalize();
                    double rot = serverLevel.getGameTime() * 0.4;

                    fx.budget(1100);
                    // 突刺轨迹：锥形枪芒 + 贴线残影
                    fx.cone(ParticleTypes.CRIT, origin, dir, origin.distanceTo(hitPos), 0.35, 24, rot, 5);
                    fx.line(ParticleTypes.END_ROD, origin, hitPos, 0.4, 0.03, 0.0, Vec3.ZERO);
                    // 命中点：破甲碎片向外崩解
                    fx.dashedRing(ParticleTypes.SOUL_FIRE_FLAME, hitPos, ParticleFx.Axis.Y, 0.9, 8, 0.45, rot, 0.04);
                    fx.dashedRing(ParticleTypes.SOUL_FIRE_FLAME, hitPos, ParticleFx.Axis.X, 0.9, 8, 0.45, -rot, 0.04);
                    fx.burst(ParticleTypes.ENCHANTED_HIT, hitPos, 20, 0.45, true);
                    fx.sphere(ParticleTypes.SOUL, hitPos, 0.8, 22, 1.0, 0.02);
                    fx.dot(ParticleTypes.FLASH, hitPos);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.2f, 1.5f);
                player.displayClientMessage(Component.literal("§c§l第三魂技：破甲突刺！"), true);
            }
        }
    }
}
