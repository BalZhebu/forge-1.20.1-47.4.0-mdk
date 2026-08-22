package com.TovidY.kunluncontinent.item.testitemblock;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class InstantKillSwordItem extends SwordItem {

    public InstantKillSwordItem() {
        super(Tiers.NETHERITE, 0, -2.4F, new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            target.invulnerableTime = 0;
            DamageSource source = (attacker instanceof Player player)
                    ? attacker.damageSources().playerAttack(player)
                    : attacker.damageSources().mobAttack(attacker);
            target.hurt(source, Float.MAX_VALUE);
            if (target.isAlive()) {
                target.setHealth(0);
                target.die(source);
                if (!(target instanceof Player)) {
                    target.discard();
                }
            }
            if (attacker instanceof Player player) {
                player.displayClientMessage(Component.literal("§c已抹除：")
                        .append(target.getDisplayName()), true);
            }
        }
        return true;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String line1 = Component.translatable("tooltip.kunlun.instant_kill_sword.1").getString();
        String line2 = Component.translatable("tooltip.kunlun.instant_kill_sword.2").getString();
        tooltip.add(makeRainbowComponent(line1).copy().withStyle(ChatFormatting.BOLD));
        tooltip.add(makeRainbowComponent(line2).copy().withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private Component makeRainbowComponent(String text) {
        MutableComponent root = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            float hue = (i * 0.15f) % 1.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
            root.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(rgb)));
        }
        return root;
    }
}
