package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.treen;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillBahuang3 extends BaseSkillItem {
    @Override
    public float getBaseCost() { return 165f; }
    @Override
    public float getDamageMultiplier() { return 1.85f; } // 跳劈高额伤害
    @Override
    public int getCastTime() { return 60; }
    @Override
    public int getCooldownTicks() { return 360; } // 18s

    @Override
    public String getDescriptionKey() {
        return "skill.bahuangji.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * 1.1, 0.7, look.z * 1.1);
        player.hurtMarked = true;
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            AABB area = player.getBoundingBox().inflate(5.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.distanceTo(player) <= 5.0);
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().mobAttack(player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2)); // 5s
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 2, 1, 0, 1, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.8f);
            player.displayClientMessage(Component.literal("§6§l第三魂技：劈山！"), true);
        }
    }
}
