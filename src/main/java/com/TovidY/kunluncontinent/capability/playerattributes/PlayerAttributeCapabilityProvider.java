package com.TovidY.kunluncontinent.capability.playerattributes;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PlayerAttributeCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    // 注册 Capability
    public static Capability<PlayerAttributeCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerAttributeCapability>() {});

    // 存储 Capability 实例
    private PlayerAttributeCapability capability = null;
    
    // 用于存储从 NBT 反序列化的数据
    private CompoundTag nbtData = null;

    // 包装能力的 LazyOptional，延迟加载
    private final LazyOptional<PlayerAttributeCapability> CapabilityLazyOptional;

    public PlayerAttributeCapabilityProvider() {
        // 初始化 LazyOptional，确保懒加载
        CapabilityLazyOptional = LazyOptional.of(this::createPlayerCapability);
    }

    // 创建新的 PlayerAttributeCapability 实例
    private PlayerAttributeCapability createPlayerCapability() {
        if (capability == null) {
            this.capability = new PlayerAttributeCapability(); // 创建一个新的属性实例
            // 如果有存储的 NBT 数据，则从 NBT 恢复
            if (nbtData != null) {
                capability.deserializeNBT(nbtData);
                nbtData = null; // 清除临时存储
            }
        }
        return capability;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // 如果请求的是 PlayerAttributeCapability，就返回对应的 LazyOptional
        if (cap == CAPABILITY) {
            return CapabilityLazyOptional.cast();
        }
        return LazyOptional.empty(); // 否则返回空的 LazyOptional
    }

    @Override
    public CompoundTag serializeNBT() {
        // 序列化 PlayerAttributeCapability 实例的 NBT 数据
        return createPlayerCapability().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // 存储 NBT 数据，直到 capability 实例被创建时使用
        this.nbtData = nbt.copy();
        // 如果 capability 已存在，则直接应用 NBT 数据
        if (capability != null) {
            capability.deserializeNBT(nbt);
        }
    }
}