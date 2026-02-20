package com.TovidY.kunluncontinent.capability.itemattribute;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
public interface ItemAttribute {
    static void appendItemAttribute(ItemStack stack, List<Component> tooltip) {
        stack.getCapability(ItemAttributeCapabilityProvider.CAPABILITY).ifPresent(attr -> {
            if (attr.getNianxian() > 0) {
                if (attr.getSourceName() != null && !attr.getSourceName().isEmpty()) {
                    tooltip.add(Component.literal("魂骨来源: " + attr.getSourceName()).withStyle(ChatFormatting.GRAY));
                }

                tooltip.add(Component.literal("年限: " + attr.getNianxian() + "年").withStyle(ChatFormatting.GOLD));
                List<String> active = attr.getActiveAttributes();
                if (active.contains("maxshengming"))
                    tooltip.add(Component.literal("生命加成: +" + String.format("%.1f", attr.getMaxshengming())).withStyle(ChatFormatting.BLUE));

                if (active.contains("gongji"))
                    tooltip.add(Component.literal("攻击: +" + String.format("%.1f", attr.getGongji())).withStyle(ChatFormatting.BLUE));

                if (active.contains("fangyu"))
                    tooltip.add(Component.literal("防御: +" + String.format("%.1f", attr.getFangyu())).withStyle(ChatFormatting.BLUE));

                if (active.contains("baojilv"))
                    tooltip.add(Component.literal("暴击率: +" + String.format("%.1f", attr.getBaojilv()) + "%").withStyle(ChatFormatting.BLUE));

                if (active.contains("baojishanghai"))
                    tooltip.add(Component.literal("暴击伤害: +" + String.format("%.1f", attr.getBaojishanghai()) + "%").withStyle(ChatFormatting.BLUE));

                if (active.contains("wuchuan"))
                    tooltip.add(Component.literal("穿透: +" + String.format("%.1f", attr.getWuchuan())).withStyle(ChatFormatting.BLUE));

                if (active.contains("shanbi"))
                    tooltip.add(Component.literal("闪避: +" + String.format("%.1f", attr.getShanbi())).withStyle(ChatFormatting.BLUE));

                if (active.contains("mingzhong"))
                    tooltip.add(Component.literal("命中: +" + String.format("%.1f", attr.getMingzhong())).withStyle(ChatFormatting.BLUE));

                if (active.contains("kangbao"))
                    tooltip.add(Component.literal("抗暴: +" + String.format("%.1f", attr.getKangbao())).withStyle(ChatFormatting.BLUE));

                if (active.contains("shengminghuifu"))
                    tooltip.add(Component.literal("生命恢复: +" + String.format("%.1f", attr.getShengminghuifu())).withStyle(ChatFormatting.BLUE));

                if (active.contains("xixue"))
                    tooltip.add(Component.literal("吸血: +" + String.format("%.1f", attr.getXixue())).withStyle(ChatFormatting.BLUE));
            }
        });
    }
}