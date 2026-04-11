package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.GodRegistry;
import com.TovidY.kunluncontinent.godclass.interfac.GodInfo;
import com.TovidY.kunluncontinent.godclass.interfac.GodTaskType;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class C2SCheckTaskPacket {
    public C2SCheckTaskPacket() {}
    public static void encode(C2SCheckTaskPacket msg, FriendlyByteBuf buffer) {}
    public static C2SCheckTaskPacket decode(FriendlyByteBuf buffer) {
        return new C2SCheckTaskPacket();
    }

    public static void handle(C2SCheckTaskPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        ctx.get().enqueueWork(() -> {
            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                if (!cap.hasActiveTask() || cap.isGod()) return;
                int stage = cap.getCurrentStage();
                String typeStr = cap.getAssignedTypes()[stage];
                String target = cap.getAssignedTargets()[stage];
                int required = cap.getAssignedCounts()[stage];
                if (typeStr == null || target == null) return;
                GodTaskType type = GodTaskType.valueOf(typeStr);
                boolean success = false;
                if (type == GodTaskType.ATTRIBUTE) {
                    float currentVal = cap.getGodAttributeValue(target);
                    if (currentVal >= required) success = true;
                } else if (type == GodTaskType.ITEM_CONSUME || type == GodTaskType.ITEM_CHECK) {
                    int count = countItem(player, target);
                    if (count >= required) {
                        success = true;
                        if (type == GodTaskType.ITEM_CONSUME) removeItem(player, target, required);
                    }
                }
                if (success) {
                    int completedStage = cap.getCurrentStage();
                    GodInfo info = GodRegistry.GODS.get(cap.getGodName());
                    String godDisplayName = (info != null) ? info.getName() : "未知神位";
                    cap.checkTaskCompletion(player);
                    Component title = Component.literal("§6§l★ 考 核 通 过 ★").withStyle(Style.EMPTY.withBold(true));
                    Component subtitle = Component.literal("§f" + godDisplayName + " §e第 " + completedStage + " 考 §a完成");
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle));
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 60, 20));
                    player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.8F, 0.7F);
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.2F);
                    if (player.getServer() != null) {
                        Component broadcastMsg = Component.literal("§6§l【神祇公告】 §e" + player.getScoreboardName() +
                                " §f成功突破 §b" + godDisplayName + " §f第 §c" + completedStage + " §f考试炼！");
                        player.getServer().getPlayerList().broadcastSystemMessage(broadcastMsg, false);
                    }
                    NetworkHandler.sendToClient(new PacketSyncGodData(cap), player);
                } else {
                    player.playNotifySound(SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 1.0F, 0.5F);
                    player.sendSystemMessage(Component.literal("§c§n考核条件未达成§r §7- 请继续努力"), false);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private static int countItem(Player player, String itemId) {
        ResourceLocation targetRl = ResourceLocation.tryParse(itemId);
        if (targetRl == null) return 0;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(targetRl)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItem(Player player, String itemId, int amount) {
        ResourceLocation targetRl = ResourceLocation.tryParse(itemId);
        if (targetRl == null) return;
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(targetRl)) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
                if (remaining <= 0) break;
            }
        }
    }
}