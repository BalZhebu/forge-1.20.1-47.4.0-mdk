package com.TovidY.kunluncontinent.item.baseskillist.juyuan.six;

import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldSender;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.server.level.ServerPlayer;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class SkillPanshijuyuan6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 450f; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 0; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§6§l第六魂技：裂地！"), true);
            player.setDeltaMovement(0, 1.5, 0);
            player.hurtMarked = true;
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("LieDiActive", true);
            nbt.putFloat("LieDiDamage", finalDamage);

            // ---- 起跳特效：大地被踏碎的上升气流（落地由服务端 Tick 事件接力）----
            boolean shaderFx = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
            if (shaderFx) {
                ServerPlayer sp = (ServerPlayer) player;
                SkillFieldSender.send(sp, SkillEffectStyles.BEAM_STONE_PILLAR, sp.getX(), sp.getY(), sp.getZ(), 1.0F, 20);
                SkillFieldSender.send(sp, SkillEffectStyles.SHOCK_JUYUAN, sp.getX(), sp.getY(), sp.getZ(), 3.2F, 14);
                SkillFieldSender.send(sp, SkillEffectStyles.BURST_JUYUAN, sp.getX(), sp.getY(), sp.getZ(), 1.3F, 12);
            }
            ParticleFx fx = shaderFx ? null : ParticleFx.of(level, player);
            if (fx != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.3;

                fx.budget(1400);
                fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 2.4, 7, rot, 0.15);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05, 0), 3.0, 2.0, 34);
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                        base, new Vec3(0, 1, 0), 2.5, 2.4, 14, rot, 6);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.3, 0), 26, 1.0, true);
                fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 0.4, 0), 24, 1.2, 0.15);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }
}
