package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.one;


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

public class SkillBahuang1 extends BaseSkillItem {
    public SkillBahuang1() {
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
    public Component getSkillDescription() {
        return Component.translatable("skill.bakuangji.one.description").withStyle(ChatFormatting.GRAY);
    }
    @Override
    public void applyPenalty(Player player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            float currentJs = cap.getJingshenli();
            cap.setJingshenli(Math.max(0, currentJs - 50f));
        });
    }
    @Override
    public void executeEffect(Level level, Player player) {
        if (!level.isClientSide) {
            ItemStack spear = new ItemStack(Items.DIAMOND_SWORD);

            spear.setHoverName(Component.literal("§6八荒戟 (实体化)").withStyle(ChatFormatting.BOLD));

            if (!player.getInventory().add(spear)) {
                player.drop(spear, false);
            }

            ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 1, player.getZ(),
                    5, 0.2, 0.5, 0.2, 0.0);

            player.sendSystemMessage(Component.literal("§c§l八荒戟，出！"));
        }
    }
}