package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.eight;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkillLeijinhu8 extends BaseSkillItem {
    @Override public float getBaseCost() { return 750f; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 1200; }

    @Override
    public String getDescriptionKey() { return "skill.leijinhu.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("Liejinhu8_Active", true);
            nbt.putInt("Liejinhu8_Remaining", 10);
            nbt.putInt("Liejinhu8_Timer", 0);
            nbt.putFloat("Liejinhu8_Damage", finalDamage);

            player.displayClientMessage(Component.literal("§e§l第八魂技：裂天！"), true);

            // ---- 起手式：金色斩天阵（后续十次脉冲由服务端 Tick 事件接力）----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                Vec3 base = player.position();
                Vec3 look = player.getLookAngle();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = ParticleFx.ortho(look);
                double rot = serverLevel.getGameTime() * 0.4;

                fx.budget(2400);
                // 地面金色大阵（自定义高度的三层）
                fx.magicCircle(ParticleTypes.CRIT, ParticleTypes.GLOW, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 6.0, rot, 3, 0.6);
                fx.polygon(ParticleTypes.ELECTRIC_SPARK, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 6.5, 12, -rot, 0.03);
                fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.05, 0), 7.0, 2.5, 48);
                // 前方三道斩天爪芒
                for (int i = -1; i <= 1; i++) {
                    Vec3 c = player.getEyePosition().add(right.scale(i * 1.2)).add(0, -0.2, 0);
                    fx.slash(ParticleTypes.SWEEP_ATTACK, c, look, right, 4.0, 160, 3, rot + i * 0.3);
                    fx.slash(ParticleTypes.GLOW, c, look, right, 3.6, 160, 1, rot + i * 0.3);
                }
                fx.burst(ParticleTypes.CRIT, base.add(0, 1.0, 0), 36, 1.3, true);
                fx.bloom(ParticleTypes.WAX_ON, base.add(0, 1.2, 0), 26, 1.0, 0.2);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 1.2f);
        }
    }
}
