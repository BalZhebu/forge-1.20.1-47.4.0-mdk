package com.TovidY.kunluncontinent.item.baseskillist.juyuan.five;

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

public class SkillPanshijuyuan5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 280f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 800; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§8§l第五魂技：石铠！"), true);
            player.addEffect(new MobEffectInstance(ModEffects.STONE_ARMOR.get(), 400, 1));

            // ---- 特效：石铠 —— 环绕旋转的石板阵列逐块合体成甲 ----
            ParticleFx fx = ParticleFx.of(level, player);
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

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1.2f, 0.6f);
        }
    }
}
