package com.TovidY.kunluncontinent.item.baseskillist.zhenshen;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;

/**
 * 破魂枪真身 —— 开关型魂技，见 {@link BaseZhenshenSkill}。
 */
public class SkillPohun7 extends BaseZhenshenSkill {

    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.7.description";
    }

    @Override protected MobEffect getZhenshenEffect() { return ModEffects.POHUNQIANG.get(); }

    @Override protected String getActiveMessage() { return "§c§l破魂枪真身"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§c§l第七魂技：破魂枪真身！"), true);

        // ---- 特效：真身降临 —— 立体法阵塔 + 环绕上升魂焰 ----
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            double rot = level.getGameTime() * 0.34;

            fx.budget(1800);
            // 三层法阵塔（自定义高度：每层间隔 1.1 格）
            fx.magicCircle(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 2.8, rot, 3, 1.1);
            // 环绕上升的双螺旋魂焰
            fx.spiral(ParticleTypes.SOUL, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 2.4, 0.5, 6.0, 2.6, -rot, 120, 0.06);
            fx.spiral(ParticleTypes.CRIMSON_SPORE, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 2.4, 0.5, 6.0, 2.6, -rot + Math.PI, 120, 0.06);
            // 中心魂柱
            fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL, base, 0.4, 5.0, 4, rot, ParticleFx.TAU * 1.4, 0.5);
            // 收尾冲击环
            fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.1, 0), 4.5, 2.0, 40);
            fx.burst(ParticleTypes.ENCHANTED_HIT, base.add(0, 1.0, 0), 30, 0.9, true);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    @Override
    protected void playDeactivateFx(ServerLevel level, ServerPlayer player) {
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            fx.budget(360);
            fx.shockRing(ParticleTypes.SOUL, base.add(0, 0.1, 0), 4.0, 1.8, 32);
            fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 1.0, 0), 22, 0.8, true);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.7f, 1.9f);
    }
}
