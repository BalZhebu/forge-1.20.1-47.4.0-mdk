package com.TovidY.kunluncontinent.item.baseskillist.tianshi.five;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 第五魂技 · <b>神圣庇护</b>：立刻回复「最大生命 × 30%~70%」，并附上一小段抗性提升。
 *
 * <h3>比例怎么定</h3>
 * <pre>
 *   ratio = 0.30 + 0.25 × (等级 / 99) + 0.15 × (魂环年限档归一)
 * </pre>
 * <ul>
 *   <li>等级是主项（最高 +0.25）—— 越强的人越能从神恩里榨出更多回复；</li>
 *   <li>魂环年限是副项（最高 +0.15），用 {@link #getPowerMultiplier(long)} 归一，
 *       1 万年以下基本吃不到，十万年往上接近满值；</li>
 *   <li>两项都取满才 0.70，也就是「封顶 70%」。</li>
 * </ul>
 */
public class SkillTianshi5 extends BaseSkillItem {

    /** 下限：任何等级至少回 30%。 */
    private static final float MIN_RATIO = 0.30f;
    /** 上限：最多回 70%。 */
    private static final float MAX_RATIO = 0.70f;
    /** 等级项的最大加成。 */
    private static final float LEVEL_WEIGHT = 0.25f;
    /** 年限项的最大加成。 */
    private static final float NIANXIAN_WEIGHT = 0.15f;
    /** 庇佑（抗性提升）持续时间：6 秒。 */
    private static final int BLESSING_TICKS = 120;

    @Override public float getBaseCost() { return 300f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 20; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.five.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;

        int dengji = ModAttributeAPI.getDengji(player);
        float levelPart = clamp(dengji / 99.0f);
        float nianxianPart = clamp((powerMultiplier - 1.0f) / 24.0f);
        float ratio = Math.min(MAX_RATIO, MIN_RATIO + LEVEL_WEIGHT * levelPart + NIANXIAN_WEIGHT * nianxianPart);

        float maxHealth = ModAttributeAPI.getMaxshengming(player);
        float heal = maxHealth * ratio;
        player.heal(heal);
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            cap.setShengming(player.getHealth());
            com.TovidY.kunluncontinent.network.SynsAPI.synsPlayerAttribute(player);
        });

        // 「庇佑」：一段短抗性提升，时长不随等级变
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BLESSING_TICKS, 0, false, false, true));

        // ---- 特效：着色器（庇护阵 + 垂落光柱）／ 粒子（光柱 + 光羽）二选一 ----
        if (SkillFxToggle.useShader(player)) {
            // 阵和柱都跟着玩家（真身/祝福类场域应该随人走）
            SkillFieldFx.atEntity(player, SkillEffectStyles.SIGIL_ANGEL_BLESS, 3.2F, 40);
            SkillFieldFx.atEntity(player, SkillEffectStyles.BEAM_ANGEL_PILLAR, 0.0F, 40);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                Vec3 base = player.position();
                double rot = serverLevel.getGameTime() * 0.45;
                fx.budget(1500);
                fx.cylinder(ParticleTypes.END_ROD, ParticleTypes.FIREWORK,
                        base.add(0, 0.1, 0), 1.6, 6.0, 18, rot);
                fx.magicCircle(ParticleTypes.FIREWORK, ParticleTypes.END_ROD,
                        base.add(0, 0.08, 0), ParticleFx.Axis.Y, 2.4, -rot);
                fx.helix(ParticleTypes.END_ROD, base, ParticleFx.Axis.Y, 1.1, 2.6, 2.5, 3, rot, 22);
                fx.burst(ParticleTypes.FIREWORK, base.add(0, 1.2, 0), 36, 0.9, true);
                // 头顶的一圈光冠
                fx.circle(ParticleTypes.END_ROD, base.add(0, 2.4, 0), ParticleFx.Axis.Y, 0.8, 16, rot, 0.03);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.3f);
        player.displayClientMessage(Component.literal("§e§l第五魂技：神圣庇护！§f回复 §a"
                + String.format("%.0f", heal) + " §f点生命 §8(" + Math.round(ratio * 100) + "%)"), true);
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }
}
