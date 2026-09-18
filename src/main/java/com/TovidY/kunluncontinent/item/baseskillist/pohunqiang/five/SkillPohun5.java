package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.five;

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

public class SkillPohun5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 350f; }
    @Override public float getDamageMultiplier() { return 1.88f; }
    @Override public int getCastTime() { return 60; }
    @Override public int getCooldownTicks() { return 840; }
    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.five.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§6§l第五魂技：碎星！"), true);
            CompoundTag nbt = player.getPersistentData();
            nbt.putInt("SuiXingTimer", 100);
            nbt.putFloat("SuiXingDamage", finalDamage);

            // ---- 特效：领域展开 —— 脚下星辉巨型法阵 + 十五格轨道环 + 上升星尘 ----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                ServerLevel serverLevel = (ServerLevel) level;
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.3;

                fx.budget(2200);
                // 巨型星辉法阵（三层，带高度差形成“立体阵”）
                fx.magicCircle(ParticleTypes.SOUL, ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.15, 0), ParticleFx.Axis.Y, 15.0, rot, 3, 0.35);
                // 十五格边界轨道环
                fx.dashedRing(ParticleTypes.END_ROD, base.add(0, 0.25, 0), ParticleFx.Axis.Y, 15.0, 24, 0.35, -rot * 1.6, 0.05);
                // 内圈高密度光环，与法阵形成同心层次
                fx.circle(ParticleTypes.CRIMSON_SPORE, base.add(0, 0.4, 0), ParticleFx.Axis.Y, 7.5, 60, rot * 2.0, 0.08);
                // 星尘上升
                fx.spiral(ParticleTypes.SOUL, base.add(0, 0.3, 0), ParticleFx.Axis.Y, 3.5, 0.4, 6.0, 3.0, rot, 140, 0.12);
                // 中心爆发
                fx.burst(ParticleTypes.ENCHANTED_HIT, base.add(0, 0.6, 0), 36, 1.1, true);
                fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), 6.0, 2.0, 44);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}
