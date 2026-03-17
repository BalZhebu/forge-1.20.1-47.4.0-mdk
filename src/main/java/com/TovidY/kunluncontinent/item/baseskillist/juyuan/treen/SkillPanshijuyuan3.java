package com.TovidY.kunluncontinent.item.baseskillist.juyuan.treen;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
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

import java.util.List;

public class SkillPanshijuyuan3 extends BaseSkillItem {
    @Override public float getBaseCost() { return 160f; }
    @Override public float getDamageMultiplier() { return 1.9f; }
    @Override public int getCastTime() { return 40; }
    @Override public int getCooldownTicks() { return 360; }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            float playerGongji = ModAttributeAPI.getGongji(player);
            float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0),
                    e -> e != player && e.isAlive() && e.distanceTo(player) <= 10.0);
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().explosion(player, player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                target.push(0, 0.3, 0);
            }
            for (int r = 1; r <= 10; r += 2) {
                for (int d = 0; d < 360; d += 20) {
                    double rad = Math.toRadians(d);
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            player.getX() + Math.cos(rad) * r, player.getY() + 0.1, player.getZ() + Math.sin(rad) * r,
                            1, 0, 0, 0, 0.01);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.5f);
            player.displayClientMessage(Component.literal("§8§l第三魂技：震地！"), true);
        }
    }
}