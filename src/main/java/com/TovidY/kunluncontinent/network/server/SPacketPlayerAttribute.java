package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.network.client.ClientPacketHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// 同步玩家属性数据的网络包

public class SPacketPlayerAttribute {
    private final int entityId;
    private final CompoundTag nbt;

    public SPacketPlayerAttribute(int entityId, CompoundTag nbt) {
        this.entityId = entityId;
        this.nbt = nbt;
    }

    public static void encode(SPacketPlayerAttribute msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeNbt(msg.nbt);
    }

    public static SPacketPlayerAttribute decode(FriendlyByteBuf buf) {
        return new SPacketPlayerAttribute(buf.readInt(), buf.readNbt());
    }

    public static void handle(SPacketPlayerAttribute msg, Supplier<NetworkEvent.Context> ctx) {
        // 客户端逻辑统一在 ClientPacketHandlers（本类不能出现 Minecraft/ClientLevel）
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.playerAttribute(msg.entityId, msg.nbt)));
        ctx.get().setPacketHandled(true);
    }
}