package com.TovidY.kunluncontinent.item.baseskillist.juyuan.eight;

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

public class SkillPanshijuyuan8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 666f; }
    @Override public float getDamageMultiplier() { return 4.3f; } // 极高爆发
    @Override public int getCastTime() { return 30; } // 蓄力久一点更霸气
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            Vec3 targetPoint = player.position().add(look.scale(3));

            player.displayClientMessage(Component.literal("§6§l第八魂技：憾岳！"), true);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, targetPoint.x, targetPoint.y, targetPoint.z, 5, 0.5, 0.5, 0.5, 0);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, targetPoint.x, targetPoint.y, targetPoint.z, 3, 0.1, 0.1, 0.1, 0);
            AABB hitArea = new AABB(targetPoint.x - 3.5, targetPoint.y - 2, targetPoint.z - 3.5, targetPoint.x + 3.5, targetPoint.y + 4, targetPoint.z + 3.5);
            level.getEntitiesOfClass(LivingEntity.class, hitArea, e -> e != player && e.isAlive())
                    .forEach(target -> {
                        target.hurt(player.damageSources().playerAttack(player), finalDamage);
                        target.setDeltaMovement(0, -2.0, 0); // 强行拍进地里
                    });

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 0.2f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}
