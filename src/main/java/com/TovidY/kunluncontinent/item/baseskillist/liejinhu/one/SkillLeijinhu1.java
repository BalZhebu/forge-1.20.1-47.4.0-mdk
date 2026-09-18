package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillLeijinhu1 extends BaseSkillItem {

    public SkillLeijinhu1() {
        super();
    }

    @Override
    public int getCastTime() {
        return 20; // 1秒吟唱 (20 ticks = 1s)
    }

    @Override
    public int getCooldownTicks() {
        return 160; // 8秒冷却
    }

    @Override
    public float getDamageMultiplier() {
        return 1.3f;//倍率
    }

    @Override
    public float getBaseCost() {
        return 100;//精神力消耗
    }

    @Override
    public String getDescriptionKey() {
        return "skill.leijinhu.one.description";
    }

    @Override
    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            float finalCost = 100f * costMultiplier;
            cap.setJingshenli(Math.max(0, currentJs - finalCost));
        });
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            double range = 4.0 + Math.log10(powerMultiplier) * 2.0;
            List<Entity> targets = level.getEntities(player, player.getBoundingBox().inflate(range));

            // ---- 特效：三道平行虎爪痕（竖直弧） + 金色爆点 ----
            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.5;
            if (fx != null) {
                Vec3 look = player.getLookAngle();
                Vec3 origin = player.getEyePosition().add(0, -0.3, 0);
                Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();

                fx.budget(1500);
                for (int i = -1; i <= 1; i++) {
                    Vec3 c = origin.add(right.scale(i * 0.7));
                    // 主爪痕（贴向 look 方向，沿 right 轴旋转 → 竖直的弯月）
                    fx.slash(ParticleTypes.CRIT, c, look, right, 2.8, 130, 3, rot + i * 0.2);
                    // 内层高亮锋线
                    fx.slash(ParticleTypes.GLOW, c, look, right, 2.5, 130, 1, rot + i * 0.2);
                }
                // 爪痕交汇处的金色爆闪
                Vec3 focus = origin.add(look.scale(1.8));
                fx.burst(ParticleTypes.CRIT, focus, 26, 0.9, true);
                fx.dot(ParticleTypes.FLASH, focus);
                fx.bloom(ParticleTypes.WAX_ON, focus, 16, 0.7, 0.15);
            }

            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget && target != player) {
                    float damage = 10.0f * getDamageMultiplier() * powerMultiplier;
                    livingTarget.hurt(level.damageSources().playerAttack(player), damage);
                    livingTarget.knockback(0.5, player.getX() - target.getX(), player.getZ() - target.getZ());
                    if (fx != null) {
                        // 被抓中的目标：三道撕裂爪痕
                        Vec3 hp = target.position().add(0, target.getBbHeight() * 0.5, 0);
                        Vec3 to = hp.subtract(player.position()).normalize();
                        Vec3 side = ParticleFx.ortho(to);
                        for (int i = -1; i <= 1; i++) {
                            fx.slash(ParticleTypes.ENCHANTED_HIT, hp.add(side.scale(i * 0.35)), to, side, 0.9, 140, 2, rot + i * 0.3);
                        }
                        fx.burst(ParticleTypes.CRIT, hp, 14, 0.35, true);
                    }
                }
            }
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }
}
