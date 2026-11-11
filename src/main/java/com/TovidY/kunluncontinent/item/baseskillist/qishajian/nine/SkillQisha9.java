package com.TovidY.kunluncontinent.item.baseskillist.qishajian.nine;

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
 * 第九魂技 · 七杀·灭魂
 *
 * <p>10 格内所有生物先吃一次巨额伤害，随后每一只都被"剑芒"钉住：
 * <b>每 0.1 秒（2 tick）掉一次血，持续 3 秒</b>（30 跳）。</p>
 */
public class SkillQisha9 extends BaseSkillItem {

    /** 作用范围：80 格（全图级大招）。 */
    private static final double RANGE = 80.0;
    /** 80 格内可能上百只生物：伤害最多点这么多只，避免 DoT 表爆炸。 */
    private static final int MAX_TARGETS = 32;
    /** 只有最近的这么几个目标会出剑影特效（其余只掉血）。 */
    private static final int MAX_FX_TARGETS = 8;
    /** 3 秒 = 60 tick。 */
    private static final int DURATION = 60;
    /** 每 0.1 秒 = 2 tick。 */
    private static final int INTERVAL = 2;
    /** 每跳伤害 = 最终伤害 × 0.02（30 跳合计 0.6 倍）。 */
    private static final float PER_PULSE = 0.02F;

    @Override
    public float getBaseCost() { return 400f; }

    @Override
    public float getDamageMultiplier() { return 7.5f; }

    @Override
    public int getCastTime() { return 30; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return;
        }
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RANGE), e -> e != player && e.isAlive());
        if (targets.isEmpty()) {
            return;
        }
        targets.sort(java.util.Comparator.comparingDouble(player::distanceToSqr));
        if (targets.size() > MAX_TARGETS) {
            targets = targets.subList(0, MAX_TARGETS);
        }

        for (LivingEntity target : targets) {
            dealSkillDamage(target, finalDamage, level.damageSources().indirectMagic(player, player));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, true));
            QishaDots.onTarget(sp, target, finalDamage * PER_PULSE, INTERVAL, DURATION,
                    SkillEffectStyles.BURST_QISHA_MARK, 1.2F, SkillEffectStyles.FALL_QISHA_SWORD);
        }

        Vec3 base = player.position();
        if (SkillFxToggle.useShader(player)) {
            // 施法者脚下「七」字大阵 + 每个目标头顶立刻落一剑
            QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_TRI, base.x, base.y, base.z, (float) RANGE, 30);
            for (LivingEntity target : targets.subList(0, Math.min(MAX_FX_TARGETS, targets.size()))) {
                QishaFx.field(sp, SkillEffectStyles.FALL_QISHA_SWORD,
                        target.getX(), target.getY(), target.getZ(), 4.0F, 20);
                QishaFx.field(sp, SkillEffectStyles.BEAM_QISHA_PILLAR,
                        target.getX(), target.getY(), target.getZ(), 1.2F, 30);
            }
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                fx.budget(2000);
                fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y,
                        RANGE, 24, level.getGameTime() * 0.2, 0);
                fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.1, 0), RANGE, 2.4, 56);
                for (LivingEntity target : targets.subList(0, Math.min(MAX_FX_TARGETS, targets.size()))) {
                    Vec3 at = target.position().add(0, target.getBbHeight() * 0.6, 0);
                    // 粗剑芒：双线 + 落地光柱 + 爆点
                    fx.line(ParticleTypes.SOUL_FIRE_FLAME, at.add(0, 9.0, 0), at, 0.12);
                    fx.line(ParticleTypes.CRIMSON_SPORE, at.add(0.2, 9.0, 0.2), at, 0.28);
                    fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.CRIMSON_SPORE,
                            target.position(), 0.5, 3.0, 3, 0, ParticleFx.TAU, 0.8);
                    fx.burst(ParticleTypes.CRIMSON_SPORE, at, 20, 0.7, true);
                }
            }
        }
        level.playSound(null, base.x, base.y, base.z,
                SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.9f, 0.8f);
    }
}
