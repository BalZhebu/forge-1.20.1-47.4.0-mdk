package com.TovidY.kunluncontinent.capability.mobattributes;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//生物属性能力提供者

public class MobAttributeCapabilityProvider<T extends MobAttributeCapability> implements ICapabilityProvider,ICapabilitySerializable<CompoundTag> {
    public static Capability<MobAttributeCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<MobAttributeCapability>() {});

    private MobAttributeCapability instance = null;
    private final LazyOptional<MobAttributeCapability> optional = LazyOptional.of(this::createInstance);

    private MobAttributeCapability createInstance() {
        if (instance == null) {
            this.instance = new MobAttributeCapability();
        }
        return instance;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return createInstance().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createInstance().deserializeNBT(nbt);
    }
}
