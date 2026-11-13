package com.TovidY.kunluncontinent.item.baseskillist.juyuan.two;

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
public class SkillPanshijuyuan2 extends BaseBuffSkill {

    @Override public float getBaseCost() { return 150f; }
    @Override public float getDamageMultiplier() { return 0.000000f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.two.description"; }

    @Override
    protected String getToggleId() { return "JUYUAN_STONE_SKIN"; }

    @Override
    protected MobEffect resolveBuffEffect(ServerPlayer player) {
        return ModEffects.STONE_SKIN.get();
    }

    @Override
    protected int getBuffAmplifier() { return 0; }

    @Override
    protected float getUpkeepMultiplier() { return 8.000000f; }

    @Override
    protected String getActiveMessage() { return "§6§l石肤"; }

    @Override
    protected void playActivateFx(ServerLevel level, ServerPlayer player) {
        player.displayClientMessage(Component.literal("§6§l石肤！"), true);
        boolean shaderFx = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
            if (shaderFx) {
                ServerPlayer sp = (ServerPlayer) player;
                SkillFieldSender.send(sp, SkillEffectStyles.SHOCK_JUYUAN, sp.getX(), sp.getY(), sp.getZ(), 2.6F, 12);
            }
            ParticleFx fx = shaderFx ? null : ParticleFx.of(level, player);
            if (fx != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                Vec3 base = player.position();
                Vec3 hub = base.add(0, 1.0, 0);
                double rot = serverLevel.getGameTime() * 0.3;

                fx.budget(2000);
                // 环绕岩球（球面均匀撒点 → 包裹全身的碎石层）
                fx.sphere(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), hub, 1.6, 60, 1.0, 0.02);
                fx.sphere(ParticleTypes.SOUL_FIRE_FLAME, hub, 1.75, 34, 1.0, 0.02);
                // 石壳穹顶：一圈圈向上收拢的护罩
                fx.dome(ParticleTypes.CAMPFIRE_COSY_SMOKE, base, 2.2, 5, 22);
                fx.dome(ParticleTypes.WHITE_ASH, base, 1.7, 4, 18);

                fx.ringStack(ParticleTypes.LARGE_SMOKE, base.add(0, 0.08, 0), ParticleFx.Axis.Y, 2.4, 2.4, 3, 26, rot, 0.35, 0.5);
                fx.shockRing(ParticleTypes.WHITE_ASH, base.add(0, 0.05, 0), 3.0, 1.6, 34);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, hub, 26, 0.7, true);
            }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);
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
