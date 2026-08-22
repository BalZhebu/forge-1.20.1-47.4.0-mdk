package com.TovidY.kunluncontinent.item.baseskillist.bahuangji.one;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BahuangjiItem extends SwordItem {
    public BahuangjiItem() {
        super(ModToolTiers.COLD_HEARTED_STEEL, 0, -1.8F, new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        CompoundTag nbt = stack.getOrCreateTag();

        // 1. 自动绑定所有者
        if (!nbt.contains("OwnerUUID")) {
            nbt.putUUID("OwnerUUID", player.getUUID());
            nbt.putString("OwnerName", player.getScoreboardName());
        }

        // 2. 非所有者使用校验：清理加成并直接销毁
        if (!nbt.getUUID("OwnerUUID").equals(player.getUUID())) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                removeBonus(cap, nbt, player);
            });
            stack.setCount(0);
            return;
        }

        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // 3. 精神力不足校验
            if (cap.getJingshenli() < 20.0f) {
                removeBonus(cap, nbt, player);
                stack.setCount(0);
                player.sendSystemMessage(Component.literal("§c精神力不足，八荒戟本体溃散了！"));
                return;
            }

            // 4. 手持逻辑处理
            if (inHand) {
                // 每 3 秒 (60 Ticks) 扣除 1 点精神力
                if (player.tickCount % 60 == 0) {
                    cap.setJingshenli(Math.max(0, cap.getJingshenli() - 1.0f));
                    SynsAPI.synsPlayerAttribute(player);
                }

                // 每 10 Ticks 检查与计算攻击力加成，降低服务器频繁同步的开销
                if (player.tickCount % 10 == 0) {
                    float multiplier = 1.2f + (player.experienceLevel * 0.001f);
                    multiplier = Math.min(multiplier, 3.0f);

                    float currentGongji = cap.getGongji();
                    float lastBonus = nbt.getFloat("BahuangBonusValue");
                    float rawGongji = currentGongji - lastBonus;
                    float expectedTotal = rawGongji * multiplier;
                    float newBonus = expectedTotal - rawGongji;

                    // 变化大于 0.1f 时才更新并同步发包
                    if (Math.abs(newBonus - lastBonus) > 0.1f) {
                        cap.setGongji(rawGongji + newBonus);
                        nbt.putFloat("BahuangBonusValue", newBonus);
                        SynsAPI.synsPlayerAttribute(player);
                    }
                }
            } else {
                // 没握在手上（在背包里）时清除加成
                removeBonus(cap, nbt, player);
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("OwnerName")) {
            String ownerName = nbt.getString("OwnerName");
            tooltip.add(Component.literal("§7当前拥有者: §6" + ownerName));
            tooltip.add(Component.literal("§8§o此武魂已与灵魂绑定，不可掉落"));
        } else {
            tooltip.add(Component.literal("§7尚未绑定所有者"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private void removeBonus(PlayerAttributeCapability cap, CompoundTag nbt, Player player) {
        float lastBonus = nbt.getFloat("BahuangBonusValue");
        if (lastBonus > 0) {
            cap.setGongji(Math.max(0, cap.getGongji() - lastBonus));
            nbt.putFloat("BahuangBonusValue", 0.0f);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            removeBonus(cap, item.getOrCreateTag(), player);
        });
        item.setCount(0);
        return false;
    }
}