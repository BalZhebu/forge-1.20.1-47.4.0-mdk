package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.nine;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// Skill 类只负责开启任务
public class SkillBahuang9 extends BaseSkillItem {
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

    @Override public int getCooldownTicks() { return 1400; } // 65秒
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

            // ---- 起手式：镇压天地的开场阵（后续脉冲由服务端 Tick 事件接力）----
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
