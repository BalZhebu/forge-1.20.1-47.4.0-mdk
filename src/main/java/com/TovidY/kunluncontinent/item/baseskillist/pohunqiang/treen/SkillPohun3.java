package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.treen;

import com.TovidY.kunluncontinent.capability.ModAttributeAPI;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SkillPohun3 extends BaseSkillItem {
    @Override
    public float getBaseCost() { return 150f; }
    @Override
    public float getDamageMultiplier() { return 1.5f; } // 基础倍率
    @Override
    public int getCastTime() { return 5; }
    @Override
    public int getCooldownTicks() { return 300; } // 15s

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.treen.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 view = player.getViewVector(1.0F);
            AABB area = player.getBoundingBox().expandTowards(view.scale(5.0)).inflate(1.0);

            LivingEntity target = null;
            for (Entity e : level.getEntities(player, area, entity -> entity instanceof LivingEntity && entity.isAlive())) {
                if (player.distanceTo(e) <= 5.0) {
                    target = (LivingEntity) e;
                    break;
                }
            }

            if (target != null) {
                float targetFangyu = ModAttributeAPI.getFangyu(target);
                float effectiveFangyu = targetFangyu * 0.7f;
                float reductionFactor = 100f / (100f + effectiveFangyu);
                float actualDamage = finalDamage * reductionFactor;
                target.hurt(level.damageSources().indirectMagic(player, player), Math.max(1.0f, actualDamage));
                for (double d = 0; d < target.distanceTo(player); d += 0.5) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            player.getX() + view.x * d, player.getY() + 1.2 + view.y * d, player.getZ() + view.z * d,
                            1, 0, 0, 0, 0);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.2f, 1.5f);
                player.displayClientMessage(Component.literal("§c§l第三魂技：破甲！"), true);
            }
        }
    }
}