package com.TovidY.kunluncontinent.item.baseskillist.juyuan.four;

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

public class SkillPanshijuyuan4 extends BaseSkillItem {
    @Override public float getBaseCost() { return 220f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 800; }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.four.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(ModEffects.MOVING_MOUNTAINS.get(), 400, 1));
            ServerLevel serverLevel = (ServerLevel) level;

            // ---- 特效：搬山 —— 头顶悬浮巨石阵 + 地脉光柱 + 抬升气流 ----
            ParticleFx fx = ParticleFx.of(level, player);
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

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);
            player.displayClientMessage(Component.literal("§8§l第四魂技：搬山！"), true);
        }
    }
}
