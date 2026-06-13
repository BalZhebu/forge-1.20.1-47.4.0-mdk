package com.TovidY.kunluncontinent.network.server;

import com.TovidY.kunluncontinent.render.DamageIndicatorRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CSkillNotifyPacket {
    private final String text;
    private final int color;
    private final Vec3 pos;

    public S2CSkillNotifyPacket(String text, int color, Vec3 pos) {
        this.text = text;
        this.color = color;
        this.pos = pos;
    }

    // 编码（服务端 -> 字节流）
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(text);
        buf.writeInt(color);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
    }

    // 解码（字节流 -> 客户端）
    public static S2CSkillNotifyPacket decode(FriendlyByteBuf buf) {
        return new S2CSkillNotifyPacket(
                buf.readUtf(),
                buf.readInt(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    // 客户端安全接收并调用你的渲染器
    public static void handle(S2CSkillNotifyPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // 使用 DistExecutor 隔绝逻辑，防止服务端加载此类时崩溃
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                // 【核心绝杀】：直接将服务端传过来的文字、颜色、坐标扔进你现有的渲染器！
                DamageIndicatorRenderer.addIndicator(pkt.text, pkt.color, pkt.pos);
            });
        });
        ctx.setPacketHandled(true);
    }
}
