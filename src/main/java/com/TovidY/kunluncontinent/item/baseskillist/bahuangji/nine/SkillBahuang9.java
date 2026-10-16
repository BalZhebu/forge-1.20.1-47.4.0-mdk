package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.nine;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CSkillFieldPacket;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

// Skill 类只负责开启任务
public class SkillBahuang9 extends BaseSkillItem {

    /** 寂灭地阵半径（格）。与服务端每轮脉冲的判定半径（inflate(20.0)）保持一致。 */
    public static final float FIELD_RADIUS = 20.0F;

    /**
     * 寂灭地阵的显示时长（tick）。
     * <p>服务端是 10 轮脉冲、每 30 tick 一轮，所以正好 {@code 10 × 30 = 300}。</p>
     */
    public static final int FIELD_DURATION_TICKS = 300;


    @Override public float getBaseCost() { return 850f; }
    /**
     * ⚠️ 这里**不能是 0**。基类算伤害的公式是
     * {@code finalDamage = 攻击力 * getDamageMultiplier() * 年限倍率}，
     * 本技能把 finalDamage 存进 {@code Bahuang9_Damage} 交给 Tick 事件当每次脉冲的伤害。
     * 之前写成 0，导致八荒寂灭的每次脉冲伤害恒为 0 —— 完全不吃魂环年限加成。
     * 数值大小（每次脉冲 = 攻击 × 此值 × 年限倍率 × 1.5，共 10 次）按平衡自行调整。
     */
    @Override
    public float getDamageMultiplier() {
        return 1.0f;
    }
    @Override
    public int getCastTime() {
        return 0;
    }

    @Override public String getDescriptionKey() { return "skill.bahuangji.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putBoolean("Bahuang9_Active", true);
            nbt.putInt("Bahuang9_Remaining", 10);
            nbt.putInt("Bahuang9_Timer", 0);
            nbt.putFloat("Bahuang9_Damage", finalDamage * 1.5f);
            player.displayClientMessage(Component.literal("§6§l第九魂技：八荒寂灭！"), true);

            if (SkillFxToggle.useShader(player)) {
                // ---- 表现 A：着色器地阵（玩家把「技能特效优化」关掉了）----
                // 通知客户端在本位置画一个半径 20 的寂灭地阵。单帧一个面就能画出
                // 原来几千个粒子才堆出来的形状，而且是逐像素的矢量级锐利边缘。
                // 客户端会自己按 FIELD_DURATION_TICKS 判断过期，所以这里只发这一次。
                // 注意：这条分支下**一个粒子都不发** —— 两种表现二选一，不叠加。
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer),
                            new S2CSkillFieldPacket(
                                    SkillEffectStyles.BAHUANG9_FIELD,
                                    serverPlayer.getId(),
                                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                    FIELD_RADIUS, FIELD_DURATION_TICKS));
                }
            } else {
                // ---- 表现 B：粒子起手式（默认）----
                // 镇压天地的开场阵。后续每轮的脉冲阵由服务端 Tick 事件接力。
                ParticleFx fx = ParticleFx.of(level, player);
                if (fx != null) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    Vec3 base = player.position();
                    double rot = serverLevel.getGameTime() * 0.25;

                    fx.budget(2600);
                    // 半径二十格的寂灭地阵：双层二十四边形 + 二十四等分刻度
                    fx.polygon(ParticleTypes.END_ROD, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 20.0, 24, rot, 0.0);
                    fx.polygon(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 14.0, 12, -rot, 0.05);
                    fx.polygon(ParticleTypes.LAVA, base.add(0, 0.28, 0), ParticleFx.Axis.Y, 8.0, 8, rot * 1.5, 0.05);
                    fx.magicCircle(ParticleTypes.FLAME, ParticleTypes.CAMPFIRE_COSY_SMOKE, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 5.5, rot * 1.9);
                    // 由内向外三圈冲击环
                    for (int r = 1; r <= 3; r++) {
                        fx.shockRing(ParticleTypes.LARGE_SMOKE, base.add(0, 0.05 + r * 0.05, 0), 6.0 * r, 2.0, 44);
                    }
                    // 二十四根环绕光柱
                    fx.pillars(ParticleTypes.SOUL_FIRE_FLAME, base, 20.0, 24, 3.0, rot);
                    fx.burst(ParticleTypes.FLAME, base.add(0, 0.6, 0), 48, 2.0, true);
                }
            }
        }
    }
}
