package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CSkillFieldPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
 * 场域特效的<b>通用发包器</b>（所有武魂的着色器分支都用它，不再各自写一份）。
 *
 * <p>⚠️ 与七杀剑 {@code QishaFx} 同一个坑：{@code SkillEffect#anchor()} 只要 owner
 * 非空就返回 owner 的位置，所以<b>世界坐标特效必须传 ownerId = -1</b>；
 * 只有需要朝向的横向弧斩（{@code ArcEffect}）才用 {@link #sendOwned}。</p>
 */
public final class SkillFieldSender {

    /**
     * {@link #broadcast} 的广播半径（格）。
     *
     * <p>方块类特效（祭坛召唤）没有「施法者实体」可以挂 tracking，只能按距离挑玩家。
     * 128 格足够覆盖视野内所有能看到大阵的人。</p>
     */
    private static final double BROADCAST_RANGE = 128.0;

    private SkillFieldSender() {
    }

    /** 在世界坐标放一个特效（锚点 = 传入的坐标，不跟随施法者）。 */
    public static void send(ServerPlayer caster, int style, double x, double y, double z,
                            float radius, int durationTicks) {
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster),
                new S2CSkillFieldPacket(style, -1, x, y, z, radius, durationTicks));
    }

    /** 以施法者为锚点的特效（弧斩等需要 owner yaw 的样式）。 */
    public static void sendOwned(ServerPlayer caster, int style, float radius, int durationTicks) {
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster),
                new S2CSkillFieldPacket(style, caster.getId(),
                        caster.getX(), caster.getY(), caster.getZ(), radius, durationTicks));
    }

    /**
     * 方块 / 世界事件用的广播：发给该维度里 {@value #BROADCAST_RANGE} 格内的所有玩家。
     *
     * <p>没有「施法者实体」时不能走 {@code TRACKING_ENTITY_AND_SELF}（那需要实体），
     * 而 {@code PacketDistributor.NEAR} 的维度+半径判定在不同版本 API 有出入，
     * 这里直接遍历玩家列表按距离筛，行为最直白也不会踩 API 差异。</p>
     */
    public static void broadcast(ServerLevel level, int style, double x, double y, double z,
                                 float radius, int durationTicks) {
        if (level == null || level.players().isEmpty()) {
            return;
        }
        S2CSkillFieldPacket packet = new S2CSkillFieldPacket(style, -1, x, y, z, radius, durationTicks);
        double maxSqr = BROADCAST_RANGE * BROADCAST_RANGE;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) <= maxSqr) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }
}
