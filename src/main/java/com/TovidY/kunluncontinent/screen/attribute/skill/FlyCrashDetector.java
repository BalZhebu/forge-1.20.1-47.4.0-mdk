package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.KlMain;
import com.TovidY.kunluncontinent.capability.playerattributes.FlySpeedTuning;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapability;
import com.TovidY.kunluncontinent.capability.playerattributes.PlayerAttributeCapabilityProvider;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端飞行撞墙检测（神速档位以上生效）。
 *
 * <p>为什么必须在客户端：原版移动包只上报位置/朝向/着地状态，
 * <b>不携带碰撞标记</b>，所以服务端 tick 里读 {@code horizontalCollision}
 * 拿到的已经是"下一 tick"的值。客户端 {@code LocalPlayer} 每 tick 都有准确的碰撞状态。</p>
 *
 * <p>检测到"高速 + 飞行 + 神速以上 + 撞墙"⇒ 发一个
 * {@link CPacketFlyCrash}（只带撞点与撞前速度），真正的爆炸与伤害在服务端算。</p>
 *
 * <p>冷却存在玩家 persistentData（服务端维护），客户端也用本地 tick 计数做一次
 * 兜底节流，避免贴墙滑行时刷包。</p>
 */
@Mod.EventBusSubscriber(modid = KlMain.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlyCrashDetector {

    /** 客户端兜底节流（tick）——与服务端冷却同值，双保险。 */
    private static int localCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !player.isAlive()) {
            localCooldown = 0;
            return;
        }

        if (localCooldown > 0) {
            localCooldown--;
            return;
        }

        // 1. 必须在飞行
        if (!player.getAbilities().flying) return;

        // 2. 必须神速档位以上
        int level = player.getCapability(PlayerAttributeCapabilityProvider.CAPABILITY)
                .map(PlayerAttributeCapability::getFlySpeedLevel)
                .orElse(0);
        if (!FlySpeedTuning.isCrashArmed(level)) return;

        // 3. 必须"速度已经提起来"——用当前水平速度
        double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
        if (horizontalSpeed < FlySpeedTuning.CRASH_MIN_SPEED) return;

        // 4. 必须确实撞墙（客户端的 horizontalCollision 是准确的碰撞标记）
        if (!player.horizontalCollision) return;

        // 5. 发送 —— 撞点在脚下偏前 0.4 格（爆炸中心稍微离开墙面一点）
        Vec3 look = player.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        if (flatLook.lengthSqr() < 1.0E-4D) flatLook = new Vec3(0.0D, 0.0D, 1.0D);
        flatLook = flatLook.normalize().scale(0.4D);
        Vec3 center = player.position().add(0.0D, 0.6D, 0.0D).add(flatLook);

        localCooldown = FlySpeedTuning.CRASH_COOLDOWN_TICKS;
        NetworkHandler.sendToServer(new CPacketFlyCrash(center.x, center.y, center.z, horizontalSpeed));
    }
}
