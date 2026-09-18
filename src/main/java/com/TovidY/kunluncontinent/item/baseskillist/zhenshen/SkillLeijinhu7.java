package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkillLeijinhu7 extends BaseSkillItem {
    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() {
        return "skill.liejinhu.7.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.addEffect(new MobEffectInstance(ModEffects.LIEJINHU.get(), 800, 0));
            player.displayClientMessage(Component.literal("§e§l第七魂技：裂金虎真身！"), true);

            // ---- 特效：虎形金纹法阵 —— 外环芒阵 + 内层爪痕 + 上升金辉 ----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.36;

                fx.budget(1900);
                fx.magicCircle(ParticleTypes.CRIT, ParticleTypes.GLOW, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 3.6, rot);
                // 虎纹芒阵：六芒 + 十二等分刻度
                fx.star(ParticleTypes.GLOW, base.add(0, 0.35, 0), ParticleFx.Axis.Y, 4.4, 1.8, 6, -rot * 1.3, 0.05);
                fx.dashedRing(ParticleTypes.ELECTRIC_SPARK, base.add(0, 0.5, 0), ParticleFx.Axis.Y, 5.0, 12, 0.4, rot * 1.7, 0.1);
                // 三道巨大爪痕烙印在阵上
                Vec3 look = player.getLookAngle();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = ParticleFx.ortho(look);
                for (int i = -1; i <= 1; i++) {
                    fx.slash(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 1.2, 0).add(right.scale(i * 1.1)), look, right, 2.6, 150, 2, rot + i * 0.25);
                }
                // 上升金辉
                fx.spiral(ParticleTypes.WAX_ON, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.0, 0.5, 6.0, 2.6, -rot, 120, 0.06);
                fx.column(ParticleTypes.GLOW, ParticleTypes.CRIT, base, 0.4, 5.0, 4, rot, ParticleFx.TAU * 1.3, 0.5);
                fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.1, 0), 5.0, 2.0, 42);
                fx.burst(ParticleTypes.CRIT, base.add(0, 1.0, 0), 32, 1.0, true);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }
}
