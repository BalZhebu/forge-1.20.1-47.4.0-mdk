package com.TovidY.kunluncontinent.item.baseskillist.liejinhu.four;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SkillLeijinhu4 extends BaseSkillItem {

    @Override public float getBaseCost() { return 210f; }
    @Override public float getDamageMultiplier() { return 2.3f; }
    @Override public int getCastTime() { return 60; }
    @Override public int getCooldownTicks() { return 640; } // 32s

    @Override
    public String getDescriptionKey() {
        return "skill.leijinhu.four.description";
    }


    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos center = player.blockPosition().relative(player.getDirection(), 2);

            // 范围伤害
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(2.0), e -> e != player);
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().mobAttack(player), finalDamage);
            }

            // 破坏方块逻辑 (3x3x3)
            BlockPos.betweenClosedStream(center.offset(-2, 0, -2), center.offset(2, 1, 2)).forEach(pos -> {
                BlockState state = level.getBlockState(pos);
                // 判定硬度低于 1.5 的方块（泥土、沙子、树叶、木头等）
                if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0 && state.getDestroySpeed(level, pos) <= 1.5f) {
                    level.destroyBlock(pos, true);
                }
            });

            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.getX(), center.getY(), center.getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);
            player.displayClientMessage(Component.literal("§e§l第四魂技：碎金！"), true);
        }
    }
}
