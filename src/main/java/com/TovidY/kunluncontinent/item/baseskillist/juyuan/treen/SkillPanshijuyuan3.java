package com.TovidY.kunluncontinent.item.baseskillist.juyuan.treen;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillPanshijuyuan3 extends BaseSkillItem {
    @Override public float getBaseCost() { return 160f; }
    @Override public float getDamageMultiplier() { return 1.9f; }
    @Override public int getCastTime() { return 40; }
    @Override public int getCooldownTicks() { return 360; }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            float playerGongji = ModAttributeAPI.getGongji(player);
            float finalDamage = playerGongji * getDamageMultiplier() * powerMultiplier;
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0),
                    e -> e != player && e.isAlive() && e.distanceTo(player) <= 10.0);

            ParticleFx fx = ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.28;
            if (fx != null) {
                fx.budget(2400);
            }

            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().explosion(player, player), finalDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                target.push(0, 0.3, 0);
                if (fx != null) {
                    // 被震者脚下炸开的碎岩与裂纹
                    Vec3 tp = target.position();
                    fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, tp.add(0, 0.1, 0), ParticleFx.Axis.Y, 1.6, 6, -rot, 0.15);
                    fx.burst(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), tp.add(0, 0.4, 0), 16, 0.5, true);
                }
            }

            // ---- 主特效：十格震地 —— 四重扩散环 + 地纹多边形 + 十二向土柱 ----
            if (fx != null) {
                Vec3 base = player.position();
                fx.polygon(ParticleTypes.SMOKE, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 10.0, 10, rot, 0.25);
                fx.polygon(ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 6.5, 8, -rot * 1.3, 0.2);
                // 由内向外四重扩散环（对应半径 2.5 / 5 / 7.5 / 10）
                for (int r = 1; r <= 4; r++) {
                    fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.04 * r, 0), 2.5 * r, 2.0, 44);
                }
                fx.dashedRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.3, 0), ParticleFx.Axis.Y, 10.0, 16, 0.5, rot * 1.5, 0.15);
                // 十二根震波柱
                fx.pillars(ParticleTypes.CAMPFIRE_COSY_SMOKE, base, 10.0, 12, 2.5, rot);
                fx.cone(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        base, new Vec3(0, 1, 0), 4.0, 3.0, 18, rot, 6);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.5, 0), 44, 1.6, true);
                fx.bloom(ParticleTypes.WHITE_ASH, base.add(0, 0.8, 0), 36, 3.0, 0.15);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.5f);
            player.displayClientMessage(Component.literal("§8§l第三魂技：震地！"), true);
        }
    }
}
