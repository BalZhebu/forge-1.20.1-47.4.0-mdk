package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CSkillFieldPacket;
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
}
