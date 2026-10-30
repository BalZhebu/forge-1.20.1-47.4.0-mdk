package com.TovidY.kunluncontinent.item.baseskillist.tianshi.six;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.tianshi.TianshiPower;
import com.TovidY.kunluncontinent.potion.ModEffects;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 第六魂技 · <b>圣光锁链</b>：以自身为中心 30 格内全体各吃一次伤害，
 * 并挂上<b>禁锢</b>（移速归零 + 无法攻击）。
 *
 * <h3>禁锢时长（3 秒 ~ 120 秒）</h3>
 * <p>由「目标比自己弱多少」决定，落点是 {@link TianshiPower#jinguTicks}
 * —— 那是个临时实现，等用户的实力工具类写完，只改那一处即可。</p>
 *
 * <h3>为什么能一次命中 30 格内所有怪</h3>
 * <p>直接用一个 30 格半径的 {@link AABB} 取实体，不需要像第一魂技那样采样去重：
 * 这里就是个球，没有形状可言。</p>
 */
public class SkillTianshi6 extends BaseSkillItem {

    /** 作用半径（格）。 */
    private static final double RANGE = 30.0;

    /** 锁链的条数（视觉用，纯装饰）。 */
    private static final int CHAINS = 6;

    @Override public float getBaseCost() { return 480f; }
    @Override public float getDamageMultiplier() { return 1.8f; }
    @Override public int getCastTime() { return 20; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.six.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 base = player.position();

        // ---- 判定 ----
        AABB box = new AABB(base.x - RANGE, base.y - RANGE, base.z - RANGE,
                base.x + RANGE, base.y + RANGE, base.z + RANGE);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && e.distanceToSqr(base) <= RANGE * RANGE);

        // ---- 特效：着色器（半径 30 的锁链大阵）／ 粒子（金环 + 锁链）二选一 ----
        if (player instanceof ServerPlayer sp && SkillFxToggle.useShader(player)) {
            // 阵钉在释放点：锁链是"这一瞬间撒出去的"，不该跟着玩家飘
            SkillFieldFx.atPoint(sp, SkillEffectStyles.SIGIL_ANGEL_CHAIN,
                    base.x, base.y, base.z, (float) RANGE, 30);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = serverLevel.getGameTime() * 0.4;
                fx.budget(1400);

                // 中心阵与脚下爆发
                fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.FIREWORK,
                        base.add(0, 0.08, 0), ParticleFx.Axis.Y, 3.0, rot);
                fx.shockRing(ParticleTypes.FIREWORK, base.add(0, 0.1, 0), RANGE, 0.5, 60);

                // 从玩家向外甩出的锁链（用螺旋模拟链节）
                for (int i = 0; i < CHAINS; i++) {
                    double angle = rot + i * ParticleFx.TAU / CHAINS;
                    Vec3 dir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
                    Vec3 start = base.add(0, 1.0, 0);
                    fx.helixAround(ParticleTypes.WAX_ON, start, dir, 0.55, RANGE, 9, 2, angle, 44);
                }

                // 每个目标身上套一圈竖直锁环
                for (LivingEntity target : targets) {
                    Vec3 c = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    fx.circle(ParticleTypes.END_ROD, c, ParticleFx.Axis.X, 0.9, 14, rot, 0.02);
                    fx.circle(ParticleTypes.FIREWORK, c, ParticleFx.Axis.Z, 0.9, 14, -rot, 0.02);
                    fx.burst(ParticleTypes.END_ROD, c, 10, 0.4, true);
                }
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 2.0f, 0.7f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.6f);

        // ---- 伤害 + 禁锢 ----
        int locked = 0;
        for (LivingEntity target : targets) {
            dealSkillDamage(target, finalDamage, level.damageSources().mobAttack(player));
            if (!target.isAlive()) {
                continue;
            }
            int ticks = TianshiPower.jinguTicks(player, target);
            target.addEffect(new MobEffectInstance(ModEffects.JINGU.get(), ticks, 0, false, true, true));
            locked++;
        }

        String extra = locked > 0 ? " §7禁锢 §e" + locked + " §7个目标" : "";
        player.displayClientMessage(Component.literal("§e§l第六魂技：圣光锁链！" + extra), true);
    }
}
