package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.six;

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

import java.util.List;

public class SkillPohun6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 500f; }
    @Override public float getDamageMultiplier() { return 3.2f; }
    @Override public int getCastTime() { return 25; }
    @Override public int getCooldownTicks() { return 960; }

    @Override
    public String getDescriptionKey() { return "skill.pohunqiang.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            Vec3 targetPos = player.position().add(look.x * 5, 0, look.z * 5);
            player.displayClientMessage(Component.literal("§c§l第六魂技：陨星巨枪！"), true);
            for (int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                serverLevel.sendParticles(ParticleTypes.FLAME, targetPos.x + Math.cos(rad) * 4, targetPos.y + 0.1, targetPos.z + Math.sin(rad) * 4, 1, 0, 0, 0, 0);
            }
            for (double h = 15; h > 0; h -= 0.5) {
                serverLevel.sendParticles(ParticleTypes.CRIT, targetPos.x, targetPos.y + h, targetPos.z, 20, 0.2, 0.5, 0.2, 0.1);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, targetPos.x, targetPos.y + h, targetPos.z, 5, 0.1, 0.1, 0.1, 0);
            }
            AABB area = new AABB(targetPos.x - 5, targetPos.y - 2, targetPos.z - 5, targetPos.x + 5, targetPos.y + 5, targetPos.z + 5);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, targetPos.x, targetPos.y, targetPos.z, 1, 0, 0, 0, 0);
            level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}
