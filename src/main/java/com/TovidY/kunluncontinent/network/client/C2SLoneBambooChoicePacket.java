package com.TovidY.kunluncontinent.network.client;

import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.godclass.LoneBambooGodPath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端：玩家在「孤竹之缘」对话框里选了哪条路。
 *
 * <ul>
 *   <li>{@code choice = 0}：正常魂师之路（什么都不做，只回一句确认）；</li>
 *   <li>{@code choice = 1}：孤竹神位之路（{@link LoneBambooGodPath#startGod}）。</li>
 * </ul>
 *
 * <p>⚠️ 服务端<b>必须</b>重新校验资格：弹框只是客户端表现，
 * 包本身可以被伪造，不校验就等于谁都能直接要孤竹神位。</p>
 */
public class C2SLoneBambooChoicePacket {

    /** 正常魂师之路。 */
    public static final int CHOICE_NORMAL = 0;
    /** 孤竹神位之路。 */
    public static final int CHOICE_BAMBOO = 1;

    private final int choice;

    public C2SLoneBambooChoicePacket(int choice) {
        this.choice = choice;
    }

    public static void encode(C2SLoneBambooChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.choice);
    }

    public static C2SLoneBambooChoicePacket decode(FriendlyByteBuf buf) {
        return new C2SLoneBambooChoicePacket(buf.readInt());
    }

    public static void handle(C2SLoneBambooChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                // 兜底：超时已经默认走了正常路（或重复点击）→ 这次包不再受理
                if (!LoneBambooGodPath.consumePending(player)) {
                    player.sendSystemMessage(Component.literal(
                            "§7【孤竹之缘】已作罢（未及时抉择，按正常魂师之路处理）。").withStyle(ChatFormatting.GRAY));
                    return;
                }

                if (msg.choice != CHOICE_BAMBOO) {
                    player.sendSystemMessage(Component.literal(
                            "§7你选择了正常魂师之路，孤竹之缘就此揭过。").withStyle(ChatFormatting.GRAY));
                    return;
                }

                // 防伪造：必须真的满足彩蛋条件、并且还没有神考
                if (!LoneBambooGodPath.isEligible(cap)) {
                    player.sendSystemMessage(Component.literal(
                            "§c【孤竹之缘】条件不满足，无法踏上这条路。").withStyle(ChatFormatting.RED));
                    return;
                }

                LoneBambooGodPath.startGod(player, cap);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
