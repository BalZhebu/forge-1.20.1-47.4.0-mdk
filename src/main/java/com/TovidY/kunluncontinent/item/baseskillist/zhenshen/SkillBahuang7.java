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
 * 八荒戟真身 —— 开关型魂技，见 {@link BaseZhenshenSkill}。
 */
public class SkillBahuang7 extends BaseZhenshenSkill {

    @Override public float getBaseCost() { return 550f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() {
        return "skill.bahuangji.7.description";
    }

    @Override protected MobEffect getZhenshenEffect() { return ModEffects.BHUANGJI.get(); }

    @Override protected String getActiveMessage() { return "§4§l八荒戟真身"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§4§l第七魂技：八荒戟真身！"), true);

        // ---- 特效：八荒真身 —— 八角星阵 + 岩浆光柱环绕 + 贴地扩散环 ----
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            double rot = level.getGameTime() * 0.32;

            fx.budget(1900);
            // 八角星阵（双层旋转，八荒母题）
            fx.star(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 4.2, 1.7, 8, rot, 0.03);
            fx.star(ParticleTypes.LAVA, base.add(0, 0.5, 0), ParticleFx.Axis.Y, 3.2, 1.3, 8, -rot * 1.3, 0.03);
            fx.magicCircle(ParticleTypes.FLAME, ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 3.0, rot * 1.4);
            // 八荒戟真身：环绕上升的岩浆螺旋
            fx.spiral(ParticleTypes.LAVA, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.2, 0.6, 6.5, 2.4, -rot, 120, 0.06);
            fx.spiral(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.2, 0.6, 6.5, 2.4, -rot + Math.PI, 120, 0.06);
            // 中心戟柱 + 八方光柱
            fx.column(ParticleTypes.LAVA, ParticleTypes.FLAME, base, 0.4, 5.5, 4, rot, ParticleFx.TAU * 1.4, 0.45);
            fx.pillars(ParticleTypes.SOUL_FIRE_FLAME, base, 4.2, 8, 3.4, -rot);
            fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), 6.5, 2.0, 44);
            fx.burst(ParticleTypes.FLAME, base.add(0, 0.8, 0), 34, 1.1, true);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    @Override
    protected void playDeactivateFx(ServerLevel level, ServerPlayer player) {
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            Vec3 base = player.position();
            fx.budget(380);
            fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), 4.5, 1.8, 30);
            fx.burst(ParticleTypes.LAVA, base.add(0, 0.8, 0), 20, 0.7, true);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}
