package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.six;

import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
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

public class SkillPohun6 extends BaseSkillItem {
    @Override public float getBaseCost() { return 500f; }
    @Override public float getDamageMultiplier() { return 3.2f; }
    @Override public int getCastTime() { return 25; }
    @Override public int getCooldownTicks() { return 960; }

    @Override
    public String getDescriptionKey() { return "skill.pohunqiang.six.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            Vec3 targetPos = player.position().add(look.x * 5, 0, look.z * 5);
            player.displayClientMessage(Component.literal("§c§l第六魂技：陨星巨枪！"), true);

            // ---- 特效：地面火环法阵 → 天际垂落的巨枪（枪身螺旋 + 落点冲击环）----
            ParticleFx fx = ParticleFx.of(level, player);
            if (fx != null) {
                double rot = serverLevel.getGameTime() * 0.32;

                fx.budget(2000);
                // 落点：半径四格的灼烧法阵 + 双层冲击环
                fx.magicCircle(ParticleTypes.FLAME, ParticleTypes.LAVA, targetPos.add(0, 0.12, 0), ParticleFx.Axis.Y, 4.0, rot);
                fx.shockRing(ParticleTypes.SOUL_FIRE_FLAME, targetPos.add(0, 0.1, 0), 4.0, 3.0, 40);
                fx.shockRing(ParticleTypes.LARGE_SMOKE, targetPos.add(0, 0.05, 0), 6.5, 2.0, 40);

                // 天际垂落的巨枪：15 格长的锥形枪体 + 双螺旋枪身 + 尾焰拖尾
                Vec3 apex = targetPos.add(0, 15.0, 0);
                Vec3 down = new Vec3(0, -1, 0);
                fx.cone(ParticleTypes.CRIT, apex, down, 15.0, 1.5, 30, rot, 6);
                fx.helixAround(ParticleTypes.SOUL_FIRE_FLAME, apex, down, 1.1, 15.0, 6.0, 2, rot, 60);
                fx.line(ParticleTypes.FLAME, apex, targetPos, 0.6, 0.08, 0.0, Vec3.ZERO);
                fx.column(ParticleTypes.FLAME, ParticleTypes.SOUL_FIRE_FLAME, targetPos, 0.35, 15.0, 3, rot, ParticleFx.TAU * 1.5, 1.0);
                // 高空枪尾的光环
                fx.ringAround(ParticleTypes.END_ROD, apex, down, 1.6, 24, rot, 0.03);
                fx.bloom(ParticleTypes.LARGE_SMOKE, targetPos.add(0, 1.0, 0), 30, 1.2, 0.06);
            }

            AABB area = new AABB(targetPos.x - 5, targetPos.y - 2, targetPos.z - 5, targetPos.x + 5, targetPos.y + 5, targetPos.z + 5);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), finalDamage);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, targetPos.x, targetPos.y, targetPos.z, 1, 0, 0, 0, 0);
            level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.5f);
        }
    }
}
