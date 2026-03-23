package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.nine;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkillLeijinhu9 extends BaseSkillItem {
    @Override public float getBaseCost() { return 900f; }
    @Override public float getDamageMultiplier() { return 3.2f; }

    @Override
    public int getCastTime() {
        return 0;
    }

    @Override public int getCooldownTicks() { return 1300; }
    @Override public String getDescriptionKey() { return "skill.leijinhu.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§e§l第九魂技：猛虎破界！"), true);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0), e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));
                AreaEffectCloud voidCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
                voidCloud.setOwner(player);
                voidCloud.setRadius(3.0f);
                voidCloud.setDuration(200);
                voidCloud.addTag("PohunVoidZone");
                voidCloud.getPersistentData().putFloat("VoidDamage", finalDamage * 0.1f);
                level.addFreshEntity(voidCloud);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 3, 2, 1, 2, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0f, 0.6f);
        }
    }
}