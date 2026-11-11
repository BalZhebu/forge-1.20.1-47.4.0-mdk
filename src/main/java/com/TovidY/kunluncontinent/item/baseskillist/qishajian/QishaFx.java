package com.TovidY.kunluncontinent.item.baseskillist.qishajian;

import com.TovidY.kunluncontinent.network.NetworkHandler;
import com.TovidY.kunluncontinent.network.server.S2CSkillFieldPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

/**
 * 七杀剑的<b>着色器表现</b>统一出口。
 *
 * <p>服务端只发一个样式 id + 位置/半径/时长，客户端 {@code SkillEffects} 按 id 建特效。
 * 「粒子 or 着色器」由 {@code SkillFxToggle.useShader(player)} 决定，两条路二选一、不叠加。</p>
 */
public final class QishaFx {

    private QishaFx() {
    }

    /**
     * 在世界坐标放一个场域特效。
     *
     * <p>⚠️ <b>ownerId 必须传 -1</b>：{@code SkillEffect#anchor()} 只要 owner 活着
     * 就返回 owner 的位置，把施法者 id 传进去会让所有特效都黏在施法者身上
     * （这就是"剑影落在自己脚下而不是目标身上"的根因）。</p>
     */
    public static void field(ServerPlayer caster, int style, double x, double y, double z,
                             float radius, int durationTicks) {
        send(caster, style, -1, x, y, z, radius, durationTicks);
    }

    /**
     * 需要<b>朝向</b>的特效（横向弧斩）：这类必须带施法者 id，
     * 因为 {@code ArcEffect} 用 owner 的 yaw 决定剑气扫向哪边。
     */
    public static void fieldOwned(ServerPlayer caster, int style, float radius, int durationTicks) {
        send(caster, style, caster.getId(), caster.getX(), caster.getY(), caster.getZ(),
                radius, durationTicks);
    }

    private static void send(ServerPlayer caster, int style, int ownerId,
                             double x, double y, double z, float radius, int durationTicks) {
        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> caster),
                new S2CSkillFieldPacket(style, ownerId, x, y, z, radius, durationTicks));
    }

    /** 以某个实体脚下为原点放特效。 */
    public static void fieldAt(ServerPlayer caster, int style, Entity at, float radius, int durationTicks) {
        field(caster, style, at.getX(), at.getY(), at.getZ(), radius, durationTicks);
    }
}
