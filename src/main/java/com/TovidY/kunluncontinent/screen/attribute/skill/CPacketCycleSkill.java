package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.client.SyncShenciAttributesPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketCycleSkill {
    public CPacketCycleSkill() {}

    public static void encode(CPacketCycleSkill msg, FriendlyByteBuf buffer) {}

    public static CPacketCycleSkill decode(FriendlyByteBuf buffer) {
        return new CPacketCycleSkill();
    }

    public static void handle(CPacketCycleSkill msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                String currentWuhun = cap.getWuhunName();
                if (currentWuhun == null || currentWuhun.isEmpty()) {
                    player.displayClientMessage(
                            Component.literal("§c§l[ 提示 ] §f您尚打开武魂，无法操作技能面板！"),
                            true
                    );
                    return;
                }
                int currentIndex = cap.getSelectedSkillSlot();
                int nextIndex = (currentIndex + 1) % 9;
                cap.setSelectedSkillSlot(nextIndex);
                BaseSkillItem[] skills = cap.getWuhunSkillsMap().get(currentWuhun);
                if (skills != null && skills[nextIndex] != null) {
                    player.displayClientMessage(
                            Component.literal("§6当前魂技: §e" + skills[nextIndex].getName(ItemStack.EMPTY).getString()),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.literal("§7当前槽位: " + (nextIndex + 1) + " §8(未装备技能)"),
                            true
                    );
                }
                NetworkHandler.sendToClient(new SyncShenciAttributesPacket(cap), player);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
