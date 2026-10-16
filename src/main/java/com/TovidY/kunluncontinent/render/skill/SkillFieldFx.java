package com.TovidY.kunluncontinent.render.skill;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CSkillFieldPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

/**
 * 发「技能场域特效」包的小工具。
 *
 * <p>直接写 {@code NetworkHandler.INSTANCE.send(...)} 也行，但每个接着色器的技能都要写一遍，
 * 而且很容易踩一个坑：<b>锚点实体 id 传什么</b>。所以这里收成两个方法，语义写清楚。</p>
 *
 * <h3>关于 {@code ownerEntityId}</h3>
 * <p>它决定特效锚点跟谁走：</p>
 * <ul>
 *   <li>传实体 id → 客户端每帧取该实体的当前位置（阵跟着人走）；</li>
 *   <li>传 {@code -1} → 客户端用发包时那个固定坐标（阵钉在原地）。</li>
 * </ul>
 *
 * <p>这个类刻意<b>不加</b> {@code @OnlyIn(Dist.CLIENT)} —— 它是服务端调用的。</p>
 */
public final class SkillFieldFx {

    private SkillFieldFx() {
    }

    /**
     * 在<b>实体身上</b>挂一个特效，锚点跟随该实体。
     *
     * <p>发给「追踪该实体的人 + 实体本身（若是玩家）」。</p>
     */
    public static void atEntity(Entity anchor, int styleId, float radius, int durationTicks) {
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> anchor),
                new S2CSkillFieldPacket(styleId, anchor.getId(),
                        anchor.getX(), anchor.getY(), anchor.getZ(),
                        radius, durationTicks));
    }

    /**
     * 在<b>固定世界坐标</b>放一个特效，锚点不跟任何实体走。
     *
     * @param source 决定发给谁 —— 以它的位置为中心广播给周围的玩家
     */
    public static void atPoint(ServerPlayer source, int styleId,
                               double x, double y, double z,
                               float radius, int durationTicks) {
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> source),
                new S2CSkillFieldPacket(styleId, -1, x, y, z, radius, durationTicks));
    }
}
