package com.TovidY.kunluncontinent.item.baseskillist.qishajian.five;

import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaFx;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.item.baseskillist.qishajian.QishaDots;
import com.TovidY.kunluncontinent.render.skill.SkillEffectStyles;
import com.TovidY.kunluncontinent.render.skill.SkillFxToggle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * 第五魂技 · 断魂刺
 *
 * <p>一道暗红巨剑<b>从天上戳下来</b>落在身前，落点周围 8 格内的生物受到高额伤害
 * 并被施加虚弱 I（10 秒）。</p>
 */
public class SkillQisha5 extends BaseSkillItem {

    /** 剑落点离玩家多远。 */
    private static final double DROP_DISTANCE = 3.0;
    /** 落点周围的伤害半径。 */
    private static final double AOE_RADIUS = 8.0;

    @Override
    public float getBaseCost() { return 140f; }

    @Override
    public float getDamageMultiplier() { return 4.2f; }

    @Override
    public int getCastTime() { return 10; }

    @Override
    public String getDescriptionKey() { return "skill.qishajian.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (level.isClientSide) {
            return;
        }
        Vec3 look = player.getLookAngle();
        Vec3 impact = player.position().add(look.x * DROP_DISTANCE, 0, look.z * DROP_DISTANCE);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(impact, impact).inflate(AOE_RADIUS), e -> e != player && e.isAlive());
        for (LivingEntity target : targets) {
            dealSkillDamage(target, finalDamage, player.damageSources().indirectMagic(player, player));
            // 虚弱 I，10 秒
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, true));
        }

        boolean shader = SkillFxToggle.useShader(player) && player instanceof ServerPlayer;
        if (shader) {
            ServerPlayer sp = (ServerPlayer) player;
            // 暗红巨剑从天上砸下来（专用剑形特效）+ 落点光柱 + 爆点
            QishaFx.field(sp, SkillEffectStyles.FALL_QISHA_SWORD, impact.x, impact.y, impact.z, 4.0F, 20);
            QishaFx.field(sp, SkillEffectStyles.BEAM_QISHA_PILLAR, impact.x, impact.y, impact.z, 1.6F, 16);
            QishaFx.field(sp, SkillEffectStyles.BURST_QISHA_MARK,
                    impact.x, impact.y + 1.0, impact.z, (float) (AOE_RADIUS * 0.35), 16);
            QishaFx.field(sp, SkillEffectStyles.SHOCK_QISHA, impact.x, impact.y + 0.1, impact.z, (float) AOE_RADIUS, 16);
        } else {
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                fx.budget(1800);
                // 从天而降的剑柱
                fx.line(ParticleTypes.SOUL_FIRE_FLAME, impact.add(0, 8.0, 0), impact, 0.2);
                fx.line(ParticleTypes.CRIMSON_SPORE, impact.add(0.2, 8.0, 0.2), impact, 0.3);
                fx.column(ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.CRIMSON_SPORE, impact, 0.6, 3.0, 4, 0, 1.2, 0.5);
                fx.shockRing(ParticleTypes.SOUL, impact.add(0, 0.1, 0), AOE_RADIUS, 2.0, 48);
                fx.burst(ParticleTypes.CRIMSON_SPORE, impact.add(0, 0.5, 0), 30, 1.0, true);
            }
        }
        level.playSound(null, impact.x, impact.y, impact.z,
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
