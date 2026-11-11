package com.TovidY.kunluncontinent.item.baseskillist.qishajian.six;

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
 * 第六魂技 · 七杀剑阵
 *
 * <p>展开半径 20 格的暗红结界，持续 5 秒；结界内每 0.5 秒被剑影穿刺一次（共 10 跳）。</p>
 *
 * <p>持续伤害交给 {@link QishaDots#onArea}：它是"以施法者为圆心"的场域，
 * 每跳重新扫一遍范围内的生物，所以中途走进来 / 走出去都按真实位置算。</p>
 */
public class SkillQisha6 extends BaseSkillItem {

    private static final float FIELD_RADIUS = 20.0F;
    /** 5 秒 = 100 tick。 */
    private static final int DURATION = 100;
    /** 每 0.5 秒 = 10 tick 一跳。 */
    private static final int INTERVAL = 10;
    /** 每跳伤害 = 最终伤害 × 0.15（10 跳合计 1.5 倍）。 */
    private static final float PER_PULSE = 0.15F;

    @Override
    public float getBaseCost() { return 200f; }

    @Override
    public float getDamageMultiplier() { return 2.0f; }

    @Override
    public int getCastTime() { return 20; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) {
            return;
        }

        // 每跳的爆点 + 剑影都落在**被命中的生物**身上（不是施法者脚下）
        QishaDots.onArea(sp, FIELD_RADIUS, finalDamage * PER_PULSE, INTERVAL, DURATION,
                SkillEffectStyles.BURST_QISHA_MARK, 1.4F, SkillEffectStyles.FALL_QISHA_SWORD);

        Vec3 base = player.position();
        if (SkillFxToggle.useShader(player)) {
            // 结界本体：半径 20 的巨型法阵，持续 100 tick
            QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_OCTA, base.x, base.y, base.z, 8.0F, DURATION);
            QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_FIELD, base.x, base.y, base.z, FIELD_RADIUS, DURATION);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = level.getGameTime() * 0.25;
                fx.budget(1200);
                fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y,
                        FIELD_RADIUS, 24, rot, 0);
                fx.star(ParticleTypes.CRIMSON_SPORE, base.add(0, 0.6, 0), ParticleFx.Axis.Y,
                        FIELD_RADIUS * 0.5, FIELD_RADIUS * 0.18, 7, rot, 0.03);
                fx.pillars(ParticleTypes.SOUL, base, FIELD_RADIUS, 7, 3.0, rot);
                fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.1, 0), FIELD_RADIUS, 2.4, 56);
            }
        }
        level.playSound(null, base.x, base.y, base.z,
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.6f);
    }
}
