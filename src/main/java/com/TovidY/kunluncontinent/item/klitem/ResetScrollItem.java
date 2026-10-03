package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.capability.playerattributes.AttributePoints;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 重置卷轴：右键消耗 1 个自身，重置全部属性点并把点数全额退还。
 *
 * <p>卷轴的扣除与属性点重置都在 {@link AttributePoints#resetWithScroll} 里，
 * 本类<b>只负责响应右键</b>，不要在这里再写一遍扣卷轴的逻辑。
 * 与属性点面板上的"重置属性点"按钮共用同一个入口，两条路行为完全一致。</p>
 */
public class ResetScrollItem extends Item {

    public ResetScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel,
                                List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.reset_scroll.tooltip").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("item.reset_scroll.tooltip1").withStyle(ChatFormatting.DARK_GRAY));
        pTooltipComponents.add(Component.translatable("item.reset_scroll.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        boolean ok = serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(cap -> AttributePoints.resetWithScroll(serverPlayer, cap))
                .orElse(false);

        if (ok) {
            SynsAPI.synsPlayerAttribute(serverPlayer);
            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}