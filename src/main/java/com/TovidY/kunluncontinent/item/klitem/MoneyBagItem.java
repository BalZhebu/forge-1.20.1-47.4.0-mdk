package com.TovidY.kunluncontinent.item.klitem;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class MoneyBagItem extends Item {

    private final MoneyBagLootTable.BagType bagType;

    public MoneyBagItem(Properties properties, MoneyBagLootTable.BagType bagType) {
        super(properties);
        this.bagType = bagType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            List<ItemStack> loots = MoneyBagLootTable.generateLoot(this.bagType, level.getRandom());
            for (ItemStack loot : loots) {
                if (!player.getInventory().add(loot)) {
                    player.drop(loot, false);
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}