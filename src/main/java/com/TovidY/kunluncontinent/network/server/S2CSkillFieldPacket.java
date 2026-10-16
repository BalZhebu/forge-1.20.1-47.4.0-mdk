package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.render.skill.SkillEffect;
import com.TovidY.kunluncontinent.render.skill.SkillEffectRenderer;
import com.TovidY.kunluncontinent.render.skill.SkillEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 客户端：在某个位置放一个「技能场域特效」。
 *
 * <p>纯表现层。技能本体（伤害、减速、持续时间）全部由服务端 tick 逻辑决定；
 * 这个包只负责让客户端知道"要在哪画一个多大、画多久、什么样式的地阵"。</p>
 *
 * <p>只需要在<b>开始</b>时发一次 —— 客户端按 {@code durationTicks} 自己判断过期，
 * 不需要服务端再发"结束"包。</p>
 *
 * <p>样式用<b>数字 id</b>（见 {@code SkillEffects} 里的常量）而不是字符串：
 * 包更小，也不会因为拼错字符串变成"静默不显示"这种难查的问题。</p>
 */
public class S2CSkillFieldPacket {

    private final int styleId;
    private final int ownerEntityId;
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final int durationTicks;

    public S2CSkillFieldPacket(int styleId, int ownerEntityId,
                               double x, double y, double z,
                               float radius, int durationTicks) {
        this.styleId = styleId;
        this.ownerEntityId = ownerEntityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.durationTicks = durationTicks;
    }

    public static void encode(S2CSkillFieldPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.styleId);
        buffer.writeInt(msg.ownerEntityId);
        buffer.writeDouble(msg.x);
        buffer.writeDouble(msg.y);
        buffer.writeDouble(msg.z);
        buffer.writeFloat(msg.radius);
        buffer.writeInt(msg.durationTicks);
    }

    public static S2CSkillFieldPacket decode(FriendlyByteBuf buffer) {
        return new S2CSkillFieldPacket(
                buffer.readInt(), buffer.readInt(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readFloat(), buffer.readInt());
    }

    public static void handle(S2CSkillFieldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            SkillEffect effect = SkillEffects.create(
                    msg.styleId, msg.ownerEntityId,
                    new Vec3(msg.x, msg.y, msg.z),
                    msg.radius, level.getGameTime(), msg.durationTicks);
            SkillEffectRenderer.spawn(effect);
        }));
        ctx.get().setPacketHandled(true);
    }
}
