package com.TovidY.kunluncontinent.item.baseskillist.qishajian.seven;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaFx;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.BaseZhenshenSkill;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;

/**
 * 第七魂技 · 七杀真身
 *
 * <p><b>开关型</b>（与其它武魂的第 7 魂技一致，见 {@link BaseZhenshenSkill}）：
 * 再按一次关闭。效果只有一个 —— <b>物攻翻倍</b>（{@code ModEffects.QISHA_ZHENSHEN}），
 * 不加任何其它属性。</p>
 */
public class SkillQisha7 extends BaseZhenshenSkill {

    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.seven.description"; }

    @Override protected MobEffect getZhenshenEffect() { return ModEffects.QISHA_ZHENSHEN.get(); }

    @Override protected String getActiveMessage() { return "§4§l七杀真身"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§4§l第七魂技：七杀真身！"), true);
        Vec3 base = player.position();

        if (SkillFxToggle.useShader(player)) {
            QishaFx.field(player, SkillEffectStyles.SIGIL_QISHA_ZHENSHEN, base.x, base.y, base.z, 2.2F, 60);
            QishaFx.field(player, SkillEffectStyles.BEAM_QISHA_PILLAR, base.x, base.y, base.z, 1.3F, 60);
            QishaFx.field(player, SkillEffectStyles.BURST_QISHA, base.x, base.y + 1.0, base.z, 1.4F, 20);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = level.getGameTime() * 0.3;
                fx.budget(1600);
                // 暗红光芒缠绕全身
                fx.spiral(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), ParticleFx.Axis.Y,
                        2.2, 0.5, 5.5, 2.6, rot, 120, 0.05);
                fx.spiral(ParticleTypes.CRIMSON_SPORE, base.add(0, 0.2, 0), ParticleFx.Axis.Y,
                        2.2, 0.5, 5.5, 2.6, rot + Math.PI, 120, 0.05);
                fx.star(ParticleTypes.SOUL, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 2.6, 0.9, 7, rot, 0.03);
                fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.CRIMSON_SPORE, base, 0.4, 3.2, 4, rot, 1.2, 0.6);
                fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.05, 0), 4.0, 1.8, 36);
            }
        }
        level.playSound(null, base.x, base.y, base.z,
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    @Override
    protected void playDeactivateFx(ServerLevel level, ServerPlayer player) {
        Vec3 base = player.position();
        if (SkillFxToggle.useShader(player)) {
            QishaFx.field(player, SkillEffectStyles.SHOCK_QISHA, base.x, base.y + 0.1, base.z, 3.0F, 10);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                fx.budget(300);
                fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.05, 0), 3.0, 1.5, 28);
                fx.burst(ParticleTypes.CRIMSON_SPORE, base.add(0, 0.8, 0), 16, 0.6, true);
            }
        }
        level.playSound(null, base.x, base.y, base.z,
                SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.6f, 0.7f);
    }
}
