package com.TovidY.kunluncontinent.item.baseskillist.juyuan.five;

import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.potion.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SkillPanshijuyuan5 extends BaseSkillItem {
    @Override public float getBaseCost() { return 280f; }
    @Override public float getDamageMultiplier() { return 0f; }
    @Override public int getCastTime() { return 10; }
    @Override public int getCooldownTicks() { return 800; }

    @Override
    public String getDescriptionKey() { return "skill.panshijuyuan.five.description"; }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier, float finalDamage) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("§8§l第五魂技：石铠！"), true);
            player.addEffect(new MobEffectInstance(ModEffects.STONE_ARMOR.get(), 400, 4));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1.2f, 0.6f);
        }
    }
}
