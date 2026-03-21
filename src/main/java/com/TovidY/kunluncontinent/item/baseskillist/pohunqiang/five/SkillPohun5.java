package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.five;

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

import java.util.List;

public class SkillPohun5 extends BaseSkillItem {
    @Override
    public float getBaseCost() {
        return 300f;
    }

    @Override
    public float getDamageMultiplier() {
        return 2.5f;
    }

    @Override
    public int getCastTime() {
        return 0;
    }

    @Override
    public int getCooldownTicks() {
        return 800;
    }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.five.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§6§l第五魂技：碎星！"), true);
            AABB area = player.getBoundingBox().inflate(15.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().magic(), finalDamage);
                serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1, target.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2f, 1.5f);
            }
        }
    }
}
