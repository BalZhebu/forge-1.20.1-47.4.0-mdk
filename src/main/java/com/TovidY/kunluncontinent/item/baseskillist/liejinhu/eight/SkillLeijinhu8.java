package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.eight;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 裂金虎 · 第八魂技「裂天」。
 *
 * <h3>技能本身</h3>
 * <p>吟唱 0.5 秒后开启裂天：<b>每 10 tick 对自身 20 格内的所有敌人各砍一刀，共 10 轮</b>。
 * 伤害结算在服务端 tick 里（见 {@code PWPlayerTickEvent#handleLiejinhuEight}），
 * 本类只负责开启任务。</p>
 *
 * <h3>着色器版的表现（本类发不了，在 tick 里发）</h3>
 * <p>着色器版是「<b>每一轮脉冲，在每个被命中生物体内炸开一次金光</b>」——
 * 它跟着伤害走，所以必须发在 {@code handleLiejinhuEight} 的伤害循环里，
 * 而不是在释放那一刻发一个大范围地面法阵。</p>
 *
 * <p>刻意<b>不再有地面大阵</b>：一是原来那个九边+星芒的法阵视觉上太杂，
 * 二是「谁被打了就在谁体内炸」本身就说明了范围，比画一圈大阵更直观。</p>
 */
public class SkillLeijinhu8 extends BaseSkillItem {

    /**
     * 「体内爆裂」的视觉半径（格）—— 是四边形<b>半边长</b>，不是判定范围。
     *
     * <p>判定范围是 20 格。1.25 大约是一只普通怪的身体半径，
     * 四边形正好把生物包住，撑出轮廓的那部分露在模型外面。</p>
     */
    public static final float BURST_RADIUS = 1.25F;

    /**
     * 每次爆裂的时长（tick）。
     *
     * <p>和脉冲周期（10 tick）对齐 —— 一次脉冲一次爆闪，下一轮来的时候上一发已经淡完，
     * 不会糊成一片连续的白光。</p>
     */
    public static final int BURST_TICKS = 10;

    /** 裂天的轮数。与 {@code handleLiejinhuEight} 里的计数保持一致。 */
    public static final int PULSES = 10;

    @Override public float getBaseCost() { return 750f; }
    @Override public float getDamageMultiplier() { return 2.8f; }
    @Override public int getCastTime() { return 10; }

    @Override
    public String getDescriptionKey() { return "skill.leijinhu.eight.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("Liejinhu8_Active", true);
            nbt.putInt("Liejinhu8_Remaining", PULSES);
            nbt.putInt("Liejinhu8_Timer", 0);
            nbt.putFloat("Liejinhu8_Damage", finalDamage);

            player.displayClientMessage(Component.literal("§e§l第八魂技：裂天！"), true);

            // ⚠️ 着色器版这里什么都不发 —— 它的表现是"每轮脉冲在每个被命中生物体内炸一次"，
            //    必须跟着伤害一起发（见 PWPlayerTickEvent#handleLiejinhuEight）。
            if (!SkillFxToggle.useShader(player)) {
                // ---- 粒子版（默认）----
                ParticleFx fx = ParticleFx.of(level, player);
                if (fx != null) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    Vec3 base = player.position();
                    Vec3 look = player.getLookAngle();
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
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 1.2f);
        }
    }
}
