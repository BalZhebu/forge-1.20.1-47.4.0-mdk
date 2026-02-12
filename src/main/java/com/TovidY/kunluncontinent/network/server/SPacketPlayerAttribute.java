package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collections;
import java.util.function.Supplier;

// 同步玩家属性数据的网络包
public class SPacketPlayerAttribute {

    private int entityId;
    private CompoundTag nbt;

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
        ctx.get().enqueueWork(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                Entity entity = world.getEntity(msg.entityId);
                if (entity != null) {
                    entity.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                        cap.deserializeNBT(msg.nbt);
                        if (cap.getWuhunListsname() != null) {
                            Collections.sort(cap.getWuhunListsname());
                        }
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
