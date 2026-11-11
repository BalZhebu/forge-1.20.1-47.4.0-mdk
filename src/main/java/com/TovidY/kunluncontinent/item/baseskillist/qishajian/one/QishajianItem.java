package com.TovidY.kunluncontinent.item.baseskillist.qishajian.one;

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

/**
 * 七杀剑本体 —— 器武魂的"实体化武器"。
 *
 * <p>结构与 {@code BahuangjiItem} 一致：灵魂绑定、拿在手上给攻击加成、
 * 精神力耗尽或换人持有就溃散。数值按七杀剑"轻、快、偏斩"的定位略调：</p>
 * <ul>
 *   <li>基础攻击加成 1.25 倍（八荒戟是 1.3 倍，但八荒是重武器）；</li>
 *   <li>每 3 秒扣 1 点精神力（与八荒戟相同）。</li>
 * </ul>
 */
public class QishajianItem extends SwordItem {

    /** 手持时的攻击力倍率（基础值，随等级微涨）。 */
    private static final float BASE_MULTIPLIER = 1.25f;
    /** 倍率上限，避免高等级叠到离谱。 */
    private static final float MAX_MULTIPLIER = 4.5f;
    /** 溃散所需的最低精神力。 */
    private static final float MIN_MP = 20.0f;

    public QishajianItem() {
        super(ModToolTiers.COLD_HEARTED_STEEL, 0, -1.8F, new Item.Properties().stacksTo(1).fireResistant());
    }

    /** 无法破坏：耐久永不消耗（详见 BahuangjiItem 里的同一条注释）。 */
    @Override
    public boolean canBeDepleted() {
        return false;
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

        // 2. 非所有者持有 → 清加成并销毁
        if (!nbt.getUUID("OwnerUUID").equals(player.getUUID())) {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                    .ifPresent(cap -> removeBonus(cap, nbt, player));
            stack.setCount(0);
            return;
        }

        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;

        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // 3. 精神力耗尽 → 溃散
            if (cap.getJingshenli() < MIN_MP) {
                removeBonus(cap, nbt, player);
                stack.setCount(0);
                player.sendSystemMessage(Component.literal("§c精神力不足，七杀剑本体溃散了！"));
                return;
            }

            if (inHand) {
                // 每 3 秒扣 1 点精神力
                if (player.tickCount % 60 == 0) {
                    cap.setJingshenli(Math.max(0, cap.getJingshenli() - 1.0f));
                    SynsAPI.synsPlayerAttribute(player);
                }

                // 每 10 tick 刷新一次攻击加成
                if (player.tickCount % 10 == 0) {
                    float multiplier = Math.min(MAX_MULTIPLIER,
                            BASE_MULTIPLIER + player.experienceLevel * 0.001f);
                    float lastBonus = nbt.getFloat("QishaBonusValue");
                    float raw = cap.getGongji() - lastBonus;
                    float newBonus = raw * multiplier - raw;
                    if (Math.abs(newBonus - lastBonus) > 0.1f) {
                        cap.setGongji(raw + newBonus);
                        nbt.putFloat("QishaBonusValue", newBonus);
                        SynsAPI.synsPlayerAttribute(player);
                    }
                }
            } else {
                removeBonus(cap, nbt, player);
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("OwnerName")) {
            tooltip.add(Component.literal("§7当前拥有者: §c" + nbt.getString("OwnerName")));
            tooltip.add(Component.translatable("tooltip.kunluncontinent.qishajian").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("§8§o此武魂已与灵魂绑定，不可掉落"));
        } else {
            tooltip.add(Component.literal("§7尚未绑定所有者"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private void removeBonus(PlayerAttributeCapability cap, CompoundTag nbt, Player player) {
        float lastBonus = nbt.getFloat("QishaBonusValue");
        if (lastBonus > 0) {
            cap.setGongji(Math.max(0, cap.getGongji() - lastBonus));
            nbt.putFloat("QishaBonusValue", 0.0f);
            SynsAPI.synsPlayerAttribute(player);
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .ifPresent(cap -> removeBonus(cap, item.getOrCreateTag(), player));
        item.setCount(0);
        return false;
    }
}
