package com.TovidY.kunluncontinent.item.baseskillist.juyuan.four;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillPanshijuyuan4 extends BaseSkillItem {
    @Override public float getBaseCost() { return 220f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 800; }

    @Override
    public String getDescriptionKey() {
        return "skill.panshijuyuan.four.description";
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(ModEffects.MOVING_MOUNTAINS.get(), 400, 1));

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY(), player.getZ(), 20, 0.5, 1, 0.5, 0.05);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);
            player.displayClientMessage(Component.literal("§8§l第四魂技：搬山！"), true);
        }
    }
}
