package com.TovidY.kunluncontinent.capability.playerattributes;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * 飞行撞墙爆炸 —— 玩法工具类。
 *
 * <p>规则（数值全部在 {@link FlySpeedTuning}）：</p>
 * <ol>
 *   <li>飞行档位达到 <b>神速及以上</b>（{@code FlySpeedTuning#CRASH_MIN_LEVEL}）；</li>
 *   <li>并且 <b>实际水平速度已提起来</b>（≥ {@code CRASH_MIN_SPEED}）——
 *       刚起飞或低速蹭墙不会触发；</li>
 *   <li>撞上方块 ⇒ 以撞点为中心爆炸，伤害 = <b>玩家面板攻击力</b>
 *       × {@code CRASH_DAMAGE_RATIO} × 速度增伤系数（{@link #crashSpeedFactor}），
 *       攻击力和档位越高，威力越强；</li>
 *   <li><b>35% 概率</b>伤害反作用到自己（{@code CRASH_SELF_DAMAGE_CHANCE}）；
 *       若这次反伤致死，播一条随机"撞墙死法"提示（见 {@link #DEATH_QUOTES}）。</li>
 * </ol>
 *
 * <p>冷却 {@code CRASH_COOLDOWN_TICKS} 存在玩家 persistentData，
 * 避免贴着墙滑行时每 tick 连炸。</p>
 */
public final class FlyCrashBlast {

    private FlyCrashBlast() {
    }

    private static final Random RANDOM = new Random();

    // ==================== 冷却（存玩家 persistentData，不写 capability） ====================

    private static final String KEY_COOLDOWN = "FlyCrash_Cooldown";

    /**
     * 每 tick 调用一次：递减冷却。
     *
     * <p>冷却<b>必须在服务端递减</b>（客户端包只负责触发，服务端持有权威冷却），
     * 所以由 {@code PWPlayerTickEvent#updatePlayerFly} 调用。</p>
     */
    public static void tickCooldown(Player player) {
        var data = player.getPersistentData();
        if (!data.contains(KEY_COOLDOWN)) return;
        int left = data.getInt(KEY_COOLDOWN) - 1;
        if (left <= 0) {
            data.remove(KEY_COOLDOWN);
        } else {
            data.putInt(KEY_COOLDOWN, left);
        }
    }

    /** 冷却是否已好（冷却中返回 false）。 */
    public static boolean isCooldownReady(Player player) {
        return player.getPersistentData().getInt(KEY_COOLDOWN) <= 0;
    }

    private static void startCooldown(Player player) {
        player.getPersistentData().putInt(KEY_COOLDOWN, FlySpeedTuning.CRASH_COOLDOWN_TICKS);
    }

    // ==================== 触发判定 ====================

    /**
     * 服务端触发入口（客户端包 {@code CPacketFlyCrash} 走的就是这条路径）。
     *
     * <p>冷却在<b>真正引爆时</b>才启动 —— 包的处理侧还会再复核一次
     * {@link #isCooldownReady}，避免"判定通过但还没上冷却"被连发绕过。</p>
     *
     * @param player           玩家（服务端）
     * @param horizontalSpeed  <b>撞墙前一刻</b>的水平速度（格/tick）
     * @param impactPos        撞点
     */
    public static void tryTrigger(Player player, double horizontalSpeed, Vec3 impactPos) {
        if (!player.getAbilities().flying) return;
        int level = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::getFlySpeedLevel).orElse(0);
        if (!FlySpeedTuning.isCrashArmed(level)) return;
        if (horizontalSpeed < FlySpeedTuning.CRASH_MIN_SPEED) return;
        if (!isCooldownReady(player)) return;

        startCooldown(player);
        detonate(player, horizontalSpeed, impactPos);
    }

    // ==================== 爆炸本体 ====================

    /**
     * 执行爆炸：视觉 + 伤害 + 35% 反伤。
     *
     * @param horizontalSpeed 撞墙前一刻的水平速度（格/tick），用于速度增伤
     * @param center          爆炸中心
     */
    public static void detonate(Player player, double horizontalSpeed, Vec3 center) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        int speedLevel = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::getFlySpeedLevel).orElse(0);

        // 伤害 = 面板攻击力 × 系数 × 速度增伤
        float gongji = ModAttributeAPI.getGongji(player);
        float speedFactor = FlySpeedTuning.crashSpeedFactor(speedLevel, horizontalSpeed);
        float damage = gongji * FlySpeedTuning.CRASH_DAMAGE_RATIO * speedFactor;
        if (damage < 1.0F) damage = 1.0F;

        float radius = FlySpeedTuning.CRASH_RADIUS;

        // ---- 视觉与音效（走统一粒子工具类）----
        ParticleFx fx = ParticleFx.of(serverLevel, player);
        if (fx != null) {
            fx.budget(900);
            // 中心爆核 + 递减冲击环 + 打散断环 + 放射火花 + 上升烟柱
            fx.bloom(ParticleTypes.EXPLOSION_EMITTER, center, 8, 0.5, 0.35);
            fx.shockRing(ParticleTypes.EXPLOSION, center, radius * 0.55, 0.35, 40);
            fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, center, radius * 0.85, 0.25, 32);
            fx.dashedRing(ParticleTypes.SOUL, center, ParticleFx.Axis.Y, radius, 12, 0.45, 0.0, 0.05);
            fx.burst(ParticleTypes.SOUL_FIRE_FLAME, center, 60, radius * 0.9, true);
            fx.vortex(ParticleTypes.CLOUD, center, ParticleFx.Axis.Y, radius * 0.8, 1.6, 5, 26, 0.0, 0.55);
        }
        serverLevel.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 0.7F);
        serverLevel.playSound(null, center.x, center.y, center.z,
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.6F, 0.6F);

        // ---- 伤害周围生物（排除自己：反伤单独处理）----
        AABB area = player.getBoundingBox().inflate(radius + 2.0D);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != player);
        for (LivingEntity target : targets) {
            double dist = target.distanceToSqr(center);
            if (dist > (radius + 2.0D) * (radius + 2.0D)) continue;
            // 距离衰减：越远伤害越低（中心全额，边缘 40%）
            float falloff = (float) (1.0D - 0.6D * Math.min(1.0D, Math.sqrt(dist) / (radius + 2.0D)));
            // 击退
            Vec3 push = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(center)
                    .normalize().scale(0.9D * falloff);
            target.push(push.x, 0.35D, push.z);
            target.hurtMarked = true;
            // 走 playerAttack：能吃到攻击力加成、暴击、飘字
            target.hurt(player.damageSources().playerAttack(player), damage * falloff);
        }

        // ---- 35% 概率反作用到自己 ----
        // 走【专属撞墙伤害源】：致死时原版死亡界面直接显示我们定的自嘲文案（4 选 1），
        // 不额外发任何系统消息。
        if (RANDOM.nextFloat() < FlySpeedTuning.CRASH_SELF_DAMAGE_CHANCE) {
            float selfDamage = damage * 0.6F;
            FlyCrashDamageSource.hurtPlayer(serverLevel, player, selfDamage);
            // 反作用力提示：用**原版 ActionBar**（物品栏上方那条），不自己画 HUD
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal(
                        "§c✖ 反作用力 §7· §f自身受到 §c"
                                + String.format("%.1f", selfDamage) + " §f点伤害"), true);
            }
        }
    }
}
