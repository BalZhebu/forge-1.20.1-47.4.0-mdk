package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.nine;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import static com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem.dealSkillDamage;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SkillPohun9 extends BaseSkillItem {

    /** 光刃特效时长（tick）。 */
    private static final int BLADE_TICKS = 30;

    @Override public float getBaseCost() { return 750F; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 25; }

    @Override
    public String getDescriptionKey() { return "skill.pohunqiang.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            Vec3 end = start.add(look.scale(12));
            player.displayClientMessage(Component.literal("§4§l第九魂技：弑神！"), true);
            AABB targetBox = new AABB(start, end).inflate(1.5);
            // 路径上**所有**生物都挨刀 —— 不再只挑最近的那一个。
            // 每个目标各自掷一次斩杀骰，所以一道光刃横扫过去有可能同时斩掉好几个。
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, targetBox,
                    e -> e != player && e.isAlive() && player.distanceTo(e) <= 12.0);

            // ---- 特效：指向前方的死神射线（细针状光柱 + 螺旋缠绕 + 空间涟漪）----
            // 粒子 / 着色器二选一（全局开关，见 SkillFxToggle）。
            // 后面斩杀 / 未斩杀两段的附加特效也靠 fx == null 自动跳过。
            boolean shaderMode = SkillFxToggle.useShader(player);
            ParticleFx fx = shaderMode ? null : ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.45;

            if (shaderMode) {
                // ---- 表现 A：着色器版 —— 一道劈出去的光刃 ----
                // 用专用着色器 skill_blade（月牙轮廓 + 上下锐利刃口 + 白热核心线），
                // 比通用能量带更有"劈斩"的气势。方向取发包那一刻的视线，
                // 起点抬到眼睛高度（预设里的 startHeight）。
                SkillFieldFx.atEntity(player, SkillEffectStyles.POHUN9_BLADE, 1.0F, BLADE_TICKS);
            } else if (fx != null) {
                // ---- 表现 B：粒子版（默认）----
                fx.budget(2200);
                // 射线核心：极细高亮 + 暗紫剪影
                fx.line(ParticleTypes.FLASH, start, end, 2.0, 0.0, 0.0, Vec3.ZERO);
                fx.line(ParticleTypes.REVERSE_PORTAL, start, end, 0.3, 0.05, 0.0, Vec3.ZERO);
                fx.line(ParticleTypes.SQUID_INK, start, end, 0.7, 0.15, 0.0, Vec3.ZERO);
                // 三股螺旋缠绕射线
                fx.helixAround(ParticleTypes.SOUL_FIRE_FLAME, start, look, 0.45, 12.0, 9.0, 3, rot, 60);
                // 沿射线的空间涟漪环
                for (double d = 1.5; d <= 12.0; d += 1.5) {
                    fx.ringAround(ParticleTypes.SCULK_SOUL, start.add(look.scale(d)), look, 0.55, 16, rot + d * 0.4, 0.03);
                }
            }

            for (LivingEntity target : targets) {
                float baseChance = 0.05f; // 基础 5%
                if (target instanceof Player targetPlayer) {
                    int targetLevel = ModAttributeAPI.getDengji(targetPlayer);
                    float levelBonus = targetLevel * 0.0015f;
                    baseChance = Math.min(0.20f, baseChance + levelBonus); // 最高 20%
                }
                Vec3 hitPos = target.position().add(0, target.getBbHeight() * 0.55, 0);
                DamageSource killSource = player.damageSources().playerAttack(player);
                if (level.random.nextFloat() < baseChance) {
                    player.sendSystemMessage(Component.literal("§c§l[触发弑神斩杀 - 概率: " + (int)(baseChance * 100) + "%]"));
                    target.hurt(killSource, Float.MAX_VALUE);
                    if (target.isAlive()) {
                        target.setHealth(0f);
                        target.die(killSource);
                    }
                    if (fx != null) {
                        // 斩杀的“神陨”特效：十字斩 + 血色法阵 + 死亡冲击环
                        fx.magicCircle(ParticleTypes.CRIMSON_SPORE, ParticleTypes.SOUL_FIRE_FLAME, hitPos, ParticleFx.Axis.Y, 3.0, rot);
                        fx.crossSlash(ParticleTypes.SONIC_BOOM, hitPos, look, new Vec3(0, 1, 0), 2.6, 150, 3);
                        fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, hitPos, 4.0, 2.5, 40);
                        fx.sphereBlades(ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME, hitPos, 1.4, 18, 1.4, rot);
                    }
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1, target.getZ(), 2, 0, 0, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                } else {
                    dealSkillDamage(target, finalDamage, killSource);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1)); // 虚弱2
                    if (fx != null) {
                        // 未斩杀：目标身上浮现被压制的血色锁环
                        fx.dashedRing(ParticleTypes.CRIMSON_SPORE, hitPos, ParticleFx.Axis.Y, 1.0, 8, 0.5, rot, 0.05);
                        fx.dashedRing(ParticleTypes.SOUL, hitPos, ParticleFx.Axis.Z, 1.0, 8, 0.5, -rot, 0.05);
                    }
                }
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5, 0.5, 0.5, 0.5);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}
