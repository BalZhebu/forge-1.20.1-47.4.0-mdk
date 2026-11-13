package com.TovidY.kunluncontinent.item.baseskillist.juyuan.five;

import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldSender;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.server.level.ServerPlayer;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import com.TovidY.kunluncontinent.item.baseskillist.zhenshen.BaseBuffSkill;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.world.effect.MobEffect;

/**
 * 本技能已改造为<b>增益开关</b>（见 {@link com.TovidY.kunluncontinent.item.baseskillist.zhenshen.BaseBuffSkill}）：
 * <ul>
 *   <li>释放 → 开启，持续获得增益，<b>武魂维持消耗大幅增加</b>（×{@code MULT}）；</li>
 *   <li>开启期间再释放 → 关闭；</li>
 *   <li>切换武魂 / 武魂关闭 / 精神力见底 → 自动关闭。</li>
 * </ul>
 */
public class SkillPanshijuyuan5 extends BaseBuffSkill {

    @Override public float getBaseCost() { return 200f; }
    @Override public float getDamageMultiplier() { return 0.000000f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.five.description"; }

    @Override
    protected String getToggleId() { return "JUYUAN_STONE_ARMOR"; }

    @Override
    protected MobEffect resolveBuffEffect(ServerPlayer player) {
        return ModEffects.STONE_ARMOR.get();
    }

    @Override
    protected int getBuffAmplifier() { return 1; }

    @Override
    protected float getUpkeepMultiplier() { return 10.000000f; }

    @Override
    protected String getActiveMessage() { return "§8§l石铠"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§8§l石铠！"), true);
        boolean shaderFx = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
            if (shaderFx) {
                ServerPlayer sp = (ServerPlayer) player;
                SkillFieldSender.send(sp, SkillEffectStyles.SIGIL_JUYUAN_FIELD, sp.getX(), sp.getY(), sp.getZ(), 7.0F, 40);
                SkillFieldSender.send(sp, SkillEffectStyles.SHOCK_JUYUAN, sp.getX(), sp.getY(), sp.getZ(), 6.0F, 16);
            }
            ParticleFx fx = shaderFx ? null : ParticleFx.of(level, player);
            if (fx != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                Vec3 base = player.position();
                Vec3 hub = base.add(0, 1.0, 0);
                double rot = serverLevel.getGameTime() * 0.32;

                fx.budget(2200);
                // 八块旋转石板（小四边形 + 中心刻线），高度错落
                for (int i = 0; i < 8; i++) {
                    double a = rot + ParticleFx.TAU * i / 8;
                    double h = 0.6 + (i % 3) * 0.55;
                    Vec3 c = base.add(Math.cos(a) * 1.5, h, Math.sin(a) * 1.5);
                    fx.polygon(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                            c, ParticleFx.Axis.Y, 0.62, 4, -rot * 2.0 + i * 0.4, 0.0);
                    fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, c, ParticleFx.Axis.Y, 0.68, 4, -rot * 2.0 + i * 0.4, 0.0);
                }
                // 合体后的甲壳：包裹全身的石壳穹顶
                fx.dome(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), base, 1.4, 4, 16);
                fx.dome(ParticleTypes.SOUL_FIRE_FLAME, base, 1.55, 4, 16);
                // 甲缝间的锁定环
                fx.dashedRing(ParticleTypes.CAMPFIRE_COSY_SMOKE, hub, ParticleFx.Axis.Y, 1.2, 8, 0.5, rot * 2.2, 0.04);
                fx.dashedRing(ParticleTypes.WHITE_ASH, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 1.9, 10, 0.45, -rot * 1.6, 0.06);
                fx.ringStack(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), ParticleFx.Axis.Y, 1.8, 2.0, 3, 22, rot, 0.4, 0.5);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, hub, 24, 0.8, true);
            }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    @Override
    protected void playDeactivateFx(ServerLevel level, ServerPlayer player) {
        ParticleFx fx = ParticleFx.of(level, player);
        if (fx != null) {
            fx.budget(240);
            fx.shockRing(ParticleTypes.CRIMSON_SPORE, player.position().add(0, 0.05, 0), 2.4, 1.4, 26);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6f, 1.4f);
    }
}
