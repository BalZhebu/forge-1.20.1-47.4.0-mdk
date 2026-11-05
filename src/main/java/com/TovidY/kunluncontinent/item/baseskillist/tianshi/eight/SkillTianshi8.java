package com.TovidY.kunluncontinent.item.baseskillist.tianshi.eight;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import com.TovidY.kunluncontinent.skillfield.SkillWaveField;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 第八魂技 · <b>圣裁之域</b>：在脚下展开一个半径 20 格、持续 20 秒的圆形领域。
 *
 * <h3>规则（全部由 {@link SkillWaveField} 执行）</h3>
 * <ul>
 *   <li><b>每隔 {@value #WAVE_TICKS} tick 从圆心推出一圈光波</b>，波前扫到谁、谁才受伤 ——
 *       所以「波扩散的速度」和「掉血频率」是同一个数，不会出现"波还没到、怪已经掉血"；</li>
 *   <li>范围内除施法者外的生物<b>出不去</b>（越界会被拉回边界内侧）；</li>
 *   <li>被扫中的目标附加 10 秒禁锢；</li>
 *   <li>命中瞬间在目标身上炸一团金色光（{@code BURST_ANGEL_HIT}）。</li>
 * </ul>
 *
 * <h3>伤害</h3>
 * <p>每一波 = 攻击 × {@value #PULSE_GONGJI_RATIO} × 年限倍率。20 秒共 {@value #WAVES} 波，
 * 全吃满约 5.5 倍攻击 —— 看着不高，但它是「站桩 20 秒、一个都不跑」才可能吃满的总量。</p>
 *
 * <h3>视觉</h3>
 * <p>着色器版：底座是常驻的二十边大阵（{@code SIGIL_ANGEL_JUDGMENT}，与领域同寿
 * {@value #DURATION_TICKS} tick），每一波再叠一圈冲击波。
 * 粒子版：开场的阵 + 每波一圈冲击环。</p>
 */
public class SkillTianshi8 extends BaseSkillItem {

    /** 领域半径（格）。 */
    public static final double RADIUS = 20.0;

    /** 领域总时长（tick）：20 秒。 */
    public static final int DURATION_TICKS = 400;

    /** 一圈波的扩散时长：2 秒 —— 也就是圈内生物的掉血间隔。 */
    public static final int WAVE_TICKS = 40;

    /** 总波数。 */
    public static final int WAVES = DURATION_TICKS / WAVE_TICKS;

    /** 每一波伤害占攻击力的比例。 */
    private static final float PULSE_GONGJI_RATIO = 0.55f;

    /** 命中特效的半边长（格）。 */
    private static final float HIT_RADIUS = 1.3f;

    /** 命中特效时长（tick）。 */
    private static final int HIT_TICKS = 8;

    @Override public float getBaseCost() { return 700f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 40; }

    @Override
    public String getDescriptionKey() {
        return "skill.tianshi.eight.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp) || !(level instanceof ServerLevel sl)) {
            return;
        }

        // 每一波 = 魂技基底（点数攻击 + 原版武器攻击）× 比例 × 年限倍率
        float pulse = BaseSkillItem.getSkillBaseDamage(player) * PULSE_GONGJI_RATIO * powerMultiplier;
        Vec3 center = player.position();
        boolean shader = SkillFxToggle.useShader(player);

        // ---- 规则层：波扫描伤害 + 禁锢 + 禁止离开 ----
        SkillWaveField.spawn(sp, sl, center, new SkillWaveField.Spec(
                RADIUS, pulse,
                0, WAVE_TICKS, WAVES,
                true, true,
                shader ? SkillEffectStyles.SHOCK_ANGEL : -1,
                shader ? SkillEffectStyles.BURST_ANGEL_HIT : -1,
                HIT_RADIUS, HIT_TICKS,
                !shader,
                0.0F));

        // ---- 底座：常驻大阵（与领域同寿）----
        if (shader) {
            SkillFieldFx.atPoint(sp, SkillEffectStyles.SIGIL_ANGEL_JUDGMENT,
                    center.x, center.y, center.z, (float) RADIUS, DURATION_TICKS);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = sl.getGameTime() * 0.3;
                Vec3 c = center.add(0, 0.08, 0);
                fx.budget(1800);
                fx.magicCircle(ParticleTypes.END_ROD, ParticleTypes.FIREWORK, c, ParticleFx.Axis.Y, 4.5, rot, 2, 0.3);
                fx.shockRing(ParticleTypes.FIREWORK, c, RADIUS, 0.8, 60);
                fx.polygon(ParticleTypes.END_ROD, c, ParticleFx.Axis.Y, RADIUS, 6, rot, 0.0);
                for (int i = 0; i < 12; i++) {
                    double a = rot + ParticleFx.TAU * i / 12;
                    Vec3 foot = c.add(Math.cos(a) * RADIUS, 0, Math.sin(a) * RADIUS);
                    fx.line(ParticleTypes.WAX_ON, foot, foot.add(0, 6.0, 0), 0.7);
                    fx.burst(ParticleTypes.END_ROD, foot.add(0, 0.1, 0), 8, 0.4, true);
                }
            }
        }

        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.5f, 0.8f);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.7f, 1.2f);

        player.displayClientMessage(Component.literal(
                "§e§l第八魂技：圣裁之域！§7半径 " + (int) RADIUS + " 格 · 持续 20 秒 · 越界者将被拖回"), true);
    }
}
