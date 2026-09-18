package com.TovidY.kunluncontinent.item.baseskillist.pohunqiang.one;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.tool.ModToolTiers;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
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

public class PohunqiangItem extends SwordItem {
    public PohunqiangItem() {
        super(ModToolTiers.TEST_ITEM, 0, -1.8F, new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        CompoundTag nbt = stack.getOrCreateTag();

        // 1. 绑定所有者逻辑
        if (!nbt.contains("OwnerUUID")) {
            nbt.putUUID("OwnerUUID", player.getUUID());
            nbt.putString("OwnerName", player.getScoreboardName());
        }

        // 2. 非所有者使用校验：清除加成并销毁
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
                player.sendSystemMessage(Component.literal("§c精神力不足，武魂本体溃散了！"));
                return;
            }

            // 4. 手持逻辑处理
            if (inHand) {
                // 每 3 秒 (60 ticks) 扣除精神力
                if (player.tickCount % 60 == 0) {
                    cap.setJingshenli(Math.max(0, cap.getJingshenli() - 1.0f));
                    SynsAPI.synsPlayerAttribute(player);
                }

                // 限制更新属性加成的频率，防止每 Tick 计算与网络发包（每 10 Ticks 检查一次）
                if (player.tickCount % 10 == 0) {
                    float multiplier = 1.25f + (player.experienceLevel * 0.001f);
                    multiplier = Math.min(multiplier, 3.0f);

                    float currentGongji = cap.getGongji();
                    float lastBonus = nbt.getFloat("PohunBonusValue");
                    float rawGongji = currentGongji - lastBonus;
                    float expectedTotal = rawGongji * multiplier;
                    float newBonus = expectedTotal - rawGongji;

                    // 只有当增益变化大于 0.1 时才更新并发包，降低高频同步消耗
                    if (Math.abs(newBonus - lastBonus) > 0.1f) {
                        cap.setGongji(rawGongji + newBonus);
                        nbt.putFloat("PohunBonusValue", newBonus);
                        SynsAPI.synsPlayerAttribute(player);
                    }
                }
            } else {
                // 不在手持状态（仅在背包中）时移除加成
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

            tooltip.add(Component.translatable("tooltip.kunluncontinent.pohunqiang").withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.literal("§8§o此武魂已与灵魂绑定，不可掉落"));
        } else {
            tooltip.add(Component.literal("§7尚未绑定所有者"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private void removeBonus(PlayerAttributeCapability cap, CompoundTag nbt, Player player) {
        float lastBonus = nbt.getFloat("PohunBonusValue");
        if (lastBonus > 0) {
            cap.setGongji(Math.max(0, cap.getGongji() - lastBonus));
            nbt.putFloat("PohunBonusValue", 0.0f);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        // 按 Q 丢弃时立刻销毁并清除增益
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            removeBonus(cap, item.getOrCreateTag(), player);
        });
        item.setCount(0);
        return false;
    }
}