package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
        ctx.get().enqueueWork(() -> {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
                if (level != null) {
                    net.minecraft.world.entity.Entity entity = level.getEntity(msg.entityId);
                    if (entity instanceof net.minecraft.world.entity.player.Player player) {
                        player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                            cap.deserializeNBT(msg.nbt);
                        });
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}