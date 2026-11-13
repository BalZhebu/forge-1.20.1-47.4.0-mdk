package com.TovidY.kunluncontinent.item.baseskillist.juyuan.four;

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
public class SkillPanshijuyuan4 extends BaseBuffSkill {

    @Override public float getBaseCost() { return 180f; }
    @Override public float getDamageMultiplier() { return 0.000000f; }
    @Override public int getCastTime() { return 10; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.four.description"; }

    @Override
    protected String getToggleId() { return "JUYUAN_MOVING_MOUNTAINS"; }

    @Override
    protected MobEffect resolveBuffEffect(ServerPlayer player) {
        return ModEffects.MOVING_MOUNTAINS.get();
    }

    @Override
    protected int getBuffAmplifier() { return 1; }

    @Override
    protected float getUpkeepMultiplier() { return 10.000000f; }

    @Override
    protected String getActiveMessage() { return "§6§l搬山"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§6§l搬山！"), true);
        ServerLevel serverLevel = level;

        boolean shaderFx = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
            if (shaderFx) {
                ServerPlayer sp = (ServerPlayer) player;
                SkillFieldSender.send(sp, SkillEffectStyles.SHOCK_JUYUAN, sp.getX(), sp.getY(), sp.getZ(), 3.0F, 10);
                SkillFieldSender.send(sp, SkillEffectStyles.BURST_JUYUAN, sp.getX(), sp.getY(), sp.getZ(), 1.2F, 10);
            }
            ParticleFx fx = shaderFx ? null : ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.26;

                fx.budget(2600);
                // 贴地法阵
                fx.magicCircle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 4.0, rot);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), 5.0, 2.0, 40);
                // 头顶悬浮巨石阵：三层高度、每层六块巨石（球面撒点做碎石块）
                double[] heights = {3.0, 4.4, 5.8};
                for (int layer = 0; layer < 3; layer++) {
                    double h = heights[layer];
                    double r = 3.6 - layer * 0.7;
                    for (int i = 0; i < 6; i++) {
                        double a = rot + ParticleFx.TAU * i / 6 + layer * 0.5;
                        Vec3 c = base.add(Math.cos(a) * r, h, Math.sin(a) * r);
                        fx.sphere(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), c, 0.55, 16, 1.0, 0.02);
                        fx.dashedRing(ParticleTypes.SOUL_FIRE_FLAME, c, ParticleFx.Axis.Y, 0.7, 5, 0.5, -rot, 0.03);
                    }
                }
                // 地脉光柱：把“山”从地面拉起（自定义高度 6 格）
                fx.pillars(ParticleTypes.CAMPFIRE_COSY_SMOKE, base, 3.6, 6, 6.0, rot);
                fx.column(ParticleTypes.WHITE_ASH, ParticleTypes.SOUL_FIRE_FLAME, base, 0.9, 6.5, 4, rot, ParticleFx.TAU, 0.5);
                // 抬升气流
                fx.spiral(ParticleTypes.WHITE_ASH, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 4.0, 1.0, 7.0, 2.5, -rot, 130, 0.1);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 1.0, 0), 36, 1.2, true);
                fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 2.0, 0), 30, 2.0, 0.1);
            }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 1.0f, 0.6f);
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
