package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.six;

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

public class SkillLeijinhu6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 450f; }
    @Override public float getDamageMultiplier() { return 0f; } // Buff类技能
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 1000; }

    @Override
    public String getDescriptionKey() { return "skill.leijinhu.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            boolean isDay = level.isDay();

            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.3;
            Vec3 base = player.position();

            if (isDay) {
                player.addEffect(new MobEffectInstance(ModEffects.SUN_POWER.get(), 400, 1)); // 20秒
                player.displayClientMessage(Component.literal("§e§l第六魂技：啸月（大日之威）！"), true);

                // ---- 大日形态：日轮圆阵 + 十二道放射光 + 环绕日珥 ----
                if (fx != null) {
                    fx.budget(1800);
                    Vec3 hub = base.add(0, 2.2, 0);
                    // 日轮：双层同心环 + 芒阵
                    fx.circle(ParticleTypes.FLAME, hub, ParticleFx.Axis.Y, 1.6, 36, rot, 0.02);
                    fx.star(ParticleTypes.LAVA, hub, ParticleFx.Axis.Y, 2.6, 1.4, 12, -rot, 0.03);
                    fx.dashedRing(ParticleTypes.SMALL_FLAME, hub, ParticleFx.Axis.Y, 2.2, 12, 0.5, rot * 1.6, 0.05);
                    // 十二道放射光线（自定义高度：从日轮向四周铺开）
                    for (int i = 0; i < 12; i++) {
                        double a = rot + ParticleFx.TAU * i / 12;
                        Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
                        fx.line(ParticleTypes.SMALL_FLAME, hub, hub.add(dir.scale(3.4)), 0.4, 0.08, 0.0, Vec3.ZERO);
                    }
                    // 日珥螺旋
                    fx.spiral(ParticleTypes.FLAME, hub, ParticleFx.Axis.Y, 2.0, 4.0, 3.5, 3.0, rot, 90, 0.08);
                    fx.column(ParticleTypes.FLAME, ParticleTypes.LAVA, base, 0.6, 6.0, 4, rot, ParticleFx.TAU, 0.5);
                    fx.burst(ParticleTypes.FLAME, hub, 30, 1.1, true);
                    fx.bloom(ParticleTypes.SMALL_FLAME, base.add(0, 1.0, 0), 24, 1.2, 0.1);
                }
            } else {
                player.addEffect(new MobEffectInstance(ModEffects.POWER_OF_THE_MOON.get(), 500, 1));
                player.displayClientMessage(Component.literal("§b§l第六魂技：啸月（邀月之华）！"), true);

                // ---- 邀月形态：月牙 + 银白环 + 垂落的星屑 ----
                if (fx != null) {
                    fx.budget(1800);
                    Vec3 hub = base.add(0, 2.2, 0);
                    // 月牙：用两个错开的环裁出弯月轮廓
                    fx.circle(ParticleTypes.END_ROD, hub, ParticleFx.Axis.Y, 2.4, 40, rot, 0.02);
                    fx.circle(ParticleTypes.END_ROD, hub.add(0.9, 0, 0.6), ParticleFx.Axis.Y, 2.1, 34, rot, 0.02);
                    // 银白月华环（打散 → 月晕）
                    fx.dashedRing(ParticleTypes.END_ROD, hub, ParticleFx.Axis.Y, 3.6, 16, 0.35, -rot * 1.4, 0.08);
                    fx.dashedRing(ParticleTypes.GLOW, hub, ParticleFx.Axis.Y, 4.4, 20, 0.25, rot * 0.8, 0.1);
                    // 垂落的星屑（自定义高度 6 格的竖直光幕）
                    fx.spiral(ParticleTypes.END_ROD, hub, ParticleFx.Axis.Y, 3.4, 0.6, -6.0, 2.2, -rot, 90, 0.08);
                    fx.ringStack(ParticleTypes.GLOW, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.4, 5.0, 4, 28, rot, 0.5, 0.5);
                    fx.burst(ParticleTypes.END_ROD, hub, 30, 0.9, true);
                    fx.dot(ParticleTypes.FLASH, hub);
                    fx.bloom(ParticleTypes.SNOWFLAKE, base.add(0, 1.2, 0), 24, 1.2, 0.06);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.5f, 1.0f);
        }
    }
}
