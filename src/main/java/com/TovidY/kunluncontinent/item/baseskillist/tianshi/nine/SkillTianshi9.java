package com.TovidY.kunluncontinent.item.baseskillist.tianshi.nine;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import com.TovidY.kunluncontinent.render.skill.effect.FallEffect;
import com.TovidY.kunluncontinent.skillfield.SkillWaveField;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 第九魂技 · <b>终焉救赎</b>：一个巨大的发光体从天而降砸在落点，落地后从落点推翻开的光波，
 * 波扫过 30 格内的生物各吃一次极高倍率伤害，并有 <b>1%</b> 概率<b>无视实力</b>直接斩杀。
 *
 * <h3>「砸下来 → 波扩散 → 掉血」是同一条时间线</h3>
 * <ol>
 *   <li>释放瞬间发 {@code FALL_ANGEL_SWORD}（{@code FallEffect}）：9 格宽的巨大发光体
 *       从 34 格高处加速砸落，{@link #FALL_TICKS} tick 后触地；</li>
 *   <li>同一刻起，{@link SkillWaveField} 在落点推出<b>一圈</b>光波
 *       （{@link #WAVE_TICKS} tick 内扩到 {@link #RANGE} 格）；</li>
 *   <li>伤害和斩杀都由那个波场结算 —— 波前扫到谁才打谁，不会"还没扫到就掉血"。</li>
 * </ol>
 *
 * <h3>落点怎么取</h3>
 * <p>从眼睛沿视线 {@link #AIM_RANGE} 格做一次方块射线：打到方块就砸在命中点，
 * 什么都没打到（朝天空放）就落在身前 {@link #FALLBACK_DISTANCE} 格的地面高度上。</p>
 *
 * <h3>斩杀为什么是无视实力的</h3>
 * <p>第六魂技的禁锢、第四魂技的斩杀都看战力比，这一招是「神罚」定位 ——
 * 命中就 1% 直接带走，不管对方多强。</p>
 */
public class SkillTianshi9 extends BaseSkillItem {

    /** 伤害半径（格）。 */
    private static final double RANGE = 30.0;

    /** 视线射线长度（格）。 */
    private static final double AIM_RANGE = 30.0;

    /** 射线没命中方块时的落点距离。 */
    private static final double FALLBACK_DISTANCE = 8.0;

    /** 粒子版光柱高度（格）。 */
    private static final double BEAM_HEIGHT = 40.0;

    /** 巨物落地所需 tick —— 必须和 {@code FallEffect} 的 FALL_PORTION 对齐（0.6 × 20）。 */
    private static final int FALL_TICKS = 12;

    /** 光波扩散时长（tick）：30 格用 2 秒扫完。 */
    private static final int WAVE_TICKS = 40;

    /** 命中特效的半边长（格）。 */
    private static final float HIT_RADIUS = 1.8f;

    /** 命中特效时长（tick）。 */
    private static final int HIT_TICKS = 10;

    /** 无视实力斩杀概率。 */
    private static final float EXECUTE_CHANCE = 0.01f;

    @Override public float getBaseCost() { return 900f; }
    @Override public float getDamageMultiplier() { return 8.0f; }
    @Override public int getCastTime() { return 60; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.nine.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;

        Vec3 impact = resolveImpact(level, player);

        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp) || !(level instanceof ServerLevel sl)) {
            return;
        }
        boolean shader = SkillFxToggle.useShader(player);

        // ---- 表现：天降巨物 / 粒子光柱 ----
        if (shader) {
            // 9 格宽的巨大发光体从 34 格高处砸下来（约 12 tick 落地）
            SkillFieldFx.atPoint(sp, SkillEffectStyles.FALL_ANGEL_SWORD,
                    impact.x, impact.y, impact.z, 0.0F, FallEffect.DURATION_TICKS);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = serverLevel.getGameTime() * 0.6;
                fx.budget(2600);
                Vec3 top = impact.add(0, BEAM_HEIGHT, 0);

                // 光柱本体：内芯密 + 外壳螺旋
                fx.line(ParticleTypes.END_ROD, top, impact, 0.32);
                fx.cylinder(ParticleTypes.FIREWORK, ParticleTypes.END_ROD, impact, 2.2, BEAM_HEIGHT, 22, rot);
                fx.helix(ParticleTypes.WAX_ON, impact, ParticleFx.Axis.Y, 2.6, BEAM_HEIGHT, 3.0, 2, rot, 90);
                // 顶端的光环（"门"打开的感觉）
                fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.FIREWORK, top, ParticleFx.Axis.Y, 4.0, rot);
                // 落地：冲击环 + 穹顶 + 六芒
                fx.shockRing(ParticleTypes.FIREWORK, impact.add(0, 0.1, 0), RANGE, 1.4, 64);
                fx.polygon(ParticleTypes.END_ROD, impact.add(0, 0.06, 0), ParticleFx.Axis.Y, RANGE * 0.7, 6, rot, 0.0);
                fx.dome(ParticleTypes.END_ROD, impact, RANGE * 0.55, 5, 26);
                fx.burst(ParticleTypes.END_ROD, impact.add(0, 1.0, 0), 60, 1.4, true);
                fx.spiral(ParticleTypes.FIREWORK, impact.add(0, 0.2, 0), ParticleFx.Axis.Y, 3.0, 0.4, 22.0, 4.0, rot, 160, 0.06);
            }
        }
        level.playSound(null, impact.x, impact.y, impact.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.6f, 1.1f);
        level.playSound(null, impact.x, impact.y, impact.z,
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4f, 0.6f);

        // ---- 规则：巨物落地那一刻起推一圈光波，波扫到谁、谁才吃这一发 ----
        // 伤害/斩杀都在 SkillWaveField 里结算，所以"光波扩散"和"掉血"天然同速。
        SkillWaveField.spawn(sp, sl, impact, new SkillWaveField.Spec(
                RANGE, finalDamage,
                FALL_TICKS, WAVE_TICKS, 1,
                false, false,
                shader ? SkillEffectStyles.SHOCK_ANGEL : -1,
                shader ? SkillEffectStyles.BURST_ANGEL_HIT : -1,
                HIT_RADIUS, HIT_TICKS,
                !shader,
                EXECUTE_CHANCE));

        player.displayClientMessage(Component.literal("§e§l第九魂技 · 终焉救赎！"), true);
    }

    /**
     * 求落点：视线射线打到方块就用命中点，否则落在身前的同一高度。
     */
    private static Vec3 resolveImpact(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(AIM_RANGE)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.MISS) {
            return hit.getLocation();
        }
        Vec3 flat = look.multiply(1, 0, 1);
        Vec3 dir = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        return player.position().add(dir.scale(FALLBACK_DISTANCE));
    }
}
