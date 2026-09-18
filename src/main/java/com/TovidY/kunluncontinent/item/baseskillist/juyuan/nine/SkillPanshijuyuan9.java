package com.TovidY.kunluncontinent.item.baseskillist.juyuan.nine;

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

public class SkillPanshijuyuan9 extends BaseSkillItem {
    @Override public float getBaseCost() { return 700f; }
    @Override public float getDamageMultiplier() { return 4.5f; }
    @Override public int getCastTime() {return 20;}
    @Override public int getCooldownTicks() { return 1400; }
    @Override public String getDescriptionKey() { return "skill.panshijuyuan.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§6§l第九魂技：不周倾！"), true);
            player.setDeltaMovement(0, 2.5, 0);
            player.hurtMarked = true;
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("BuZhouQing_Active", true);
            nbt.putFloat("BuZhouQing_Damage", finalDamage);

            // ---- 起手特效：不周山拔地而起的气柱（落地由服务端 Tick 事件接力）----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.28;

                fx.budget(2200);
                // 拔地法阵：三层多边形 + 巨型星芒
                fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 6.5, 12, rot, 0.25);
                fx.star(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        base.add(0, 0.2, 0), ParticleFx.Axis.Y, 5.5, 2.2, 8, -rot, 0.1);
                fx.magicCircle(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.WHITE_ASH, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 3.6, rot * 1.6);
                // 自下而上的巨柱 + 环柱
                fx.column(ParticleTypes.CAMPFIRE_COSY_SMOKE, ParticleTypes.SOUL_FIRE_FLAME, base, 1.3, 9.0, 4, rot, ParticleFx.TAU * 0.8, 0.55);
                fx.ringStack(ParticleTypes.LARGE_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 4.6, 9.0, 5, 28, rot, 0.3, 0.9);
                fx.spiral(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 4.4, 0.8, 9.0, 3.0, -rot, 150, 0.1);
                // 四周崩塌的岩屑
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        base, new Vec3(0, 1, 0), 4.5, 4.0, 18, rot, 6);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.8, 0), 48, 2.0, true);
                fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 1.5, 0), 34, 3.0, 0.2);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }
}
