package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.eight;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import static com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem.dealSkillDamage;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CSkillFieldPacket;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class SkillBahuang8 extends BaseSkillItem {

    /**
     * 雷柱的视觉半宽（格）。<b>不是判定范围</b> —— 判定是 50 格内全体。
     * 这个值通过 {@code S2CSkillFieldPacket} 的 {@code radius} 字段传给客户端。
     *
     * <p>取 3.0 是和 {@code skill_bolt.fsh} 里的折线摆幅配套的：折线在归一化空间里
     * 最多摆到 ±0.75，再加辉光的扩散，正好铺满 ±1 而不会被四边形边界硬裁。</p>
     */
    private static final float BOLT_VISUAL_RADIUS = 3.0F;

    /**
     * 雷柱特效的持续时间（tick）。
     * <p>雷是瞬发的，不该像地阵那样持续几秒：3 tick 白热 + 18 tick 余辉。</p>
     */
    private static final int BOLT_FX_TICKS = 22;

    @Override public float getBaseCost() { return 800f; }
    @Override public float getDamageMultiplier() { return 3.8f; }
    @Override public int getCastTime() { return 20; }

    @Override
    public String getDescriptionKey() { return "skill.bahuangji.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.displayClientMessage(Component.literal("§4§l第八魂技：降雷！"), true);

            // ---- 粒子 / 着色器二选一（全局开关，见 SkillFxToggle）----
            // 着色器模式下 ParticleFx 直接不建，一个粒子都不发。
            boolean shaderMode = SkillFxToggle.useShader(player);
            ParticleFx fx = shaderMode ? null : ParticleFx.of(level, player);
            double rot = serverLevel.getGameTime() * 0.45;
            if (fx != null) {
                fx.budget(2400);
            }

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50.0),
                    e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                dealSkillDamage(target, finalDamage, player.damageSources().playerAttack(player));

                if (shaderMode) {
                    // ---- 表现 A：着色器雷柱（玩家关掉了「技能特效优化」）----
                    // 每个目标一道。发给「追踪该目标的玩家」而不是以施法者为锚：
                    // 50 格边缘的目标，看得见它的玩家未必在施法者的追踪列表里。
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY.with(() -> target),
                            new S2CSkillFieldPacket(
                                    SkillEffectStyles.BAHUANG8_BOLT,
                                    target.getId(),
                                    target.getX(), target.getY(), target.getZ(),
                                    BOLT_VISUAL_RADIUS, BOLT_FX_TICKS));
                } else if (fx != null) {
                    // ---- 表现 B：粒子雷（默认）----
                    Vec3 tp = target.position();
                    Vec3 head = tp.add(0, 20.0, 0);
                    // 头顶雷云环（打散环 → 乌云裂隙感）
                    fx.dashedRing(ParticleTypes.CLOUD, target.position().add(0, 20.0, 0), ParticleFx.Axis.Y, 2.4, 9, 0.55, rot, 0.25);
                    fx.dashedRing(ParticleTypes.GLOW, target.position().add(0, 19.4, 0), ParticleFx.Axis.Y, 1.6, 7, 0.5, -rot, 0.12);
                    // 主雷柱：锯齿状闪电
                    fx.lightning(ParticleTypes.ELECTRIC_SPARK, head, tp, 22, 1.6, rot + target.getId());
                    // 分叉雷
                    for (int k = 0; k < 3; k++) {
                        double a = rot + ParticleFx.TAU * k / 3;
                        Vec3 from = head.add(Math.cos(a) * 1.2, 0, Math.sin(a) * 1.2);
                        fx.lightning(ParticleTypes.ELECTRIC_SPARK, from, tp.add(0, 0.5, 0), 16, 1.2, a * 2.3);
                    }
                    // 落点：电环 + 光晕
                    fx.shockRing(ParticleTypes.GLOW, tp.add(0, 0.1, 0), 2.2, 2.0, 30);
                    fx.ringStack(ParticleTypes.ELECTRIC_SPARK, tp.add(0, 0.1, 0), ParticleFx.Axis.Y, 1.6, 3.0, 4, 20, rot, 1.6, 0.7);
                    fx.burst(ParticleTypes.ELECTRIC_SPARK, tp.add(0, 1.0, 0), 26, 0.9, true);
                    fx.dot(ParticleTypes.GLOW_SQUID_INK, tp.add(0, 1.0, 0));
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 3.0f, 0.8f);
        }
    }
}
