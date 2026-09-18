package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.nine;

import com.TovidY.kunluncontinent.effect.ParticleFx;
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
import net.minecraft.world.phys.Vec3;

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

            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.45;

            // ---- 主特效：十格破界虎爪大阵 ----
            if (fx != null) {
                Vec3 base = player.position();
                Vec3 look = player.getLookAngle();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = look.cross(up).normalize();

                fx.budget(2800);
                // 十格地面大阵：星芒 + 双层多边形 + 三重冲击环
                fx.star(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 8.0, 3.2, 6, rot, 0.04);
                fx.polygon(ParticleTypes.CRIT, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 10.0, 16, -rot, 0.02);
                fx.magicCircle(ParticleTypes.GLOW, ParticleTypes.CRIT, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 5.0, rot * 1.5);
                for (int r = 1; r <= 3; r++) {
                    fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.05 + r * 0.06, 0), 3.5 * r, 2.0, 44);
                }
                // 四道破界爪痕（前后左右各一道竖劈）
                for (int i = 0; i < 4; i++) {
                    double a = rot + ParticleFx.TAU * i / 4;
                    Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
                    Vec3 side = dir.cross(up).normalize();
                    fx.slash(ParticleTypes.SWEEP_ATTACK, base.add(0, 1.6, 0), dir, side, 7.0, 150, 3, rot + i * 0.2);
                }
                fx.column(ParticleTypes.GLOW, ParticleTypes.CRIT, base, 0.9, 8.0, 4, rot, ParticleFx.TAU * 1.2, 0.4);
                fx.pillars(ParticleTypes.SOUL_FIRE_FLAME, base, 10.0, 8, 4.0, -rot);
                fx.burst(ParticleTypes.CRIT, base.add(0, 1.0, 0), 48, 2.0, true);
                fx.bloom(ParticleTypes.WAX_ON, base.add(0, 1.4, 0), 30, 1.6, 0.25);
            }

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

                if (fx != null) {
                    // 每个被破界者身上炸开一记虎爪
                    Vec3 hp = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    Vec3 to = hp.subtract(player.position()).normalize();
                    Vec3 side = ParticleFx.ortho(to);
                    for (int i = -1; i <= 1; i++) {
                        fx.slash(ParticleTypes.CRIT, hp.add(side.scale(i * 0.5)), to, side, 1.4, 160, 2, rot + i * 0.3);
                    }
                    fx.burst(ParticleTypes.SOUL_FIRE_FLAME, hp, 18, 0.5, true);
                    fx.dome(ParticleTypes.GLOW, target.position(), 2.0, 4, 18);
                }
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 3, 2, 1, 2, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0f, 0.6f);
        }
    }
}
