package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SkillPohun1 extends BaseSkillItem {
    public SkillPohun1() {
        super();
    }
    @Override
    public int getCastTime() {
        return 0; // 瞬发 10 = 0.5秒
    }
    @Override
    public int getCooldownTicks() {
        return 200; // 10秒冷却 (20 * 10)
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
            ItemStack spear = new ItemStack(Items.DIAMOND_SWORD);

            spear.setHoverName(Component.literal("§6破魂枪 (实体化)").withStyle(ChatFormatting.BOLD));

            if (!player.getInventory().add(spear)) {
                player.drop(spear, false);
            }

            ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 1, player.getZ(),
                    5, 0.2, 0.5, 0.2, 0.0);

            player.sendSystemMessage(Component.literal("§c§l破魂枪，现！"));
        }
    }
}