package com.TovidY.kunluncontinent.special.lonebamboo;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CPoleVaultPacket;
import com.TovidY.kunluncontinent.special.SpecialSoulRingEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

/**
 * 撑杆跳 —— 孤竹魂环赋予的<b>娱乐魂技</b>。
 *
 * <p>把玩家竖直速度顶上去，原地弹起约 6 格；单纯图个乐，不造成任何伤害。
 * 是「特殊魂技」的样板实现（{@link BaseSkillItem#getSpecialCooldownTicks()} /
 * {@link BaseSkillItem#getSpecialFixedCost()} 两个覆盖点就是为它这类技能开的）。</p>
 *
 * <h3>数值</h3>
 * <table border="1">
 *   <tr><th>项</th><th>值</th><th>说明</th></tr>
 *   <tr><td>吟唱</td><td>0 tick</td><td>瞬发</td></tr>
 *   <tr><td>冷却</td><td>10 tick = 0.5 秒</td><td><b>突破</b>全局 3 秒锁定</td></tr>
 *   <tr><td>精神力</td><td>固定 10 点</td><td>不吃年限系数与全局倍率</td></tr>
 *   <tr><td>跳跃</td><td>约 6 格</td><td>{@link #JUMP_VELOCITY}</td></tr>
 * </table>
 */
public class SkillPoleVault extends BaseSkillItem {

    /**
     * 起跳初速度（格 / tick）。
     *
     * <p>按 MC 玩家竖直物理逐 tick 推演得到：{@code v0 = 0.42}（原版跳跃）→ 峰值 1.25 格，
     * 而 {@code v0 = 1.00} → 峰值约 <b>5.95 格</b>。想改高度直接改这个数：
     * 大致是 {@code 峰值 ≈ 6 × v0²}。</p>
     */
    public static final float JUMP_VELOCITY = 1.00F;

    /** 冷却：10 tick = 0.5 秒。 */
    public static final int COOLDOWN_TICKS = 10;

    /** 固定精神力消耗。 */
    public static final float MP_COST = 10.0F;

    /** 免坠落伤害窗口（tick）。6 格起跳滞空约 30 tick，给 100 tick 足够宽裕。 */
    private static final int FALL_GUARD_TICKS = 100;

    /**
     * 「双手举杆 + 一根竹竿」的显示时长（tick）。
     * <p>起跳滞空约 26 tick，多给的这些是收尾余韵，免得竹竿"啪"地凭空消失。</p>
     */
    public static final int VISUAL_DURATION_TICKS = 40;

    @Override
    public float getBaseCost() {
        // 只作为「设计基准值」展示；实际扣费走 getSpecialFixedCost()
        return MP_COST;
    }

    @Override
    public float getDamageMultiplier() {
        return 0.0F;   // 纯位移，无伤害
    }

    @Override
    public int getCastTime() {
        return 0;      // 瞬发
    }

    /** 特殊魂技：冷却不受全局 3 秒锁定约束。 */
    @Override
    protected int getSpecialCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    /** 特殊魂技：固定 10 点，不吃年限系数与全局倍率。 */
    @Override
    protected float getSpecialFixedCost() {
        return MP_COST;
    }

    @Override
    public String getDescriptionKey() {
        return "skill.pole_vault.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }

        // 只顶竖直分量，保留玩家原有的水平速度 —— 跑动中起跳不会突然刹车
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, JUMP_VELOCITY, motion.z);
        player.hasImpulse = true;
        // 必须置 true：否则客户端本地预测会把这次竖直加速「拉回」，表现为
        // 服务端已经飞起来了、自己屏幕上却还站在地上
        player.hurtMarked = true;

        // 从 6 格掉回来会吃坠落伤害，开一个免伤窗口
        player.fallDistance = 0.0F;
        SpecialSoulRingEvents.grantFallImmunity(player, FALL_GUARD_TICKS);

        // 通知客户端「谁、从哪个点、持续多久」—— 双手举杆 + 竹竿是纯客户端表现，
        // 广播给 TRACKING_ENTITY_AND_SELF，所以别的玩家也看得到
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                    new S2CPoleVaultPacket(serverPlayer.getId(), VISUAL_DURATION_TICKS));
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.1D, player.getZ(),
                    12, 0.3D, 0.05D, 0.3D, 0.02D);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.8F, 1.6F);
    }
}
