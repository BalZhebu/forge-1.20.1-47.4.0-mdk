package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.tool.ModToolTiers;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public class PohunqiangItem extends SwordItem {
    public PohunqiangItem() {
        super(ModToolTiers.TEST_ITEM, 0, -1.8F, new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag nbt = stack.getOrCreateTag();
            if (nbt.contains("OwnerUUID") && !nbt.getUUID("OwnerUUID").equals(player.getUUID())) {
                stack.setCount(0);
                return;
            }
            boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.getJingshenli() < 20.0f) {
                    removeBonus(cap, nbt, player);
                    stack.setCount(0);
                    player.sendSystemMessage(Component.literal("§c精神力不足，武魂本体溃散了！"));
                    return;
                }
                if (inHand) {
                    if (player.tickCount % 60 == 0) {
                        cap.setJingshenli(Math.max(0, cap.getJingshenli() - 1.0f));
                        SynsAPI.synsPlayerAttribute(player);
                    }
                    float multiplier = 1.2f + (player.experienceLevel * 0.001f);
                    multiplier = Math.min(multiplier, 3.0f);
                    float currentGongji = cap.getGongji();
                    float lastBonus = nbt.getFloat("PohunBonusValue");
                    float rawGongji = currentGongji - lastBonus;
                    float expectedTotal = rawGongji * multiplier;
                    float newBonus = expectedTotal - rawGongji;
                    if (Math.abs(newBonus - lastBonus) > 0.01f) {
                        cap.setGongji(rawGongji + newBonus);
                        nbt.putFloat("PohunBonusValue", newBonus);
                        SynsAPI.synsPlayerAttribute(player);
                    }
                } else {
                    removeBonus(cap, nbt, player);
                }
            });
        }
    }

    private void removeBonus(PlayerAttributeCapability cap, CompoundTag nbt, Player player) {
        float lastBonus = nbt.getFloat("PohunBonusValue");
        if (lastBonus > 0) {
            cap.setGongji(cap.getGongji() - lastBonus);
            nbt.putFloat("PohunBonusValue", 0.0f);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        item.setCount(0);
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            removeBonus(cap, item.getOrCreateTag(), player);
        });
        return false;
    }
}