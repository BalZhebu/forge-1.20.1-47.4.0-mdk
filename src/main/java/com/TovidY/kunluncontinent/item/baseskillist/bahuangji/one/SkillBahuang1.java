package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.one;


import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SkillBahuang1 extends BaseSkillItem {
    public SkillBahuang1() {
        super();
    }

    @Override
    public int getCastTime() {
        return 0; // 瞬发
    }

    @Override
    public float getBaseCost() {
        return 50f;
    }

    @Override
    public int getCooldownTicks() {
        return 200; // 10秒冷却
    }

    @Override
    public float getDamageMultiplier() {
        return 1.0f;
    }

    @Override
    public String getDescriptionKey() {
        return "skill.bakuangji.one.description";
    }

    @Override
    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            float finalCost = 50f * costMultiplier;
            cap.setJingshenli(Math.max(0, currentJs - finalCost));
            SynsAPI.synsPlayerAttribute(player);
        });
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ItemStack bahuangji = new ItemStack(ModItems.BAHUANGJI.get());
            CompoundTag nbt = bahuangji.getOrCreateTag();
            nbt.putUUID("OwnerUUID", player.getUUID());
            nbt.putString("OwnerName", player.getScoreboardName());
            bahuangji.setHoverName(Component.literal("§c§l八荒戟")
                    .append(Component.literal(" §7(本命武魂)").withStyle(ChatFormatting.ITALIC)));
            if (player.getOffhandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND, bahuangji);
            } else {
                if (!player.getInventory().add(bahuangji)) {
                    player.drop(bahuangji, false);
                }
            }
            ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 0.3, 0.8, 0.3, 0.05);
            ((ServerLevel) level).sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);

            player.sendSystemMessage(Component.literal("§4§l[武魂召唤] §c八荒戟，出！"));
        }
    }
}