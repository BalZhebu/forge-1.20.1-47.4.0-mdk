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
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            player.setDeltaMovement(look.x * 4.0, 0.1, look.z * 4.0);
            player.hurtMarked = true;
            player.displayClientMessage(Component.literal("§4§l第六魂技：破军！"), true);
            for (int i = 0; i < 30; i++) {
                double px = player.getX() + look.x * i;
                double py = player.getY() + 0.5;
                double pz = player.getZ() + look.z * i;
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, px, py, pz, 5, 0.5, 0.5, 0.5, 0.1);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 2, 0.2, 0.2, 0.2, 0.05);
                AABB hitBox = new AABB(px - 2, py - 1, pz - 2, px + 2, py + 2, pz + 2);
                level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())
                        .forEach(t -> t.hurt(player.damageSources().playerAttack(player), finalDamage));
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.5f, 1.2f);
        }
    }
}
