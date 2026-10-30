package com.TovidY.kunluncontinent.item.baseskillist.tianshi.one;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 第一魂技 · <b>圣光斩</b>：前方挥出一道弧形金色光刃。
 *
 * <p>判定是「扇形」：以视线为中心，前方 {@link #RANGE} 格、半角 {@link #HALF_ANGLE_DEG} 度。
 * 用采样点 + 去重集合收集目标，<b>同一个怪只吃一次伤害</b>（采样点重叠不会连击）。</p>
 */
public class SkillTianshi1 extends BaseSkillItem {

    /** 光刃推进距离（格）。 */
    private static final double RANGE = 7.0;
    /** 扇形半角（度）。 */
    private static final double HALF_ANGLE_DEG = 60.0;

    @Override public float getBaseCost() { return 70f; }
    @Override public float getDamageMultiplier() { return 2.2f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.one.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;

        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x);
        Vec3 base = player.position().add(0, 1.0, 0);

        // ---- 特效：着色器（120° 金色弧光）／ 粒子（弧形光刃）二选一，由配置位 13 决定 ----
        if (SkillFxToggle.useShader(player)) {
            // 半径 = 判定半径，弧带张角 120°（和判定扇形一致），0.7 秒内从一侧斩到另一侧
            SkillFieldFx.atEntity(player, SkillEffectStyles.SLASH_ANGEL, (float) RANGE, 14);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = serverLevel.getGameTime() * 0.6;
                fx.budget(1600);
                // 主刃（金色）+ 内层（白金）：绕视线轴翻转 90°，形成"月牙"
                fx.slash(ParticleTypes.FIREWORK, base.add(look.scale(1.6)), look, right,
                        3.4, 120, 3, rot);
                fx.slash(ParticleTypes.END_ROD, base.add(look.scale(1.8)), look, right,
                        3.1, 110, 2, rot + 0.25);
                // 刃口拖尾
                for (int i = 1; i <= 3; i++) {
                    fx.line(ParticleTypes.END_ROD,
                            base.add(look.scale(i * 2.0)).add(right.scale(1.4)),
                            base.add(look.scale(i * 2.0)).add(right.scale(-1.4)),
                            0.35, 0.06, 0.0, Vec3.ZERO);
                }
                // 脚下弧光
                fx.shockRing(ParticleTypes.FIREWORK, player.position().add(0, 0.05, 0), RANGE, 0.6, 26);
            }
        }

        // ---- 判定：扇形内去重 ----
        Set<LivingEntity> hits = new LinkedHashSet<>();
        double cosLimit = Math.cos(Math.toRadians(HALF_ANGLE_DEG));
        for (double dist = 1.0; dist <= RANGE; dist += 1.0) {
            for (double side = -dist; side <= dist; side += 1.0) {
                Vec3 point = base.add(look.scale(dist)).add(right.scale(side));
                Vec3 toPoint = point.subtract(base);
                if (toPoint.lengthSqr() > RANGE * RANGE) {
                    continue;
                }
                if (toPoint.normalize().dot(look) < cosLimit) {
                    continue;
                }
                hits.addAll(level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(point.x - 0.9, point.y - 1.0, point.z - 0.9,
                                point.x + 0.9, point.y + 1.0, point.z + 0.9),
                        e -> e != player && e.isAlive()));
            }
        }
        for (LivingEntity target : hits) {
            dealSkillDamage(target, finalDamage, level.damageSources().mobAttack(player));
        }

        player.displayClientMessage(Component.literal("§e§l第一魂技：圣光斩！"), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6f, 1.2f);
    }
}
