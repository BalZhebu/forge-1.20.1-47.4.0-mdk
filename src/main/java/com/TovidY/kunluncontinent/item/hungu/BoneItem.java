package com.TovidY.kunluncontinent.item.hungu;

import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttribute;
import com.TovidY.kunluncontinent.capability.itemattribute.ItemAttributeCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BoneItem extends Item {
    public BoneItem(Properties props) {
        super(props.stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemAttribute.appendItemAttribute(stack, tooltip);
    }

    @Override
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemAttributeCapabilityProvider();
    }
}
