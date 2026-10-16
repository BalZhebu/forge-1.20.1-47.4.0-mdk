package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.nine;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import static com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem.dealSkillDamage;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFieldFx;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 裂金虎 · 第九魂技「猛虎破界」。
 *
 * <h3>技能本身</h3>
 * <p>瞬发。对自身 10 格内所有敌人各造成一次高额伤害，附加缓慢 / 虚弱 / 反胃，
 * 并在原地留下一个虚空领域（{@code AreaEffectCloud}）。</p>
 *
 * <h3>着色器版：旋涡张开 → 金光打出</h3>
 * <p>表现换成「每只生物<b>身后</b>张开一个黑色旋涡，随后旋涡打出金色光芒」。
 * 这两步是有先后的，所以：</p>
 * <ol>
 *   <li>释放瞬间 —— 只在每个目标身后发<b>旋涡</b>（{@code VORTEX_POJIE}），
 *       伤害<b>不</b>结算；</li>
 *   <li>{@value #LIGHT_DELAY_TICKS} tick 后 —— 发<b>金光</b>（{@code VORTEX_POJIE_LIGHT}）
 *       并<b>在同一 tick 结算伤害</b>。</li>
 * </ol>
 * <p>延迟结算由 {@code PWPlayerTickEvent#handleLiejinhuNine} 按玩家 persistentData 推进，
 * 目标 id 存成 {@code int[]}。这样做到「光到即伤」，不会出现
 * "怪已经掉血了，旋涡才开始张开"这种违和（不周倾的光波踩过同样的坑）。</p>
 *
 * <p>⚠️ 粒子版（默认）仍然是<b>立刻结算</b> —— 它没有"旋涡 → 金光"这两拍，
 * 保持原手感。也就是说开不开着色器，这一击的结算时机差 0.5 秒。</p>
 */
public class SkillLeijinhu9 extends BaseSkillItem {

    /** 旋涡 / 金光的视觉半径（格）—— 四边形半边长。 */
    public static final float VORTEX_RADIUS = 1.70F;
    public static final float LIGHT_RADIUS = 2.60F;

    /** 旋涡存在时长（tick）。 */
    public static final int VORTEX_TICKS = 44;

    /**
     * 旋涡张开到金光打出的间隔（tick）。
     *
     * <p>{@code skill_vortex} 里张开阶段占寿命前 22%，44 tick 里约 10 tick，
     * 所以这个值正好让"旋涡完全张开"和"金光打出"对上。</p>
     */
    public static final int LIGHT_DELAY_TICKS = 10;

    /** 金光时长（tick）。 */
    public static final int LIGHT_TICKS = 22;

    /** 判定范围（格）。 */
    public static final double RANGE = 10.0D;

    @Override public float getBaseCost() { return 900f; }
    @Override public float getDamageMultiplier() { return 3.2f; }

    @Override
    public int getCastTime() {
        return 0;
    }

    @Override public String getDescriptionKey() { return "skill.leijinhu.nine.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        player.displayClientMessage(Component.literal("§e§l第九魂技：猛虎破界！"), true);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RANGE), e -> e != player && e.isAlive());

        // 粒子 / 着色器二选一（全局开关，见 SkillFxToggle）。
        boolean shaderMode = SkillFxToggle.useShader(player);
        ParticleFx fx = shaderMode ? null : ParticleFx.of(level, player);
        double rot = serverLevel.getGameTime() * 0.45;

        if (shaderMode) {
            // ---- 表现 A：着色器版 ----
            // 第一步只开旋涡；伤害等金光那一下（见 handleLiejinhuNine）。
            List<Integer> ids = new ArrayList<>(targets.size());
            for (LivingEntity target : targets) {
                SkillFieldFx.atEntity(target, SkillEffectStyles.VORTEX_POJIE, VORTEX_RADIUS, VORTEX_TICKS);
                ids.add(target.getId());
            }
            if (!ids.isEmpty()) {
                int[] arr = new int[ids.size()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = ids.get(i);
                }
                CompoundTag nbt = player.getPersistentData();
                nbt.putInt("Liejinhu9_Delay", LIGHT_DELAY_TICKS);
                nbt.putFloat("Liejinhu9_Damage", finalDamage);
                nbt.putIntArray("Liejinhu9_Targets", arr);
            }
        } else if (fx != null) {
            // ---- 表现 B：粒子版（默认）----
            Vec3 base = player.position();
            Vec3 up = new Vec3(0, 1, 0);

            fx.budget(2800);
            // 十格地面大阵：星芒 + 双层多边形 + 三重冲击环
            fx.star(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.12, 0), ParticleFx.Axis.Y, 8.0, 3.2, 6, rot, 0.04);
            fx.polygon(ParticleTypes.CRIT, base.add(0, 0.2, 0), ParticleFx.Axis.Y, 10.0, 16, -rot, 0.02);
            fx.magicCircle(ParticleTypes.GLOW, ParticleTypes.CRIT, base.add(0, 0.1, 0), ParticleFx.Axis.Y, 5.0, rot * 1.5);
            for (int r = 1; r <= 3; r++) {
                fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, base.add(0, 0.05 + r * 0.06, 0), 3.5 * r, 2.0, 44);
            }
            // 四道破界爪痕（前后左右各一道竖劈）
            for (int i = 0; i < 4; i++) {
                double a = rot + ParticleFx.TAU * i / 4;
                Vec3 dir = new Vec3(Math.cos(a), 0, Math.sin(a));
                Vec3 side = dir.cross(up).normalize();
                fx.slash(ParticleTypes.SWEEP_ATTACK, base.add(0, 1.6, 0), dir, side, 7.0, 150, 3, rot + i * 0.2);
            }
            fx.column(ParticleTypes.GLOW, ParticleTypes.CRIT, base, 0.9, 8.0, 4, rot, ParticleFx.TAU * 1.2, 0.4);
            fx.pillars(ParticleTypes.SOUL_FIRE_FLAME, base, 10.0, 8, 4.0, -rot);
            fx.burst(ParticleTypes.CRIT, base.add(0, 1.0, 0), 48, 2.0, true);
            fx.bloom(ParticleTypes.WAX_ON, base.add(0, 1.4, 0), 30, 1.6, 0.25);

            // 粒子版立刻结算
            for (LivingEntity target : targets) {
                applyStrike(player, serverLevel, target, finalDamage, fx, rot);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY(), player.getZ(), 3, 2, 1, 2, 0);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0f, 0.6f);
    }

    /**
     * 对单个目标结算这一击：伤害 + 三种负面效果 + 虚空领域 +（可选）粒子爆点。
     *
     * <p>抽出来是为了让两条路径共用 —— 粒子版在释放瞬间调用，
     * 着色器版在"金光打出"那一 tick 由 {@code handleLiejinhuNine} 调用。</p>
     *
     * @param fx 粒子表现；传 {@code null} 表示只结算逻辑（着色器版走这条）
     */
    public static void applyStrike(Player player, ServerLevel level, LivingEntity target,
                                   float damage, @Nullable ParticleFx fx, double rot) {
        dealSkillDamage(target, damage, player.damageSources().playerAttack(player));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));

        AreaEffectCloud voidCloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        voidCloud.setOwner(player);
        voidCloud.setRadius(3.0f);
        voidCloud.setDuration(200);
        voidCloud.addTag("PohunVoidZone");
        voidCloud.getPersistentData().putFloat("VoidDamage", damage * 0.1f);
        level.addFreshEntity(voidCloud);

        if (fx != null) {
            // 每个被破界者身上炸开一记虎爪
            Vec3 hp = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 to = hp.subtract(player.position()).normalize();
            Vec3 side = ParticleFx.ortho(to);
            for (int i = -1; i <= 1; i++) {
                fx.slash(ParticleTypes.CRIT, hp.add(side.scale(i * 0.5)), to, side, 1.4, 160, 2, rot + i * 0.3);
            }
            fx.burst(ParticleTypes.SOUL_FIRE_FLAME, hp, 18, 0.5, true);
            fx.dome(ParticleTypes.GLOW, target.position(), 2.0, 4, 18);
        }
    }
}
