package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CCastingSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;
public class CPacketReleaseSkill {
    public CPacketReleaseSkill() {}

    public static void encode(CPacketReleaseSkill msg, FriendlyByteBuf buffer) {}

    public static CPacketReleaseSkill decode(FriendlyByteBuf buffer) {
        return new CPacketReleaseSkill();
    }

    public static void handle(CPacketReleaseSkill msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                String currentWuhun = cap.getWuhunName();
                if (currentWuhun == null) {
                    player.displayClientMessage(Component.literal("§c请先开启武魂！"), true);
                    return;
                }

                BaseSkillItem[] skills = cap.getWuhunSkillsMap().get(currentWuhun);
                int selectedSlot = cap.getSelectedSkillSlot();

                if (skills != null && selectedSlot >= 0 && selectedSlot < 9) {
                    BaseSkillItem skill = skills[selectedSlot];
                    if (skill == null) {
                        player.displayClientMessage(Component.literal("§c当前槽位未装备魂技！"), true);
                        return;
                    }

                    int nianxian = 10;
                    List<MobAttributeCapability> rings = cap.getMonsterCapabilityLists().get(currentWuhun);
                    if (rings != null && selectedSlot < rings.size()) {
                        nianxian = (int) rings.get(selectedSlot).getNianxian();
                    }

                    long lastUsed = cap.getSkillLastUsedTime(currentWuhun, selectedSlot);
                    long currentTime = player.level().getGameTime();
                    int cooldownTicks = skill.getCooldownTicks();

                    if (currentTime - lastUsed < cooldownTicks) {
                        float remainingSeconds = (cooldownTicks - (currentTime - lastUsed)) / 20.0f;
                        player.displayClientMessage(
                                Component.literal("§c魂技冷却中... 剩余 §e" + String.format("%.1f", remainingSeconds) + "§cs"),
                                true
                        );
                        return;
                    }
                    int castTime = skill.getCastTime();
                    if (castTime > 0) {
                        cap.startCasting(skill, castTime);
                        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CCastingSyncPacket(castTime));
                        player.displayClientMessage(Component.literal("§e正在引导魂技..."), true);
                    } else {
                        skill.handleRelease(player.level(), player, nianxian);
                        cap.setSkillLastUsedTime(currentWuhun, selectedSlot, currentTime);
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}