package com.TovidY.kunluncontinent.item.klitem;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerHunhuanAPI;
import com.TovidY.kunluncontinent.capability.playerattributes.Wuhunname;
import com.TovidY.kunluncontinent.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WuhunguoshiItem extends Item {
    private String wuhunname;
    public WuhunguoshiItem(Properties properties) {
        super(properties);
    }

    public static ItemStack getWuhunguo(String s) {
        ItemStack itemStack = ItemStack.EMPTY;
        switch (s){
            case Wuhunname.pohunqiang:
                itemStack = new ItemStack(ModItems.GUOSHI_POHUNQIANG.get());
                break;
            case Wuhunname.bahuangji:
                itemStack = new ItemStack(ModItems.GUOSHI_BAHUANGJI.get());
                break;
            case Wuhunname.liejinhu:
                itemStack = new ItemStack(ModItems.GUOSHI_LEIJINHU.get());
                break;
            case Wuhunname.panshijuyuan:
                itemStack = new ItemStack(ModItems.GUOSHI_PANSHIJUYUAN.get());
                    break;
        }
        return itemStack;
    }


    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.getDengji() <= 0) {
                    serverPlayer.sendSystemMessage(Component.literal("你的等级不足，无法承载武魂觉醒！").withStyle(ChatFormatting.RED));
                    return;
                }
                List<String> currentWuhuns = cap.getWuhunListsname();
                if (currentWuhuns.contains(this.getWuhunname())) {
                    serverPlayer.sendSystemMessage(Component.literal("你已经觉醒过该武魂了。").withStyle(ChatFormatting.YELLOW));
                    return;
                }
                if (currentWuhuns.size() >= 3) {
                    serverPlayer.sendSystemMessage(Component.literal("一个人的识海有限，最多只能承载 3 个武魂！").withStyle(ChatFormatting.DARK_RED));
                    return;
                }
                PlayerHunhuanAPI.addWuHun(serverPlayer, this.getWuhunname());
                serverPlayer.sendSystemMessage(Component.literal("成功觉醒武魂：" + this.getWuhunname()).withStyle(ChatFormatting.GREEN));
                if (!serverPlayer.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            });
        }
        return itemStack;
    }

    public String getWuhunname() {
        return wuhunname;
    }

    public WuhunguoshiItem setWuhunname(String wuhunname) {
        this.wuhunname = wuhunname;
        return this;
    }

    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Component.translatable("服用后觉醒武魂",wuhunname).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
