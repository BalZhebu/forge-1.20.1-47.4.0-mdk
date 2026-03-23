package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.six;

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

public class SkillBahuang6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 500f; }
    @Override public float getDamageMultiplier() { return 3.3f; }
    @Override public int getCastTime() { return 5; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            player.displayClientMessage(Component.literal("§4§l第六魂技：破军·影杀！"), true);
            player.setDeltaMovement(look.x * 5.0, 0.2, look.z * 5.0);
            player.hurtMarked = true;
            for (int i = 0; i < 30; i++) {
                double dist = i;
                double px = player.getX() + look.x * dist;
                double py = player.getY() + 1.0 + look.y * dist;
                double pz = player.getZ() + look.z * dist;
                if (i % 5 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0, 0, 0, 0);
                    level.playSound(null, px, py, pz, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                serverLevel.sendParticles(ParticleTypes.SOUL, px, py, pz, 3, 0.2, 0.2, 0.2, 0.02);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 1, 0.1, 0.1, 0.1, 0.05);
                AABB hitBox = new AABB(px - 2.5, py - 2.0, pz - 2.5, px + 2.5, py + 2.0, pz + 2.5);
                level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())
                        .forEach(target -> {
                            target.hurt(player.damageSources().playerAttack(player), finalDamage);
                            Vec3 push = target.position().subtract(player.position()).normalize().scale(0.8);
                            target.push(push.x, 0.3, push.z);
                        });
            }
            Vec3 endPos = player.position().add(look.scale(30));
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, endPos.x, endPos.y + 1, endPos.z, 50, 1.0, 1.0, 1.0, 0.1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.5f, 0.8f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }
}