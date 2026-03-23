package com.TovidY.kunluncontinent.item.testitemblock;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerUpgradeSystem.processSuccessfulUpgrade;

//测试物品，用于测试等级提升
public class TestLevelUp extends Item {
    private final int levelChange;

    public TestLevelUp(Properties properties, int levelChange) {
        super(properties);
        this.levelChange = levelChange;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
            pPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                int currentLevel = capability.getDengji();
                if (this.levelChange > 0) {
                    for (int i = 0; i < this.levelChange; i++) {
                        int targetLevel = capability.getDengji() + 1;
                        processSuccessfulUpgrade(serverPlayer, capability, targetLevel);
                    }
                    pPlayer.sendSystemMessage(Component.literal("§a等级增加，当前等级: §e" + capability.getDengji()));
                } else if (this.levelChange < 0) {
                    int newLevel = Math.max(0, currentLevel + this.levelChange);
                    capability.setDengji(newLevel);
                    pPlayer.sendSystemMessage(Component.literal("§c等级已回退！当前等级: §e" + newLevel));
                }
                SynsAPI.synsPlayerAttribute(pPlayer);
            });
        }
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }
}