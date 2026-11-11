package com.TovidY.kunluncontinent.item.baseskillist.qishajian.two;

import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaFx;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
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
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * 第二魂技 · 血刃斩
 *
 * <p>挥出一道<b>红色剑波</b>横扫身前扇形，最远 5 格；命中附加裂伤（凋零）。</p>
 *
 * <p>着色器模式：{@code SLASH_QISHA} 横向弧斩（和天使圣光斩共用 skill_arc，只换配色）；
 * 粒子模式：三道弧形剑气 + 血雾。</p>
 */
public class SkillQisha2 extends BaseSkillItem {

    /** 剑气最远 5 格（判定与视觉一致）。 */
    private static final double RANGE = 5.0;
    /** 扇形半角：cos 0.5 ≈ 60°，总张角 120°。 */
    private static final double COS_ARC = 0.5;

    @Override
    public float getBaseCost() { return 80f; }

    @Override
    public float getDamageMultiplier() { return 2.6f; }

    @Override
    public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.two.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        Vec3 look = player.getLookAngle();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RANGE),
                e -> e != player && e.isAlive()
                        && e.position().subtract(player.position()).normalize().dot(look) > COS_ARC);

        for (LivingEntity target : targets) {
            dealSkillDamage(target, finalDamage, player.damageSources().playerAttack(player));
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true));
        }

        boolean shader = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
        if (shader) {
            ServerPlayer sp = (ServerPlayer) player;
            // 三层剑波叠着扫：近→远、层层错开，单层太薄会被说"淡淡的"
            QishaFx.fieldOwned(sp, SkillEffectStyles.SLASH_QISHA, 5.0F, 14);
            QishaFx.fieldOwned(sp, SkillEffectStyles.SLASH_QISHA, 6.6F, 17);
            QishaFx.fieldOwned(sp, SkillEffectStyles.SLASH_QISHA, 8.2F, 20);
            // 起手：脚下一圈冲击环 + 身前爆光
            QishaFx.field(sp, SkillEffectStyles.SHOCK_QISHA, sp.getX(), sp.getY() + 0.1, sp.getZ(), 4.0F, 12);
            Vec3 fwd = sp.position().add(sp.getLookAngle().scale(2.5));
            QishaFx.field(sp, SkillEffectStyles.BURST_QISHA, fwd.x, fwd.y + 1.0, fwd.z, 1.6F, 12);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 eye = player.getEyePosition().add(0, -0.25, 0);
                Vec3 right = ParticleFx.ortho(look);
                fx.budget(900);
                fx.slash(ParticleTypes.SOUL_FIRE_FLAME, eye, look, right, 3.2, 120, 2, 0);
                fx.slash(ParticleTypes.CRIMSON_SPORE, eye, look, right, 3.6, 120, 3, 0.2);
                fx.slash(ParticleTypes.CRIT, eye, look, right, 3.0, 110, 1, 0.45);
                fx.burst(ParticleTypes.CRIMSON_SPORE, eye.add(look.scale(2.2)), 18, 0.7, true);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
