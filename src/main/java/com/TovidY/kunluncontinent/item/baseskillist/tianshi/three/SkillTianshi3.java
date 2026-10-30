package com.TovidY.kunluncontinent.item.baseskillist.tianshi.three;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 第三魂技 · <b>圣愈之光</b>：半径 5 格的低倍率范围伤害，并按<b>实际造成的总伤害</b>回血。
 *
 * <p>为什么按「实际伤害」而不是「面板伤害」：{@link #dealSkillDamage} 走的还是正常战斗结算
 * （目标防御、抗性都会削），所以这里用打前 / 打后的血量差来累计 —— 打在石头人身上回得少，
 * 打在软目标身上回得多，符合「越痛越补」的定位。</p>
 */
public class SkillTianshi3 extends BaseSkillItem {

    /** 判定半径（格）：实际打到的范围。 */
    private static final double RADIUS = 8.0;
    /**
     * 视觉半径（格）：阵法画多大。
     *
     * <p>比判定大一圈是刻意的 —— 阵是要"看得见的范围提示"，
     * 和判定完全等大会显得阵缩在脚下很小一圈，视觉上像没生效。</p>
     */
    private static final double FX_RADIUS = 12.0;
    /** 回血上限：一次最多补到最大生命的这个比例（防止怪堆里一口回满）。 */
    private static final float HEAL_CAP_RATIO = 0.6f;

    @Override public float getBaseCost() { return 220f; }
    @Override public float getDamageMultiplier() { return 0.8f; }
    @Override public int getCastTime() { return 20; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.three.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 base = player.position();

        // ---- 特效：着色器（治疗阵）／ 粒子（光穹顶 + 扩散）二选一 ----
        if (player instanceof net.minecraft.server.level.ServerPlayer sp && SkillFxToggle.useShader(player)) {
            // 阵钉在释放点，半径用视觉半径（12 格），比判定大一圈
            SkillFieldFx.atPoint(sp, SkillEffectStyles.SIGIL_ANGEL_HEAL, base.x, base.y, base.z, (float) FX_RADIUS, 30);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = serverLevel.getGameTime() * 0.35;
                fx.budget(1600);
                fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.FIREWORK,
                        base.add(0, 0.08, 0), ParticleFx.Axis.Y, FX_RADIUS, rot);
                fx.dome(ParticleTypes.END_ROD, base, FX_RADIUS, 4, 24);
                fx.shockRing(ParticleTypes.FIREWORK, base.add(0, 0.05, 0), FX_RADIUS, 0.8, 48);
                fx.cylinder(ParticleTypes.END_ROD, ParticleTypes.FIREWORK,
                        base.add(0, 0.2, 0), 1.4, 4.0, 16, rot);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.6f);

        // ---- 伤害 + 累计实际伤害 ----
        AABB area = new AABB(
                base.x - RADIUS, base.y - RADIUS * 0.6, base.z - RADIUS,
                base.x + RADIUS, base.y + RADIUS * 0.6, base.z + RADIUS);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        float totalDamage = 0.0f;
        for (LivingEntity target : targets) {
            float before = target.getHealth();
            dealSkillDamage(target, finalDamage, level.damageSources().mobAttack(player));
            float delta = before - target.getHealth();
            if (delta > 0.0f) {
                totalDamage += delta;
            }
        }

        // ---- 回血：按实际总伤害，最多补到最大生命的 HEAL_CAP_RATIO ----
        float maxHealth = player.getMaxHealth();
        float heal = Math.min(totalDamage, maxHealth * HEAL_CAP_RATIO);
        if (heal > 0.0f) {
            player.heal(heal);
            player.getCapability(com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> cap.setShengming(player.getHealth()));
        }

        player.displayClientMessage(Component.literal(totalDamage > 0.0f
                ? "§e§l第三魂技：圣愈之光！§f回复 §a" + String.format("%.0f", heal) + " §f点生命"
                : "§e§l第三魂技：圣愈之光！§7（未命中目标）"), true);
    }
}
