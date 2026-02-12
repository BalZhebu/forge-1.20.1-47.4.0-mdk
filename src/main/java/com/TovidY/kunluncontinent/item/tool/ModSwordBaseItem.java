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
    //1 = 1F
    public float getGongji(ItemStack stack) {
        if (this.getTier() == ModToolTiers.GRAY_IRON) return 30.0f;
        if (this.getTier() == ModToolTiers.CLOUD_PATTERNED_BRONZE) return 50.0f;

        if (this.getTier() == ModToolTiers.TEST_ITEM) return 999999.0f;
        return 0;
    }

    // 物理穿透
    //1 = 100%
    public float getWuchuan(ItemStack stack) {
        if (this.getTier() == ModToolTiers.CLOUD_PATTERNED_BRONZE) return 0.35f;

        if (this.getTier() == ModToolTiers.TEST_ITEM) return 100.0f;
        return 0;
    }

    // 吸血
    //1 = 100%
    public float getXixue(ItemStack stack) {


        if (this.getTier() == ModToolTiers.TEST_ITEM) return 100f;
        return 0;
    }

    // 暴击
    //1 = 100%
    public float getBaoji(ItemStack stack) {


        if (this.getTier() == ModToolTiers.TEST_ITEM) return 100f;
        return 0;
    }

    // 暴击伤害
    //1 = 100%
    public float getBaojiShanghai(ItemStack stack) {


        if (this.getTier() == ModToolTiers.TEST_ITEM) return 300f;
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {

        float gongji = getGongji(stack);
        float wuchuan = getWuchuan(stack);
        float xixue = getXixue(stack);
        float baoji = getBaoji(stack);
        float baojiSh = getBaojiShanghai(stack);

        if (gongji > 0 || wuchuan > 0 || xixue > 0 || baoji > 0 || baojiSh > 0) {
            list.add(Component.translatable("武器附加属性").withStyle(ChatFormatting.DARK_AQUA));
        }

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

        if (gongji > 0 || wuchuan > 0 || xixue > 0 || baoji > 0 || baojiSh > 0) {
            list.add(Component.translatable("拿在手上生效").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}