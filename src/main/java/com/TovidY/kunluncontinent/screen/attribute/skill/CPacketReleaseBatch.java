package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 客户端 → 服务端：一次按到了多个魂技槽位（也就是"一键释放"）。
 *
 * <p>当玩家在轮盘左侧把同一个物理键绑到多个槽位上时，客户端会把这些槽位<b>合并成一个包</b>
 * 发过来，而不是一人一个 {@link CPacketReleaseDirectSkill}。服务端交给
 * {@link SkillBatchRelease} 逐个、按序、跨 tick 释放并汇总反馈。</p>
 *
 * <p>携带具体槽位列表而不是"全部 9 个"，是因为共用一键未必是全部 —— 也可能只是
 * 第一/第三魂技绑在同一个键上。</p>
 */
public class CPacketReleaseBatch {

    /** 一次最多 9 个槽位，收发两侧都按这个上限做防护。 */
    private static final int MAX_SLOTS = 9;

    private final List<Integer> slots;

    public CPacketReleaseBatch(List<Integer> slots) {
        this.slots = slots;
    }

    public static void encode(CPacketReleaseBatch msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.slots.size());
        for (int slot : msg.slots) {
            buffer.writeInt(slot);
        }
    }

    public static CPacketReleaseBatch decode(FriendlyByteBuf buffer) {
        int size = Math.min(buffer.readInt(), MAX_SLOTS);
        List<Integer> slots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            slots.add(buffer.readInt());
        }
        return new CPacketReleaseBatch(slots);
    }

    public static void handle(CPacketReleaseBatch msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap ->
                    SkillBatchRelease.start(player, cap, msg.slots));
        });
        ctx.get().setPacketHandled(true);
    }
}
