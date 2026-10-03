package com.TovidY.kunluncontinent.capability.playerattributes;

import net.minecraft.world.entity.player.Player;

/**
 * 飞行速度档位 —— 唯一权威表。
 *
 * <p>玩家在技能面板左侧用 ± 按钮调档位：<b>越快，飞行时精神力消耗越高</b>。
 * 玩法侧（{@code PWPlayerTickEvent#updatePlayerFly}）和界面侧
 * （{@code SkillWheelScreen}）都只读这一份表，改数值只需动这里。</p>
 *
 * <p>档位 0 是原版默认（{@code 0.05F}，即创造模式飞行速度），因此
 * <b>老存档 / 新玩家不调档时手感与改动前完全一致</b>。</p>
 *
 * <p>⚠️ 三张表的<b>长度必须一致</b>（下标 = 档位）。{@link #MAX_LEVEL}
 * 由数组长度自动推导，加档位时只要三张表同步加一项，不会再出现
 * "MAX_LEVEL 超数组长度 → 调档直接数组越界崩溃"。</p>
 */
public final class FlySpeedTuning {

    private FlySpeedTuning() {
    }

    /** 各档的飞行速度倍率，下标 = 档位。 */
    private static final float[] SPEED_MULTIPLIER = {1.0F, 1.8F, 2.8F, 4.2F, 6.0F, 8.0F, 10.0F};

    /** 各档的精神力消耗倍率，下标 = 档位。 */
    private static final float[] COST_MULTIPLIER = {1.0F, 1.6F, 2.4F, 3.4F, 4.5F, 5.5F, 7.0F};

    /** 各档展示名，下标 = 档位。 */
    private static final String[] LEVEL_NAMES = {"初阶", "疾行", "迅捷", "极速", "神速", "电掣", "瞬影"};

    /** 最高档位下标 —— **由数组长度推导**，不要再手写数字。 */
    public static final int MAX_LEVEL = SPEED_MULTIPLIER.length - 1;

    /** 原版默认飞行速度（创造模式）。 */
    public static final float BASE_SPEED = 0.05F;

    /** 触发撞墙爆炸的最低档位下标（"神速"=4）。低于此档撞墙只有原版画墙擦伤。 */
    public static final int CRASH_MIN_LEVEL = 4;

    /**
     * 撞墙起爆的**最低实际速度**（格/tick）。
     *
     * <p>用来实现"必须是速度已经提起来"：刚起飞/低速蹭墙不触发。
     * 水平满速约等于 {@code flyingSpeed × 8}（{@code Player#getFlyingSpeed}
     * 冲刺再 ×2），所以这个门槛大约相当于"该档满速的 55%"。</p>
     */
    public static final float CRASH_MIN_SPEED = 0.22F;

    /** 撞墙后再次可触发的冷却（tick），避免贴着墙滑行时连环爆炸。 */
    public static final int CRASH_COOLDOWN_TICKS = 20;

    /** 爆炸对周围造成的伤害 = 玩家面板攻击力 × 本系数 × 速度增伤系数。 */
    public static final float CRASH_DAMAGE_RATIO = 1.0F;

    /** 爆炸波及半径（格），随档位略增。 */
    public static final float CRASH_RADIUS = 3.2F;

    /** 爆炸伤害反作用到自身的概率。 */
    public static final float CRASH_SELF_DAMAGE_CHANCE = 0.45F;

    private static int clamp(int level) {
        return Math.max(0, Math.min(MAX_LEVEL, level));
    }

    /** 该档的飞行速度倍率。 */
    public static float speedMultiplier(int level) {
        return SPEED_MULTIPLIER[clamp(level)];
    }

    /** 该档的精神力消耗倍率（越快越费）。 */
    public static float costMultiplier(int level) {
        return COST_MULTIPLIER[clamp(level)];
    }

    /** 该档的实际飞行速度（直接写进 {@code Abilities#setFlyingSpeed}）。 */
    public static float flyingSpeed(int level) {
        return BASE_SPEED * speedMultiplier(level);
    }

    /** 该档的展示名。 */
    public static String levelName(int level) {
        return LEVEL_NAMES[clamp(level)];
    }

    /** 该档是否达到"神速以上"（撞墙会引爆）。 */
    public static boolean isCrashArmed(int level) {
        return clamp(level) >= CRASH_MIN_LEVEL;
    }

    /**
     * 按当前<b>实际水平速度</b>算爆炸伤害增伤系数。
     *
     * <p>以"该档理论满速"为 1.0 基准，实际速度占满速的比例线性映射到
     * {@code 1.0 ~ 2.2}：撞得越快、威力越大；速度不足满速一半时也不会低于 1.0。</p>
     */
    public static float crashSpeedFactor(int level, double horizontalSpeed) {
        float full = Math.max(1.0E-4F, flyingSpeed(level) * 8.0F);
        float ratio = (float) Math.min(1.0D, horizontalSpeed / full);
        return 1.0F + ratio * 1.2F;
    }

    /**
     * 把该档的飞行速度写进玩家能力，并同步给客户端。
     *
     * <p>飞行速度在 {@code ClientboundPlayerAbilitiesPacket} 里，
     * 所以改完必须调 {@code onUpdateAbilities()} 才会真正生效。</p>
     */
    public static void applyFlyingSpeed(Player player, int level) {
        if (player == null) return;
        float target = flyingSpeed(level);
        if (Math.abs(player.getAbilities().getFlyingSpeed() - target) < 1.0E-5F) return;
        player.getAbilities().setFlyingSpeed(target);
        player.onUpdateAbilities();
    }
}
