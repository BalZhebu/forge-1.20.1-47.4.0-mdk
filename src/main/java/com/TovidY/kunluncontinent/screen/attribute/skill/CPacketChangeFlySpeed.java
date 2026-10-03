package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.FlySpeedTuning;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.SynsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 调整飞行速度档位（技能面板的 −/+ 按钮，客户端 → 服务端）。
 *
 * <p>只传一个相对增量 {@code delta}（−1 减速 / +1 加速），档位范围由
 * {@link PlayerAttributeCapability#adjustFlySpeedLevel(int)} 夹住，
 * 所以客户端可以放心连点、也不会越界。</p>
 *
 * <p>改完立刻：① 落 capability 存档 → ② 把新飞行速度写进 abilities
 * → ③ 回同步给客户端（否则面板上的档位文字要等下一次属性同步才变）。</p>
 */
public class CPacketChangeFlySpeed {
    private final int delta;

    public CPacketChangeFlySpeed(int delta) {
        this.delta = delta;
    }

    public static void encode(CPacketChangeFlySpeed msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.delta);
    }

    public static CPacketChangeFlySpeed decode(FriendlyByteBuf buffer) {
        return new CPacketChangeFlySpeed(buffer.readInt());
    }

    public static void handle(CPacketChangeFlySpeed msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    int before = cap.getFlySpeedLevel();
                    cap.adjustFlySpeedLevel(msg.delta);
                    int after = cap.getFlySpeedLevel();
                    if (before == after) return; // 已经到顶/到底，按钮无效
                    // 立刻生效（仅在允许飞行时才有实际手感，但 abilities 一律写好）
                    FlySpeedTuning.applyFlyingSpeed(player, after);
                    // 回同步，让面板上的档位与倍率立刻刷新
                    SynsAPI.synsPlayerAttribute(player);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§b【飞行】§f速度档位 " + FlySpeedTuning.levelName(after)
                                    + " §7(×" + trim(FlySpeedTuning.speedMultiplier(after))
                                    + "，耗神 ×" + trim(FlySpeedTuning.costMultiplier(after)) + ")"));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /** 整数倍率不带小数点。 */
    private static String trim(float value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.valueOf(value);
    }
}
