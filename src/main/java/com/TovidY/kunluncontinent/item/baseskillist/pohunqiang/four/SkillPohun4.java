package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.four;

import com.TovidY.kunluncontinent.effect.ParticleFx;
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

public class SkillPohun4 extends BaseSkillItem {
    @Override public float getBaseCost() { return 220f; }
    @Override public float getDamageMultiplier() { return 2.2f; }
    @Override public int getCastTime() { return 40; }
    @Override public int getCooldownTicks() { return 700; } // 35s

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.four.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            for (int i = 1; i <= 20; i++) {
                Vec3 point = start.add(look.scale(i));
                AABB checkArea = new AABB(point.x - 1.2, point.y - 1.2, point.z - 1.2, point.x + 1.2, point.y + 1.2, point.z + 1.2);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, checkArea,
                        e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    target.hurt(level.damageSources().indirectMagic(player, player), finalDamage);
                    target.invulnerableTime = 0;
                }
            }

            // ---- 特效：20格贯穿枪轨（双螺旋缠绕 + 沿途冲击环 + 终点爆发）----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 end = start.add(look.scale(20));
                double rot = serverLevel.getGameTime() * 0.5;

                fx.budget(1600);
                // 贯穿主轨：先给一个自发光核心线
                fx.line(ParticleTypes.END_ROD, start, end, 0.55, 0.02, 0.0, Vec3.ZERO);
                // 围绕主轨的双螺旋，形成“枪身缠绕气流”
                fx.helixAround(ParticleTypes.SOUL_FIRE_FLAME, start, look, 0.55, 20.0, 12.0, 2, rot, 80);
                // 锥形枪头
                fx.cone(ParticleTypes.CRIT, start, look, 4.0, 0.6, 16, rot, 5);
                // 沿途冲击环（每 4 格一道，半径随距离收束）
                for (double d = 2; d <= 20; d += 4) {
                    Vec3 c = start.add(look.scale(d));
                    fx.ringAround(ParticleTypes.SOUL, c, look, 0.85 - d * 0.02, 20, rot + d * 0.3, 0.02);
                }
                // 终点贯穿点
                fx.dot(ParticleTypes.FLASH, end);
                fx.sphereBlades(ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME, end, 1.0, 14, 0.9, rot);
                fx.burst(ParticleTypes.ENCHANTED_HIT, end, 24, 0.55, true);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.5f, 2.0f);
            player.displayClientMessage(Component.literal("§c§l第四魂技：贯日！"), true);
        }
    }
}
