package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.four;

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

public class SkillPohun4 extends BaseSkillItem {
    @Override public float getBaseCost() { return 220f; }
    @Override public float getDamageMultiplier() { return 2.2f; }
    @Override public int getCastTime() { return 40; }
    @Override public int getCooldownTicks() { return 700; } // 35s

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.four.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 start = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            for (int i = 1; i <= 20; i++) {
                Vec3 point = start.add(look.scale(i));
                serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 5, 0.1, 0.1, 0.1, 0.05);
                serverLevel.sendParticles(ParticleTypes.FLASH, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                AABB checkArea = new AABB(point.x - 1.2, point.y - 1.2, point.z - 1.2, point.x + 1.2, point.y + 1.2, point.z + 1.2);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, checkArea,
                        e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    target.hurt(level.damageSources().indirectMagic(player, player), finalDamage);
                    target.invulnerableTime = 0;
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.5f, 2.0f);
            player.displayClientMessage(Component.literal("§c§l第四魂技：贯日！"), true);
        }
    }
}
