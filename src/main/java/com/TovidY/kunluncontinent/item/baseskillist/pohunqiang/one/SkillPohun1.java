package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.ModItems;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class SkillPohun1 extends BaseSkillItem {
    public SkillPohun1() {
        super();
    }
    @Override
    public int getCastTime() {
        return 0;
    }
    @Override
    public int getCooldownTicks() {
        return 200;
    }
    @Override
    public float getDamageMultiplier() {
        return 1.0f;
    }

    @Override
    public float getBaseCost() {
        return 50f;
    }

    @Override
    public String getDescriptionKey() {
        return "skill.pohunqiang.one.description";
    }

    @Override
    public void applyPenalty(Player player, float costMultiplier) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            float finalCost = 50f * costMultiplier;
            cap.setJingshenli(Math.max(0, currentJs - finalCost));
        });
    }

    @Override
    public void executeEffect(Level level, Player player, float powerMultiplier) {
        if (!level.isClientSide) {
            ItemStack spear = new ItemStack(ModItems.POHUNQIANG.get());

            CompoundTag nbt = spear.getOrCreateTag();
            nbt.putUUID("OwnerUUID", player.getUUID());

            spear.setHoverName(Component.literal("§6" + player.getName().getString() + "的破魂枪").withStyle(ChatFormatting.BOLD));
            if (player.getOffhandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND, spear);
            } else {
                if (!player.getInventory().add(spear)) {
                    player.drop(spear, false);
                }
            }

            player.sendSystemMessage(Component.literal("§c§l破魂枪，现！"));

            ((ServerLevel) level).sendParticles(ParticleTypes.SOUL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    15, 0.2, 0.5, 0.2, 0.05);
        }
    }
}