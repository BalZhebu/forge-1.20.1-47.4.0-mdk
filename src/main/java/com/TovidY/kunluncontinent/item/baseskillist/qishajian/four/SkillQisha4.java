package com.TovidY.kunluncontinent.item.baseskillist.qishajian.four;

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
 * 第四魂技 · 七杀印记
 *
 * <p><b>以自身脚下为中心</b>铺开一张暗红法阵（七芒星 + 内圈符文），
 * 阵内的生物受到一次高额伤害并被减速 III。</p>
 *
 * <p>着色器模式：{@code SIGIL_QISHA_FIELD}（16 边 + 七芒 + 三重冲击环）；
 * 粒子模式：星阵 + 符环 + 冲击环。</p>
 */
public class SkillQisha4 extends BaseSkillItem {

    /** 法阵半径。 */
    private static final double RADIUS = 6.0;

    @Override
    public float getBaseCost() { return 100f; }

    @Override
    public float getDamageMultiplier() { return 2.4f; }

    @Override
    public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.four.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        Vec3 base = player.position();

        // ① 先出法阵（六芒星），伤害延后 10 tick 再结算 —— 法阵刚冒出来就掉血太突兀
        boolean shader = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
        if (shader) {
            ServerPlayer sp = (ServerPlayer) player;
            QishaFx.field(sp, SkillEffectStyles.SIGIL_QISHA_GLYPH, base.x, base.y, base.z, (float) RADIUS, 40);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = level.getGameTime() * 0.25;
                fx.budget(1500);
                fx.star(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.15, 0), ParticleFx.Axis.Y,
                        RADIUS, RADIUS * 0.35, 7, rot, 0.03);
                fx.magicCircle(ParticleTypes.CRIMSON_SPORE, ParticleTypes.SOUL, base.add(0, 0.1, 0),
                        ParticleFx.Axis.Y, RADIUS * 0.6, -rot);
                fx.polygon(ParticleTypes.SOUL, base.add(0, 0.05, 0), ParticleFx.Axis.Y, RADIUS, 16, rot, 0);
            }
        }
        level.playSound(null, base.x, base.y, base.z,
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.9f, 0.8f);

        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        // ② 法阵成型后的那一击
        QishaDots.delay(sp, 10, caster -> {
            Level lvl = caster.level();
            Vec3 center = base;
            List<LivingEntity> hits = lvl.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(RADIUS), e -> e != caster && e.isAlive());
            for (LivingEntity target : hits) {
                dealSkillDamage(target, finalDamage, caster.damageSources().playerAttack(caster));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, true));
            }
            if (SkillFxToggle.useShader(caster)) {
                QishaFx.field(caster, SkillEffectStyles.SHOCK_QISHA,
                        center.x, center.y + 0.1, center.z, (float) RADIUS, 18);
                QishaFx.field(caster, SkillEffectStyles.BURST_QISHA,
                        center.x, center.y + 1.0, center.z, 1.8F, 14);
            } else {
                ParticleFx fx2 = ParticleFx.of(lvl, caster);
                if (fx2 != null) {
                    fx2.budget(700);
                    fx2.shockRing(ParticleTypes.SOUL, center.add(0, 0.1, 0), RADIUS, 1.6, 40);
                    fx2.burst(ParticleTypes.CRIMSON_SPORE, center.add(0, 0.6, 0), 26, 0.9, true);
                }
            }
            lvl.playSound(null, center.x, center.y, center.z,
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.9f, 0.7f);
        });
    }
}
