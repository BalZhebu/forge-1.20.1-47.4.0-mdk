package com.TovidY.kunluncontinent.item.baseskillist.tianshi.seven;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.BaseZhenshenSkill;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
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
 * 第七魂技 · <b>武魂真身</b>（天使）—— 开关型，逻辑全在 {@link BaseZhenshenSkill} /
 * {@code ZhenshenState}，这里只负责「用哪个效果」和「长什么样」。
 *
 * <p>增幅数值写在 {@link com.TovidY.kunluncontinent.potion.skilleffect.wuhun.TianshiEffect}：
 * 攻击 +80%、最大生命 +50%、防御 +25%、暴击率 +15%、暴击伤害 +35%、生命回复 +30%。</p>
 *
 * <p>开启期间武魂维持费 ×20（{@code ZhenshenState.UPKEEP_MULTIPLIER}），
 * 和另外四个真身一致，没有为天使单独开口子。</p>
 */
public class SkillTianshi7 extends BaseZhenshenSkill {

    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.seven.description";
    }

    @Override protected MobEffect getZhenshenEffect() { return ModEffects.TIANSHI.get(); }

    @Override protected String getActiveMessage() { return "§e§l天使真身"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§e§l第七魂技：天使真身！"), true);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.5f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8f, 1.4f);

        // ---- 着色器版：神临阵 + 通天光柱（都跟人走，1.5 秒后淡出）----
        if (SkillFxToggle.useShader(player)) {
            SkillFieldFx.atEntity(player, SkillEffectStyles.SIGIL_ANGEL_ZHENSHEN, 4.2F, 45);
            SkillFieldFx.atEntity(player, SkillEffectStyles.BEAM_ANGEL_PILLAR, 0.0F, 45);
            return;
        }

        // ---- 粒子版：光柱通天 + 脚下圣环 + 背后展开的巨大光翼 ----
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            double rot = level.getGameTime() * 0.34;
            Vec3 look = player.getLookAngle();
            Vec3 right = ParticleFx.ortho(look);

            fx.budget(2200);
            // 通天光柱（内芯 + 外壳）
            fx.column(ParticleTypes.END_ROD, ParticleTypes.FIREWORK, base, 1.1, 14.0, 5, rot, ParticleFx.TAU * 2.2, 0.35);
            // 地面圣环 + 六芒
            fx.magicCircle(ParticleTypes.FIREWORK, ParticleTypes.END_ROD, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 4.2, rot, 2, 0.25);
            fx.star(ParticleTypes.GLOW, base.add(0, 0.3, 0), ParticleFx.Axis.Y, 5.4, 2.2, 6, -rot * 1.2, 0.05);
            fx.dashedRing(ParticleTypes.WAX_ON, base.add(0, 0.5, 0), ParticleFx.Axis.Y, 6.0, 16, 0.5, rot * 1.6, 0.08);
            // 光翼：两侧各三片，层层外扩
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 0; i < 3; i++) {
                    Vec3 origin = base.add(0, 1.5 - i * 0.35, 0).add(right.scale(side * (0.6 + i * 0.2)));
                    fx.slash(ParticleTypes.END_ROD, origin, look,
                            right.scale(side), 3.2 + i * 0.9, 110, 3, rot + i * 0.3);
                }
            }
            // 上升光羽
            fx.spiral(ParticleTypes.FIREWORK, base.add(0, 0.2, 0), ParticleFx.Axis.Y,
                    3.4, 0.6, 8.0, 3.0, -rot, 130, 0.05);
            fx.shockRing(ParticleTypes.END_ROD, base.add(0, 0.1, 0), 6.0, 2.0, 44);
            fx.burst(ParticleTypes.END_ROD, base.add(0, 1.2, 0), 40, 1.1, true);
        }
    }

    @Override
    protected void playDeactivateFx(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.7f, 1.4f);

        // 关闭：身上一团光散开（两种模式共用同一个 burst 样式）
        if (SkillFxToggle.useShader(player)) {
            SkillFieldFx.atEntity(player, SkillEffectStyles.BURST_ANGEL_WINGS, 2.6F, 16);
            return;
        }

        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            fx.budget(420);
            fx.shockRing(ParticleTypes.END_ROD, base.add(0, 0.2, 0), 4.6, 2.0, 32);
            fx.burst(ParticleTypes.FIREWORK, base.add(0, 1.0, 0), 24, 0.9, true);
        }
    }
}
