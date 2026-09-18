package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.four;

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

            // ---- 特效：一道沿前方推进的“斩浪之墙” —— 十道竖直月牙 + 顶缘亮线 + 水面裂线 ----
            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.55;
            if (fx != null) {
                fx.budget(2400);
                Vec3 base = player.position();
                Vec3 up = new Vec3(0, 1, 0);
                for (int dist = 1; dist <= 10; dist++) {
                    Vec3 center = base.add(look.scale(dist)).add(0, 1.0, 0);
                    double t = dist / 10.0;
                    // 竖直斩击弧（绕右向量旋转 → 竖劈方向），半径随距离递减形成刀锋收束
                    fx.slash(ParticleTypes.SWEEP_ATTACK, center, look, right, 2.4 * (1.0 - t * 0.35), 150, 2, rot + dist * 0.25);
                    fx.slash(ParticleTypes.END_ROD, center, look, right, 2.1 * (1.0 - t * 0.35), 150, 1, rot + dist * 0.25);
                    // 墙面上下的边界线
                    Vec3 top = center.add(right.scale(2.0)).add(0, 1.6, 0);
                    Vec3 top2 = center.add(right.scale(-2.0)).add(0, 1.6, 0);
                    fx.line(ParticleTypes.SOUL_FIRE_FLAME, top2, top, 0.4, 0.05, 0.0, Vec3.ZERO);
                    fx.line(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.add(right.scale(2.2)).add(0, -0.9, 0),
                            center.add(right.scale(-2.2)).add(0, -0.9, 0), 0.5, 0.15, 0.0, Vec3.ZERO);
                }
                // 两侧刀尖迸射
                fx.burst(ParticleTypes.CRIT, base.add(look.scale(1.5)).add(0, 1.0, 0), 30, 1.0, true);
                fx.line(ParticleTypes.LARGE_SMOKE, base, base.add(look.scale(10)), 0.6, 0.5, 0.0, Vec3.ZERO);
            }

            for (int dist = 1; dist <= 10; dist++) {
                Vec3 center = player.position().add(look.scale(dist)).add(0, 1, 0);
                for (double width = -2; width <= 2; width += 0.5) {
                    Vec3 pos = center.add(right.scale(width));
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
