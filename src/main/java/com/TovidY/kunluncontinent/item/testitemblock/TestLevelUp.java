package com.TovidY.kunluncontinent.item.testitemblock;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

//测试物品，用于测试等级提升
public class TestLevelUp extends Item {
    private final int levelChange; // 1 表示加一级，-1 表示减一级

    public TestLevelUp(Properties properties, int levelChange) {
        super(properties);
        this.levelChange = levelChange;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide()) {
            pPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(capability -> {
                int currentLevel = capability.getDengji();
                int newLevel = currentLevel + this.levelChange;
                if (newLevel < 0) newLevel = 0;
                capability.setDengji(newLevel);
                SynsAPI.synsPlayerAttribute(pPlayer);
                pPlayer.sendSystemMessage(Component.literal("§a等级已变动！当前等级: §e" + newLevel));
            });
        }
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }
}