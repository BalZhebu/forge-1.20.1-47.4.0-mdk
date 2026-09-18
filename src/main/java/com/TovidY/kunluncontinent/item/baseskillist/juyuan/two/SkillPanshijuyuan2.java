package com.TovidY.kunluncontinent.item.baseskillist.juyuan.two;

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

public class SkillPanshijuyuan2 extends BaseSkillItem {
    public SkillPanshijuyuan2() {
        super();
    }

    @Override
    public int getCastTime() {
        return 0; // 瞬发技能，强化自身
    }

    @Override
    public int getCooldownTicks() {
        return 400; // 20秒冷却 (20 * 20)
    }

    @Override
    public float getDamageMultiplier() {
        return 0f;
    }

    @Override
    public float getBaseCost() {
        return 100f;
    }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.two.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(ModEffects.STONE_SKIN.get(), 200, 0));

            // ---- 特效：石肤 —— 环绕岩球汇聚 + 石壳穹顶 + 贴地尘环 ----
            ParticleFx fx = ParticleFx.of(level, player);
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
                // 贴地尘环（自定义高度的环柱，收束成“石”的厚重感）
                fx.ringStack(ParticleTypes.LARGE_SMOKE, base.add(0, 0.08, 0), ParticleFx.Axis.Y, 2.4, 2.4, 3, 26, rot, 0.35, 0.5);
                fx.shockRing(ParticleTypes.WHITE_ASH, base.add(0, 0.05, 0), 3.0, 1.6, 34);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, hub, 26, 0.7, true);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);

            player.displayClientMessage(Component.literal("§6§l第二魂技：石肤！"), true);
        }
    }
}
