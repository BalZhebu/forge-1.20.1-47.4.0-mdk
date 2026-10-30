package com.TovidY.kunluncontinent.item.baseskillist.tianshi.four;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 第四魂技 · <b>审判之矛</b>：向前方 20 格、宽 3 格、高 4 格的长条打出高倍率伤害，
 * 有较高概率<b>直接斩杀</b>实力明显低于玩家的目标。
 *
 * <h3>「实力」怎么算</h3>
 * <p>用一个统一的粗粒度战力：{@code 最大生命 + 攻击力 × 10}。
 * 玩家的战力走 {@link ModAttributeAPI}（含加点 / 魂环 / 魂骨 / 护甲），
 * 目标的战力用实体自身属性。只有<b>目标战力低于玩家</b>才可能被斩 —— 这样打同级或更强的怪
 * 不会出现「莫名其妙秒杀」，而清小怪的手感又很爽。</p>
 *
 * <p>斩杀概率叠两层：基础 {@link #EXECUTE_CHANCE}，目标战力低于玩家一半时提到
 * {@link #EXECUTE_CHANCE_WEAK}。</p>
 */
public class SkillTianshi4 extends BaseSkillItem {

    /** 长条长度（格）。 */
    private static final int LENGTH = 20;
    /** 半宽（格）：总宽 3 → 半宽 1.5。 */
    private static final double HALF_WIDTH = 1.5;
    /** 半高（格）：总高 4 → 半高 2。 */
    private static final double HALF_HEIGHT = 2.0;

    /** 基础斩杀概率。 */
    private static final float EXECUTE_CHANCE = 0.3f;
    /** 目标战力不到玩家一半时的斩杀概率。 */
    private static final float EXECUTE_CHANCE_WEAK = 0.6f;

    @Override public float getBaseCost() { return 380f; }
    @Override public float getDamageMultiplier() { return 4.5f; }
    @Override public int getCastTime() { return 40; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.four.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 look = player.getLookAngle();
        Vec3 flat = look.multiply(1, 0, 1);
        Vec3 dir = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        Vec3 right = new Vec3(-dir.z, 0, dir.x);
        Vec3 base = player.position().add(0, 1.0, 0);

        // ---- 特效：着色器（细长光矛）／ 粒子（矛身+地面刻线）二选一 ----
        if (SkillFxToggle.useShader(player)) {
            SkillFieldFx.atEntity(player, SkillEffectStyles.BEAM_JUDGMENT_SPEAR, 0.0F, 20);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = serverLevel.getGameTime() * 0.5;
                fx.budget(2000);
                // 矛身：中线 + 两侧缠绕
                fx.line(ParticleTypes.END_ROD, base, base.add(dir.scale(LENGTH)).add(0, 0.2, 0), 0.35);
                fx.helixAround(ParticleTypes.FIREWORK, base, dir, 0.45, LENGTH, 5, 2, rot, 26);
                // 命中端：爆开的光刺
                Vec3 tip = base.add(dir.scale(LENGTH));
                fx.burst(ParticleTypes.END_ROD, tip, 40, 1.1, true);
                fx.shockRing(ParticleTypes.FIREWORK, tip, 2.4, 0.6, 26);
                // 地面刻线：宣示判定范围
                for (int d = 2; d <= LENGTH; d += 2) {
                    Vec3 c = player.position().add(dir.scale(d)).add(0, 0.06, 0);
                    fx.line(ParticleTypes.CRIT, c.add(right.scale(HALF_WIDTH)), c.add(right.scale(-HALF_WIDTH)), 0.5);
                }
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.4f, 0.7f);

        // ---- 判定：沿视线每 2 格取一个截面，去重 ----
        Set<LivingEntity> hits = new LinkedHashSet<>();
        for (int dist = 2; dist <= LENGTH; dist += 2) {
            Vec3 c = base.add(dir.scale(dist));
            hits.addAll(level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(c.x - HALF_WIDTH, c.y - HALF_HEIGHT, c.z - HALF_WIDTH,
                            c.x + HALF_WIDTH, c.y + HALF_HEIGHT, c.z + HALF_WIDTH),
                    e -> e != player && e.isAlive()));
        }

        int executed = 0;
        for (LivingEntity target : hits) {
            dealSkillDamage(target, finalDamage, level.damageSources().mobAttack(player));
            if (target.isAlive() && tryExecute(player, target)) {
                executed++;
            }
        }

        String extra = executed > 0 ? " §c斩杀 §e" + executed + " §c个" : "";
        player.displayClientMessage(Component.literal("§e§l第四魂技：审判之矛！" + extra), true);
    }

    /**
     * 尝试斩杀。
     *
     * <p>判定用 {@code magic()} 真伤（绕过攻防重算），伤害给 {@code 血量 + 10} 确保必死 ——
     * 不要用 {@code kill()}：那样会跳过死亡事件里的一整套结算（经验、掉落、成就）。</p>
     */
    private boolean tryExecute(Player player, LivingEntity target) {
        float playerPower = playerPower(player);
        float targetPower = targetPower(target);
        if (targetPower >= playerPower) {
            return false;
        }
        float chance = targetPower <= playerPower * 0.5f ? EXECUTE_CHANCE_WEAK : EXECUTE_CHANCE;
        if (player.getRandom().nextFloat() >= chance) {
            return false;
        }
        target.hurt(target.level().damageSources().magic(), target.getHealth() + 10.0f);
        ParticleFx fx = ParticleFx.of(target.level(), player);
        if (fx != null) {
            fx.budget(300);
            fx.burst(ParticleTypes.END_ROD, target.position().add(0, target.getBbHeight() * 0.5, 0), 22, 0.5, true);
        }
        return !target.isAlive();
    }

    /** 玩家战力：最大生命 + 攻击力 × 10（全部走 ModAttributeAPI，含一切加成）。 */
    private static float playerPower(Player player) {
        return ModAttributeAPI.getMaxshengming(player) + ModAttributeAPI.getGongji(player) * 10.0f;
    }

    /** 目标战力：最大生命 + 攻击力 × 10。 */
    private static float targetPower(LivingEntity target) {
        double attack = target.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return target.getMaxHealth() + (float) attack * 10.0f;
    }
}
