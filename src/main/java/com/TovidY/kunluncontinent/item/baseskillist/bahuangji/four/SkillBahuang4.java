package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.four;

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

public class SkillBahuang4 extends BaseSkillItem {
    @Override public float getBaseCost() { return 230f; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 60; }
    @Override public int getCooldownTicks() { return 700; }

    @Override
    public String getDescriptionKey() {
        return "skill.bahuangji.four.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
            Vec3 right = new Vec3(-look.z, 0, look.x); // 计算右向量
            for (int dist = 1; dist <= 10; dist++) {
                Vec3 center = player.position().add(look.scale(dist)).add(0, 1, 0);
                for (double width = -2; width <= 2; width += 0.5) {
                    Vec3 pos = center.add(right.scale(width));
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                    List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos.x-1, pos.y-1, pos.z-1, pos.x+1, pos.y+1, pos.z+1), e -> e != player);
                    for (LivingEntity target : targets) {
                        target.hurt(level.damageSources().mobAttack(player), finalDamage);
                    }
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.5f);
            player.displayClientMessage(Component.literal("§b§l第四魂技：断江！"), true);
        }
    }
}
