package com.TovidY.kunluncontinent.item.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

//剑类武器增幅类
public class ModSwordBaseItem extends SwordItem {

    public ModSwordBaseItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    // 攻击力加成
    public float getGongji(ItemStack stack) {
        return this.getTier() instanceof ICustomWeaponAttributes attrs ? attrs.getCustomGongji() : 0.0f;
    }

    // 物理穿透
    public float getWuchuan(ItemStack stack) {
        return this.getTier() instanceof ICustomWeaponAttributes attrs ? attrs.getCustomWuchuan() : 0.0f;
    }

    // 吸血
    public float getXixue(ItemStack stack) {
        return this.getTier() instanceof ICustomWeaponAttributes attrs ? attrs.getCustomXixue() : 0.0f;
    }

    // 暴击
    public float getBaoji(ItemStack stack) {
        return this.getTier() instanceof ICustomWeaponAttributes attrs ? attrs.getCustomBaoji() : 0.0f;
    }

    // 暴击伤害
    public float getBaojiShanghai(ItemStack stack) {
        return this.getTier() instanceof ICustomWeaponAttributes attrs ? attrs.getCustomBaojiShanghai() : 0.0f;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        float gongji = getGongji(stack);
        float wuchuan = getWuchuan(stack);
        float xixue = getXixue(stack);
        float baoji = getBaoji(stack);
        float baojiSh = getBaojiShanghai(stack);

        // 如果没有任何自定义属性，直接返回，不打扰后续渲染
        if (gongji <= 0 && wuchuan <= 0 && xixue <= 0 && baoji <= 0 && baojiSh <= 0) {
            return;
        }

        list.add(Component.translatable("武器附加属性").withStyle(ChatFormatting.DARK_AQUA));

        if (gongji > 0) {
            list.add(Component.translatable("攻击: " + gongji).withStyle(ChatFormatting.YELLOW));
        }
        if (wuchuan > 0) {
            list.add(Component.translatable("穿透: " + (int)(wuchuan * 100) + "%").withStyle(ChatFormatting.YELLOW));
        }
        if (xixue > 0) {
            list.add(Component.translatable("吸血: " + (int)(xixue * 100) + "%").withStyle(ChatFormatting.YELLOW));
        }
        if (baoji > 0) {
            list.add(Component.translatable("暴击率: " + (int)(baoji * 100) + "%").withStyle(ChatFormatting.YELLOW));
        }
        if (baojiSh > 0) {
            list.add(Component.translatable("暴击伤害: " + (int)(baojiSh * 100) + "%").withStyle(ChatFormatting.YELLOW));
        }

        list.add(Component.translatable("拿在手上生效").withStyle(ChatFormatting.DARK_GRAY));
    }
}