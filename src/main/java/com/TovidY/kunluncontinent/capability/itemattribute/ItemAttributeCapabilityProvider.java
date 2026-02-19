package com.TovidY.kunluncontinent.capability.itemattribute;


import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemAttributeCapabilityProvider<T extends ItemAttributeCapability> implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<ItemAttributeCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<ItemAttributeCapability>(){});
    private ItemAttributeCapability capability = null;
    private final LazyOptional<ItemAttributeCapability> CapabilityLazyOptional;
    public ItemAttributeCapabilityProvider(){
        CapabilityLazyOptional = LazyOptional.of(this::createPlayerCapability);
    }
    private ItemAttributeCapability createPlayerCapability(){
        if(capability ==null){
            this.capability =  new ItemAttributeCapability();
        }
        return capability;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap== CAPABILITY){
            return CapabilityLazyOptional.cast();
        }
        return LazyOptional.empty();
    }
    @Override
    public CompoundTag serializeNBT() {
        return createPlayerCapability().serializeNBT();
    }
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerCapability().deserializeNBT(nbt);
    }
}
