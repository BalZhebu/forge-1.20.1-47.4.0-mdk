package com.TovidY.kunluncontinent.item.baseskillist.qishajian.eight;

import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaFx;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaDots;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * 第八魂技 · 七绝斩
 *
 * <p>锁定 30 格内最近的敌人，5 秒内从天空降下 <b>7 枚暗红剑影</b>逐次命中；
 * 期间目标被施加缓慢 255（≈ 无法移动）。</p>
 */
public class SkillQisha8 extends BaseSkillItem {

    private static final double LOCK_RANGE = 30.0;
    /** 5 秒 = 100 tick，7 剑 → 每 14 tick 一剑。 */
    private static final int DURATION = 100;
    private static final int INTERVAL = 14;
    /** 每剑伤害 = 最终伤害 × 0.14（7 剑合计约 1 倍；因为是 30 格全体，单只不能再按单体算）。 */
    private static final float PER_STRIKE = 0.14F;
    /** 目标脚下最多给多少个目标单独铺阵（避免几十只怪时刷屏）。 */
    private static final int MAX_SIGILS = 6;

    @Override
    public float getBaseCost() { return 260f; }

    @Override
    public float getDamageMultiplier() { return 5.2f; }

    @Override
    public int getCastTime() { return 10; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return;
        }
        // 30 格内**所有**生物：每只都被钉住并挨 7 剑（不是单体）
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(LOCK_RANGE), e -> e != player && e.isAlive());
        if (targets.isEmpty()) {
            return;
        }
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));

        for (LivingEntity target : targets) {
            // 禁足：缓慢 255（amplifier 254），持续整段 5 秒
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DURATION, 254, false, true));
            // 每只独立吃 7 剑：第 1 剑立刻落（视觉在下面统一发），之后每 14 tick 一剑
            QishaDots.onTarget(sp, target, finalDamage * PER_STRIKE, INTERVAL, DURATION,
                    SkillEffectStyles.BURST_QISHA_MARK, 1.8F, SkillEffectStyles.FALL_QISHA_SWORD);
        }

        Vec3 base = player.position();
        boolean shader = SkillFxToggle.useShader(player);
        if (shader) {
            // 施法者脚下大五芒阵（标记"这一片都被锁了"）
            QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_PENTA, base.x, base.y, base.z, 9.0F, DURATION);
            QishaFx.field(sp, SkillEffectStyles.SHOCK_QISHA, base.x, base.y + 0.1, base.z, 9.0F, 20);
            // 最近的几个目标各铺一个阵 + 立刻落第一剑
            int sigils = Math.min(MAX_SIGILS, targets.size());
            for (int i = 0; i < sigils; i++) {
                LivingEntity t = targets.get(i);
                QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_PENTA, t.getX(), t.getY(), t.getZ(), 4.0F, DURATION);
                QishaFx.field(sp, SkillEffectStyles.FALL_QISHA_SWORD, t.getX(), t.getY(), t.getZ(), 4.0F, 20);
                QishaFx.field(sp, SkillEffectStyles.BEAM_QISHA_PILLAR, t.getX(), t.getY(), t.getZ(), 1.4F, DURATION);
            }
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                fx.budget(2400);
                fx.magicCircle(ParticleTypes.CRIMSON_SPORE, ParticleTypes.SOUL, base.add(0, 0.1, 0),
                        ParticleFx.Axis.Y, 5.0, level.getGameTime() * 0.3);
                fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 9.0, 20, 0, 0);
                fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.1, 0), 9.0, 2.4, 48);
                // 每只目标：七道剑影 + 落地光柱，和"7 剑"对得上
                for (LivingEntity target : targets) {
                    Vec3 at = target.position();
                    for (int i = 0; i < 7; i++) {
                        double ang = i * (Math.PI * 2 / 7);
                        Vec3 from = at.add(Math.cos(ang) * 1.6, 9.0, Math.sin(ang) * 1.6);
                        fx.line(ParticleTypes.SOUL_FIRE_FLAME, from, at.add(0, 1.0, 0), 0.16);
                        fx.line(ParticleTypes.CRIMSON_SPORE, from.add(0.2, 0, 0.2), at.add(0, 1.0, 0), 0.32);
                    }
                    fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.CRIMSON_SPORE,
                            at, 0.6, 3.2, 3, 0, ParticleFx.TAU, 0.7);
                    fx.burst(ParticleTypes.CRIMSON_SPORE, at.add(0, 1.0, 0), 16, 0.6, true);
                }
            }
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "§4[七绝斩] §c锁定 " + targets.size() + " 个目标"), true);
        level.playSound(null, base.x, base.y, base.z,
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}
