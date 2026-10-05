package com.TovidY.kunluncontinent.screen.attribute.skill;

import com.TovidY.kunluncontinent.capability.playerattributes.FlyCrashBlast;
import com.TovidY.kunluncontinent.effect.ParticleFx;
import com.TovidY.kunluncontinent.network.NetworkHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 高速飞行撞墙 → 请求服务端引爆（客户端 → 服务端）。
 *
 * <p><b>为什么必须客户端发</b>：原版移动包（{@code ServerboundMovePlayerPacket}）
 * 只带位置/朝向/是否着地，<b>不携带碰撞状态</b>；服务端的 {@code horizontalCollision}
 * 只在 {@code handleMovePlayer} 里做碰撞修正的那一瞬间为真，tick 事件里读到的
 * 已经是下一 tick 的值 → 服务端根本无法可靠判断"这一 tick 撞了墙"。
 * 而客户端 {@code LocalPlayer} 每 tick 都有准确的 {@code horizontalCollision}，
 * 所以检测放客户端，只把结果（撞点 + 撞前速度）告诉服务端。</p>
 */
public class CPacketFlyCrash {
    private final double x;
    private final double y;
    private final double z;
    private final double speed;

    public CPacketFlyCrash(double x, double y, double z, double speed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.speed = speed;
    }

    public static void encode(CPacketFlyCrash msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.x);
        buffer.writeDouble(msg.y);
        buffer.writeDouble(msg.z);
        buffer.writeDouble(msg.speed);
    }

    public static CPacketFlyCrash decode(FriendlyByteBuf buffer) {
        return new CPacketFlyCrash(buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(CPacketFlyCrash msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isAlive()) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            // 服务端复核：必须仍在飞行、冷却已好（防止客户端刷包）
            if (!player.getAbilities().flying) return;
            if (!FlyCrashBlast.isCooldownReady(player)) return;

            // 撞点与玩家实际位置做一次距离校验（最多 3 格，防止恶意传远坐标）
            Vec3 center = new Vec3(msg.x, msg.y, msg.z);
            if (player.distanceToSqr(center) > 9.0D) {
                center = player.position();
            }
            // 只接受合理速度范围（0 ~ 3 格/tick，飞得再快也不该超这个量级）
            double speed = Math.max(0.0D, Math.min(3.0D, msg.speed));

            // 撞墙处先给一簇火花粒子，让爆炸有"撞点"（走项目统一粒子工具类）
            ParticleFx fx = ParticleFx.of(serverLevel, player);
            if (fx != null) {
                fx.budget(120);
                fx.bloom(ParticleTypes.CRIT, center, 14, 0.45, 0.25);
                fx.burst(ParticleTypes.SOUL_FIRE_FLAME, center, 10, 0.7, true);
            }
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.7F);

            FlyCrashBlast.detonate(player, speed, center);
        });
        ctx.get().setPacketHandled(true);
    }
}
