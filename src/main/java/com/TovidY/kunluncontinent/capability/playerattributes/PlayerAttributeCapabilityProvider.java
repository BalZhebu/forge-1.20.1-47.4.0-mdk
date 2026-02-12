package com.TovidY.kunluncontinent.capability.playerattributes;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

//玩家属性注册器
public class PlayerAttributeCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<PlayerAttributeCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerAttributeCapability>() {});

    private PlayerAttributeCapability capability = null;

    private CompoundTag nbtData = null;

    private final LazyOptional<PlayerAttributeCapability> CapabilityLazyOptional;

    public PlayerAttributeCapabilityProvider() {
        CapabilityLazyOptional = LazyOptional.of(this::createPlayerCapability);
    }

    private PlayerAttributeCapability createPlayerCapability() {
        if (capability == null) {
            this.capability = new PlayerAttributeCapability();
            if (nbtData != null) {
                capability.deserializeNBT(nbtData);
                nbtData = null;
            }
        }
        return capability;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CAPABILITY) {
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
        this.nbtData = nbt.copy();
        if (capability != null) {
            capability.deserializeNBT(nbt);
        }
    }
}