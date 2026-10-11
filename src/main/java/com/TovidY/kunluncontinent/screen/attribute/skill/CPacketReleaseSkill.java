package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.mobattributes.MobAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.item.baseskillist.BaseSkillItem;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import com.TovidY.kunluncontinent.network.server.S2CCastingSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

import static com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability.CASTING_SLOWDOWN_MODIFIER;

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
                // 直接调用公共施法逻辑
                executeRelease(player, cap);
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static void executeRelease(ServerPlayer player, PlayerAttributeCapability cap) {
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

            // 开关型技能（武魂真身）的「已开启时再按 = 关闭」必须放在最前面：
            // 开启后玩家立刻处于冷却中，走正常流程会被下面的冷却检查拦住，
            // 导致「开了就关不掉」。
            if (skill.tryToggleOff(player)) {
                SynsAPI.synsPlayerAttribute(player);
                return;
            }

            float costMultiplier = skill.getCostMultiplier(nianxian);
            float finalCost = skill.getBaseCost() * costMultiplier;
            // 开关型技能不扣一次性能耗（成本在开启期间按 tick 扣），跳过这次前置校验
            if (skill.hasUpfrontCost() && cap.getJingshenli() < finalCost) {
                player.displayClientMessage(
                        Component.literal("§c精神力不足！需要 §e" + String.format("%.1f", finalCost) + " §c当前 §e" + String.format("%.1f", cap.getJingshenli())),
                        true
                );
                return;
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
                AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null && !speedAttr.hasModifier(CASTING_SLOWDOWN_MODIFIER)) {
                    speedAttr.addTransientModifier(CASTING_SLOWDOWN_MODIFIER);
                }
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CCastingSyncPacket(castTime));
                player.displayClientMessage(Component.translatable("gui.kunluncontinent.casting"), true);
            } else {
                // 只有真的释放成功才刷新冷却：武魂真身「关闭」时返回 false，
                // 于是关闭动作不会把冷却时间戳重置（关闭不额外加冷却）
                boolean cast = skill.handleRelease(player.level(), player, nianxian);
                if (cast) {
                    cap.setSkillLastUsedTime(currentWuhun, selectedSlot, currentTime);
                }
                SynsAPI.synsPlayerAttribute(player);
            }
        }
    }
}